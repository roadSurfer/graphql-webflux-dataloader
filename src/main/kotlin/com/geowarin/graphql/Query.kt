package com.geowarin.graphql

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import graphql.language.Field
import graphql.schema.DataFetchingEnvironment
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.concurrent.CompletableFuture

class Query(private val userRepository: UserRepository, private val companyRepository: CompanyRepository) : GraphQLQueryResolver {

  fun users(env: DataFetchingEnvironment): CompletableFuture<Iterable<UserRecord>> {
    // Simulate fetching data reactively by scheduling the work to run on a different thread
    return Mono.fromCallable {
      if (env.field.selectionSet.selections.any { it is Field && it.name == "company" }) {
        // GraphQL request includes company information, so fetch user and company information in one
        // go from the database. (If joins aren't possible, just do the "else", and UserResolver.company
        // will be called, which will end up using the data loader to make one single call to the DB
        // to get the companies for all the returned users).
        val usersAndCompanies = userRepository.findAllWithCompanyInfo()
        val companyDataLoader = env.dataLoader<Long, CompanyRecord>("Company")
        usersAndCompanies.map { it.second }.distinct().forEach({ companyDataLoader.prime(it.id, it) })
        usersAndCompanies.map { it.first }
      } else
        userRepository.findAll()
    }.subscribeOn(Schedulers.elastic()).toFuture()
  }

  fun companies(): CompletableFuture<Iterable<CompanyRecord>> {
    // Simulate fetching data reactively by scheduling the work to run on a different thread
    return Mono
      .fromCallable { companyRepository.findAll() }
      .subscribeOn(Schedulers.elastic()).toFuture()
  }
}
