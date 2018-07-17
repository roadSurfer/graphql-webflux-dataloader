package com.yg.gqlwfdl.dataaccess

import graphql.language.Field
import org.jooq.Record
import org.jooq.TableField

/**
 * Defines a join that exists in the underlying database.
 *
 * @param TFieldType The type of the fields being joined.
 * @param TPrimaryRecord The type of the primary record in the join.
 * @param TForeignRecord The type of the foreign record in the join.
 * @property name The name of this join. This should match the name of the property in the GraphQL schema which exposes
 * a value which has to be populated by querying a secondary table.
 * @param primaryField The primary field in the join.
 * @param foreignField The foreign field in the join.
 */
class JoinDefinition<TFieldType : Any, TPrimaryRecord : Record, TForeignRecord : Record>(
        val name: String,
        val primaryField: TableField<TPrimaryRecord, TFieldType>,
        val foreignField: TableField<TForeignRecord, TFieldType>) {

    /**
     * Creates a [JoinRequest] instance based on this definition.
     *
     * @param graphQLField The GraphQL field which caused this join to be created. This field's children will be
     * used to populate the [subsequent joins][JoinRequest.subsequentJoins] of the join request.
     */
    fun createRequest(graphQLField: Field): JoinRequest<TFieldType, TPrimaryRecord, TForeignRecord> =
            JoinRequest(this, graphQLField.toJoinDefinitions(foreignField.table))
}