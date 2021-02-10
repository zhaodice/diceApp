package org.mirai.zhao.dice.activity.ui.login

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
        val error: Int? = null
)