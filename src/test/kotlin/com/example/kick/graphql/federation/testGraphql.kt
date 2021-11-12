package com.example.kick.graphql.federation

import com.fasterxml.jackson.databind.ObjectMapper
import com.graphql.spring.boot.test.GraphQLResponse
import com.graphql.spring.boot.test.GraphQLTestTemplate
import org.springframework.boot.test.context.TestComponent
import org.springframework.core.io.ResourceLoader
import org.springframework.util.StreamUtils
import java.nio.charset.StandardCharsets

@TestComponent
class TestGraphQL(
    private val graphQLTestTemplate: GraphQLTestTemplate,
    private val resourceLoader: ResourceLoader,
    private val objectMapper: ObjectMapper
) {

    fun execute(
        input: String,
        variables: Map<String, Any> = emptyMap()
    ): GraphQLResponse {
        val query = if (input.endsWith(GRAPHQL)) loadQuery(input) else input
        return graphQLTestTemplate.postMultipart(query, objectMapper.writeValueAsString(variables))
    }

    private fun loadQuery(location: String): String {
        val resource = resourceLoader.getResource("classpath:$location")
        return resource.inputStream.use { inputStream -> StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8) }
    }

    companion object {
        private const val GRAPHQL = ".graphql"
    }
}
