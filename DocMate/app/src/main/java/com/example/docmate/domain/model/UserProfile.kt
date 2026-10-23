package com.example.docmate.domain.model

data class UserProfile(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val location: String = "",
    val isPremium: Boolean = false
) {
    /** Accessor for Data Binding to get initials. */
    val avatarInitials: String get() = initials()

    /** Generates initials from the full name (e.g., "Sarah Mitchell" -> "SM"). */
    fun initials(): String {
        return fullName.trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
    }
}
