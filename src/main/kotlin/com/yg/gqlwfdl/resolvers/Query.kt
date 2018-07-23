package com.yg.gqlwfdl.resolvers

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import com.yg.gqlwfdl.services.CompanyPartnershipService
import com.yg.gqlwfdl.services.CompanyService
import com.yg.gqlwfdl.services.Customer
import com.yg.gqlwfdl.services.CustomerService
import com.yg.gqlwfdl.withLogging
import graphql.schema.DataFetchingEnvironment
import java.util.concurrent.CompletableFuture

@Suppress("unused")
/**
 * The resolver for GraphQL queries. Has methods corresponding to the properties on the Query type in the GraphQL
 * schema.
 */
class Query(private val customerService: CustomerService,
            private val companyService: CompanyService,
            private val companyPartnershipService: CompanyPartnershipService)
    : GraphQLQueryResolver {

    /**
     * Gets all customers in the system.
     */
    fun customers(env: DataFetchingEnvironment): CompletableFuture<List<Customer>> =
            withLogging("getting all customers") { customerService.findAll(env) }

    /**
     * Gets all customers with the passed in IDs.
     */
    fun customersByIds(ids: List<Long>, env: DataFetchingEnvironment): CompletableFuture<List<Customer>> =
            withLogging("getting customers with IDs $ids") { customerService.findByIds(ids, env) }

    /**
     * Gets all companies in the system.
     */
    fun companies(env: DataFetchingEnvironment) =
            withLogging("getting all companies") { companyService.findAll(env) }

    /**
     * Gets all company partnerships in the system.
     */
    fun companyPartnerships(env: DataFetchingEnvironment) =
            withLogging("getting all company partnerships") { companyPartnershipService.findAll(env) }
}
