package com.geowarin.graphql

data class UserRecord(val id: Long, val firstName: String, val lastName: String, val companyId: Long)

data class CompanyRecord(val id: Long, val name: String, val address: String)

val companies = listOf(CompanyRecord(1, "Company-A", "Address-A"),
  CompanyRecord(2, "Company-B", "Address-B"))

val users = listOf(UserRecord(1, "Albert", "Albertson", 1),
  UserRecord(2, "Andrew", "Anderson", 1),
  UserRecord(3, "Bob", "Bobson", 2),
  UserRecord(4, "Bill", "Billson", 2))
