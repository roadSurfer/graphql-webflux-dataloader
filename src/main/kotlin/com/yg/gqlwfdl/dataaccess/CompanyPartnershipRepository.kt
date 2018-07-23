package com.yg.gqlwfdl.dataaccess

import com.yg.gqlwfdl.dataaccess.joins.*
import com.yg.gqlwfdl.services.CompanyPartnership
import com.yg.gqlwfdl.yg.db.public_.Tables.COMPANY_PARTNERSHIP
import com.yg.gqlwfdl.yg.db.public_.tables.Company
import com.yg.gqlwfdl.yg.db.public_.tables.records.CompanyPartnershipRecord
import com.yg.gqlwfdl.yg.db.public_.tables.records.CompanyRecord
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SelectJoinStep
import org.springframework.stereotype.Repository
import java.util.concurrent.Executor

/**
 * Repository providing access to company partnership information.
 */
interface CompanyPartnershipRepository : EntityRepository<CompanyPartnership, Long>

/**
 * Concrete implementation of [CompanyPartnershipRepository], which uses a database for its data.
 */
@Repository
class DBCompanyPartnershipRepository(create: DSLContext,
                                     asyncExecutor: Executor,
                                     recordToEntityConverterProvider: JoinedRecordToEntityConverterProvider,
                                     graphQLFieldToJoinMapper: GraphQLFieldToJoinMapper)
    : DBEntityRepository<CompanyPartnership, Long, CompanyPartnershipRecord>(
        create, asyncExecutor, recordToEntityConverterProvider, graphQLFieldToJoinMapper,
        COMPANY_PARTNERSHIP, COMPANY_PARTNERSHIP.ID),
        CompanyPartnershipRepository {

    /**
     * The (aliased) instance of the [Company] table that we'll join to when querying for partnerships, for the
     * "company A" in the partnership.
     */
    private val companyATable = COMPANY_PARTNERSHIP_COMPANY_A.getAliasedForeignTable(COMPANY_PARTNERSHIP) as Company

    /**
     * The (aliased) instance of the [Company] table that we'll join to when querying for partnerships, for the
     * "company B" in the partnership.
     */
    private val companyBTable = COMPANY_PARTNERSHIP_COMPANY_B.getAliasedForeignTable(COMPANY_PARTNERSHIP) as Company

    override fun getEntity(record: Record): CompanyPartnership =
            CompanyPartnership(
                    record.into(COMPANY_PARTNERSHIP).id,
                    record.into(companyATable).toEntity(),
                    record.into(companyBTable).toEntity())

    override fun addDefaultJoins(select: SelectJoinStep<Record>)
            : List<JoinInstance<out Any, CompanyPartnershipRecord, out Record>> {

        select.join(companyATable).on(COMPANY_PARTNERSHIP.COMPANY_A.eq(companyATable.ID))
                .join(companyBTable).on(COMPANY_PARTNERSHIP.COMPANY_B.eq(companyBTable.ID))

        return listOf(
                JoinInstance<Long, CompanyPartnershipRecord, CompanyRecord>(
                        COMPANY_PARTNERSHIP_COMPANY_A, COMPANY_PARTNERSHIP.COMPANY_A, companyATable.ID),
                JoinInstance<Long, CompanyPartnershipRecord, CompanyRecord>(
                        COMPANY_PARTNERSHIP_COMPANY_B, COMPANY_PARTNERSHIP.COMPANY_B, companyBTable.ID))
    }
}