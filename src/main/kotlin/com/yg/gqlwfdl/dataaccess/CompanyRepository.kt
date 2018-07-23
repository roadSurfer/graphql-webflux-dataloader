package com.yg.gqlwfdl.dataaccess

import com.yg.gqlwfdl.dataaccess.joins.GraphQLFieldToJoinMapper
import com.yg.gqlwfdl.dataaccess.joins.JoinedRecordToEntityConverterProvider
import com.yg.gqlwfdl.services.Company
import com.yg.gqlwfdl.yg.db.public_.Tables.COMPANY
import com.yg.gqlwfdl.yg.db.public_.tables.records.CompanyRecord
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.stereotype.Repository
import java.util.concurrent.Executor

/**
 * Repository providing access to company information.
 */
interface CompanyRepository : EntityRepository<Company, Long>

/**
 * Concrete implementation of [CompanyRepository], which uses a database for its data.
 */
@Repository
class DBCompanyRepository(create: DSLContext,
                          asyncExecutor: Executor,
                          recordToEntityConverterProvider: JoinedRecordToEntityConverterProvider,
                          graphQLFieldToJoinMapper: GraphQLFieldToJoinMapper)
    : DBEntityRepository<Company, Long, CompanyRecord>(
        create, asyncExecutor, recordToEntityConverterProvider, graphQLFieldToJoinMapper, COMPANY, COMPANY.ID),
        CompanyRepository {

    override fun getEntity(record: Record) = record.into(COMPANY).toEntity()
}

/**
 * Converts a [CompanyRecord] to its corresponding entity, a [Company].
 */
fun CompanyRecord.toEntity() = Company(this.id, this.name, this.address, this.pricingDetails, this.primaryContact)