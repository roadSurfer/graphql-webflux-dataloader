package com.yg.gqlwfdl.dataaccess

import org.jooq.Record

/**
 * Listener to events related to records from a repository, e.g. listening to whenever a record is found so some action
 * can be taken on it, e.g. caching it in a data loader.
 */
interface RecordListener {
    /**
     * Called whenever a query is run and one or more records are retrieved from that query (there can be multiple
     * records for each row if joins are involved). Typically implementors will do something with the received records
     * such as cache them (or cache data based on them).
     */
    fun onRecordsReceived(records: List<Record>)
}