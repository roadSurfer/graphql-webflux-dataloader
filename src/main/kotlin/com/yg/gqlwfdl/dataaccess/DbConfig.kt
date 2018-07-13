package com.yg.gqlwfdl.dataaccess

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService

/**
 * Configuration object providing information related to working with the database (e.g. the URL to access it).
 *
 * @property url The URL used to connect to the database. Read from the application.properties file from the
 * spring.datasource.url setting.
 * @property connectionPoolSize The size of the connection pool to the database, i.e. the maximum number of
 * database connections that can be open at any point time. Used by the [asyncExecutor] property to define how many
 * threads to allow in the thread pool.
 */
@Configuration
class DbConfig(@param:Value("\${spring.datasource.url}") val url: String,
               @param:Value("\${jdbc.max-pool-size}") val connectionPoolSize: Int) {

    /**
     * An [ExecutorService] used to run database queries asynchronously by delegating them to a thread from a dedicated
     * thread pool. The size of the pool is defined by the [connectionPoolSize] setting. If there can only ever be X
     * threads executing database queries then there can only ever be X database connections open.
     *
     * TODO: research the above. There seem to be some articles that suggest that the above thinking is over-simplistic
     * and not quite right. Maybe the database connection pool size and the thread pool size shouldn't be exactly the
     * same.
     */
    @Bean
    fun asyncExecutor() = Executors.newFixedThreadPool(connectionPoolSize)
}