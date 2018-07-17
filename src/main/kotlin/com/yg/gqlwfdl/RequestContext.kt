package com.yg.gqlwfdl

import com.yg.gqlwfdl.dataaccess.DbConfig
import com.yg.gqlwfdl.dataloaders.ContextAwareDataLoader
import com.yg.gqlwfdl.dataloaders.DataLoaderType
import com.yg.gqlwfdl.services.DataLoaderPrimerRecordListener
import graphql.ExecutionInput
import graphql.language.Field
import graphql.schema.DataFetchingEnvironment
import org.dataloader.DataLoaderRegistry

/**
 * An object representing the context of a single HTTP request (i.e. a GraphQL request). Passed from the GraphQL
 * entry points to the resolvers by storing it in the [ExecutionInput.context]. This allows the resolvers to then
 * retrieve it by calling the [DataFetchingEnvironment.getContext] method. The primary use of this object is to provide
 * access to the [DataLoaderRegistry] so that the resolvers have access to the (request-scoped) data loaders.
 *
 * @property dataLoaderRegistry The registry of data loaders which provides access to the data loaders.
 */
class RequestContext(private val dataLoaderRegistry: DataLoaderRegistry) {

    /**
     * Gets a data loader registered with the passed in key, in the current request context. Throws an exception if no
     * such data loader is registered.
     *
     * @param K The type of the key which the items cached by the requested data loader are stored (i.e. the type of
     * its unique identifier).
     * @param V The type of the objects cached by the requested data loader.
     * @param type The type of the data loader being requested.
     */
    fun <K, V> dataLoader(type: DataLoaderType) =
            dataLoaderRegistry.getDataLoader<K, V>(type.registryKey)!! as ContextAwareDataLoader

    val dataLoaderPrimerRecordListener = DataLoaderPrimerRecordListener(this)
}

/**
 * Gets the [RequestContext] of the current request from the receiver, which is the current [DataFetchingEnvironment].
 */
val DataFetchingEnvironment.requestContext: RequestContext
    get() = this.getContext<RequestContext>()