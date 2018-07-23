package com.yg.gqlwfdl.dataaccess

import com.yg.gqlwfdl.dataaccess.joins.*
import com.yg.gqlwfdl.services.DiscountRate
import com.yg.gqlwfdl.services.PaymentMethod
import com.yg.gqlwfdl.services.PricingDetails
import com.yg.gqlwfdl.services.VatRate
import com.yg.gqlwfdl.yg.db.public_.Tables.*
import com.yg.gqlwfdl.yg.db.public_.tables.records.DiscountRateRecord
import com.yg.gqlwfdl.yg.db.public_.tables.records.PaymentMethodRecord
import com.yg.gqlwfdl.yg.db.public_.tables.records.PricingDetailsRecord
import com.yg.gqlwfdl.yg.db.public_.tables.records.VatRateRecord
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SelectJoinStep
import org.springframework.stereotype.Repository
import java.util.concurrent.Executor

/**
 * Repository providing access to pricing details information.
 */
interface PricingDetailsRepository : EntityRepository<PricingDetails, Long>

/**
 * Concrete implementation of [CompanyPartnershipRepository], which uses a database for its data.
 */
@Repository
class DBPricingDetailsRepository(create: DSLContext,
                                 asyncExecutor: Executor,
                                 recordToEntityConverterProvider: JoinedRecordToEntityConverterProvider,
                                 graphQLFieldToJoinMapper: GraphQLFieldToJoinMapper)
    : DBEntityRepository<PricingDetails, Long, PricingDetailsRecord>(
        create, asyncExecutor, recordToEntityConverterProvider, graphQLFieldToJoinMapper, PRICING_DETAILS, PRICING_DETAILS.ID),
        PricingDetailsRepository {

    override fun getEntity(record: Record): PricingDetails {
        return PricingDetailsRecords(
                record.into(PRICING_DETAILS), record.into(VAT_RATE), record.into(DISCOUNT_RATE), record.into(PAYMENT_METHOD)
        ).toEntity()
    }

    override fun addDefaultJoins(select: SelectJoinStep<Record>)
            : List<JoinInstance<out Any, PricingDetailsRecord, out Record>> {

        select.join(VAT_RATE).on(PRICING_DETAILS.VAT_RATE.eq(VAT_RATE.ID))
                .join(DISCOUNT_RATE).on(PRICING_DETAILS.DISCOUNT_RATE.eq(DISCOUNT_RATE.ID))
                .join(PAYMENT_METHOD).on(PRICING_DETAILS.PREFERRED_PAYMENT_METHOD.eq(PAYMENT_METHOD.ID))

        return listOf(
                JoinInstance<Long, PricingDetailsRecord, VatRateRecord>(
                        PRICING_DETAILS_VAT_RATE, PRICING_DETAILS.VAT_RATE, VAT_RATE.ID),
                JoinInstance<Long, PricingDetailsRecord, DiscountRateRecord>(
                        PRICING_DETAILS_DISCOUNT_RATE, PRICING_DETAILS.DISCOUNT_RATE, DISCOUNT_RATE.ID),
                JoinInstance<Long, PricingDetailsRecord, PaymentMethodRecord>(
                        PRICING_DETAILS_PREFERRED_PAYMENT_METHOD, PRICING_DETAILS.PREFERRED_PAYMENT_METHOD, PAYMENT_METHOD.ID))
    }
}

/**
 * A class aggregating all the different types of records that contain the data required to construct a [PricingDetails]
 * object.
 */
data class PricingDetailsRecords(val pricingDetailsRecord: PricingDetailsRecord,
                                 val vatRateRecord: VatRateRecord,
                                 val discountRateRecord: DiscountRateRecord,
                                 val paymentMethodRecord: PaymentMethodRecord)

/**
 * Converts a [PricingDetailsRecords] to its corresponding entity, a [PricingDetails] object.
 */
fun PricingDetailsRecords.toEntity() =
        PricingDetails(this.pricingDetailsRecord.id, this.pricingDetailsRecord.description,
                with(this.vatRateRecord) { VatRate(this.id, this.description, this.value) },
                with(this.discountRateRecord) { DiscountRate(this.id, this.description, this.value) },
                with(this.paymentMethodRecord) { PaymentMethod(this.id, this.description, this.charge) })