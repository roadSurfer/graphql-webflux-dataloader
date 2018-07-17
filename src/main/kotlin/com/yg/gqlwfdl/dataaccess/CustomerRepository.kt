package com.yg.gqlwfdl.dataaccess

import com.yg.gqlwfdl.yg.db.public_.Tables.CUSTOMER
import com.yg.gqlwfdl.yg.db.public_.tables.records.CustomerRecord
import org.jooq.DSLContext
import java.util.concurrent.Executor
import com.yg.gqlwfdl.yg.db.public_.Tables.COMPANY as COMPANY_TABLE
import org.springframework.stereotype.Repository as SpringRepository

/**
 * Repository providing access to customer information.
 */
interface CustomerRepository : Repository<Long, CustomerRecord>

/**
 * Concrete implementation of [CustomerRepository].
 */
@SpringRepository
class CustomerRepositoryImpl(create: DSLContext, asyncExecutor: Executor)
    : RepositoryImpl<Long, CustomerRecord>(create, asyncExecutor, CUSTOMER, CUSTOMER.ID), CustomerRepository