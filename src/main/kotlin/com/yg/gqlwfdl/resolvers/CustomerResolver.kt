package com.yg.gqlwfdl.resolvers

import com.coxautodev.graphql.tools.GraphQLResolver
import com.yg.gqlwfdl.requestContext
import com.yg.gqlwfdl.services.Company
import com.yg.gqlwfdl.services.Customer
import com.yg.gqlwfdl.services.companyDataLoader
import com.yg.gqlwfdl.services.customerDataLoader
import graphql.schema.DataFetchingEnvironment
import java.util.concurrent.CompletableFuture

/**
 * Resolver for [Customer]s. Provides access to properties which the GraphQL schema exposes of these objects, but which
 * don't exist directly on the domain model object (Customer in this case), and need to be queried for separately. This
 * is done by delegating the work to the data loaders, so that the N+1 problem is bypassed, and the fetches can be
 * batches in one single call.
 */
class CustomerResolver : DataLoadingResolver(), GraphQLResolver<Customer> {

    /**
     * Gets a [CompletableFuture] which, when completed, when return the company for the passed in customer.
     */
    fun company(customer: Customer, env: DataFetchingEnvironment): CompletableFuture<Company> =
            prepareDataLoader(env) { env.requestContext.companyDataLoader }.load(customer.companyId)

    /**
     * Gets a [CompletableFuture] which, when completed, when return the out-of-office delegate for the passed in
     * customer.
     *
     * This might return null.
     */
    fun outOfOfficeDelegate(customer: Customer, env: DataFetchingEnvironment): CompletableFuture<Customer?> =
            if (customer.outOfOfficeDelegate == null) CompletableFuture.completedFuture(null)
            else prepareDataLoader(env) { env.requestContext.customerDataLoader }.load(customer.outOfOfficeDelegate)
}