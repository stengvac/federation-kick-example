package com.example.kick.graphql.federation.config

import com.fasterxml.jackson.databind.ObjectMapper
import graphql.TypeResolutionEnvironment
import graphql.schema.GraphQLObjectType
import com.example.kick.graphql.federation.FederationReferenceResolver
import com.example.kick.graphql.federation.ObjectMapperReferenceResolver
import com.example.kick.graphql.federation.ReferenceResolver
import java.util.concurrent.ConcurrentHashMap

internal class FederationResolvingSupport(
    private val objectMapper: ObjectMapper,
    federationResolvers: Set<FederationReferenceResolver<*>>,
) {
    private val cachedFederationResolvers = federationResolvers.map { federationResolver ->
        var referenceResolver = federationResolver.referenceResolver()
        val dataClass = federationResolver.dataClass().java
        if (referenceResolver is ObjectMapperReferenceResolver.UseJacksonReferenceResolver) {
           referenceResolver = ObjectMapperReferenceResolver(
               dataClass = dataClass,
               objectMapper = objectMapper
           )
        }

        CachedResolverData(
            backingClass = federationResolver.dataClass().java,
            referenceResolver = referenceResolver,
            reference = federationResolver.reference()
        )
    }
    //aka mapping reference (GQL type name) to resolver implementing transformation logic
    private val reference2Resolver = cachedFederationResolvers.associateBy { it.reference }
    //mapping between kotlin classes and graphql object types
    private val backingDataClass2GraphqlType = ConcurrentHashMap<Class<*>, GraphQLObjectType>()

    fun resolveGqlTypeForBackingClass(entity: Any, env: TypeResolutionEnvironment): GraphQLObjectType {
        //other way. federation object now has to be mapped back to Gql schema so this code has to resolve type name in schema
        val entityClass = entity::class.java
        return backingDataClass2GraphqlType.getOrPut(entityClass) {
            val resolver = cachedFederationResolvers
                .firstOrNull { it.backingClass.isAssignableFrom(entityClass) }
                ?: throw IllegalArgumentException("Entity with class=${entityClass.simpleName} does not have any graphql type in schema")

            val graphQlTypeName = resolver.reference
            env.schema.getObjectType(graphQlTypeName)
                ?: throw IllegalArgumentException("Entity=${entityClass.simpleName} with gql type=$graphQlTypeName. This gql type not in schema.")
        }
    }

    fun resolveInstanceFromReference(reference: Map<String, Any?>): Any? {
        //it is like this... apollo send typename and some data - eq fields from extended type. and this service has to resolve from data object
        val typeName = reference["__typename"]?.toString() ?: throw IllegalArgumentException("__typeName not provided")
        val cachedResolverData = reference2Resolver[typeName]
            ?: throw IllegalArgumentException("No federation resolver found for __typeName=$typeName")

        return cachedResolverData.referenceResolver.resolve(reference)
    }

    private data class CachedResolverData(
        val backingClass: Class<out Any>,
        val referenceResolver: ReferenceResolver<out Any>,
        val reference: String,
    )
}
