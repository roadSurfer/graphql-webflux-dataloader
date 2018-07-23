package com.yg.gqlwfdl.dataaccess.joins

import org.jooq.Record
import org.jooq.TableField

/**
 * An instance of a join in an executed query. Provides access to the actual instances of the joined fields (which were
 * possibly aliased).
 *
 * @param TFieldType The type of the fields being joined.
 * @param TPrimaryRecord The type of the primary record in the join.
 * @param TForeignRecord The type of the foreign record in the join.
 * @property primaryFieldInstance The instance of the primary field, which was probably aliased.
 * @property foreignFieldInstance The instance of the foreign field, which was probably aliased.
 * @property subsequentJoins Any subsequent join from the [foreignFieldInstance]'s table.
 */
class JoinInstance<TFieldType : Any, TPrimaryRecord : Record, TForeignRecord : Record>(
        private val definition: JoinDefinition<TFieldType, TPrimaryRecord, TForeignRecord>,
        val primaryFieldInstance: TableField<TPrimaryRecord, TFieldType>,
        private val foreignFieldInstance: TableField<TForeignRecord, TFieldType>,
        private val subsequentJoins: List<JoinInstance<out Any, TForeignRecord, out Record>> = listOf()) {

    /**
     * Gets the [JoinResult] corresponding to this instance, from the passed in row of data ([wholeRecord]). In other
     * words, gets the primary and foreign records at either side of the join, or null if the join didn't result in any
     * data at the foreign side.
     *
     * @param wholeRecord The whole record, i.e. the whole row of data containing all the data from all the tables that
     * were included in the query.
     * @param primaryRecord The primary record which was read from the [wholeRecord], i.e. the primary side of the join.
     */
    fun getResult(wholeRecord: Record, primaryRecord: TPrimaryRecord)
            : JoinResult<TFieldType, TPrimaryRecord, TForeignRecord>? {

        // If there's a value in the foreign field instance, that means the joined table has data, so get the record and
        // use it to construct the join result. Otherwise return null.
        return wholeRecord[foreignFieldInstance]?.let {
            val foreignRecord = wholeRecord.into(foreignFieldInstance.table)
            JoinResult(definition, primaryRecord, foreignRecord,
                    subsequentJoins.mapNotNull { it.getResult(wholeRecord, foreignRecord) })
        }
    }
}