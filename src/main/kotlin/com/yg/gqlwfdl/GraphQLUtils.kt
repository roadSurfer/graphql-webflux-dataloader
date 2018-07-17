package com.yg.gqlwfdl

import graphql.language.Field

/**
 * Gets a list of all the child fields of the receiver.
 */
val Field.childFields
    get() = this.selectionSet.selections.filter { it is Field }.map { it as Field }