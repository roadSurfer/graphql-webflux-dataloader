package com.yg.gqlwfdl.dataaccess

import com.yg.gqlwfdl.dataaccess.joins.GraphQLFieldToJoinMapper
import com.yg.gqlwfdl.dataaccess.joins.JoinedRecordToEntityConverterProvider
import com.yg.gqlwfdl.services.Customer
import com.yg.gqlwfdl.yg.db.public_.Tables.CUSTOMER
import com.yg.gqlwfdl.yg.db.public_.tables.records.CustomerRecord
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.stereotype.Repository
import java.util.concurrent.Executor
import com.yg.gqlwfdl.yg.db.public_.Tables.COMPANY as COMPANY_TABLE

/**
 * Repository providing access to customer information.
 */
interface CustomerRepository : EntityRepository<Customer, Long>

/**
 * Concrete implementation of [CustomerRepository], which uses a database for its data.
 */
@Repository
class DBCustomerRepository(create: DSLContext,
                           asyncExecutor: Executor,
                           recordToEntityConverterProvider: JoinedRecordToEntityConverterProvider,
                           graphQLFieldToJoinMapper: GraphQLFieldToJoinMapper)
    : DBEntityRepository<Customer, Long, CustomerRecord>(
        create, asyncExecutor, recordToEntityConverterProvider, graphQLFieldToJoinMapper, CUSTOMER, CUSTOMER.ID),
        CustomerRepository {

    override fun getEntity(record: Record) = record.into(CUSTOMER).toEntity()
}

/**
 * Converts a [CustomerRecord] to its corresponding entity, a [Customer].
 */
fun CustomerRecord.toEntity() =
        Customer(this.id, this.firstName, this.lastName, this.companyId, this.pricingDetails, this.outOfOfficeDelegate)