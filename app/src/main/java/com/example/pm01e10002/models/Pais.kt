package com.example.pm01e10002.models

data class Pais(
    val nombre: String,
    val codigo: String
) {
    fun getDisplayName(): String = "$nombre ($codigo)"
}
