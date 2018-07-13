package com.yg.gqlwfdl.dataaccess

import com.yg.gqlwfdl.withLogging
import org.jooq.DSLContext
import org.jooq.Table
import org.jooq.TableField
import org.jooq.UpdatableRecord
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
     */
    fun findAll(): CompletableFuture<out List<TRecord>>

    /**
     * Returns a [CompletableFuture] which, when completed, will provide a [List] of all the [TRecord] items which have
     * the passed in IDs, in the underlying database table.
     */
    fun findByIds(ids: List<TId>): CompletableFuture<out List<TRecord>>
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

    override fun findAll(): CompletableFuture<out List<TRecord>> =
            withLogging("querying ${table.name} for all records") {
                create.selectFrom(table).fetchAsync(asyncExecutor).toCompletableFuture()
            }

    override fun findByIds(ids: List<TId>): CompletableFuture<out List<TRecord>> =
            withLogging("querying ${table.name} for records with IDs $ids") {
                create.selectFrom(table).where(idField.`in`(ids)).fetchAsync(asyncExecutor).toCompletableFuture()
            }
}