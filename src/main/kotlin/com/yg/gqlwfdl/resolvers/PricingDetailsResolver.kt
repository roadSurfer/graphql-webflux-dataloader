package com.yg.gqlwfdl.resolvers

import com.coxautodev.graphql.tools.GraphQLResolver
import com.yg.gqlwfdl.services.PricingDetails

@Suppress("unused")
/**
 * Resolver for [PricingDetails] objects. Provides access to properties which the GraphQL schema exposes of these
 * objects, but which don't exist directly on the domain model object (PricingDetails in this case), and need to be
 * retrieved separately. In this case this is simply done by mapping the properties to values which exist on sub-objects
 * of the PricingDetails object.
 */
class PricingDetailsResolver : DataLoadingResolver(), GraphQLResolver<PricingDetails> {

    /**
     * Gets the value of the passed in [pricingDetails] object's [vatRate][PricingDetails.vatRate] property.
     */
    fun vatRateValue(pricingDetails: PricingDetails) =
            pricingDetails.vatRate.value

    /**
     * Gets the value of the passed in [pricingDetails] object's [discountRate][PricingDetails.discountRate] property.
     */
    fun discountRateValue(pricingDetails: PricingDetails) =
            pricingDetails.discountRate.value

    /**
     * Gets the description of the passed in [pricingDetails] object's
     * [preferredPaymentMethod][PricingDetails.preferredPaymentMethod] property.
     */
    fun preferredPaymentMethodDescription(pricingDetails: PricingDetails) =
            pricingDetails.preferredPaymentMethod.description
}