package com.yg.gqlwfdl

import com.yg.gqlwfdl.dataaccess.DbConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.sql.DriverManager

@SpringBootApplication
class GraphQLApplication(private val dbConfig: DbConfig) {
    init {
        DriverManager.getConnection(dbConfig.url, "sa", "").use { connection ->
            connection.createStatement().use { statement ->
                // Drop existing tables.
                statement.execute("drop table if exists customer;")
                statement.execute("drop table if exists company;")

                // Create the tables.
                statement.execute("""
                    | create table company (
                    |     id bigint auto_increment not null primary key,
                    |     name varchar(255) not null,
                    |     address varchar(255) not null
                    | );
                """.trimMargin())
                statement.execute("""
                    | create table customer (
                    |     id bigint auto_increment not null primary key,
                    |     first_name varchar(255) not null,
                    |     last_name varchar(255) not null,
                    |     company_id bigint not null
                    | );
                """.trimMargin())

                // Insert dummy data.
                // Companies: A to C.
                ('A'..'C').forEach {
                    statement.execute("insert into company (name, address) values('Company-$it', '$it Street, $it Town');")
                }

                // Customers: 3 customers for each company.
                val resultSet = statement.executeQuery("select id, name from company order by id")
                while (resultSet.next()) {
                    val companyId = resultSet.getLong("id")
                    val companyName = resultSet.getString("name")
                    (1..3).forEach {
                        connection.createStatement().use { statement ->
                            statement.execute("""
                                | insert into customer (first_name, last_name, company_id)
                                | values('User-$it', 'From-$companyName', $companyId);
                            """.trimMargin())
                        }
                    }
                }
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<GraphQLApplication>(*args)
}
