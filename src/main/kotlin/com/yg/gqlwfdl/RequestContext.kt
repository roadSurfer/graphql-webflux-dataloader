package com.yg.gqlwfdl

import com.yg.gqlwfdl.dataaccess.DbConfig
import com.yg.gqlwfdl.dataloaders.ContextAwareDataLoader
import org.dataloader.DataLoaderRegistry

class RequestContext(private val dataLoaderRegistry: DataLoaderRegistry,
                     val dbConfig: DbConfig) {
    fun <K, V> dataLoader(key: String) = dataLoaderRegistry.getDataLoader<K, V>(key) as ContextAwareDataLoader
}