package com.bbhgroup.zeroone_task1

import jakarta.validation.Valid
import jakarta.websocket.server.PathParam
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.ExceptionHandler
import java.util.Date

@ControllerAdvice
class ExceptionHandler(private val errorMessageSource: ResourceBundleMessageSource){

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(exception: MethodArgumentNotValidException): ResponseEntity<BaseMessage> {
        val errorMessages = exception.bindingResult.fieldErrors.joinToString(separator = ", ") { fieldError ->
            "${fieldError.field}: ${fieldError.defaultMessage ?: "Invalid value"}"
        }
        val baseMessage = BaseMessage(
            code = HttpStatus.BAD_REQUEST.value(),
            message = "Validation errors: $errorMessages"
        )
        return ResponseEntity.badRequest().body(baseMessage)
    }

    @ExceptionHandler(TaskException::class)
    fun exceptionHandling(exception: TaskException): ResponseEntity<BaseMessage> {
        return ResponseEntity.badRequest().body(exception.getErrorMessage(errorMessageSource))
    }
}

@RestController
@RequestMapping("api/v1/category")
class  CategoryController(
    private val service: CategoryService
) {
    @PostMapping("create")
    private fun create(@RequestBody request: CategoryCreateRequest) {
        service.create(request)
    }
    @PutMapping("update/{id}")
    fun update(@PathVariable("id") id: Long, @RequestBody request: CategoryUpdateRequest) {
        service.update(request, id)
    }
    @GetMapping("get-one/{id}")
    fun getOne(@PathVariable("id") id: Long): CategoryResponse {
        return service.getOne(id)
    }
    @GetMapping("get-all")
    fun getAll(): List<CategoryResponse> {
        return service.getAll()
    }
    @GetMapping("get-all-pageable")
    fun getAllPageable(pageable: Pageable): Page<CategoryResponse> {
        return service.getAll(pageable)
    }
    @DeleteMapping("delete/{id}")
    fun delete(@PathVariable("id") id: Long) {
        service.delete(id)
    }
}
@RestController
@RequestMapping("api/v1/product")
class ProductController(private val service: ProductService) {

    @PostMapping("create")
    fun create(@RequestBody request: ProductCreateRequest) {
        service.create(request)
    }
    @PutMapping("update/{id}")
    fun update(@PathVariable("id") id: Long, @RequestBody request: ProductUpdateRequest) {
        service.update(request, id)
    }
    @GetMapping("get-one/{id}")
    fun getOne(@PathVariable("id") id: Long): ProductResponse {
        return service.getOne(id)
    }
    @GetMapping("get-all")
    fun getAll(): List<ProductResponse> {
        return service.getAll()
    }
    @GetMapping("get-all-pageable")
    fun getAllPageable(pageable: Pageable): Page<ProductResponse> {
        return service.getAll(pageable)
    }
    @DeleteMapping("delete/{id}")
    fun deleteOne(@PathVariable("id") id: Long) {
        service.delete(id)
    }
}

@RestController
@RequestMapping("api/v1/user")
class UserController(private val service: UserService) {

    @PostMapping("create")
    fun create(@RequestBody request: UserCreateRequest){
        service.create(request)
    }
    @PutMapping("update/{id}")
    fun update(@RequestBody request: UserUpdateRequest, @PathVariable("id") id: Long) {
        service.update(request, id)
    }
    @GetMapping("get-one/{id}")
    fun getOne(@PathVariable("id") id: Long): UserResponse {
        return service.getOne(id)
    }
    @GetMapping("get-all")
    fun getAll(): List<UserResponse> {
        return service.getAll()
    }
    @GetMapping("get-all-pageable")
    fun getAllPageable(pageable: Pageable): Page<UserResponse> {
        return service.getAll(pageable)
    }
    @DeleteMapping("delete/{id}")
    fun deleteOne(@PathVariable("id") id: Long) {
        service.delete(id)
    }
}

@RestController
@RequestMapping("api/v1/order")
class OrderController(private val service: OrderService) {

    @PostMapping("create")
    fun create(@RequestBody orderItemRequest: OrderItemRequest) {
        service.order(orderItemRequest)
    }
    @PostMapping("change-status/{user-id}")
    fun changeStatus(@RequestBody @Valid changeOrderStatusRequest: ChangeOrderStatusRequest, @PathVariable("user-id") userId: Long) {
        service.changeStatus(changeOrderStatusRequest, userId)
    }
    @PostMapping("cancel-order/{order-id}/{user-id}")
    fun cancelOrder(@PathVariable("order-id") orderId: Long,@PathVariable("user-id") userId:Long) {
        service.cancelOrder(orderId, userId)
    }
    @GetMapping("get-one/{order-id}/{user-id}")
    fun getOne(@PathVariable("order-id") orderId: Long,@PathVariable("user-id") userId: Long): OrderResponse {
        return service.getOne(userId, orderId)
    }
    @GetMapping("get-all/{user-id}")
    fun getAllByUser(@PathVariable("user-id") userId: Long): List<OrderResponse> {
        return service.getAllOrdersByUserId(userId)
    }

    @GetMapping("get-order-count")
    fun getOrderCount(@PathParam("month") month:Int, @PathParam("year") year:Int, @PathParam("userId") userId: Long): OrderOfMonthDto {
        return service.ordersOfMonth(userId, month, year)
    }
    @GetMapping("get-statistic-product/{user-id}")
    fun getStatisticProductByUserId(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") startDate: Date,
                                    @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") endDate: Date,
                                    @PathVariable("user-id")userId: Long):List<ProductStatisticDto> {
        return service.getStatistic(startDate, endDate, userId)
    }
    @GetMapping("get-count-client/{product-id}")
    fun getCountClientByProductId(@PathVariable("product-id") productId: Long): CountOfProductClient {
        return service.getCountOfProductClient(productId)
    }
}
@RestController
@RequestMapping("api/v1/payment")
class PaymentController(private val service: PaymentService) {

    @GetMapping("get-all/{user-id}")
    fun getAllByUser(@PathVariable("user-id") userId: Long): List<PaymentResponse> {
        return service.getAll(userId)
    }
}
