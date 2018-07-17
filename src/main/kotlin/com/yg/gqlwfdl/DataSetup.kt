package com.yg.gqlwfdl

import com.yg.gqlwfdl.services.Company
import com.yg.gqlwfdl.services.Customer
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement
import java.util.*

/**
 * Used to create test data.
 */
class DataSetup(private val dbUrl: String) {
    fun execute() {
        DriverManager.getConnection(dbUrl, "sa", "").use {
            it.createStatement().use {
                createTables(it)
                createTestData(it)
            }
        }
    }

    private fun createTestData(statement: Statement) {
        val companies = createCompanies(statement)
        val customers = createCustomers(statement, companies)
        setOutOfOfficeDelegates(statement, customers)
        setPrimaryContacts(statement, companies, customers)
    }

    private fun setPrimaryContacts(statement: Statement, companies: List<Company>, customers: List<Customer>) {
        // Run through all existing companies, and for each one set their primary contact.
        // There's a 50% chance that this will be null.  If non-null, it will be a randomly chosen user from that company.
        val customersByCompany = customers.groupBy { it.companyId }
        val random = Random()

        fun Company.setPrimaryContact() {
            // Get a list of the people in this company.
            customersByCompany[this.id]?.let {
                // Choose a random customer from this list and use them as the primary contact.
                this.primaryContact = it.randomItem().id
                statement.execute(
                        "update company set primary_contact = ${this.primaryContact} where id = ${this.id};")
            }
        }

        companies.filter { random.nextBoolean() }.forEach { it.setPrimaryContact() }

        // Make sure that there is at least one company with a primay contact
        if (companies.all { it.primaryContact == null })
            companies.randomItem().setPrimaryContact()
    }

    private fun setOutOfOfficeDelegates(statement: Statement, customers: List<Customer>) {
        // Run through all existing customers, and for each one set their out-of-office delegate.
        // There's a 50% chance that this will be null.  If non-null, it will be a randomly chosen user from the same company.
        val customersByCompany = customers.groupBy { it.companyId }
        val random = Random()

        fun Customer.setOutOfOfficeDelegate() {
            // Get a list of the other people in the same company as this customer, excluding the customer themselves.
            customersByCompany[this.companyId]?.minusElement(this)?.let {
                // Choose a random customer from this list and use them as the delegate.
                this.outOfOfficeDelegate = it.randomItem().id
                statement.execute(
                        "update customer set out_of_office_delegate = ${this.outOfOfficeDelegate} where id = ${this.id};")
            }
        }

        customers.filter { random.nextBoolean() }.forEach { it.setOutOfOfficeDelegate() }

        // Make sure that there is at least one customer with an out-of-office delegate
        if (customers.all { it.outOfOfficeDelegate == null })
            customers.randomItem().setOutOfOfficeDelegate()
    }

    private fun createCustomers(statement: Statement, companies: List<Company>): List<Customer> {
        // Customers: 3 customers for each company.
        companies.forEach { company: Company ->
            (1..3).forEach { userNumber: Int ->
                statement.connection.createStatement().use {
                    it.execute("""
                            | insert into customer (first_name, last_name, company_id)
                            | values('User-$userNumber', 'From-${company.name}', ${company.id});
                        """.trimMargin())
                }
            }
        }
        return listFromQuery(statement, "select id, first_name, last_name, company_id from customer order by id") {
            Customer(it.getLong("id"), it.getString("first_name"), it.getString("last_name"), it.getLong("company_id"))
        }
    }

    private fun createCompanies(statement: Statement): List<Company> {
        // Companies: A to C.
        ('A'..'C').forEach {
            statement.execute("insert into company (name, address) values('Company-$it', '$it Street, $it Town');")
        }
        return listFromQuery(statement, "select id, name, address from company order by id") {
            Company(it.getLong("id"), it.getString("name"), it.getString("address"))
        }
    }

    private fun <T : Any> listFromQuery(statement: Statement, sql: String, extractor: (ResultSet) -> T): List<T> {
        return statement.executeQuery(sql).use {
            // From https://stackoverflow.com/questions/44315985/producing-a-list-from-a-resultset
            generateSequence {
                if (it.next()) extractor(it) else null
            }.toList()
        }
    }

    private fun createTables(statement: Statement) {
        // Drop existing tables.
        statement.execute("drop table if exists customer;")
        statement.execute("drop table if exists company;")

        // Create the tables.
        statement.execute("""
            | create table company (
            |     id bigint auto_increment not null primary key,
            |     name varchar(255) not null,
            |     address varchar(255) not null,
            |     primary_contact bigint
            | );
        """.trimMargin())

        statement.execute("""
            | create table customer (
            |     id bigint auto_increment not null primary key,
            |     first_name varchar(255) not null,
            |     last_name varchar(255) not null,
            |     company_id bigint not null,
            |     out_of_office_delegate bigint,
            | );
        """.trimMargin())
    }
}

private fun <T> List<T>.randomItem(random: Random = Random()): T = this[random.nextInt(this.size)]