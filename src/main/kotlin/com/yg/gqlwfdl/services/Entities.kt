package com.yg.gqlwfdl.services

data class Customer(val id: Long, var firstName: String, var lastName: String, var companyId: Long, var outOfOfficeDelegate: Long? = null)

data class Company(val id: Long, var name: String, var address: String)
