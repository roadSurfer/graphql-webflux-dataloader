package com.yg.gqlwfdl.dataaccess.joins

import graphql.language.Field
import org.jooq.Record

/**
 * Represents a [JoinDefinition], along with any subsequent joins (from the [JoinDefinition.foreignField] to one or more
 * other tables. This then ends up defining a hierarchy of join definitions, which can be treated as a single set, e.g.
 * when mapping GraphQL fields to database joins.
 *
 * @param joinDefinition The main join definition.
 * @param subsequentJoins Any subsequent joins from the [JoinDefinition.foreignField] to one or more other tables. If
 * omitted, an empty list is used, meaning there are no subsequent joins.
 */
data class NestedJoinDefinition<TPrimaryRecord : Record, TForeignRecord : Record>(
        private val joinDefinition: JoinDefinition<out Any, TPrimaryRecord, TForeignRecord>,
        private val subsequentJoins: List<NestedJoinDefinition<TForeignRecord, out Record>> = listOf()) {

    /**
     * Creates a [JoinRequest] instance based on this [NestedJoinDefinition].
     *
     * @param graphQLField The GraphQL field which caused this set of joins to be created.
     * @param mapper The mapper to use to get the subsequent joins for the passed in [graphQLField]'s children.
     * @param rootForChildGraphQLFields Each GraphQL field can have child fields. If it does, the system will need to
     * know which of the joined tables to use as the starting point for any subsequent joins. This parameter specifies
     * that. The value specified here should either be this [NestedJoinDefinition] itself, or one of its
     * [subsequentJoins][NestedJoinDefinition.subsequentJoins]. This can be null if there are no subsequent joins that
     * need to be processed.
     */
    fun createRequest(graphQLField: Field,
                      mapper: GraphQLFieldToJoinMapper,
                      rootForChildGraphQLFields: NestedJoinDefinition<out Record, out Record>?)
            : JoinRequest<out Any, TPrimaryRecord, out Record> {

        val subsequentJoinRequests = subsequentJoins.map {
            it.createRequest(graphQLField, mapper, rootForChildGraphQLFields)
        }

        return if (this === rootForChildGraphQLFields)
            joinDefinition.createRequest(graphQLField, mapper, subsequentJoinRequests)
        else
            JoinRequest(joinDefinition, subsequentJoinRequests)
    }
}