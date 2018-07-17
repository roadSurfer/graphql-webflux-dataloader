package com.yg.gqlwfdl.services

import com.yg.gqlwfdl.RequestContext
import com.yg.gqlwfdl.dataaccess.CompanyRepository
import com.yg.gqlwfdl.dataaccess.toJoinDefinitions
import com.yg.gqlwfdl.dataloaders.DataLoaderType
import com.yg.gqlwfdl.requestContext
import com.yg.gqlwfdl.yg.db.public_.tables.records.CompanyRecord
import graphql.schema.DataFetchingEnvironment
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
     * @param env The environment for the current GraphQL data fetch, if this method is called from such a context.
     */
    fun findAll(env: DataFetchingEnvironment? = null): CompletableFuture<List<Company>> =
            companyRepository
                    .findAll(
                            env?.field?.toJoinDefinitions(companyRepository.table),
                            env?.requestContext?.dataLoaderPrimerRecordListener)
                    .toEntityListCompletableFuture()

    /**
     * Returns a [CompletableFuture] which, when completed, will provide a [List] of all [Company] objects with the
     * passed in IDs.
     *
     * @param env The environment for the current GraphQL data fetch, if this method is called from such a context.
     */
    fun findByIds(ids: List<Long>, env: DataFetchingEnvironment? = null): CompletableFuture<List<Company>> =
            companyRepository
                    .findByIds(ids,
                            env?.field?.toJoinDefinitions(companyRepository.table),
                            env?.requestContext?.dataLoaderPrimerRecordListener)
                    .toEntityListCompletableFuture()

    /**
     * Converts a [CompletableFuture] which wraps an [Iterable] of [CompanyRecord]s to a CompletableFuture which will
     * return a [List] of the corresponding domain model objects (i.e. [Company]).
     */
    private fun CompletableFuture<out Iterable<CompanyRecord>>.toEntityListCompletableFuture() =
            this.thenApply { it.map { it.toEntity() } }
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