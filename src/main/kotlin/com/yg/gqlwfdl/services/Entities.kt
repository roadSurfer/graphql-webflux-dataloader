package com.yg.gqlwfdl.services

/**
 * Abstract base class from which all entities (domain model objects) derive.
 *
 * @param TId The data type of the unique [id] of this entity.
 * @property id The unique identifier of this entity.
 */
abstract class Entity<TId>(open val id: TId)

data class Customer(override val id: Long,
                    var firstName: String,
                    var lastName: String,
                    var companyId: Long,
                    var pricingDetailsId: Long,
                    var outOfOfficeDelegate: Long? = null) : Entity<Long>(id)

data class Company(override val id: Long,
                   var name: String,
                   var address: String,
                   var pricingDetailsId: Long,
                   var primaryContact: Long? = null) : Entity<Long>(id)

data class CompanyPartnership(override val id: Long,
                              val companyA: Company,
                              val companyB: Company) : Entity<Long>(id)

data class VatRate(override val id: Long,
                   var description: String,
                   var value: Double) : Entity<Long>(id)

data class DiscountRate(override val id: Long,
                        var description: String,
                        var value: Double) : Entity<Long>(id)

data class PaymentMethod(override val id: Long,
                         var description: String,
                         var charge: Double) : Entity<Long>(id)

data class PricingDetails(override val id: Long,
                          var description: String,
                          var vatRate: VatRate,
                          var discountRate: DiscountRate,
                          var preferredPaymentMethod: PaymentMethod) : Entity<Long>(id)