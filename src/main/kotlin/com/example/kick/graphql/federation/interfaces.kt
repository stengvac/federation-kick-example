package com.example.kick.graphql.federation

import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.reflect.KClass

/**
 * Interface for Federation type resolving.
 */
interface FederationReferenceResolver<T : Any> {

    /**
     * Reference - type name in schema, which extend type in other graph.
     * Default implementation return simple name of dataClass - for class Account return string Account.
     * Override this function if data class name differ from name in schema
     */
    fun reference(): String = dataClass().simpleName!!

    /**
     * Backing data class for graphql type in schema (matching reference) - used for matching gql type and its data class
     */
    @Suppress("UNCHECKED_CAST")
    fun dataClass(): KClass<T> {
        return this::class.supertypes[0].arguments[0].type!!.classifier as KClass<T>
    }

    /**
     * From reference (Map<String, Any?>) build instance with provided data - external field from extended type.
     *
     * Default [ObjectMapperReferenceResolver] is used. Override if there is more complex logic how to create instance.
     */
    fun referenceResolver(): ReferenceResolver<T> = ObjectMapperReferenceResolver.useObjectMapper()
}

fun interface ReferenceResolver<T : Any> {

    /**
     * Graphql data to instance resolver
     */
   fun resolve(reference: Map<String, Any?>): T?
}

/**
 * Object mapper reference resolver.
 */
class ObjectMapperReferenceResolver<T : Any>(
    private val dataClass: Class<T>,
    private val objectMapper: ObjectMapper
) : ReferenceResolver<T> {

    override fun resolve(reference: Map<String, Any?>): T {
        return objectMapper.convertValue(reference, dataClass)
    }

    internal object UseJacksonReferenceResolver : ReferenceResolver<Any> {
        override fun resolve(reference: Map<String, Any?>): Any? {
            TODO("Marker - use ObjectMapperReferenceResolver instance")
        }
    }

    companion object {
        /**
         * Marker - [ObjectMapperReferenceResolver] will be used - once object mapper instance is available at runtime
         */
        fun <T : Any> useObjectMapper(): ReferenceResolver<T> {
            @Suppress("UNCHECKED_CAST")
            return UseJacksonReferenceResolver as ReferenceResolver<T>
        }
    }
}