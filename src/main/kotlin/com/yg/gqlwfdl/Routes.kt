package com.yg.gqlwfdl

import com.coxautodev.graphql.tools.SchemaParser
import com.coxautodev.graphql.tools.SchemaParserOptions
import com.yg.gqlwfdl.dataaccess.DbConfig
import com.yg.gqlwfdl.dataloaders.CompanyDataLoader
import com.yg.gqlwfdl.dataloaders.DataLoaderFetchContext
import com.yg.gqlwfdl.resolvers.CustomerResolver
import com.yg.gqlwfdl.resolvers.Query
import com.yg.gqlwfdl.services.CompanyService
import com.yg.gqlwfdl.services.CustomerService
import graphql.ExecutionInput.newExecutionInput
import graphql.ExecutionResult
import graphql.GraphQL
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation
import graphql.schema.GraphQLSchema
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

private val GraphQLMediaType = MediaType.parseMediaType("application/GraphQL")

@Configuration
class Routes(private val customerService: CustomerService,
             private val companyService: CompanyService,
             private val dbConfig: DbConfig) {

    private val schema = buildSchema(customerService, companyService)

    @Bean
    fun router() = router {
        GET("/", serveStatic(ClassPathResource("/graphiql.html")))
        (POST("/graphql") or GET("/graphql")).invoke { req: ServerRequest ->
            getGraphQLParameters(req)
                    .flatMap { executeGraphQLQuery(it) }
                    .flatMap { ok().syncBody(it) }
                    .switchIfEmpty(badRequest().build())
        }
    }

    private fun executeGraphQLQuery(graphQLParameters: GraphQLParameters): Mono<ExecutionResult> {
        val registry = DataLoaderRegistry()
        val requestContext = RequestContext(registry, dbConfig)
        registry.register("Company", CompanyDataLoader(requestContext, DataLoaderFetchContext(), companyService))

        val executionInput = newExecutionInput()
                .query(graphQLParameters.query)
                .operationName(graphQLParameters.operationName)
                .variables(graphQLParameters.variables)
                .context(requestContext)

        val graphQL = GraphQL
                .newGraphQL(schema)
                .instrumentation(DataLoaderDispatcherInstrumentation(registry))
                .build()

        return fromFuture(graphQL.executeAsync(executionInput))
    }
}

private fun getGraphQLParameters(req: ServerRequest): Mono<GraphQLParameters> = when {
    req.queryParam("query").isPresent -> graphQLParametersFromRequestParameters(req)
    req.method() == HttpMethod.POST -> parsePostRequest(req)
    else -> empty()
}

private fun parsePostRequest(req: ServerRequest) = when {
    req.contentTypeIs(GraphQLMediaType) -> req.withBody { GraphQLParameters(query = it) }
    else -> req.withBody { readJson<GraphQLParameters>(it) }
}

private fun graphQLParametersFromRequestParameters(req: ServerRequest) = just(
        GraphQLParameters(
                query = req.queryParam("query").get(),
                operationName = req.queryParam("operationName").orElseGet { null },
                variables = getVariables(req)
        )
)

private fun getVariables(req: ServerRequest): Map<String, Any>? {
    return req.queryParam("variables")
            .map { URLDecoder.decode(it, "UTF-8") }
            .map { readJsonMap(it) }
            .orElseGet { null }
}

private fun buildSchema(customerService: CustomerService, companyService: CompanyService): GraphQLSchema {

    return SchemaParser.newParser()
            .file("schema.graphqls")
            .resolvers(
                    Query(customerService, companyService),
                    CustomerResolver())
            .options(SchemaParserOptions.newOptions()
                    .genericWrappers(SchemaParserOptions.GenericWrapper(Mono::class.java, 0))
                    .build())
            .build()
            .makeExecutableSchema()
}

private data class GraphQLParameters(
        val query: String,
        val operationName: String? = null,
        val variables: Map<String, Any>? = null
)
