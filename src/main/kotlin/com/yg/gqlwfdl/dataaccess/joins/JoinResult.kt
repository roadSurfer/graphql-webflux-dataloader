package com.yg.gqlwfdl.dataaccess.joins

import org.jooq.Record

/**
 * Represents the result of a join in a query, i.e. the data for a single join, for a single result row returned from
 * the query.
 *
 * @param definition The [JoinDefinition] which resulted in this object being created.
 * @param primaryRecord The primary record of the join.
 * @param foreignRecord The foreign record of the join.
 * @param subsequentJoins If the [foreignRecord] had subsequent joins, those results are provided by this property,
 * otherwise an empty list is used.
 */
data class JoinResult<TFieldType : Any, TPrimaryRecord : Record, TForeignRecord : Record>(
        val definition: JoinDefinition<TFieldType, TPrimaryRecord, TForeignRecord>,
        val primaryRecord: TPrimaryRecord,
        val foreignRecord: TForeignRecord,
        val subsequentJoins: List<JoinResult<out Any, TForeignRecord, out Record>>)