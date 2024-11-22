package com.bbhgroup.zeroone_task1

import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.Date

interface CategoryService{
    fun create(request: CategoryCreateRequest)
    fun update(request: CategoryUpdateRequest, id: Long)
    fun getOne(id: Long): CategoryResponse
    fun getAll(): List<CategoryResponse>
    fun getAll(pageable: Pageable): Page<CategoryResponse>
    fun delete(id: Long)
    fun existByCategoryName(name: String)
}
interface ProductService{
    fun create(request: ProductCreateRequest)
    fun update(request: ProductUpdateRequest, id: Long)
    fun getOne(id: Long): ProductResponse
    fun getAll(): List<ProductResponse>
    fun getAll(pageable: Pageable): Page<ProductResponse>
    fun delete(id: Long)
}
interface UserService{
    fun create(request: UserCreateRequest)
    fun update(request: UserUpdateRequest, id: Long)
    fun getOne(id: Long): UserResponse
    fun getAll(pageable: Pageable): Page<UserResponse>
    fun getAll(): List<UserResponse>
    fun delete(id: Long)
    fun existsByUsername(username: String?)
    fun existsByEmail(email: String?)
}
interface OrderService{
    fun order(orderItemRequest: OrderItemRequest)
    fun changeStatus(changeOrderStatusRequest: ChangeOrderStatusRequest, userId: Long)
    fun getAllOrdersByUserId(id:Long):List<OrderResponse>
    fun getOne(userId: Long, orderId: Long):OrderResponse
    fun cancelOrder(orderId:Long, userId:Long)
    fun ordersOfMonth(userId:Long, month: Int, year: Int): OrderOfMonthDto
    fun getStatistic(startDate: Date, endDate: Date, userId: Long): List<ProductStatisticDto>
    fun getCountOfProductClient(id:Long): CountOfProductClient
}
interface PaymentService{
    fun getAll(userId:Long): List<PaymentResponse>
}
@Service
class CategoryServiceImpl(
    private val categoryRepository: CategoryRepository,
    private val categoryMapper: CategoryMapper
) : CategoryService {
    override fun create(request: CategoryCreateRequest) {
        existByCategoryName(request.name)
        val category = categoryMapper.toEntity(request)
        categoryRepository.save(category)
    }

    override fun update(request: CategoryUpdateRequest, id: Long) {
        request.name?.let {
            val categoryFromDB =  categoryRepository.findByName(id, it)
            if (categoryFromDB != null) throw CategoryAlreadyExistsException()
        }
        categoryRepository.findByIdAndDeletedFalse(id)?.let {
            categoryRepository.save(categoryMapper.fromUpdateDto(request, it))
        }?: throw CategoryNotFoundException()
    }

    override fun getOne(id: Long): CategoryResponse {
        val category = categoryRepository.findByIdAndDeletedFalse(id) ?: throw CategoryNotFoundException()
        return categoryMapper.toDto(category)
    }
    override fun getAll(): List<CategoryResponse> {
        return categoryRepository.findAllNotDeleted().map { categoryMapper.toDto(it) }
    }

    override fun getAll(pageable: Pageable): Page<CategoryResponse> {
        return categoryRepository.findAllNotDeletedForPageable(pageable).map { categoryMapper.toDto(it) }
    }

    override fun delete(id: Long) {
        categoryRepository.trash(id)?: throw CategoryNotFoundException()
    }

    override fun existByCategoryName(name: String) {
      val category = categoryRepository.findByNameAndDeletedFalse(name)
        if(category != null) throw CategoryAlreadyExistsException()
    }
}

@Service
class ProductServiceImpl(
    private val productRepository: ProductRepository,
    private val productMapper: ProductMapper
) :ProductService {
    override fun create(request: ProductCreateRequest) {
        productRepository.save(productMapper.toEntity(request))
    }

    override fun update(request: ProductUpdateRequest, id: Long) {
        val product = productRepository.findByIdAndDeletedFalse(id)?: throw ProductNotFoundException()
        productRepository.save(productMapper.fromUpdateDto(request, product))
    }

    override fun getOne(id: Long): ProductResponse {
        return productRepository.findByIdAndDeletedFalse(id)?.let {
            productMapper.toDto(it)
        }?: throw ProductNotFoundException()
    }

    override fun getAll(): List<ProductResponse> {
        return productRepository.findAllNotDeleted().map { productMapper.toDto(it) }
    }

    override fun getAll(pageable: Pageable): Page<ProductResponse> {
        return productRepository.findAllNotDeletedForPageable(pageable).map { productMapper.toDto(it) }
    }

    override fun delete(id: Long) {
        productRepository.trash(id) ?: throw ProductNotFoundException()
    }
}

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper
):UserService{
    override fun create(request: UserCreateRequest) {
        request.run {
            existsByEmail(email)
            existsByUsername(username)
        }
        userRepository.save(userMapper.toEntity(request))
    }

    override fun update(request: UserUpdateRequest, id: Long) {
        val user = userRepository.findByIdAndDeletedFalse(id) ?: throw UserNotFoundException()
        request.run {
            username?.let {
                val usernameAndDeletedFalse = userRepository.findByUsername(it, id)
                if (usernameAndDeletedFalse != null) throw UsernameAlreadyExistException()
                user.username = it
            }
            userRepository.save(userMapper.fromUpdateDto(request, user))
        }
    }

    override fun getOne(id: Long): UserResponse {
        return userMapper.toDto(userRepository.findByIdAndDeletedFalse(id)?: throw UserNotFoundException())
    }

    override fun getAll(pageable: Pageable): Page<UserResponse> {
        return userRepository.findAll(pageable).map { userMapper.toDto(it) }
    }

    override fun getAll(): List<UserResponse> {
        return userRepository.findAllNotDeleted().map { userMapper.toDto(it) }
    }

    override fun delete(id: Long) {
        userRepository.trash(id) ?: throw UserNotFoundException()
    }

    override fun existsByUsername(username: String?) {
        if (userRepository.existsUserByUsername(username))
            throw UsernameAlreadyExistException()
    }
    override fun existsByEmail(email: String?) {
        if(userRepository.existsUserByEmail(email))
            throw EmailAlreadyExistsException()
    }
}
@Service
class OrderServiceImpl(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val paymentRepository: PaymentRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val orderItemMapper: OrderItemMapper,
    private val orderMapper: OrderMapper
): OrderService {

    @Transactional
    override fun order(orderItemRequest: OrderItemRequest) {
        val productIds: MutableList<Long> = mutableListOf()
        var totalPrice = 0.0
        for (item in orderItemRequest.items){
            productIds.add(item.productId)
            val product = productRepository.findByIdAndDeletedFalse(item.productId)
            if (product != null && product.stockCount >= item.quantity) {
                product.stockCount -= item.quantity
                productRepository.save(product)
            }else throw ProductNotEnoughException()
            totalPrice += item.unitPrice*item.quantity
        }
        val products = productRepository.findAllByIdAndDeletedFalse(productIds)
        if (products.isEmpty()) throw ProductNotFoundException()
        var order = Order(
            totalAmount = BigDecimal(totalPrice),
            status = OrderStatus.PENDING,
            user = orderItemRequest.userId.let {
                userRepository.findByIdAndDeletedFalse(it) } ?: throw UserNotFoundException()
        )
        order = orderRepository.save(order)
        val orderItems = orderItemRequest.items.map { orderItem ->  orderItemMapper.toEntity(orderItem, order)}
        orderItemRepository.saveAll(orderItems)
        val payment = Payment(
            paymentMethod = orderItemRequest.paymentMethod,
            amount = BigDecimal(totalPrice),
            order = order,
            user = orderItemRequest.userId.let {
                userRepository.findByIdAndDeletedFalse(it) } ?: throw UserNotFoundException()
        )
        paymentRepository.save(payment)
    }

    override fun changeStatus(changeOrderStatusRequest: ChangeOrderStatusRequest, userId: Long) {
        val user = userRepository.findByIdAndDeletedFalse(userId)?: throw UserNotFoundException()
        if (user.role != Role.ADMIN) throw UserAccessDeniedException()
         changeOrderStatusRequest.run {
            orderId.let {
                val order =  orderRepository.findByIdAndDeletedFalse(it)?: throw OrderNotFoundException()
                if (order.status == OrderStatus.CANCELLED) throw UserAccessDeniedException()
                order.status = orderStatus
                orderRepository.save(order)
            }
        }
    }

    override fun getAllOrdersByUserId(id: Long): List<OrderResponse> {
        return orderRepository.getAllOrderByUser(id).map { orderMapper.toDto(it) }
    }

    override fun getOne(userId: Long, orderId: Long): OrderResponse {
        return orderMapper.toDto(
            orderRepository.findByIdAndUserId(orderId, userId)?: throw OrderNotFoundException())
    }

    override fun cancelOrder(orderId: Long, userId: Long) {
        val order = orderRepository.findByIdAndUserId(orderId, userId)?: throw OrderNotFoundException()
        if (order.status != OrderStatus.PENDING) throw UserAccessDeniedException()
        order.status = OrderStatus.CANCELLED
        orderRepository.save(order)
    }

    override fun ordersOfMonth(userId: Long, month: Int, year: Int): OrderOfMonthDto {
        return orderRepository.getOrderStatisticOfMonth(userId, month, year)
    }

    override fun getStatistic(startDate: Date, endDate: Date, userId: Long): List<ProductStatisticDto> {
        return orderItemRepository.getStatisticProductByUserId(startDate, endDate, userId)
    }

    override fun getCountOfProductClient(id: Long): CountOfProductClient {
        val count = orderItemRepository.countProductClient(id)
        return CountOfProductClient(
            countUsers = count
        )
    }
}
@Service
class PaymentServiceImpl(
    private val paymentRepository: PaymentRepository,
    private val paymentMapper: PaymentMapper
): PaymentService {

    override fun getAll(userId: Long): List<PaymentResponse> {
        return paymentRepository.findAllByUserId(userId).map { paymentMapper.toDto(it) }
    }

}
