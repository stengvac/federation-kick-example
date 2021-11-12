package com.example.kick.graphql.federation.app

import graphql.kickstart.tools.GraphQLQueryResolver
import graphql.kickstart.tools.GraphQLResolver
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.stereotype.Component
import com.example.kick.graphql.federation.FederationReferenceResolver
import com.example.kick.graphql.federation.config.EnableGraphQlFederation
import graphql.scalars.ExtendedScalars
import graphql.schema.GraphQLScalarType
import org.springframework.context.annotation.Bean
import java.math.BigDecimal

@EnableGraphQlFederation
@SpringBootApplication
class TestApplication {

    @Bean
    fun bigDecimalScalar(): GraphQLScalarType = ExtendedScalars.GraphQLBigDecimal
}

@Component
class QueryResolver : GraphQLQueryResolver {

    fun account(iban: String) = Account(
        iban = iban,
        currency = returnedCurrency
    )

    companion object {
        const val returnedCurrency = "EUR"
    }
}

@Component
class AccountResolver : GraphQLResolver<Account>, FederationReferenceResolver<Account> {
    //extending Account with balance field - hopefully I am rich
    fun balance(context: Account): BigDecimal = returnedBalance

    companion object {
        val returnedBalance: BigDecimal = BigDecimal.TEN
    }
}

data class Account(
    val iban: String,
    val currency: String
)