# GraphQL and Spring Webflux

This is a sample GraphQL application written in kotlin that
uses [graphQL-java](https://github.com/graphql-java/graphql-java) and spring webflux (with spring-boot 2),
additionally using [java-dataloader] (https://github.com/graphql-java/java-dataloader). This is forked from
https://github.com/geowarin/graphql-webflux, to show how support for DataLoader can be added.

When browsing the application on `localhost:8080`, you will see the [graphiQL](https://github.com/graphql/graphiql) explorer.

## Current status

Initial checkin with DataLoader added. Unit tests not yet implemented, and no real database access (just dummy data).
Explanation of how it all hangs together still to be added.