package com.yg.gqlwfdl.dataloaders

import com.yg.gqlwfdl.RequestContext
import com.yg.gqlwfdl.services.Company
import com.yg.gqlwfdl.services.CompanyService

class CompanyDataLoader(requestContext: RequestContext,
                        dataLoaderFetchContext: DataLoaderFetchContext,
                        companyService: CompanyService)
    : ContextAwareDataLoader<Long, Company>(requestContext, dataLoaderFetchContext,
        { company -> company.id }, { keys -> companyService.findByIds(keys) }
)