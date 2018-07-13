package com.yg.gqlwfdl.dataloaders

import com.yg.gqlwfdl.RequestContext
import com.yg.gqlwfdl.services.Company
import com.yg.gqlwfdl.services.CompanyService

/**
 * Data loader responsible for getting company information. Uses the [CompanyService] to get at companies.
 */
class CompanyDataLoader(requestContext: RequestContext,
                        dataLoaderFetchContext: DataLoaderFetchContext,
                        companyService: CompanyService)
    : ContextAwareDataLoader<Long, Company>(requestContext, dataLoaderFetchContext,
        { it.id }, { companyService.findByIds(it) })