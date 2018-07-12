package com.yg.gqlwfdl.dataaccess

import com.yg.gqlwfdl.logMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.scheduler.Schedulers
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

@Configuration
class DbConfig(@param:Value("\${spring.datasource.url}") val url: String,
               @param:Value("\${jdbc.max-pool-size}") val connectionPoolSize: Int) {

    @Bean
    fun asyncScheduler() = Schedulers.fromExecutor(asyncExecutor())

    @Bean
    fun asyncExecutor() = Executors.newFixedThreadPool(connectionPoolSize, AsyncDbThreadFactory())

    private class AsyncDbThreadFactory : ThreadFactory {
        private val group: ThreadGroup
        private val threadNumber = AtomicInteger(1)
        private val namePrefix: String

        init {
            val s = System.getSecurityManager()
            group = if (s == null) Thread.currentThread().threadGroup else s.threadGroup
            namePrefix = "pool-${poolNumber.getAndIncrement()}-thread-"
        }

        override fun newThread(r: Runnable): Thread {
            val t = AsyncDbThread(group, Runnable {
                try {
                    logMessage("Executing database call on thread from async DB thread pool...")
                    r.run()
                    logMessage("Finished executing database call on thread from async DB thread pool")
                } catch (t: Throwable) {
                    logMessage("Error executing database call on thread from async DB thread pool: $t")
                    throw t
                }
            }, namePrefix + threadNumber.getAndIncrement(), 0)
            if (t.isDaemon)
                t.isDaemon = false
            if (t.priority != Thread.NORM_PRIORITY)
                t.priority = Thread.NORM_PRIORITY
            return t
        }

        companion object {
            private val poolNumber = AtomicInteger(1)
        }
    }

    private class AsyncDbThread(group: ThreadGroup, target: Runnable, name: String, stackSize: Long)
        : Thread(group, target, name, stackSize) {

        override fun run() {
            try {
                logMessage("Running thread from async DB thread pool...")
                super.run()
                logMessage("Finished running thread from async DB thread pool")
            } catch (t: Throwable) {
                logMessage("Error running thread from async DB thread pool: $t")
                throw t
            }
        }
    }
}