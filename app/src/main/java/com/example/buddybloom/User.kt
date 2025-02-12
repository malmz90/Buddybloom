package com.example.buddybloom

data class User(
    var id: String = "",
    var email: String = "",
    var name: String = "",
    var userPlants: MutableList<Plant> = mutableListOf()
) {
    // Required no-argument constructor for Firebase
    constructor() : this("", "", "", mutableListOf())
}
