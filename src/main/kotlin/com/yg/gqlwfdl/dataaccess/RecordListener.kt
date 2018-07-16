package com.yg.gqlwfdl.dataaccess

import org.jooq.Record

/**
 * Listener to events related to records from a repository, e.g. listening to whenever a record is found so some action
 * can be taken on it, e.g. caching it in a data loader.
 *
 * TODO: not used currently, but will be when joins implemented.  Confirm when done.
 */
interface RecordListener {
    /**
     * Called whenever a query is run and one or more records are retrieved from that query (there can be multiple
     * records for each row if joins are involved). This will check with each of its query listeners (if any) to see if
     * the types of records which they rely on are all included. If so, it means this listener should be used to take
     * those records and convert them into an entity (of type V) to be cached in this data loader.
     */
    fun onRecordsReceived(records: List<Record>)
}