package com.yg.gqlwfdl.services

import com.yg.gqlwfdl.dataaccess.CompanyRepository
import com.yg.gqlwfdl.yg.db.public_.tables.records.CompanyRecord
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class CompanyService(private val companyRepository: CompanyRepository) {
    fun findAll(): CompletableFuture<List<Company>> =
            companyRepository.findAll().toEntityListCompletableFuture()

    fun findByIds(ids: List<Long>): CompletableFuture<List<Company>> =
            companyRepository.findByIds(ids).toEntityListCompletableFuture()

    private fun CompletableFuture<out Iterable<CompanyRecord>>.toEntityListCompletableFuture() =
            this.thenApply { it.map { it.toEntity() } }

    private fun CompanyRecord.toEntity() = Company(this.id, this.name, this.address)
}