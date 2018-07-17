package com.yg.gqlwfdl.dataaccess

import com.yg.gqlwfdl.letIfAny
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

    // TODO: Should we be using UpdatableRecord or just TableRecord?

    /**
     * The database table which this repository is providing access to.
     */
    val table: Table<TRecord>
        get

    /**
     * Returns a [CompletableFuture] which, when completed, will provide a [List] of all the [TRecord] items in the
     * underlying database table.
     *
     * @param joinRequests The joins that should be added to the query to fetch related items, if any are required.
     * @param recordListener The listener to inform whenever a record is found. This is done by calling it
     * [RecordListener.onRecordsReceived] function.
     */
    fun findAll(joinRequests: List<JoinRequest<out Any, TRecord, out Record>>? = null,
                recordListener: RecordListener? = null): CompletableFuture<List<TRecord>>

    /**
     * Returns a [CompletableFuture] which, when completed, will provide a [List] of all the [TRecord] items which have
     * the passed in IDs, in the underlying database table.
     *
     * @param ids The IDs of the items to be found.
     * @param joinRequests The joins that should be added to the query to fetch related items, if any are required.
     * @param recordListener The listener to inform whenever a record is found. This is done by calling it
     * [RecordListener.onRecordsReceived] function.
     */
    fun findByIds(ids: List<TId>,
                  joinRequests: List<JoinRequest<out Any, TRecord, out Record>>? = null,
                  recordListener: RecordListener? = null): CompletableFuture<List<TRecord>>
}

/**
 * Abstract implementation of the [Repository] interface, providing base functionality.
 *
 * @param TId The type of value which defines the unique ID (primary key) of the records in the underlying database
 * table.
 * @param TRecord The [UpdatableRecord] which represents a single record in the underlying database table.
 * @property create The DSL context used as the starting point for all operations when working with the JOOQ API. This
 * is automatically injected by spring-boot-starter-jooq.
 * @property asyncExecutor The executor used to execute database queries asynchronously, by executing them on a thread
 * from a dedicated thread pool.
 * @property idField The field from the table which stores the unique ID of each record, i.e. the primary key field.
 */
abstract class RepositoryImpl<TId, TRecord : UpdatableRecord<TRecord>>(
        private val create: DSLContext,
        private val asyncExecutor: Executor,
        override val table: Table<TRecord>,
        private val idField: TableField<TRecord, TId>) : Repository<TId, TRecord> {

    override fun findAll(joinRequests: List<JoinRequest<out Any, TRecord, out Record>>?,
                         recordListener: RecordListener?)
            : CompletableFuture<List<TRecord>> {

        return withLogging("querying ${table.name} for all records") {
            find(recordListener, joinRequests)
        }
    }

    override fun findByIds(ids: List<TId>,
                           joinRequests: List<JoinRequest<out Any, TRecord, out Record>>?,
                           recordListener: RecordListener?)
            : CompletableFuture<List<TRecord>> {

        return withLogging("querying ${table.name} for records with IDs $ids") {
            find(recordListener, joinRequests, listOf(idField.`in`(ids)))
        }
    }

    /**
     * Finds all the records that match the passed in [conditions].
     *
     * @param recordListener The listener to inform whenever records are found. Ignored if null.
     * @param joinRequests Any joins that should be added to the query.
     * @param conditions Any conditions that should be added to the query.
     */
    private fun find(recordListener: RecordListener?,
                     joinRequests: List<JoinRequest<out Any, TRecord, out Record>>?,
                     conditions: List<Condition>? = null)
            : CompletableFuture<List<TRecord>> {

        val select: SelectJoinStep<Record> = create.select().from(table)
        val joinInstances = select.addJoins(table, joinRequests)

        return select
                .withConditions(conditions)
                .fetchAsync()
                .thenApply { it.map { processRecord(it, joinInstances, recordListener) } }
                .toCompletableFuture()
    }

    /**
     * Processes the passed in record, which is the full row containing all joined tables' data. Returns the main record
     * (of type [TRecord]) which should be returned from methods that are finding objects from the repository.
     */
    private fun processRecord(record: Record,
                              joinInstances: List<JoinInstance<out Any, out Record, out Record>>,
                              recordListener: RecordListener?)
            : TRecord {

        val joinedRecords : MutableList<Record> = mutableListOf()
        recordListener?.let {
            joinInstances.letIfAny {
                fun JoinInstance<out Any, out Record, out Record>.process() {
                    record[primaryFieldInstance]?.let {
                        joinedRecords.add(record.into(foreignFieldInstance.table))
                        subsequentJoins.forEach { it.process() }
                    }
                }
                joinInstances.forEach { it.process()}
            }
        }

        val primaryRecord = record.into(table)
        recordListener?.onRecordsReceived(joinedRecords.plusElement(primaryRecord))
        return primaryRecord
    }
}

/**
 * Adds the passed in [joinRequests] (if any) to the receiver, and returns the created [JoinRequest]s.
 */
private fun <TPrimaryRecord : Record> SelectJoinStep<Record>.addJoins(
        primaryTable: Table<TPrimaryRecord>,
        joinRequests: List<JoinRequest<out Any, TPrimaryRecord, out Record>>?)
        : List<JoinInstance<out Any, out Record, out Record>> {

    return joinRequests?.map { it.join(this, primaryTable) } ?: listOf()
}

/**
 * Updates the receiver and adds to it the passed in [conditions], if any. Returns the same instance that was passed in.
 */
private fun SelectJoinStep<Record>.withConditions(conditions: List<Condition>?): SelectConnectByStep<Record> =
        conditions?.letIfAny { this.where(it.toList()) } ?: this
