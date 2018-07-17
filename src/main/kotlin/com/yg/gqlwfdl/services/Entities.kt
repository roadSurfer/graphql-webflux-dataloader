package com.yg.gqlwfdl.services

/*
This file contains the main domain model objects, used in the services layer. In a simple example such as this, each
of these correlate with a single record in a particular database table (e.g. the "customer" table). However in more
complex situations such a one-to-one mapping wouldn't necessarily exist. Hence having a separate object from the
JOOQ-generated Record objects, or a JOOQ-generated DAO or POJO. Also see discussion here for more info:
https://github.com/jOOQ/jOOQ/issues/5984

Note that it's for a similar reason to this that we don't create a base Service class with any assumptions about how
each individual service would operate (for example assuming that each service wraps one individual repository). In the
data access layer it makes sense to have a base Repository class and put assumptions in there about general behaviour
(e.g. that each repository generally works with one individual database table). However services can be more complex
and can gather and aggregate data from multiple repository, so no such assumptions are made.
 */

data class Customer(val id: Long,
                    var firstName: String,
                    var lastName: String,
                    var companyId: Long,
                    var outOfOfficeDelegate: Long? = null)

data class Company(val id: Long,
                   var name: String,
                   var address: String,
                   var primaryContact: Long? = null)
