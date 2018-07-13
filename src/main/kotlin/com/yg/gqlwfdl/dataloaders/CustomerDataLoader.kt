package com.yg.gqlwfdl.dataloaders

import com.yg.gqlwfdl.RequestContext
import com.yg.gqlwfdl.services.Customer
import com.yg.gqlwfdl.services.CustomerService

/**
 * Data loader responsible for getting customer information. Uses the [CustomerService] to get at customers.
 */
class CustomerDataLoader(requestContext: RequestContext,
                         dataLoaderFetchContext: DataLoaderFetchContext,
                         customerService: CustomerService)
    : ContextAwareDataLoader<Long, Customer>(requestContext, dataLoaderFetchContext,
        { it.id }, { customerService.findByIds(it) })