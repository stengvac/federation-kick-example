# Graphql federation with kickstart
See basic concepts in [Apollo doc](https://www.apollographql.com/docs/federation/).

## How to for back-end services:
One service declare GQL type, which other services may want to extend (add its fields onto type). How to do it on back-end side:

Service `account-service` provide type `Account` in its schema see example bellow.

```graphql
type Query {
    userAccount(userId: String!): Account!
}

type Account @key(fields: "iban") {
    iban: String!
    currency: String!
}
```
So frond end could execute query like:

```graphql
query GetUserAccount($userId: String!) {
    userAccount(userId: $userId) {
        iban
        currency
    }
}
```

With federation another back-end service can provide additional fields for `Account` type and provide richer API for frond end.
To do this GQL offer key word `extend` or alternatively for libs, which does not support federation syntax yet directive
`@extends` placed on type definition.

Extended types has to provide `@key` directive - at least one field but can provide more or composite see [doc](https://www.apollographql.com/docs/federation/entities/).
Key is used to identify `entity`. Entity is object, which is declared in subschema and can be referenced via keys
in other subschemas - basically allow to resolve entity instance with key and then add their own fields via extension mechanism. 

Example `Account` type extension by `balance-service`.
Service `balance-service` can extend type `Account` and add new fields in this case field `balance`. 

```graphql
scalar BigDecimal

type Account @key(fields: "iban") @extends {
    iban: String! @external
    currency: String!
    # lets say, that balance is returned in currency provided by account - so currency is required    
    balance: BigDecimal! @requires(fields: "currency")
}
```
Notable points in example:

In example is used `@extends` directive instead of `extend` key word. Kickstart has problem with `extend` and Apollo support both ways - behaves same.
Fields with directive `@external` - those fields are declared on extended type. So it is not redeclaration, but info for
Apollo, that we expect those fields as incoming data required for resolving our added fields.
Field `balance` is only newly declared field here. Directive `requires` instrument Apollo federation, that 
`currency` from extended type has to be send with data. Field `iban` is key, thus provided automatically.

So `balance-service` can now enrich super schema. Frond end can perform calls like

```graphql
# user id is usually taken from JWT :)
query GetUserAccount($userId: String!) {
    userAccount(userId: $userId) {
        iban
        currency
        balance
    }
}
```
after all provided sub schemas in this case schemas from `account-service` and `balance-service` schemas
are composed together using federation supporting lib like Apollo Federation.

## Implementing Federation on BE
Enable federation for Spring context - place following annotation on some config class.

```kotlin
@EnableGraphQlFederation
@Configuration
class SomeConfigClass
```

Add implementation for extended types.

```kotlin

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
    val currency: UUID
)
```

In example `GraphQLResolver<Account>` allow to add field `balance` on GQL type `Account`.
Interface `FederationReferenceResolver<Account>` handle resolution of federation related operations.
On interface are present 3 functions, which has default values. Those may need some overrides so pleas read doc on this interface. 

Behind the scenes.
With Federation approach may happen, that some types in schema are unreachable until whole federation schema is created.
Kickstart provide `SchemaParserDictionary` class which allow to add types into schema manually which may be needed for some types.
Federation lib require use of `SchemaParserDictionaryCustomizer` which provide mechanism for those overriding. So use this please, because injection
`SchemaParserDictionary` and its adjust may be performed after schema is created and app won`t start. 