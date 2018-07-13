package com.yg.gqlwfdl.services

import com.yg.gqlwfdl.dataaccess.CustomerRepository
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
     */
    fun findAll(): CompletableFuture<List<Customer>> =
            customerRepository.findAll().toEntityListCompletableFuture()

    /**
     * Returns a [CompletableFuture] which, when completed, will provide a [List] of all [Customer]s with the passed in
     * IDs.
     */
    fun findByIds(ids: List<Long>): CompletableFuture<List<Customer>> =
            customerRepository.findByIds(ids).toEntityListCompletableFuture()

    /**
     * Converts a [CompletableFuture] which wraps an [Iterable] of [CustomerRecord]s to a CompletableFuture which will
     * return a [List] of the corresponding domain model objects (i.e. [Customer]).
     */
    private fun CompletableFuture<out Iterable<CustomerRecord>>.toEntityListCompletableFuture() =
            this.thenApply { it.map { it.toEntity() } }

    /**
     * Converts a [CustomerRecord] (returned from the data access layer) to a [Customer] (instances of which are exposed
     * to callers of this service.
     */
    private fun CustomerRecord.toEntity() =
            Customer(this.id, this.firstName, this.lastName, this.companyId, this.outOfOfficeDelegate)
}