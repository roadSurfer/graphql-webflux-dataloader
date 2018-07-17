package com.yg.gqlwfdl.services

import com.yg.gqlwfdl.RequestContext
import com.yg.gqlwfdl.dataaccess.RecordListener
import com.yg.gqlwfdl.dataloaders.DataLoaderType
import com.yg.gqlwfdl.yg.db.public_.tables.records.CompanyRecord
import com.yg.gqlwfdl.yg.db.public_.tables.records.CustomerRecord
import org.jooq.Record

// TODO: one possible problem with the design... this class hears when any record is returned from the database, and if
// it's one that's of interest, it will convert it to an entity (a domain model object) and cache it. Once all the
// caching is complete, the repository will return the primary records to the service, and the service will then
// convert those records to its entity (e.g. CustomerRecord to Customer). What that means is that the conversion from
// record to entity is done twice, so two different instances exist. As they are data classes they have the same hash
// code etc., so maybe it's not too big a problem, but it's still not ideal that we duplicate the effort and have two
// instances kicking about.

/**
 * A listener which hears when records are queried from the database, converts those records to their related entities,
 * and caches those entities in the relevant data loaders.
 */
class DataLoaderPrimerRecordListener(requestContext: RequestContext) : RecordListener {

    /**
     * Responsible for caching entities in a data loader.
     *
     * @param TId The type of value which defines the unique ID of the entity, against which it is cached in the data
     * loader.
     * @param TEntity The type of the entity (domain model object) which this object is responsible for caching.
     * @property requestContext The context of the current request (i.e. a GraphQL request), from which the data loaders
     * can be retrieved.
     * @property dataLoaderType The type of the data loader to get from the [requestContext].
     */
    private abstract class EntityCacher<TId, TEntity>(
            private val requestContext: RequestContext,
            private val dataLoaderType: DataLoaderType) : RecordListener {

        override fun onRecordsReceived(records: List<Record>) {
            getEntitiesToCache(records).map{ requestContext.dataLoader<TId, TEntity>(dataLoaderType).prime(it) }
        }

        /**
         * Gets a list of the entities to cache from the received list of records. An empty list is returned if there
         * is nothing to cache.
         */
        abstract fun getEntitiesToCache(records: List<Record>): List<TEntity>
    }

    // TODO: implement if/when decide we need to support joins handling domain model objects based on multiple underlying tables.
//    private class MultiTypeRecordToCacheEntityProvider<TId, TEntity>(
//            requestContext: RequestContext,
//            dataLoaderType: DataLoaderType,
//            private val requiredTypes: List<Class<out Record>>,
//            private val converter: (Map<out Class<out Record>, out Record>) -> TEntity)
//        : EntityCacher<TId, TEntity>(requestContext, dataLoaderType) {
//
//        override fun getEntityToCache(records: List<Record>): TEntity? {
//            val recordMap = records.associateBy { it.javaClass }.filterKeys { requiredTypes.contains(it) }
//            return if (requiredTypes.size == recordMap.size) converter(recordMap) else null
//        }
//    }

    /**
     * A concrete implementation of [EntityCacher] for the case where the entity is based on a single record type.
     *
     * @param TId The type of value which defines the unique ID of the entity, against which it is cached in the data
     * loader.
     * @param TEntity The type of the entity (domain model object) which this object is responsible for caching.
     * @param TRecord The type of the record on which the cached entity is based.
     * @property recordType The [Class] of the [TRecord] type, required because of type erasure, so that at runtime
     * the system can check whether the received records are of the required type.
     * @property converter A function which converts the [TRecord] object to the [TEntity] object.
     */
    private class SingleTypeRecordToCacheEntityProvider<TId, TEntity, TRecord : Record>(
            requestContext: RequestContext,
            dataLoaderType: DataLoaderType,
            private val recordType: Class<TRecord>,
            private val converter: (TRecord) -> TEntity)
        : EntityCacher<TId, TEntity>(requestContext, dataLoaderType) {

        override fun getEntitiesToCache(records: List<Record>): List<TEntity> =
            records.filter { recordType.isInstance(it) }.map {
                // Can safely cast below because of "isInstance" check above.
                @Suppress("UNCHECKED_CAST")
                converter(it as TRecord)
            }
    }

    /**
     * A list of the cachers which this object knows about.
     */
    private val cachers: List<EntityCacher<out Any, out Any>> = listOf(
            SingleTypeRecordToCacheEntityProvider<Long, Customer, CustomerRecord>(
                    requestContext, DataLoaderType.CUSTOMER, CustomerRecord::class.java) { it.toEntity() },
            SingleTypeRecordToCacheEntityProvider<Long, Company, CompanyRecord>(
                    requestContext, DataLoaderType.COMPANY, CompanyRecord::class.java) { it.toEntity() }
    )

    override fun onRecordsReceived(records: List<Record>) {
       cachers.forEach { it.onRecordsReceived(records) }
    }
}