package com.yg.gqlwfdl

import com.yg.gqlwfdl.dataaccess.DbConfig
import com.yg.gqlwfdl.dataloaders.ContextAwareDataLoader
import graphql.ExecutionInput
import graphql.schema.DataFetchingEnvironment
import org.dataloader.DataLoaderRegistry

/**
 * An object containing information related to a single HTTP request (i.e. a GraphQL request). Passed from the GraphQL
 * entry points to the resolvers by storing it in the [ExecutionInput.context]. This allows the resolvers to then
 * retrieve it by calling the [DataFetchingEnvironment.getContext] method. The primary use of this object is to provide
 * access to the [DataLoaderRegistry] so that the resolvers have access to the (request-scoped) data loaders.
 *
 * @property dataLoaderRegistry The registry of data loaders which provides access to the data loaders.
 * @property dbConfig The database configuration object, which provides access to the executor used to perform database
 * actions asynchronously on a thread from a dedicated thread pool.
 */
class RequestContext(private val dataLoaderRegistry: DataLoaderRegistry,
                     val dbConfig: DbConfig) {

    // TODO: replace the use of a string for the data loader key with an enum, to keep it type-safe and avoid typos.
    fun <K, V> dataLoader(key: String) = dataLoaderRegistry.getDataLoader<K, V>(key) as ContextAwareDataLoader
}