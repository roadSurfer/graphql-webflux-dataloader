package com.yg.gqlwfdl.dataaccess

import org.jooq.Record
import org.jooq.TableField

/**
 * An instance of a join in an executed query. Provides access to the actual instances of the joined fields (which were
 * probably aliased).
 *
 * @param TFieldType The type of the fields being joined.
 * @param TPrimaryRecord The type of the primary record in the join.
 * @param TForeignRecord The type of the foreign record in the join.
 * @property joinRequest The [JoinRequest] which caused this instance to be created.
 * @property primaryFieldInstance The instance of the primary field, which was probably aliased.
 * @property foreignFieldInstance The instance of the foreign field, which was probably aliased.
 * @property subsequentJoins Any subsequent join from the [foreignFieldInstance]'s table.
 */
class JoinInstance<TFieldType : Any, TPrimaryRecord : Record, TForeignRecord : Record>(
        val joinRequest: JoinRequest<TFieldType, TPrimaryRecord, TForeignRecord>,
        val primaryFieldInstance: TableField<TPrimaryRecord, TFieldType>,
        val foreignFieldInstance: TableField<TForeignRecord, TFieldType>,
        val subsequentJoins: List<JoinInstance<out Any, TForeignRecord, out Record>>) {
}