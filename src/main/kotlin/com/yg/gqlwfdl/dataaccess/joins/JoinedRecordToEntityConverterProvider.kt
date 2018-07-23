package com.yg.gqlwfdl.dataaccess.joins

import com.yg.gqlwfdl.dataaccess.PricingDetailsRecords
import com.yg.gqlwfdl.dataaccess.toEntity
import com.yg.gqlwfdl.services.Entity
import com.yg.gqlwfdl.yg.db.public_.tables.records.*
import org.springframework.stereotype.Component

/**
 * Object responsible for providing all the [JoinedRecordToEntityConverter]s in the system.
 */
interface JoinedRecordToEntityConverterProvider {
    /**
     * Gets all the [JoinedRecordToEntityConverter]s in the system.
     */
    val recordToConverters: List<JoinedRecordToEntityConverter<out Entity<out Any>>>
}

/**
 * Default implementation of [JoinedRecordToEntityConverterProvider], returning all the converters in the example
 * database structure.
 */
@Component
class DefaultRecordToEntityConverterProvider : JoinedRecordToEntityConverterProvider {

    override val recordToConverters: List<JoinedRecordToEntityConverter<out Entity<out Any>>> =
            listOf(
                    SingleTypeJoinedRecordToEntityConverter(CustomerRecord::class.java) { it.toEntity() },
                    SingleTypeJoinedRecordToEntityConverter(CompanyRecord::class.java) { it.toEntity() },
                    MultiTypeJoinedRecordToEntityConverter3(PricingDetailsRecord::class.java,
                            PRICING_DETAILS_VAT_RATE.name,
                            PRICING_DETAILS_DISCOUNT_RATE.name,
                            PRICING_DETAILS_PREFERRED_PAYMENT_METHOD.name,
                            VatRateRecord::class.java,
                            DiscountRateRecord::class.java,
                            PaymentMethodRecord::class.java
                    ) { pricingDetailsRecord, vatRateRecord, discountRateRecord, paymentMethodRecord ->
                        PricingDetailsRecords(pricingDetailsRecord, vatRateRecord, discountRateRecord, paymentMethodRecord).toEntity()
                    }
            )
}