package com.example.mustardseedpasswordmanager.data

data class PasswordEntry(
    val website: String,
    val username: String,
    val password: String
) {
    /*fun getPassword(): String {
        // For simplicity, use MD5 hashing (not recommended for real-world use)
        val bytes = MessageDigest.getInstance("MD5").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }*/
}
