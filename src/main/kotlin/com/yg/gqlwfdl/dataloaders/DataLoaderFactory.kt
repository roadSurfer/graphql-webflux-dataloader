package com.yg.gqlwfdl.dataloaders

import com.yg.gqlwfdl.RequestContext
import com.yg.gqlwfdl.services.Company
import com.yg.gqlwfdl.services.CompanyService
import com.yg.gqlwfdl.services.Customer
import com.yg.gqlwfdl.services.CustomerService
import org.dataloader.DataLoaderRegistry
import org.springframework.stereotype.Component

/**
 * Class responsible for creating all the [ContextAwareDataLoader]s in the system.
 */
@Component
class DataLoaderFactory(private val customerService: CustomerService,
                        private val companyService: CompanyService) {

    /**
     * Creates a new instance of every data loader (for all the items in [DataLoaderType]) and registers it with the
     * passed in [DataLoaderRegistry].
     *
     * @param registry The registry with which to register each of the data loaders.
     * @param requestContext The object providing access to the current request context.
     */
    fun createAllAndRegister(registry: DataLoaderRegistry, requestContext: RequestContext) {
        DataLoaderType.values().forEach {
            // Use a "when" to ensure that every type is included: compiler will fail if not every entry in the enum
            // is handled.
            val dataLoader = when (it) {
                DataLoaderType.COMPANY -> ContextAwareDataLoader<Long, Company>(
                        requestContext, { it.id }, { companyService.findByIds(it) })

                DataLoaderType.CUSTOMER -> ContextAwareDataLoader<Long, Customer>(
                        requestContext, { it.id }, { customerService.findByIds(it) })
            }

            registry.register(it.registryKey, dataLoader)
        }
    }
}