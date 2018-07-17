package com.yg.gqlwfdl.dataaccess

import com.yg.gqlwfdl.childFields
import com.yg.gqlwfdl.yg.db.public_.Tables.COMPANY
import com.yg.gqlwfdl.yg.db.public_.Tables.CUSTOMER
import graphql.language.Field
import org.jooq.Record
import org.jooq.Table

/**
 * Class defining all the different joins that existin the underlying database.
 */
class JoinDefinitions {
    companion object {
        // Joins from customer table
        val CUSTOMER_COMPANY = JoinDefinition("company", CUSTOMER.COMPANY_ID, COMPANY.ID)
        val CUSTOMER_OUT_OF_OFFICE_DELEGATE = JoinDefinition("outOfOfficeDelegate", CUSTOMER.OUT_OF_OFFICE_DELEGATE, CUSTOMER.ID)

        // Joins from company table
        val COMPANY_PRIMARY_CONTACT = JoinDefinition("primaryContact", COMPANY.PRIMARY_CONTACT, CUSTOMER.ID)

        /**
         * Gets all the joins in the system. Whenever a new join is created above, it needs to be added here.
         */
        private val all
            get() = listOf(CUSTOMER_COMPANY, CUSTOMER_OUT_OF_OFFICE_DELEGATE, COMPANY_PRIMARY_CONTACT)

        /**
         * Gets a list containing all the known join definitions whose primary table matches the passed in [table].
         */
        fun <TRecord : Record> allFrom(table: Table<TRecord>): List<JoinDefinition<out Any, TRecord, out Record>> {
            return all.filter { it.primaryField.table == table }.map {
                // We can ignore unchecked casts here as the previous check in the filter ensure this wouldn't happen
                @Suppress("UNCHECKED_CAST")
                it as JoinDefinition<out Any, TRecord, out Record>
            }
        }
    }
}

/**
 * Returns a list of [JoinRequest] objects from the GraphQL field acting as the receiver of this function. The joins
 * are based on the GraphQL field's children.
 *
 * @param sourceTable The main table being queried, which generally conceptually corresponds to the GraphQL field.
 */
fun <TRecord : Record> Field.toJoinDefinitions(sourceTable: Table<TRecord>)
        : List<JoinRequest<out Any, TRecord, out Record>> {

    // For each child field of the passed in field, get its corresponding join definition and, if there is one, ask it
    // to create a join request.
    val possibleJoins = JoinDefinitions.allFrom(sourceTable)
    return this.childFields.mapNotNull {
        possibleJoins.firstOrNull { join -> it.name == join.name }?.createRequest(it)
    }
}