package com.bbhgroup.zeroone_task1

enum class Role{
    ADMIN, USER
}
enum class OrderStatus {
    PENDING, DELIVERED, FINISHED, CANCELLED
}
enum class PaymentMethod{
    UZCARD, HUMO, CASH, PAYME
}
enum class ErrorCodes(val code: Int) {
    USERNAME_ALREADY_EXISTS(100),
    EMAIL_ALREADY_EXISTS(101),
    USER_NOT_FOUND(103),
    CATEGORY_NAME_ALREADY_EXISTS(300),
    CATEGORY_NOT_FOUND(303),
    PRODUCT_NOT_FOUND(603),
    PRODUCT_COUNT_NOT_ENOUGH(604),
    ACCESS_DENIED(900),
    ORDER_NOT_FOUND(1000)
}