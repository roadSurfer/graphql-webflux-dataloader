package com.yg.gqlwfdl.services

import com.yg.gqlwfdl.dataaccess.CompanyRepository
import com.yg.gqlwfdl.yg.db.public_.tables.records.CompanyRecord
import com.yg.gqlwfdl.yg.db.public_.tables.records.CustomerRecord
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

/**
 * Service for handling functionality related to companies. Communicates with the data access layer to get the data
 * from the database, and exposes it to callers using the domain model objects (specifically, [Company]). Performs
 * all actions asynchronously.
 */
@Service
class CompanyService(private val companyRepository: CompanyRepository) {
    /**
     * Returns a [CompletableFuture] which, when completed, will provide a [List] of all [Company] objects.
     */
    fun findAll(): CompletableFuture<List<Company>> =
            companyRepository.findAll().toEntityListCompletableFuture()

    /**
     * Returns a [CompletableFuture] which, when completed, will provide a [List] of all [Company] objects with the
     * passed in IDs.
     */
    fun findByIds(ids: List<Long>): CompletableFuture<List<Company>> =
            companyRepository.findByIds(ids).toEntityListCompletableFuture()

    /**
     * Converts a [CompletableFuture] which wraps an [Iterable] of [CompanyRecord]s to a CompletableFuture which will
     * return a [List] of the corresponding domain model objects (i.e. [Company]).
     */
    private fun CompletableFuture<out Iterable<CompanyRecord>>.toEntityListCompletableFuture() =
            this.thenApply { it.map { it.toEntity() } }

    /**
     * Converts a [CompanyRecord] (returned from the data access layer) to a [Company] (instances of which are exposed
     * to callers of this service.
     */
    private fun CompanyRecord.toEntity() = Company(this.id, this.name, this.address)
}