package com.yg.gqlwfdl.dataloaders

import graphql.language.Field

class DataLoaderFetchContext {
    // TODO: rename this to clarify that it's GraphQL fields?  Also, is "source" the right term?
    val sourceFields = mutableListOf<Field>()
}