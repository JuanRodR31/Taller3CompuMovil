package com.example.taller3compumovil.data

import com.google.android.gms.maps.model.LatLng

data class User(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val online: Boolean = false,
    val mapPosition: LatLng = LatLng(0.0, 0.0),
    val photoUrl: String = "",
)
