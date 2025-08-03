package com.example.cravory.model

data class ApiResponse(
    val response_code: Int,
    val outcome_code: Int,
    val response_message: String,
    val cuisines: List<Cuisine>,
    val timestamp: String,
    val requester_ip: String,
    val timetaken: String
)
