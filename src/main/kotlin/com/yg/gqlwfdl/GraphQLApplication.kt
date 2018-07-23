package com.yg.gqlwfdl

import com.yg.gqlwfdl.dataaccess.DBConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * The main Spring Boot application. Populates the database with test data on startup.
 */
@SpringBootApplication
class GraphQLApplication(dbConfig: DBConfig) {
    init {
        DataSetup(dbConfig.url).execute()
    }
}

/**
 * Main entry point.
 */
fun main(args: Array<String>) {
    runApplication<GraphQLApplication>(*args)
}