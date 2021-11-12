package com.example.kick.graphql.federation.app

import graphql.kickstart.tools.GraphQLQueryResolver
import graphql.kickstart.tools.GraphQLResolver
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.stereotype.Component
import com.example.kick.graphql.federation.FederationReferenceResolver
import com.example.kick.graphql.federation.config.EnableGraphQlFederation

@EnableGraphQlFederation
@SpringBootApplication
class TestApplication

@Component
class QueryResolver : GraphQLQueryResolver {

    fun account(iban: String) = Account(
        iban = iban,
        userId = returnedUserId
    )

    companion object {
        const val returnedUserId = "userId"
    }
}

@Component
class AccountResolver : GraphQLResolver<Account>, FederationReferenceResolver<Account> {

    fun balance(context: Account) = returnedBalance

    companion object {
        const val returnedBalance = "5.7"
    }
}

data class Account(
    val iban: String,
    val userId: String
)