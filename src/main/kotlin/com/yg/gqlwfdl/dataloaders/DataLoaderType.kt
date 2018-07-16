package com.yg.gqlwfdl.dataloaders

import com.yg.gqlwfdl.RequestContext
import org.dataloader.DataLoaderRegistry

/**
 * Enumeration of all the different types of data loaders available in the system. Each one can be used to create an
 * instance of [ContextAwareDataLoader].
 */
enum class DataLoaderType {
    COMPANY, CUSTOMER;

    /**
     * The key under which this data loader is stored in the [DataLoaderRegistry], exposed by the
     * [ContextAwareDataLoader.requestContext]'s [RequestContext.dataLoaderRegistry] property.
     */
    val registryKey = toString()
}