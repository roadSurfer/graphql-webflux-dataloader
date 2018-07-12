package com.yg.gqlwfdl.dataloaders

import com.yg.gqlwfdl.RequestContext
import org.dataloader.BatchLoader
import org.dataloader.DataLoader
import java.util.concurrent.CompletionStage

abstract class ContextAwareDataLoader<K, V>(val requestContext: RequestContext,
                                            val dataLoaderFetchContext: DataLoaderFetchContext,
                                            keyFetcher: (V) -> K,
                                            loader: (List<K>) -> CompletionStage<List<V>>)
    : DataLoader<K, V>(BatchLoader { keys -> loader(keys).thenApply { it.syncWithKeys(keys, keyFetcher) } }) {
}

private fun <K, V> Iterable<V>.syncWithKeys(keys: Iterable<K>, keyFetcher: (V) -> K): List<V?> {
    val objectsMap = this.associate { Pair(keyFetcher(it), it) }
    return keys.map { key -> objectsMap[key] }
}