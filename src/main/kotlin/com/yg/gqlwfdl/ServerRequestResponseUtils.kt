package com.yg.gqlwfdl

import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono

fun ServerRequest.contentTypeIs(mediaType: MediaType) =
        this.headers().contentType().filter { it.isCompatibleWith(mediaType) }.isPresent

fun <T> ServerRequest.withBody(mapFun: (String) -> T): Mono<T> =
        this.bodyToMono<String>().flatMap { Mono.just(mapFun(it)) }

fun serveStatic(resource: Resource): (ServerRequest) -> Mono<ServerResponse> =
        { ServerResponse.ok().body(BodyInserters.fromResource(resource)) }