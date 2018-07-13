package com.yg.gqlwfdl.dataloaders

import graphql.language.Field
import com.yg.gqlwfdl.RequestContext

/**
 * The context of the current request to a particular data loader. This is different from the full [RequestContext]:
 * that is the context of the whole GraphQL request, whereas the DataLoaderFetchContext is just about a single call to
 * a particular data loader as part of executing the full GraphQL request. The RequestContext can be used to get global
 * information, whereas the DataLoaderFetchContext can be used to get information specific to a particular data loader,
 * such as the GraphQL fields that caused the data loader to be called (from which join information can be calculated).
 */
class DataLoaderFetchContext {
    // TODO: rename this to clarify that it's GraphQL fields?  Also, is "source" the right term?
    val sourceFields = mutableListOf<Field>()
}