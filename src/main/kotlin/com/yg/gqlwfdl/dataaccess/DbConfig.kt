package com.yg.gqlwfdl.dataaccess

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.scheduler.Schedulers
import java.util.concurrent.Executors

@Configuration
class DbConfig(@param:Value("\${spring.datasource.url}") val url: String,
               @param:Value("\${jdbc.max-pool-size}") val connectionPoolSize: Int) {

    @Bean
    fun asyncScheduler() = Schedulers.fromExecutor(asyncExecutor())

    @Bean
    fun asyncExecutor() = Executors.newFixedThreadPool(connectionPoolSize)
}