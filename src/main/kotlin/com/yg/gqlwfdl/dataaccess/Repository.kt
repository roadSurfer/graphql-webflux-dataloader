package com.yg.gqlwfdl.dataaccess

import com.yg.gqlwfdl.withLogging
import org.jooq.DSLContext
import org.jooq.Table
import org.jooq.TableField
import org.jooq.UpdatableRecord
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

interface Repository<TId, TRecord : UpdatableRecord<TRecord>> {
    fun findAll(): CompletableFuture<out Iterable<TRecord>>
    fun findByIds(ids: List<TId>): CompletableFuture<out Iterable<TRecord>>
}

abstract class RepositoryImpl<TId, TRecord : UpdatableRecord<TRecord>>(
        private val create: DSLContext,
        private val asyncExecutor: Executor,
        private val table: Table<TRecord>,
        private val idField: TableField<TRecord, TId>) : Repository<TId, TRecord> {

    override fun findAll(): CompletableFuture<out Iterable<TRecord>> =
            withLogging("querying ${table.name} for all records") {
                create.selectFrom(table).fetchAsync(asyncExecutor).toCompletableFuture()
            }

    override fun findByIds(ids: List<TId>): CompletableFuture<out Iterable<TRecord>> =
            withLogging("querying ${table.name} for records with IDs: $ids") {
                create.selectFrom(table).where(idField.`in`(ids)).fetchAsync(asyncExecutor).toCompletableFuture()
            }
}