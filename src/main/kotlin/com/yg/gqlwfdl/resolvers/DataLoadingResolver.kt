package com.yg.gqlwfdl.resolvers

import com.yg.gqlwfdl.RequestContext
import com.yg.gqlwfdl.dataloaders.ContextAwareDataLoader
import graphql.schema.DataFetchingEnvironment
import org.dataloader.DataLoaderRegistry

/**
 * Abstract base class for all resolvers for domain level objects. Provides access to properties which the GraphQL
 * schema exposes of these objects, but which don't exist directly on the domain model object, and need to be queried
 * for separately. This is done by delegating the work to the data loader, so that the N+1 problem is bypassed, and the
 * fetches can be batches in one single call.
 */
abstract class DataLoadingResolver {

    /**
     * Gets a data loader from the receiver (a [DataFetchingEnvironment]), which is registered with the passed in key.
     *
     * @param K The type of the key which the items cached by the requested data loader are stored (i.e. the type of
     * its unique identifier).
     * @param V The type of the objects cached by the requested data loader.
     * @param key The key with which the data loader is registered in the [DataLoaderRegistry]
     * @param addFieldToContext TODO: document when implemented/used.
     */
    protected fun <K, V> DataFetchingEnvironment.dataLoader(key: String, addFieldToContext: Boolean = false)
            : ContextAwareDataLoader<K, V> {
        val dataLoader = this.getContext<RequestContext>().dataLoader<K, V>(key)
        if (addFieldToContext)
            dataLoader.dataLoaderFetchContext.sourceFields.add(this.field)
        return dataLoader
    }
}