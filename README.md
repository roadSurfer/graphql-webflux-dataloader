# GraphQL, Spring WebFlux, DataLoader, JOOQ

This is a sample GraphQL application written in Kotlin that
uses [graphql-java](https://github.com/graphql-java/graphql-java) and Spring WebFlux (with Spring Boot 2),
additionally using [java-dataloader](https://github.com/graphql-java/java-dataloader) and
[jOOQ](https://www.jooq.org/). This is forked from
https://github.com/geowarin/graphql-webflux, to show how support for DataLoader can be added.

## Instructions
Load the project into your IDE (I'm using IntelliJ) and run the `GraphQLApplication` class. This will start a Spring Boot
application serving GraphQL requests.

Open `localhost:8080` in your browser. You will see the [GraphiQL](https://github.com/graphql/graphiql) explorer.
There you can start making GraphQL calls. The underlying H2 database is populated with test data at application
startup so you can immediately make a call such as:
```
{
  customers {
    id
    firstName
    lastName
    company {
      id
      name
      address
    }
  }
}
```

## Current Status
* DataLoader added.
* JOOQ added.
* Example data structure created (customers, companies)
* H2 data access implemented

## Pre-Requisites
Some settings in some of the files (e.g. gradle.properties and build.gradle) mean that as things currently stand
the code will only work on Java 10 (and possibly 9: I haven't tried), but not on Java 8.  This is because of the
changes to how JAXB was bundled up since Java 9.  This is discussed [here](https://github.com/jOOQ/jOOQ/issues/6477)
and [here](https://github.com/etiennestuder/gradle-jooq-plugin/issues/55).
However to get it working in Java 8 should be possible by removing the calls to `--add-modules` in the two files
mentioned, and possibly some other tweaking.

## To Do
* When populating data from GraphQL resolvers, prime data into data loaders to avoid redundant re-fetching in subsequent resolvers.
* Add functionality to automatically add joins to relevant tables when querying the database, based on requested GraphQL fields.
* Document explaining how it all hangs together.
* Unit tests.
* Test error handling (esp in async code)
