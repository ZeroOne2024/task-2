package com.bbhgroup.zeroone_task1

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.util.Date

@MappedSuperclass
abstract class BaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @CreatedDate @Temporal(TemporalType.TIMESTAMP) var createdAt: Date?= Date(System.currentTimeMillis()),
    @Column(nullable = false) @ColumnDefault(value = "false") var deleted: Boolean = false,
)

@Entity
class Category(
    @Column(nullable = false, length = 50)var name: String,
    var description: String?
): BaseEntity()

@Entity
class Product(
    @Column(nullable = false, length = 100) var name: String,
    var description: String,
    @Column(nullable = false) var price: BigDecimal,
    @Column(nullable = false) var stockCount: Int,
    @ManyToOne var category: Category
): BaseEntity()

@Entity
class OrderItem(
    @Column(nullable = false) val quantity: Int,
    @Column(nullable = false) val unitPrice: BigDecimal,
    @Column(nullable = false) var totalPrice: BigDecimal,
    @ManyToOne val product: Product,
    @ManyToOne val order: Order
): BaseEntity()

@Entity
@Table(name = "orders")
class Order(
    @Column(nullable = false) var totalAmount: BigDecimal,
    @Column(nullable = false) @Enumerated(EnumType.STRING) var status: OrderStatus,
    @ManyToOne val user: User
): BaseEntity()

@Entity
class Payment(
    @Column(nullable = false) @Enumerated(EnumType.STRING) var paymentMethod: PaymentMethod,
    @Column(nullable = false) var amount: BigDecimal,
    @ManyToOne var user: User,
    @ManyToOne val order: Order
): BaseEntity()

@Entity
@Table(name = "users")
class User(
    @Column(nullable = false, unique = true, length = 20) var username: String,
    @Column(nullable = false, unique = true, length = 100) var email: String,
    @Column(nullable = false, length = 100) var fullName: String,
    @Column(nullable = false, length = 100) var address: String,
    @Column(nullable = false)@Enumerated(EnumType.STRING) var role: Role,
): BaseEntity()