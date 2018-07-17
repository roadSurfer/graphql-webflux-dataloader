package com.yg.gqlwfdl.dataaccess

import org.jooq.Record
import org.jooq.SelectJoinStep
import org.jooq.Table
import org.jooq.TableField

/**
 * A request for a join to be created when a query is executed.
 *
 * @param TFieldType The type of the fields to be joined.
 * @param TPrimaryRecord The type of the primary record in the join.
 * @param TForeignRecord The type of the foreign record in the join.
 * @property definition The [JoinDefinition] which this request is based on.
 * @property subsequentJoins Any subsequent join from the [TForeignRecord]'s table.
 */
class JoinRequest<TFieldType : Any, TPrimaryRecord : Record, TForeignRecord : Record>(
        private val definition: JoinDefinition<TFieldType, TPrimaryRecord, TForeignRecord>,
        private val subsequentJoins: List<JoinRequest<out Any, TForeignRecord, out Record>>) {

    /**
     * Adds a join to the passed in [select] object, based on the data in this object.
     *
     * @param primaryTableInstance The instance of the primary table to join to.
     */
    fun join(select: SelectJoinStep<Record>, primaryTableInstance: Table<TPrimaryRecord>)
            : JoinInstance<TFieldType, TPrimaryRecord, TForeignRecord> {

        val aliasedForeignTable = getAliasedForeignTable(primaryTableInstance)
        // We can ignore unchecked casts below because we know the items will come as the right types. We need to cast
        // for the generics to be used.
        @Suppress("UNCHECKED_CAST")
        val primaryFieldInstance = primaryTableInstance.field(definition.primaryField)
                as TableField<TPrimaryRecord, TFieldType>
        @Suppress("UNCHECKED_CAST")
        val foreignFieldInstance = aliasedForeignTable.field(definition.foreignField)
                as TableField<TForeignRecord, TFieldType>
        select.leftJoin(aliasedForeignTable).on(primaryFieldInstance.eq(foreignFieldInstance))
        val subsequentJoinInstances = subsequentJoins.map { it.join(select, aliasedForeignTable) }
        return JoinInstance(this, primaryFieldInstance, foreignFieldInstance, subsequentJoinInstances)
    }

    /**
     * Gets an instance of the foreign table, aliased to ensure it has a unique name in the query.
     */
    private fun getAliasedForeignTable(primaryTableInstance: Table<TPrimaryRecord>): Table<TForeignRecord> =
            definition.foreignField.table.`as`("${primaryTableInstance.name}_${definition.name}")
}