package com.yg.gqlwfdl.services

import com.yg.gqlwfdl.dataaccess.CustomerRepository
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class CustomerService(private val customerRepository: CustomerRepository) {
    fun findAll(): CompletableFuture<List<Customer>> =
            customerRepository.findAll().thenApply { it.map { Customer(it.id, it.firstName, it.lastName, it.companyId) } }
}