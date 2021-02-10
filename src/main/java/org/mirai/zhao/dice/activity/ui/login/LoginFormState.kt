package org.mirai.zhao.dice.activity.ui.login

/**
 * Data validation state of the login form.
 */
data class LoginFormState(val usernameError: Int? = null,
                          val passwordError: Int? = null,
                          val serverResult: String? =null,
                          val isDataValid: Boolean = false)