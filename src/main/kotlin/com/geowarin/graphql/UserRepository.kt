package com.geowarin.graphql

import org.springframework.stereotype.Repository

/*
Simulates a class which communicates with a data store (e.g. talks to a database via JOOQ or JPA)
and gets data related to users.
 */
@Repository
class UserRepository {
  fun findAll(): Iterable<UserRecord> = users

  fun findAllWithCompanyInfo(): Iterable<Pair<UserRecord, CompanyRecord>> =
    users.map { Pair(it, companies.first { c -> c.id == it.companyId }) }
}
