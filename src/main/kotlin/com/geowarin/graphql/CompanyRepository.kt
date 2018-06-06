package com.geowarin.graphql

import org.springframework.stereotype.Repository

/*
Simulates a class which communicates with a data store (e.g. talks to a database via JOOQ or JPA)
and gets data related to companies.
 */
@Repository
class CompanyRepository {
  fun findAll(): Iterable<CompanyRecord> = companies.asIterable()

  fun findAllById(ids: Iterable<Long>): Iterable<CompanyRecord> = companies.filter { ids.contains(it.id) }
}
