package com.yg.gqlwfdl

import com.yg.gqlwfdl.services.*
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
        val vatRates = createVatRates(statement)
        val discountRates = createDiscountRates(statement)
        val paymentMethods = createPaymentMethods(statement)
        val pricingDetails = createPricingDetails(statement, vatRates, discountRates, paymentMethods)
        val companies = createCompanies(statement, pricingDetails)
        val customers = createCustomers(statement, companies, pricingDetails)
        setOutOfOfficeDelegates(statement, customers)
        setPrimaryContacts(statement, companies, customers)
        createCompanyPartnerships(statement, companies)
    }

    private fun createVatRates(statement: Statement): List<VatRate> {
        fun createVatRate(description: String, value: Double) {
            statement.execute("insert into vat_rate (description, value) values('$description', $value);")
        }

        createVatRate("Standard", 20.0)
        createVatRate("Reduced", 5.0)
        createVatRate("Zero", 0.0)

        return listFromQuery(statement, "select id, description, value from vat_rate order by id") {
            VatRate(it.getLong("id"), it.getString("description"), it.getDouble("value"))
        }
    }

    private fun createDiscountRates(statement: Statement): List<DiscountRate> {
        fun createDiscountRate(description: String, value: Double) {
            statement.execute("insert into discount_rate (description, value) values('$description', $value);")
        }

        createDiscountRate("None", 0.0)
        createDiscountRate("Cheap", 5.0)
        createDiscountRate("Cheapest", 10.0)

        return listFromQuery(statement, "select id, description, value from discount_rate order by id") {
            DiscountRate(it.getLong("id"), it.getString("description"), it.getDouble("value"))
        }
    }

    private fun createPaymentMethods(statement: Statement): List<PaymentMethod> {
        fun createPaymentMethods(description: String, charge: Double) {
            statement.execute("insert into payment_method (description, charge) values('$description', $charge);")
        }

        createPaymentMethods("Cash", 0.0)
        createPaymentMethods("Cheque", 2.5)
        createPaymentMethods("Card", 1.5)

        return listFromQuery(statement, "select id, description, charge from payment_method order by id") {
            PaymentMethod(it.getLong("id"), it.getString("description"), it.getDouble("charge"))
        }
    }

    private fun createPricingDetails(statement: Statement, vatRates: List<VatRate>, discountRates: List<DiscountRate>,
                                     paymentMethods: List<PaymentMethod>)
            : List<PricingDetails> {

        (1..4).forEach {
            statement.execute("""
                | insert into pricing_details (description, vat_rate, discount_rate, preferred_payment_method) values(
                |   'PricingDetails-$it',
                |   ${vatRates.randomItem().id},
                |   ${discountRates.randomItem().id},
                |   ${paymentMethods.randomItem().id}
                | );
            """.trimMargin())
        }

        return listFromQuery(statement,
                "select id, description, vat_rate, discount_rate, preferred_payment_method from pricing_details order by id") {
            PricingDetails(it.getLong("id"), it.getString("description"),
                    vatRates.first { vatRate -> vatRate.id == it.getLong("vat_rate") },
                    discountRates.first { discountRate -> discountRate.id == it.getLong("discount_rate") },
                    paymentMethods.first { paymentMethod -> paymentMethod.id == it.getLong("preferred_payment_method") })
        }
    }

    private fun createCompanyPartnerships(statement: Statement, companies: List<Company>) {
        // Create partnerships between:
        // 1. The first company and the second company.
        // 2. The first company and the third company.
        // 3. The second company and the third company.

        fun createPartnership(companyA: Company, companyB: Company) {
            statement.execute(
                    "insert into company_partnership (company_a, company_b) values (${companyA.id}, ${companyB.id});")
        }

        createPartnership(companies[0], companies[1])
        createPartnership(companies[0], companies[2])
        createPartnership(companies[1], companies[2])
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

        // Make sure that there is at least one company with a primary contact
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

    private fun createCustomers(statement: Statement, companies: List<Company>, pricingDetails: List<PricingDetails>)
            : List<Customer> {

        // Customers: 3 customers for each company.
        companies.forEach { company: Company ->
            (1..3).forEach { userNumber: Int ->
                statement.connection.createStatement().use {
                    it.execute("""
                        | insert into customer (first_name, last_name, company_id, pricing_details) values(
                        | 'User-$userNumber', 'From-${company.name}', ${company.id}, ${pricingDetails.randomItem().id});
                    """.trimMargin())
                }
            }
        }

        return listFromQuery(statement,
                "select id, first_name, last_name, company_id, pricing_details from customer order by id") {
            Customer(it.getLong("id"), it.getString("first_name"), it.getString("last_name"),
                    it.getLong("company_id"), it.getLong("pricing_details"))
        }
    }

    private fun createCompanies(statement: Statement, pricingDetails: List<PricingDetails>): List<Company> {
        // Companies: A to C.
        ('A'..'C').forEach {
            statement.execute("""
                | insert into company (name, address, pricing_details)
                | values('Company-$it', '$it Street, $it Town', ${pricingDetails.randomItem().id});
            """.trimMargin())
        }
        return listFromQuery(statement, "select id, name, address, pricing_details from company order by id") {
            Company(it.getLong("id"), it.getString("name"), it.getString("address"), it.getLong("pricing_details"))
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
        statement.execute("drop table if exists company_partnership;")
        statement.execute("drop table if exists vat_rate;")
        statement.execute("drop table if exists discount_rate;")
        statement.execute("drop table if exists payment_method;")
        statement.execute("drop table if exists pricing_details;")

        // Create the tables.
        statement.execute("""
            | create table company (
            |     id bigint auto_increment not null primary key,
            |     name varchar(255) not null,
            |     address varchar(255) not null,
            |     primary_contact bigint,
            |     pricing_details bigint not null
            | );
        """.trimMargin())

        statement.execute("""
            | create table customer (
            |     id bigint auto_increment not null primary key,
            |     first_name varchar(255) not null,
            |     last_name varchar(255) not null,
            |     company_id bigint not null,
            |     out_of_office_delegate bigint,
            |     pricing_details bigint not null
            | );
        """.trimMargin())

        statement.execute("""
            | create table company_partnership (
            |     id bigint auto_increment not null primary key,
            |     company_a bigint not null,
            |     company_b bigint not null
            | );
        """.trimMargin())

        statement.execute("""
            | create table vat_rate (
            |     id bigint auto_increment not null primary key,
            |     description varchar(255) not null,
            |     value double not null
            | );
        """.trimMargin())

        statement.execute("""
            | create table discount_rate (
            |     id bigint auto_increment not null primary key,
            |     description varchar(255) not null,
            |     value double not null
            | );
        """.trimMargin())

        statement.execute("""
            | create table payment_method (
            |     id bigint auto_increment not null primary key,
            |     description varchar(255) not null,
            |     charge double not null
            | );
        """.trimMargin())

        statement.execute("""
            | create table pricing_details (
            |     id bigint auto_increment not null primary key,
            |     description varchar(255) not null,
            |     vat_rate bigint not null,
            |     discount_rate bigint not null,
            |     preferred_payment_method bigint not null
            | );
        """.trimMargin())
    }
}

private fun <T> List<T>.randomItem(random: Random = Random()): T = this[random.nextInt(this.size)]