package com.yg.gqlwfdl.dataaccess

import com.yg.gqlwfdl.yg.db.public_.Tables.COMPANY
import com.yg.gqlwfdl.yg.db.public_.tables.records.CompanyRecord
import org.jooq.DSLContext
import java.util.concurrent.Executor

interface CompanyRepository : Repository<Long, CompanyRecord>

@org.springframework.stereotype.Repository
class CompanyRepositoryImpl(create: DSLContext, asyncExecutor: Executor)
    : RepositoryImpl<Long, CompanyRecord>(create, asyncExecutor, COMPANY, COMPANY.ID), CompanyRepository