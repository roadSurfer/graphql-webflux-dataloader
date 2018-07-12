package com.yg.gqlwfdl

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture

fun logMessage(message: String, includeThreadName: Boolean = true) {
    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY-MM-DD HH:mm:ss.SSS"))
    val threadInfo = if (includeThreadName) " [${Thread.currentThread().name}]" else ""
    println("$timestamp$threadInfo :: $message")
}

fun <T> withLogging(taskDescription: String, handler: () -> CompletableFuture<T>): CompletableFuture<T> {
    logMessage("${taskDescription.capitalize()}...")
    return handler().whenComplete { result, error ->
        logMessage(error?.let { "Error $taskDescription: $it" } ?: "Success $taskDescription. Result: $result")
    }
}