package com.yg.gqlwfdl.services

import com.yg.gqlwfdl.dataaccess.CustomerRepository
import com.yg.gqlwfdl.yg.db.public_.tables.records.CustomerRecord
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class CustomerService(private val customerRepository: CustomerRepository) {
    fun findAll(): CompletableFuture<List<Customer>> =
            customerRepository.findAll().toEntityListCompletableFuture()

    fun findByIds(ids: List<Long>): CompletableFuture<List<Customer>> =
            customerRepository.findByIds(ids).toEntityListCompletableFuture()

    private fun CompletableFuture<out Iterable<CustomerRecord>>.toEntityListCompletableFuture() =
            this.thenApply { it.map { it.toEntity() } }

    private fun CustomerRecord.toEntity() =
            Customer(this.id, this.firstName, this.lastName, this.companyId, this.outOfOfficeDelegate)
}