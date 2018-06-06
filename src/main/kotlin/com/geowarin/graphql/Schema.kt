package com.geowarin.graphql

import com.coxautodev.graphql.tools.SchemaParser
import com.coxautodev.graphql.tools.SchemaParserOptions
import graphql.schema.GraphQLSchema
import reactor.core.publisher.Mono

fun buildSchema(userRepository: UserRepository, companyRepository: CompanyRepository): GraphQLSchema {

  return SchemaParser.newParser()
    .file("schema.graphqls")
    .resolvers(
      Query(userRepository, companyRepository),
      UserResolver(companyRepository))
    .options(SchemaParserOptions.newOptions()
      .genericWrappers(SchemaParserOptions.GenericWrapper(Mono::class.java, 0))
      .build())
    .build()
    .makeExecutableSchema()
}
