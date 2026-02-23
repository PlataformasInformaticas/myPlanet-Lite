/**
 * Author: Walfre López Prado
 * Email: loppra@plataformasinformaticas.com
 * Creation date: 2025-11-17
 */

package com.pi.ole.myplanet.lite.profile

import android.content.Context

/**
 * Reads the credentials that were persisted after login so the profile screen can refresh data.
 */
object ProfileCredentialsStore {
    private const val PREFS_NAME = "server_preferences"
    private const val KEY_REMEMBERED_USERNAME = "remembered_username"
    private const val KEY_REMEMBERED_PASSWORD = "remembered_password"
    @Volatile
    private var sessionCredentials: StoredCredentials? = null

    fun setSessionCredentials(credentials: StoredCredentials?) {
        sessionCredentials = credentials
    }

    fun getStoredCredentials(context: Context): StoredCredentials? {
        sessionCredentials?.let { return it }
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val username = prefs.getString(KEY_REMEMBERED_USERNAME, null)?.takeIf { it.isNotBlank() }
        val password = prefs.getString(KEY_REMEMBERED_PASSWORD, null)?.takeIf { it.isNotBlank() }
        return if (username != null && password != null) {
            StoredCredentials(username, password)
        } else {
            null
        }
    }
}

data class StoredCredentials(val username: String, val password: String)
