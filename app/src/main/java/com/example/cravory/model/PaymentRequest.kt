package com.example.cravory.model

data class PaymentRequest(
    val totalAmount: String,
    val totalItems: Int,
    val data: List<PaymentItem>
)

data class PaymentItem(
    val cuisineId: String,
    val itemId: String,
    val itemPrice: Double,
    val quantity: Int
)
