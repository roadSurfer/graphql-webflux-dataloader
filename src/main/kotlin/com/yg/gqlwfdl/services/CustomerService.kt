package com.yg.gqlwfdl.services

import com.yg.gqlwfdl.RequestContext
import com.yg.gqlwfdl.dataaccess.CustomerRepository
import com.yg.gqlwfdl.dataaccess.toJoinDefinitions
import com.yg.gqlwfdl.dataloaders.DataLoaderType
import com.yg.gqlwfdl.requestContext
import com.yg.gqlwfdl.yg.db.public_.tables.records.CustomerRecord
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

/**
 * Service for handling functionality related to customers. Communicates with the data access layer to get the data
 * from the database, and exposes it to callers using the domain model objects (specifically, [Customer]). Performs
 * all actions asynchronously.
 */
@Service
class CustomerService(private val customerRepository: CustomerRepository) {
    /**
     * Returns a [CompletableFuture] which, when completed, will provide a [List] of all [Customer]s.
     *
     * @param env The environment for the current GraphQL data fetch, if this method is called from such a context.
     */
    fun findAll(env: DataFetchingEnvironment? = null): CompletableFuture<List<Customer>> =
            customerRepository
                    .findAll(
                            env?.field?.toJoinDefinitions(customerRepository.table),
                            env?.requestContext?.dataLoaderPrimerRecordListener)
                    .toEntityListCompletableFuture()

    /**
     * Returns a [CompletableFuture] which, when completed, will provide a [List] of all [Customer]s with the passed in
     * IDs.
     *
     * @param env The environment for the current GraphQL data fetch, if this method is called from such a context.
     */
    fun findByIds(ids: List<Long>, env: DataFetchingEnvironment? = null): CompletableFuture<List<Customer>> =
            customerRepository
                    .findByIds(ids,
                            env?.field?.toJoinDefinitions(customerRepository.table),
                            env?.requestContext?.dataLoaderPrimerRecordListener)
                    .toEntityListCompletableFuture()

    /**
     * Converts a [CompletableFuture] which wraps an [Iterable] of [CustomerRecord]s to a CompletableFuture which will
     * return a [List] of the corresponding domain model objects (i.e. [Customer]).
     */
    private fun CompletableFuture<out Iterable<CustomerRecord>>.toEntityListCompletableFuture() =
            this.thenApply { it.map { it.toEntity() } }
}

/**
 * Converts a [CustomerRecord] (returned from the data access layer) to a [Customer] (instances of which are exposed
 * to callers of this service.
 */
fun CustomerRecord.toEntity() =
        Customer(this.id, this.firstName, this.lastName, this.companyId, this.outOfOfficeDelegate)

/**
 * Gets the data loader for caching/loading customers ([Customer] objects).
 */
val RequestContext.customerDataLoader
    get() = this.dataLoader<Long, Customer>(DataLoaderType.CUSTOMER)