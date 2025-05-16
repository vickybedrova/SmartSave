package com.example.smartsave.data

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREFS_NAME = "smartsave_prefs"
    private const val KEY_SESSION_TOKEN = "session_token"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_TOKEN_TYPE = "token_type" // e.g., "Bearer"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveTokens(context: Context, sessionId: String, accessToken: String, tokenType: String) {
        val editor = getPreferences(context).edit()
        editor.putString(KEY_SESSION_TOKEN, sessionId)
        editor.putString(KEY_ACCESS_TOKEN, accessToken)
        editor.putString(KEY_TOKEN_TYPE, tokenType)
        editor.apply()
    }

    fun getSessionToken(context: Context): String? {
        return getPreferences(context).getString(KEY_SESSION_TOKEN, null)
    }

    fun getAccessToken(context: Context): String? {
        return getPreferences(context).getString(KEY_ACCESS_TOKEN, null)
    }

    fun getTokenType(context: Context): String? {
        return getPreferences(context).getString(KEY_TOKEN_TYPE, "Bearer") // Default to Bearer
    }

    fun getAuthorizationHeader(context: Context): String? {
        val token = getAccessToken(context)
        val type = getTokenType(context)
        return if (token != null && type != null) {
            "$type $token"
        } else {
            null
        }
    }

    fun clearTokens(context: Context) {
        val editor = getPreferences(context).edit()
        editor.remove(KEY_SESSION_TOKEN)
        editor.remove(KEY_ACCESS_TOKEN)
        editor.remove(KEY_TOKEN_TYPE)
        editor.apply()
    }

    fun hasTokens(context: Context): Boolean {
        return getSessionToken(context) != null && getAccessToken(context) != null
    }
}