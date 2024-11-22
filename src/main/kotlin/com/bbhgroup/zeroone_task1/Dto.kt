package com.bbhgroup.zeroone_task1

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.util.Date

data class BaseMessage(val code: Int, val message: String?)

data class CategoryCreateRequest (
    @field:NotNull @Size(max = 50) val name: String,
    val description: String?
)
data class CategoryUpdateRequest (
    var name: String?,
    var description: String?,
)
data class CategoryResponse(
    var id: Long,
    var name: String,
    var description: String?,
    var date: Date?
)
data class ProductCreateRequest (
    @field:NotNull @Size(max = 50) val name: String,
    var description: String?,
    @field:NotNull val stockCount: Int,
    @field:NotNull val price: Double,
    @field:NotNull val categoryId: Long
)
data class ProductUpdateRequest (
    var name: String?,
    var description: String?,
    var stockCount: Int?,
    var price: Double?,
    var categoryId: Long?
)
data class ProductResponse(
    var id: Long,
    var name: String,
    var description: String?,
    var stockCount: Int,
    var price: BigDecimal,
    var categoryId: Long
)
data class UserCreateRequest (
    @field:NotNull @Size(min = 5,max = 20) val username: String,
    @field:NotNull @Size(max = 100) val email: String,
    @field:NotNull val address: String,
    @field:NotNull val fullName: String
)
data class UserUpdateRequest (
    var username: String?,
    var address: String?,
    var fullName: String?
)
data class UserResponse(
    var id: Long,
    var username: String,
    var email: String,
    var address: String,
    var fullName: String,
    var role: Role,
)

data class OrderItemRequest(
    var items: List<OrderItemDto>,
    @field:NotNull var userId: Long,
    @field:NotNull val paymentMethod: PaymentMethod

)
data class OrderItemDto(
    @field:NotNull val productId: Long,
    @field:NotNull val unitPrice: Double,
    @field:NotNull val quantity: Int,
)
data class ChangeOrderStatusRequest(
    @field:NotNull var orderId: Long,
    @field:NotNull var orderStatus: OrderStatus
)
data class OrderResponse(
    var id: Long,
    var amount: BigDecimal,
    var status: OrderStatus,
    var userId: Long,
    var date: Date
)
data class PaymentResponse(
    var id: Long,
    val paymentMethod: PaymentMethod,
    val amount: BigDecimal,
    val userId: Long,
    val orderId: Long,
    val date: Date
)
interface OrderOfMonthDto {
    val countOrder: Int
    val totalAmount: BigDecimal
}

interface ProductStatisticDto{
    val productId: Long
    val productName: String
    val repetition: Int
    val count: Int
    val totalAmount: BigDecimal
}

data class CountOfProductClient(
    var countUsers: Int,
)