package com.bbhgroup.zeroone_task1

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.data.repository.query.Param
import java.util.Date

@NoRepositoryBean
interface BaseRepository<T : BaseEntity> : JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    fun findByIdAndDeletedFalse(id: Long): T?
    fun trash(id: Long): T?
    fun trashList(ids: List<Long>): List<T?>
    fun findAllNotDeleted(): List<T>
    fun findAllNotDeleted(pageable: Pageable): List<T>
    fun findAllNotDeletedForPageable(pageable: Pageable): Page<T>
    fun saveAndRefresh(t: T): T
}
class BaseRepositoryImpl<T : BaseEntity>(
    entityInformation: JpaEntityInformation<T, Long>,
    private val entityManager: EntityManager
) : SimpleJpaRepository<T, Long>(entityInformation, entityManager), BaseRepository<T> {

    val isNotDeletedSpecification = Specification<T> { root, _, cb -> cb.equal(root.get<Boolean>("deleted"), false) }

    override fun findByIdAndDeletedFalse(id: Long) = findByIdOrNull(id)?.run { if (deleted) null else this }

    @Transactional
    override fun trash(id: Long): T? = findByIdOrNull(id)?.run {
        deleted = true
        save(this)
    }

    override fun findAllNotDeleted(): List<T> = findAll(isNotDeletedSpecification)
    override fun findAllNotDeleted(pageable: Pageable): List<T> = findAll(isNotDeletedSpecification, pageable).content
    override fun findAllNotDeletedForPageable(pageable: Pageable): Page<T> =findAll(isNotDeletedSpecification, pageable)

    @Transactional
    override fun trashList(ids: List<Long>): List<T?> = ids.map { trash(it) }


    @Transactional
    override fun saveAndRefresh(t: T): T {
        return save(t).apply { entityManager.refresh(this) }
    }
}

interface CategoryRepository : BaseRepository<Category>{

    fun findByNameAndDeletedFalse(name: String): Category?

    @Query("""
        select t from Category t 
        where t.id <> :id
        and t.name = :name
        and t.deleted = false 
    """)
    fun findByName(id: Long, name: String): Category?

}
interface ProductRepository : BaseRepository<Product>{
    @Query("""
        select p from Product p where p.id in (:ids)
    """)
    fun findAllByIdAndDeletedFalse(ids: List<Long>): List<Product>
}

interface UserRepository : BaseRepository<User>{
    fun existsUserByUsername(username: String?): Boolean
    fun existsUserByEmail(email: String?): Boolean

    @Query("""
        select u from User u where 
        u.id != :id and
        u.username = :username
        order by u.id
    """)
    fun findByUsername(username: String, id: Long):User?
}

interface OrderRepository : BaseRepository<Order>{

@Query("""
    select o from Order o where o.user.id = :id order by o.id
""")
fun getAllOrderByUser(id:Long):List<Order>

    @Query("""
    select 
        count(o.id) as countOrder, 
        coalesce(sum(o.totalAmount), 0) as totalAmount
    
    from Order o 
    where o.user.id = :userId 
    and extract(month from o.createdAt) = :month 
    and extract(year from o.createdAt) = :year
""")
    fun getOrderStatisticOfMonth(
        @Param("userId") userId: Long,
        @Param("month") month: Int,
        @Param("year") year: Int
    ): OrderOfMonthDto

fun findByIdAndUserId(orderId: Long, userId: Long): Order?
}
interface OrderItemRepository : BaseRepository<OrderItem>{
    @Query("""
    select 
    o.product.id as productId,
    o.product.name as productName,
    count(o.product.id) as repetition,       
    sum(o.quantity) as count,
    sum(o.totalPrice) as totalAmount
from OrderItem o 
where o.order.user.id = :userId 
and o.createdAt between :startDate and :endDate
group by o.product.id, o.product.name

""")
    fun getStatisticProductByUserId(
        @Param("startDate") startDate: Date,
        @Param("endDate") endDate: Date,
        @Param("userId") userId: Long
    ): List<ProductStatisticDto>

    @Query("""
    select 
        COUNT(DISTINCT o.user.id) 
    FROM 
        OrderItem oi
    JOIN 
        oi.order o
    WHERE 
        oi.product.id = :id
    GROUP BY 
        oi.product.id
    """)
    fun countProductClient(id: Long): Int
}
interface PaymentRepository : BaseRepository<Payment>{
    fun findAllByUserId(userId: Long): List<Payment>
}