package com.example.buddybloom

class User(
    val id : String,
    val email : String,
    val name : String = "",
    val userPlants : MutableList<Plant> = mutableListOf()
) {
}