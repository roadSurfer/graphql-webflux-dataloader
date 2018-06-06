package com.geowarin.graphql

import org.dataloader.BatchLoader
import org.dataloader.DataLoader
import java.util.concurrent.CompletableFuture

class CompanyDataLoader(companyRepository: CompanyRepository) : DataLoader<Long, CompanyRecord>(BatchLoader { keys ->
  CompletableFuture.supplyAsync({
    companyRepository.findAllById(keys.asIterable()).toList()
  })
})
