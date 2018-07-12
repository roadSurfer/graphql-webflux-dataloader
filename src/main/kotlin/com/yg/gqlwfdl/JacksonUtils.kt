package com.yg.gqlwfdl

import com.fasterxml.jackson.databind.type.MapType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.*

val MapTypeRef: MapType =
        TypeFactory.defaultInstance().constructMapType(HashMap::class.java, String::class.java, Any::class.java)

inline fun <reified T> readJson(value: String): T = jacksonObjectMapper().readValue(value, T::class.java)

fun readJsonMap(variables: String?): Map<String, Any>? = jacksonObjectMapper().readValue(variables, MapTypeRef)
