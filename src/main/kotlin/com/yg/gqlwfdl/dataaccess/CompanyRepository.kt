package com.yg.gqlwfdl.dataaccess

import com.yg.gqlwfdl.yg.db.public_.Tables.COMPANY
import com.yg.gqlwfdl.yg.db.public_.tables.records.CompanyRecord
import org.jooq.DSLContext
import java.util.concurrent.Executor
import org.springframework.stereotype.Repository as SpringRepository

/**
 * Repository providing access to company information.
 */
interface CompanyRepository : Repository<Long, CompanyRecord>

/**
 * Concrete implementation of [CompanyRepository].
 */
@SpringRepository
class CompanyRepositoryImpl(create: DSLContext, asyncExecutor: Executor)
    : RepositoryImpl<Long, CompanyRecord>(create, asyncExecutor, COMPANY, COMPANY.ID), CompanyRepository