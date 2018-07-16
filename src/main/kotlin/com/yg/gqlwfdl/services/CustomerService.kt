package com.yg.gqlwfdl.services

import com.yg.gqlwfdl.RequestContext
import com.yg.gqlwfdl.dataaccess.CustomerRepository
import com.yg.gqlwfdl.dataloaders.DataLoaderType
import com.yg.gqlwfdl.yg.db.public_.tables.records.CustomerRecord
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
     * @param requestContext The context of the current request, if any. Used to get the relevant data loader to prime
     * with any items retrieved from the repository, so that subsequent calls can use those pre-cached items rather
     * than having to query again for them.
     */
    fun findAll(requestContext: RequestContext? = null): CompletableFuture<List<Customer>> =
            customerRepository.findAll().toEntityListCompletableFuture(requestContext)

    /**
     * Returns a [CompletableFuture] which, when completed, will provide a [List] of all [Customer]s with the passed in
     * IDs.
     *
     * @param requestContext The context of the current request, if any. Used to get the relevant data loader to prime
     * with any items retrieved from the repository, so that subsequent calls can use those pre-cached items rather
     * than having to query again for them.
     */
    fun findByIds(ids: List<Long>, requestContext: RequestContext? = null): CompletableFuture<List<Customer>> =
            customerRepository.findByIds(ids).toEntityListCompletableFuture(requestContext)

    /**
     * Converts a [CompletableFuture] which wraps an [Iterable] of [CustomerRecord]s to a CompletableFuture which will
     * return a [List] of the corresponding domain model objects (i.e. [Customer]).
     *
     * @param requestContext The context of the current request, if any. Used to get the relevant data loader to prime
     * with any items retrieved from the repository, so that subsequent calls can use those pre-cached items rather
     * than having to query again for them.
     */
    private fun CompletableFuture<out Iterable<CustomerRecord>>.toEntityListCompletableFuture(requestContext: RequestContext?) =
            this.thenApply { it.map { it.toEntity().also { requestContext?.customerDataLoader?.prime(it) } } }
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