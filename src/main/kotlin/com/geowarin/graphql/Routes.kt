package com.geowarin.graphql

import graphql.ExecutionInput.newExecutionInput
import graphql.ExecutionResult
import graphql.GraphQL
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation
import org.dataloader.DataLoaderRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.*
import java.net.URLDecoder

val GraphQLMediaType = MediaType.parseMediaType("application/GraphQL")

@Configuration
class Routes(private val userRepository: UserRepository, private val companyRepository: CompanyRepository) {

  private val schema = buildSchema(userRepository, companyRepository)

  @Bean
  fun routesFun() = router {
    GET("/", serveStatic(ClassPathResource("/graphiql.html")))
    (POST("/graphql") or GET("/graphql")).invoke { req: ServerRequest ->
      getGraphQLParameters(req)
        .flatMap { executeGraphQLQuery(it) }
        .flatMap { ok().syncBody(it) }
        .switchIfEmpty(badRequest().build())
    }
  }

  fun executeGraphQLQuery(graphQLParameters: GraphQLParameters): Mono<ExecutionResult> {
    val companyDataLoader = CompanyDataLoader(companyRepository)
    val registry = DataLoaderRegistry()
    registry.register("Company", companyDataLoader)

    val executionInput = newExecutionInput()
      .query(graphQLParameters.query)
      .operationName(graphQLParameters.operationName)
      .variables(graphQLParameters.variables)
      .context(registry)

    val graphQL = GraphQL
      .newGraphQL(schema)
      .instrumentation(DataLoaderDispatcherInstrumentation(registry))
      .build()

    return fromFuture(graphQL.executeAsync(executionInput))
  }
}

fun getGraphQLParameters(req: ServerRequest): Mono<GraphQLParameters> = when {
  req.queryParam("query").isPresent -> graphQLParametersFromRequestParameters(req)
  req.method() == HttpMethod.POST -> parsePostRequest(req)
  else -> empty()
}

fun parsePostRequest(req: ServerRequest) = when {
  req.contentTypeIs(GraphQLMediaType) -> req.withBody { GraphQLParameters(query = it) }
  else -> req.withBody { readJson<GraphQLParameters>(it) }
}

fun graphQLParametersFromRequestParameters(req: ServerRequest) =
  just(
    GraphQLParameters(
      query = req.queryParam("query").get(),
      operationName = req.queryParam("operationName").orElseGet { null },
      variables = getVariables(req)
    )
  )

fun getVariables(req: ServerRequest): Map<String, Any>? {
  return req.queryParam("variables")
    .map { URLDecoder.decode(it, "UTF-8") }
    .map { readJsonMap(it) }
    .orElseGet { null }
}

data class GraphQLParameters(
  val query: String,
  val operationName: String? = null,
  val variables: Map<String, Any>? = null
)
