package com.geowarin.graphql

import com.coxautodev.graphql.tools.GraphQLResolver
import graphql.schema.DataFetchingEnvironment
import java.util.concurrent.CompletableFuture

class UserResolver(private val companyRepository: CompanyRepository) : GraphQLResolver<UserRecord> {

  fun company(user: UserRecord, env: DataFetchingEnvironment): CompletableFuture<CompanyRecord> =
    env.dataLoader<Long, CompanyRecord>("Company").load(user.companyId)
}
