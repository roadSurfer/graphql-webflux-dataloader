package com.yg.gqlwfdl.resolvers

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import com.yg.gqlwfdl.services.CompanyService
import com.yg.gqlwfdl.services.Customer
import com.yg.gqlwfdl.services.CustomerService
import com.yg.gqlwfdl.withLogging
import graphql.schema.DataFetchingEnvironment
import java.util.concurrent.CompletableFuture

class Query(private val customerService: CustomerService, private val companyService: CompanyService) : GraphQLQueryResolver {

    fun customers(env: DataFetchingEnvironment): CompletableFuture<List<Customer>> =
            withLogging("getting all customers") { customerService.findAll() }

    fun customersByIds(ids: List<Long>, env: DataFetchingEnvironment): CompletableFuture<List<Customer>> =
            withLogging("getting customers with IDs $ids") { customerService.findByIds(ids) }

    fun companies(env: DataFetchingEnvironment) =
            withLogging("getting all companies") { companyService.findAll() }
}
