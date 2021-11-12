package com.example.kick.graphql.federation.config

import com.apollographql.federation.graphqljava.Federation
import com.apollographql.federation.graphqljava._Entity
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.kickstart.tools.SchemaParser
import graphql.kickstart.tools.SchemaParserDictionary
import graphql.kickstart.tools.SchemaParserOptions
import graphql.schema.GraphQLSchema
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import com.example.kick.graphql.federation.FederationReferenceResolver

@Import(FederationConfig::class, SchemaParserDictionaryConfig::class)
annotation class EnableGraphQlFederation

/**
 * Services may need to add its own types to dictionary so let em declare via customizer
 * Customization via SchemaParserDictionary may be to late, because schema may be already created
 */
fun interface SchemaParserDictionaryCustomizer {
    fun customize(dictionary: SchemaParserDictionary)
}

@Configuration
class SchemaParserDictionaryConfig {

    @Bean
    fun schemaParserDictionary(): SchemaParserDictionary {
        return SchemaParserDictionary()
    }
}

@Configuration
class FederationConfig(
    @Autowired(required = false)
    federationFetchers: Set<FederationReferenceResolver<*>>?,
    @Autowired(required = false)
    dictionaryCustomizer: SchemaParserDictionaryCustomizer?,
    builder: SchemaParserOptions.Builder,
    schemaParserDictionary: SchemaParserDictionary,
    objectMapper: ObjectMapper
) {
    private val federationResolverSupport = FederationResolvingSupport(objectMapper, federationFetchers.orEmpty())

    init {
        //federation types are unused in schema, because not connected to root Query/Mutation - which is done in other service
        builder.includeUnusedTypes(true)
        federationFetchers.orEmpty().forEach { resolver ->
            schemaParserDictionary.add(resolver.reference(), resolver.dataClass())
        }
        dictionaryCustomizer?.customize(schemaParserDictionary)
    }

    @Bean
    fun graphQLSchema(schemaParser: SchemaParser): GraphQLSchema {
        return Federation.transform(schemaParser.makeExecutableSchema())
            .fetchEntities { environment ->
                environment.getArgument<List<Map<String, Any?>>>(_Entity.argumentName)
                    .map { reference: Map<String, Any?> ->
                        federationResolverSupport.resolveInstanceFromReference(reference)
                    }
            }
            .resolveEntityType { env ->
                federationResolverSupport.resolveGqlTypeForBackingClass(env.getObject(), env)
            }
            .build()
    }
}

