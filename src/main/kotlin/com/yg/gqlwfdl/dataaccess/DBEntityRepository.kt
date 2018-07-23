package com.yg.gqlwfdl.dataaccess

import com.yg.gqlwfdl.RequestContext
import com.yg.gqlwfdl.dataaccess.joins.*
import com.yg.gqlwfdl.letIfAny
import com.yg.gqlwfdl.requestContext
import com.yg.gqlwfdl.services.Entity
import com.yg.gqlwfdl.withLogging
import graphql.schema.DataFetchingEnvironment
import org.jooq.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

/**
 * A repository providing access to an entity (aka domain model object) (of type [TEntity]), by querying one or more
 * database tables.
 *
 * When [findAll] or [findByIds] are called, the passed in [DataFetchingEnvironment] (if any) is interrogated to see if
 * any joins need to be added to the generated database queries, based on the requested GraphQL fields. Additionally,
 * the [DataFetchingEnvironment.requestContext]'s [RequestContext.dataLoaderPrimerEntityCreationListener] is informed
 * of any created entities, unless a specific [EntityCreationListener] is passed in instead.
 *
 * @param TEntity The type of the entity to which this repository provides access.
 * @param TId The type of value which defines the unique ID of the entity (typically corresponding to the type of the
 * primary key in the main underlying database table).
 * @param TRecord The [UpdatableRecord] which represents a single record in the underlying database table. In the case
 * where an entity is based on data from multiple database tables, this should be the primary table (sometimes known
 * as the aggregate root).
 * @property create The DSL context used as the starting point for all operations when working with the JOOQ API. This
 * is automatically injected by spring-boot-starter-jooq.
 * @property asyncExecutor The executor used to execute database queries asynchronously, by executing them on a thread
 * from a dedicated thread pool.
 * @property recordToEntityConverterProvider The object to convert [Record]s to [TEntity] objects.
 * @property graphQLFieldToJoinMapper The object to use when finding objects in the context of a GraphQL request, to
 * know which joins to add to the database queries, based on the requested GraphQL fields.
 * @property table The database table which this repository is providing access to. In the case where an entity is based on
 * data from multiple database tables, this should be the primary table (sometimes known as the aggregate root).
 * @property idField The field from the table which stores the unique ID of each record, i.e. the primary key field.
 */
abstract class DBEntityRepository<TEntity : Entity<TId>, TId : Any, TRecord : UpdatableRecord<TRecord>>(
        private val create: DSLContext,
        private val asyncExecutor: Executor,
        private val recordToEntityConverterProvider: JoinedRecordToEntityConverterProvider,
        private val graphQLFieldToJoinMapper: GraphQLFieldToJoinMapper,
        private val table: Table<TRecord>,
        private val idField: TableField<TRecord, TId>) : EntityRepository<TEntity, TId> {

    /**
     * See [EntityRepository.findAll]. Note that in this implementation the passed in [DataFetchingEnvironment] (if any)
     * is interrogated to see if any joins need to be added to the generated database queries, based on the requested
     * GraphQL fields. Additionally, the [DataFetchingEnvironment.requestContext]'s
     * [RequestContext.dataLoaderPrimerEntityCreationListener] is informed of any created entities.
     */
    override fun findAll(env: DataFetchingEnvironment?): CompletableFuture<List<TEntity>> {

        return findAll(
                env?.field?.let { graphQLFieldToJoinMapper.getJoinRequests(it, table) },
                env?.requestContext?.dataLoaderPrimerEntityCreationListener)
    }

    /**
     * See [EntityRepository.findByIds]. Note that in this implementation the passed in [DataFetchingEnvironment] (if any)
     * is interrogated to see if any joins need to be added to the generated database queries, based on the requested
     * GraphQL fields. Additionally, the [DataFetchingEnvironment.requestContext]'s
     * [RequestContext.dataLoaderPrimerEntityCreationListener] is informed of any created entities.
     */
    override fun findByIds(ids: List<TId>, env: DataFetchingEnvironment?): CompletableFuture<List<TEntity>> {
        return findByIds(
                ids,
                env?.field?.let { graphQLFieldToJoinMapper.getJoinRequests(it, table) },
                env?.requestContext?.dataLoaderPrimerEntityCreationListener)
    }

    /**
     * Returns a [CompletableFuture] which, when completed, will provide a [List] of all the [TEntity] items in the
     * underlying database table.
     *
     * @param joinRequests The joins that should be added to the query to fetch related items, if any are required.
     * @param entityCreationListener The listener to inform whenever an [Entity] is created. This is done by calling it
     * [EntityCreationListener.onEntityCreated] function.
     */
    open fun findAll(joinRequests: List<JoinRequest<out Any, TRecord, out Record>>?,
                     entityCreationListener: EntityCreationListener?)
            : CompletableFuture<List<TEntity>> {

        return withLogging("querying ${table.name} for all records") {
            find(entityCreationListener, joinRequests)
        }
    }

    /**
     * Returns a [CompletableFuture] which, when completed, will provide a [List] of all the [TEntity] items which have
     * the passed in IDs, in the underlying database table.
     *
     * @param ids The IDs of the items to be found.
     * @param joinRequests The joins that should be added to the query to fetch related items, if any are required.
     * @param entityCreationListener The listener to inform whenever an [Entity] is created. This is done by calling it
     * [EntityCreationListener.onEntityCreated] function.
     */
    open fun findByIds(ids: List<TId>,
                       joinRequests: List<JoinRequest<out Any, TRecord, out Record>>?,
                       entityCreationListener: EntityCreationListener?)
            : CompletableFuture<List<TEntity>> {

        return withLogging("querying ${table.name} for records with IDs $ids") {
            find(entityCreationListener, joinRequests, listOf(idField.`in`(ids)))
        }
    }

    /**
     * Creates the entity which this repository provides access to from the passed in [record].
     *
     * @param record A record containing all the data in a single result row from a query generated by this repository
     * when finding the items it's working with.
     */
    abstract fun getEntity(record: Record): TEntity

    /**
     * Finds all the records that match the passed in [conditions].
     *
     * @param entityCreationListener The listener to inform whenever an [Entity] is found. Ignored if null.
     * @param joinRequests Any joins that should be added to the query.
     * @param conditions Any conditions that should be added to the query.
     */
    private fun find(entityCreationListener: EntityCreationListener?,
                     joinRequests: List<JoinRequest<out Any, TRecord, out Record>>?,
                     conditions: List<Condition>? = null)
            : CompletableFuture<List<TEntity>> {

        val select: SelectJoinStep<Record> = create.select().from(table)
        val joinInstances = addDefaultJoins(select).plus(addRequestedJoins(select, joinRequests))

        return select
                .withConditions(conditions)
                .fetchAsync()
                .thenApply {
                    it.map {
                        // If we have an entity creation listener then get all entities and inform it, otherwise just
                        // need the main entity (by calling getEntity).
                        if (entityCreationListener == null)
                            getEntity(it)
                        else {
                            with(getEntitiesFromRow(it, joinInstances)) {
                                entityCreationListener.onEntityCreated(this.primaryEntity)
                                this.joinedEntities.forEach { entityCreationListener.onEntityCreated(it) }
                                this.primaryEntity
                            }
                        }
                    }
                }
                .toCompletableFuture()
    }

    /**
     * Adds any joins which are needed by default by this repository. The joins should be added to the passed in
     * [select], and a corresponding list of [JoinInstance]s should be returned, which describe the joins that were
     * added.
     *
     * The base implementation does nothing, which is the desired behaviour for repositories which work with a single
     * table. However subclasses which work with multiple tables should override this and add the required joins.
     */
    protected open fun addDefaultJoins(select: SelectJoinStep<Record>) =
            listOf<JoinInstance<out Any, TRecord, out Record>>()

    /**
     * Adds the joins described by the passed in [joinRequests] (if any). Adds the joins to the passed in [select], and
     * returns a corresponding list of [JoinInstance]s, which describe the joins that were added.
     */
    private fun addRequestedJoins(select: SelectJoinStep<Record>,
                                  joinRequests: List<JoinRequest<out Any, TRecord, out Record>>?)
            : List<JoinInstance<out Any, TRecord, out Record>> {
        return joinRequests?.map { it.join(select, table) } ?: listOf()
    }

    /**
     * One or more [TEntity] objects that were constructed from the data in a result row from a query
     *
     * @property primaryEntity The main entity that was created from the data in the result row.
     * @property joinedEntities Any entities that were created from the data in the result row, if one or more database
     * joins resulted in more entities being available.
     */
    private data class RowEntities<TEntity : Entity<TId>, TId>(
            val primaryEntity: TEntity, val joinedEntities: List<Entity<out Any>>)

    /**
     * Gets all the entities available from the passed in [wholeRecord].
     *
     * @param wholeRecord A single result row from a query
     * @param joinInstances The [JoinInstance]s that were included in the query, so that the joined data can be checked
     * to see if it resulted in any more entities being available.
     */
    private fun getEntitiesFromRow(wholeRecord: Record,
                                   joinInstances: List<JoinInstance<out Any, TRecord, out Record>>)
            : RowEntities<TEntity, TId> {

        /**
         * Extension method on a list of [JoinResult]s, which gets all entities from a given [primaryRecord], and any
         * subsequent joins in any of the join results.
         */
        fun List<JoinResult<out Any, out Record, out Record>>.getAllEntities(primaryRecord: Record): List<Entity<out Any>> {
            return recordToEntityConverterProvider.recordToConverters.flatMap { converter ->
                // First get the entities created from the initial joins.
                converter.getEntities(primaryRecord, this)
            }.plus(
                    // Now check each join's subsequent joins, recursively.
                    this.flatMap { it.subsequentJoins.getAllEntities(it.foreignRecord) }
            )
        }

        // First get a list of the join results, i.e. the join instances which had data on the foreign side of the join.
        // This list contains the top-level joins (i.e. the first level of joins from this repository's main table to
        // its related tables). Each of those join results might in turn have subsequent joins, to create a nested
        // hierarchy.
        val joinedEntities = joinInstances.letIfAny {
            // There is at least one join instance: all these (top-level) join instances all come from the main table
            // this repository is working with, so get that record: it is the primary record for all these initial joins.
            val primaryRecord = wholeRecord.into(table)

            // Go round the join instances and, for each one, get its corresponding result (if the join resulted in
            // data). Each join result might itself have subsequent join results.
            joinInstances.mapNotNull { joinInstance ->
                // If there is a value in the join's primary field instance create the primary record from it then use it
                // to get the join's result, if any.
                wholeRecord[joinInstance.primaryFieldInstance]?.let { joinInstance.getResult(wholeRecord, primaryRecord) }
            }.getAllEntities(primaryRecord)
        }

        return RowEntities(getEntity(wholeRecord), joinedEntities ?: listOf())
    }
}

/**
 * Updates the receiver and adds to it the passed in [conditions], if any. Returns the same instance that was passed in.
 */
private fun SelectJoinStep<Record>.withConditions(conditions: List<Condition>?): SelectConnectByStep<Record> =
        conditions?.letIfAny { this.where(it.toList()) } ?: this