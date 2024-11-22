package com.bbhgroup.zeroone_task1

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = BaseRepositoryImpl::class)
class ZeroOneTask1Application

fun main(args: Array<String>) {
    runApplication<ZeroOneTask1Application>(*args)
}
