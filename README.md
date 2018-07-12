# GraphQL, Spring WebFlux, DataLoader

This is a sample GraphQL application written in kotlin that
uses [graphql-java](https://github.com/graphql-java/graphql-java) and spring webflux (with spring-boot 2),
additionally using [java-dataloader](https://github.com/graphql-java/java-dataloader). This is forked from
https://github.com/geowarin/graphql-webflux, to show how support for DataLoader can be added.

When browsing the application on `localhost:8080`, you will see the [GraphiQL](https://github.com/graphql/graphiql) explorer.

## Current Status
* DataLoader added.
* Example data structure created (customers, companies)
* H2 data access implemented

## To Do
* When populating data from GraphQL resolvers, prime data into data loaders to avoid redundant re-fetching in subsequent resolvers.
* Add functionality to automatically add joins to relevant tables when querying the database, based on requested GraphQL fields.
* Document explaining how it all hangs together still to be added.
* Code comments.
* Unit tests.

