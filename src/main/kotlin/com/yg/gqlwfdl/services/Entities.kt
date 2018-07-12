package com.yg.gqlwfdl.services

data class Customer(val id: Long, val firstName: String, val lastName: String, val companyId: Long)

data class Company(val id: Long, val name: String, val address: String)
