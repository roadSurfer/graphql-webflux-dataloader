package com.yg.gqlwfdl.dataaccess

import com.yg.gqlwfdl.withLogging
import org.jooq.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

/**
 * A repository providing access to a single database table.
 *
 * @param TId The type of value which defines the unique ID (primary key) of the records in the underlying database
 * table.
 * @param TRecord The [UpdatableRecord] which represents a single record in the underlying database table.
 */
interface Repository<TId, TRecord : UpdatableRecord<TRecord>> {
    /**
     * Returns a [CompletableFuture] which, when completed, will provide a [List] of all the [TRecord] items in the
     * underlying database table.
     *
     * @param recordListener The listener to inform whenever a record is found. This is done by calling it
     * [RecordListener.onRecordsReceived] function.
     */
    fun findAll(recordListener: RecordListener? = null): CompletableFuture<List<TRecord>>

    /**
     * Returns a [CompletableFuture] which, when completed, will provide a [List] of all the [TRecord] items which have
     * the passed in IDs, in the underlying database table.
     *
     * @param ids The IDs of the items to be found.
     * @param recordListener The listener to inform whenever a record is found. This is done by calling it
     * [RecordListener.onRecordsReceived] function.
     */
    fun findByIds(ids: List<TId>, recordListener: RecordListener? = null): CompletableFuture<List<TRecord>>
}

/**
 * Abstract implementation of the [Repository] interface, providing base functionality.
 *
 * @property create The DSL context used as the starting point for all operations when working with the JOOQ API. This
 * is automatically injected by spring-boot-starter-jooq.
 * @property asyncExecutor The executor used to execute database queries asynchronously, by executing them on a thread
 * from a dedicated thread pool.
 * @property table The database table which this repository is providing access to.
 * @property idField The field from the table which stores the unique ID of each record, i.e. the primary key field.
 */
abstract class RepositoryImpl<TId, TRecord : UpdatableRecord<TRecord>>(
        private val create: DSLContext,
        private val asyncExecutor: Executor,
        private val table: Table<TRecord>,
        private val idField: TableField<TRecord, TId>) : Repository<TId, TRecord> {

    override fun findAll(recordListener: RecordListener?): CompletableFuture<List<TRecord>> =
            withLogging("querying ${table.name} for all records") {
                find(recordListener)
            }

    override fun findByIds(ids: List<TId>, recordListener: RecordListener?): CompletableFuture<List<TRecord>> =
            withLogging("querying ${table.name} for records with IDs $ids") {
                find(recordListener, listOf(idField.`in`(ids)))
            }

    // TODO: if doing joins, can we still use UpdatableRecord, or just TableRecord?
    // TODO: document the newly added methods below once join functionality implemented.

    private fun find(recordListener: RecordListener?, conditions: List<Condition>? = null): CompletableFuture<List<TRecord>> {
        return create.select().from(table).withConditions(conditions)
                .fetchAsync()
                .thenApply { it.map { it.also { recordListener?.onRecordsReceived(listOf(it)) }.into(table) } }
                .toCompletableFuture()
    }

    private fun SelectJoinStep<Record>.withConditions(conditions: List<Condition>?): SelectConnectByStep<Record> =
            if (conditions != null && conditions.any()) this.where(conditions) else this
}