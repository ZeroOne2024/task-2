package com.bbhgroup.zeroone_task1

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class DataLoader(
    private val userRepository: UserRepository
):CommandLineRunner {

    @Value("\${spring.sql.init.mode}")
    lateinit var init: String

    override fun run(vararg args: String?) {

        if (init == "always"){
            val admin = User(
                username = "admin",
                email = "admin@gmail.com",
                fullName = "Administrator",
                address = "Toshkent",
                role = Role.ADMIN,
            )
            userRepository.save(admin)
        }
    }
}