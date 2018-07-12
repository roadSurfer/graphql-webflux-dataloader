package com.yg.gqlwfdl.dataaccess

import com.yg.gqlwfdl.yg.db.public_.Tables.CUSTOMER
import com.yg.gqlwfdl.yg.db.public_.tables.records.CustomerRecord
import org.jooq.DSLContext
import java.util.concurrent.Executor

interface CustomerRepository : Repository<Long, CustomerRecord>

@org.springframework.stereotype.Repository
class CustomerRepositoryImpl(create: DSLContext, asyncExecutor: Executor)
    : RepositoryImpl<Long, CustomerRecord>(create, asyncExecutor, CUSTOMER, CUSTOMER.ID), CustomerRepository