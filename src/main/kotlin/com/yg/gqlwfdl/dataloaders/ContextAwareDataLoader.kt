package com.yg.gqlwfdl.dataloaders

import com.yg.gqlwfdl.RequestContext
import org.dataloader.BatchLoader
import org.dataloader.DataLoader
import java.util.concurrent.CompletionStage

/**
 * A data loader which is aware of the [RequestContext] in which it was called. Specialises the standard [DataLoader]
 * class by providing functionality required by subclasses such as giving access to the request context, and ensuring
 * that when items are returned from a data loader, they are returned in the exact order in which they were requested
 * (as this is part of the data loader contract).
 *
 * @param K The type of the unique ID of each object being loaded by this data loader. This value is used as the key
 * which identifies each item in the data loader's cache.
 * @param V The type of the object which this data loader retrieves and caches.
 * @param requestContext The context of the current HTTP request (i.e. GraphQL request). This object is passed around
 * so that whenever database queries are executed, the relevant data loaders can be made available (e.g. so that items
 * can be primed into them when found as part of executing a query with a database join in it).
 * @param dataLoaderFetchContext The context of the current request to this particular data loader. This is different
 * from the full request context: that is the context of the whole GraphQL request, whereas the dataLoaderFetchContext
 * is just about the current call to this data loader as part of executing the full GraphQL request.
 * @param keyFetcher A function which takes in an object of type [V] (e.g. some sort of entity) and returns its ID,
 * which is the key, of type [K], against which it's stored in the data loader's cache.
 * @param loader The function which is used to retrieve a list of objects of type [V] based on a list of their keys, of
 * type [K].
 *
 * TODO: this currently isn't using the context objects, but will do when joins are implemented.
 */
abstract class ContextAwareDataLoader<K, V>(private val requestContext: RequestContext,
                                            val dataLoaderFetchContext: DataLoaderFetchContext,
                                            keyFetcher: (V) -> K,
                                            loader: (List<K>) -> CompletionStage<List<V>>)
    : DataLoader<K, V>(BatchLoader { keys -> loader(keys).thenApply { it.syncWithKeys(keys, keyFetcher) } }) {
}

/**
 * From the receiver (a list of items which the data loader fetched by calling the `loader` function passed into the
 * constructor), returns a list of the same values, but in corresponding order to the passed in keys. This is required
 * as it's part of the DataLoader contract that values must be returned in corresponding order to the keys. If any key
 * doesn't have a corresponding item in the database, null is returned.
 *
 * @param keys The keys to synchronise the receiver with.
 * @param keyFetcher A function which takes in an object of type [V] (e.g. some sort of entity) and returns its ID,
 * which is the key, of type [K], against which it's stored in the data loader's cache.
 */
private fun <K, V> Iterable<V>.syncWithKeys(keys: Iterable<K>, keyFetcher: (V) -> K): List<V?> {
    val objectsMap = this.associateBy(keyFetcher)
    return keys.map { key -> objectsMap[key] }
}