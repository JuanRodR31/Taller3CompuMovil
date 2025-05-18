package com.example.taller3compumovil.data


data class User(
    val fullName: String = "",
    val email: String = "",
    val phone: Long=0,
    val online: Boolean = false,
    val mapPosition: List<SimpleLatLng> = listOf(SimpleLatLng(0.0, 0.0)),
    val photoUrl: String = "",
)
data class SimpleLatLng(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

