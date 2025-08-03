package com.example.cravory.model

data class PaymentResponse(
    val responseCode: Int,
    val outcomeCode: Int,
    val responseMessage: String,
    val txnRefNo: String? = null
)
