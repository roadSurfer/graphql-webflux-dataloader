package com.yg.gqlwfdl.services

import com.yg.gqlwfdl.RequestContext
import com.yg.gqlwfdl.dataaccess.CompanyRepository
import com.yg.gqlwfdl.dataloaders.DataLoaderType
import com.yg.gqlwfdl.yg.db.public_.tables.records.CompanyRecord
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
     *
     * @param requestContext The context of the current request, if any. Used to get the relevant data loader to prime
     * with any items retrieved from the repository, so that subsequent calls can use those pre-cached items rather
     * than having to query again for them.
     */
    fun findAll(requestContext: RequestContext? = null): CompletableFuture<List<Company>> =
            companyRepository.findAll().toEntityListCompletableFuture(requestContext)

    /**
     * Returns a [CompletableFuture] which, when completed, will provide a [List] of all [Company] objects with the
     * passed in IDs.
     *
     * @param requestContext The context of the current request, if any. Used to get the relevant data loader to prime
     * with any items retrieved from the repository, so that subsequent calls can use those pre-cached items rather
     * than having to query again for them.
     */
    fun findByIds(ids: List<Long>, requestContext: RequestContext? = null): CompletableFuture<List<Company>> =
            companyRepository.findByIds(ids).toEntityListCompletableFuture(requestContext)

    /**
     * Converts a [CompletableFuture] which wraps an [Iterable] of [CompanyRecord]s to a CompletableFuture which will
     * return a [List] of the corresponding domain model objects (i.e. [Company]).
     *
     * @param requestContext The context of the current request, if any. Used to get the relevant data loader to prime
     * with any items retrieved from the repository, so that subsequent calls can use those pre-cached items rather
     * than having to query again for them.
     */
    private fun CompletableFuture<out Iterable<CompanyRecord>>.toEntityListCompletableFuture(requestContext: RequestContext?) =
            this.thenApply { it.map { it.toEntity().also { requestContext?.companyDataLoader?.prime(it) } } }
}

/**
 * Converts a [CompanyRecord] (returned from the data access layer) to a [Company] (instances of which are exposed
 * to callers of this service.
 */
fun CompanyRecord.toEntity() = Company(this.id, this.name, this.address)

/**
 * Gets the data loader for caching/loading companies ([Company] objects).
 */
val RequestContext.companyDataLoader
    get() = this.dataLoader<Long, Company>(DataLoaderType.COMPANY)