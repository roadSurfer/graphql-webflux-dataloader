package com.yg.gqlwfdl.resolvers

import com.yg.gqlwfdl.RequestContext
import com.yg.gqlwfdl.dataloaders.ContextAwareDataLoader
import graphql.schema.DataFetchingEnvironment

abstract class DataLoadingResolver {

    protected fun <K, V> DataFetchingEnvironment.dataLoader(key: String, addFieldToContext: Boolean = false): ContextAwareDataLoader<K, V> {
        val dataLoader = this.getContext<RequestContext>().dataLoader<K, V>(key)
        if (addFieldToContext)
            dataLoader.dataLoaderFetchContext.sourceFields.add(this.field)
        return dataLoader
    }
}