package com.bbhgroup.zeroone_task1

import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class CategoryMapper {
    fun toEntity(request: CategoryCreateRequest): Category = Category(
        name = request.name,
        description = request.description!!
    )
    fun toDto(category: Category): CategoryResponse = CategoryResponse(
        id = category.id!!,
        name = category.name,
        description = category.description!!,
        date = category.createdAt!!
    )
    fun fromUpdateDto(request: CategoryUpdateRequest, category: Category) : Category{
        request.run {
            if (name!=null) category.name = name!!
            if (description!=null) category.description = description!!
        }
        return category
    }
}
@Component
class ProductMapper(
    private val categoryRepository: CategoryRepository
) {
    fun toEntity(request: ProductCreateRequest): Product {
        val category = categoryRepository.findByIdAndDeletedFalse(request.categoryId) ?: throw CategoryNotFoundException()
        return  Product(
                name = request.name,
                description = request.description!!,
                price = BigDecimal(request.price),
                stockCount = request.stockCount,
                category = category
            )
    }
    fun toDto(product: Product): ProductResponse = ProductResponse(
        id = product.id!!,
        name = product.name,
        description = product.description,
        price = product.price,
        stockCount = product.stockCount,
        categoryId = product.category.id!!
    )
    fun fromUpdateDto(request: ProductUpdateRequest, product: Product): Product {
        request.run {
            if (name!=null) product.name = name!!
            if (description!=null) product.description = description!!
            if (price!=null) product.price = BigDecimal(price!!)
            if (stockCount!=null) product.stockCount = stockCount!!
            if (categoryId != null) product.category = categoryRepository.findByIdAndDeletedFalse(categoryId!!)?: throw CategoryNotFoundException()
        }
        return product
    }
}

@Component
class UserMapper{

    fun toEntity(request: UserCreateRequest) : User = User(
        email = request.email,
        username = request.username,
        fullName = request.fullName,
        address = request.address,
        role = Role.USER,
    )

    fun toDto(user: User): UserResponse = UserResponse(
        id = user.id!!,
        email = user.email,
        username = user.username,
        fullName = user.fullName,
        address = user.address,
        role = Role.USER,
    )
    fun fromUpdateDto(request: UserUpdateRequest, user: User): User {
        request.run {
            if (username != null) user.username = username!!
            if (fullName != null) user.fullName = fullName!!
            if (address != null) user.address = address!!
        }
     return user
    }
}

@Component
class OrderItemMapper(
    private val productRepository: ProductRepository
){
    fun toEntity(request: OrderItemDto, order: Order): OrderItem = OrderItem(
        product = request.productId.let {
            productRepository.findByIdAndDeletedFalse(it)
        }?: throw ProductNotFoundException(),
        unitPrice = BigDecimal(request.unitPrice),
        quantity = request.quantity,
        totalPrice = BigDecimal(request.unitPrice*request.quantity),
        order = order
    )
}

@Component
class OrderMapper{
    fun toDto(order: Order): OrderResponse {
        return OrderResponse(
            id = order.id!!,
            amount = order.totalAmount,
            userId = order.user.id!!,
            status = order.status,
            date = order.createdAt!!
        )
    }
}
@Component
class PaymentMapper{
    fun toDto(payment: Payment): PaymentResponse = PaymentResponse(
        id = payment.id!!,
        amount = payment.amount,
        paymentMethod = payment.paymentMethod,
        userId = payment.user.id!!,
        orderId = payment.order.id!!,
        date = payment.createdAt!!
    )
}