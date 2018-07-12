package com.yg.gqlwfdl

import com.yg.gqlwfdl.dataaccess.DbConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GraphQLApplication(dbConfig: DbConfig) {
    init {
        DataSetup(dbConfig.url).execute()
    }
}

fun main(args: Array<String>) {
    runApplication<GraphQLApplication>(*args)
}