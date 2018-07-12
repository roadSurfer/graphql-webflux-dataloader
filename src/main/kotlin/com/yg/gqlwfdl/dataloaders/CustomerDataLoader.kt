package com.yg.gqlwfdl.dataloaders

import com.yg.gqlwfdl.RequestContext
import com.yg.gqlwfdl.services.Customer
import com.yg.gqlwfdl.services.CustomerService

class CustomerDataLoader(requestContext: RequestContext,
                         dataLoaderFetchContext: DataLoaderFetchContext,
                         customerService: CustomerService)
    : ContextAwareDataLoader<Long, Customer>(requestContext, dataLoaderFetchContext,
        { customer -> customer.id }, { keys -> customerService.findByIds(keys) }
)