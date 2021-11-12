package com.example.kick.graphql.federation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.context.support.AbstractApplicationContext
import com.example.kick.graphql.federation.app.AccountResolver
import com.example.kick.graphql.federation.app.QueryResolver
import com.example.kick.graphql.federation.app.TestApplication
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal

@ActiveProfiles("test")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [TestApplication::class]
)
@Import(TestGraphQL::class)
class FederationCT(
    context: AbstractApplicationContext
) {

    private val testGraphQL: TestGraphQL = context.getBean(TestGraphQL::class.java)

    @Test
    fun `normal query Account with balance works`() {
        val iban = "I BAN YOU"
        val accountWithBalance = testGraphQL.execute(
            input = "/gql/account-balance.graphql",
            variables = mapOf("iban" to iban)
        ).assertThatNoErrorsArePresent()
            .get("$.data.account", AccountWithBalance::class.java)

        assertThat(accountWithBalance.balance).isEqualByComparingTo(AccountResolver.returnedBalance)
        assertThat(accountWithBalance.currency).isEqualTo(QueryResolver.returnedCurrency)
        assertThat(accountWithBalance.iban).isEqualTo(iban)
    }

    @Test
    fun `federation _entities query is resolved correctly`() {
        //those 2 values are normally send from other service - extended type
        val iban = "externalIban"
        val currency = "externalCurrencyValue"
        //variables does not work with _entities call it seems
        val accountsWithBalance = testGraphQL.execute(
            input = "/gql/query-entities.graphql",
        ).assertThatNoErrorsArePresent()
            .getList("$.data._entities", AccountWithBalance::class.java)

        assertThat(accountsWithBalance).hasSize(1)
        val accountWithBalance = accountsWithBalance.first()

        assertThat(accountWithBalance.balance).isEqualByComparingTo(AccountResolver.returnedBalance)
        assertThat(accountWithBalance.currency).isEqualTo(currency)
        assertThat(accountWithBalance.iban).isEqualTo(iban)
    }
}

data class AccountWithBalance(
    val iban: String,
    val currency: String,
    val balance: BigDecimal
)

