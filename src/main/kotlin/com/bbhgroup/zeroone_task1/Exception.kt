package com.bbhgroup.zeroone_task1

import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource

sealed class TaskException : RuntimeException(){
    abstract fun errorCode(): ErrorCodes
    open fun getAllArguments(): Array<Any?>? = null

    fun getErrorMessage(resourceBundle: ResourceBundleMessageSource): BaseMessage {
        val message = try {
            resourceBundle.getMessage(
                errorCode().name, getAllArguments(), LocaleContextHolder.getLocale()
            )
        } catch (e: Exception) {
            e.message
        }
        return BaseMessage(errorCode().code, message)
    }
}

class CategoryAlreadyExistsException : TaskException() {
    override fun errorCode(): ErrorCodes = ErrorCodes.CATEGORY_NAME_ALREADY_EXISTS
}
class CategoryNotFoundException : TaskException(){
    override fun errorCode(): ErrorCodes = ErrorCodes.CATEGORY_NOT_FOUND
}
class ProductNotFoundException : TaskException(){
    override fun errorCode(): ErrorCodes = ErrorCodes.PRODUCT_NOT_FOUND
}
class  UsernameAlreadyExistException : TaskException(){
    override fun errorCode(): ErrorCodes = ErrorCodes.USERNAME_ALREADY_EXISTS
}
class  UserNotFoundException : TaskException(){
    override fun errorCode(): ErrorCodes = ErrorCodes.USER_NOT_FOUND
}
class EmailAlreadyExistsException : TaskException(){
    override fun errorCode(): ErrorCodes = ErrorCodes.EMAIL_ALREADY_EXISTS
}
class ProductNotEnoughException: TaskException(){
    override fun errorCode(): ErrorCodes = ErrorCodes.PRODUCT_COUNT_NOT_ENOUGH
}
class UserAccessDeniedException: TaskException(){
    override fun errorCode(): ErrorCodes = ErrorCodes.ACCESS_DENIED
}
class OrderNotFoundException: TaskException(){
    override fun errorCode(): ErrorCodes = ErrorCodes.ORDER_NOT_FOUND
}