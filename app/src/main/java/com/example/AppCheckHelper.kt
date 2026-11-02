package com.example

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory

private const val TAG = "AppCheckInit"

private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"

fun ensureFirebaseInitialized(context: Context) {
    try {
        if (FirebaseApp.getApps(context).isEmpty()) {
            try {
                FirebaseApp.initializeApp(context)
            } catch (e: Throwable) {
                Log.w(TAG, "Standard Firebase initialization failed, setting fallback options: ${e.message}")
                try {
                    val options = FirebaseOptions.Builder()
                        .setApplicationId(context.packageName)
                        .setApiKey("AIzaSyMeroBazarDefaultSafeKey12345")
                        .setProjectId("mero-bazar-app")
                        .build()
                    FirebaseApp.initializeApp(context, options)
                    Log.d(TAG, "Initialized FirebaseApp with fallback options successfully.")
                } catch (ex: Throwable) {
                    Log.w(TAG, "Fallback Firebase initialization failed: ${ex.message}")
                }
            }
        }
    } catch (e: Throwable) {
        Log.e(TAG, "Error ensuring FirebaseApp is initialized", e)
    }
}

fun configureAppCheck(context: Context, intent: Intent?) {
    try {
        ensureFirebaseInitialized(context)

        if (FirebaseApp.getApps(context).isEmpty()) {
            Log.w(TAG, "No FirebaseApp registered, skipping App Check.")
            return
        }

        if (BuildConfig.DEBUG) {
            val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)

            if (!debugToken.isNullOrEmpty()) {
                try {
                    val persistenceKey = FirebaseApp.getInstance().persistenceKey
                    val prefs = context.getSharedPreferences(
                        "$DEBUG_STORE_PREFIX$persistenceKey",
                        Context.MODE_PRIVATE
                    )
                    prefs.edit()
                        .putString(DEBUG_SECRET_KEY, debugToken)
                        .commit()
                    Log.d(TAG, "App Check debug secret configured successfully.")
                } catch (e: Throwable) {
                    Log.w(TAG, "Failed to store App Check debug token: ${e.message}")
                }
            } else {
                Log.w(TAG, "No debug token found in Intent extra.")
            }

            try {
                FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
                Log.d(TAG, "DebugAppCheckProviderFactory installed.")
            } catch (e: Throwable) {
                Log.w(TAG, "Failed to install DebugAppCheckProviderFactory: ${e.message}")
            }
        } else {
            val resId = context.resources.getIdentifier(RECAPTCHA_SITE_KEY_RES, "string", context.packageName)
            if (resId != 0) {
                val siteKey = context.getString(resId)
                if (siteKey.isNotBlank()) {
                    try {
                        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                            RecaptchaAppCheckProviderFactory.getInstance(siteKey)
                        )
                        Log.d(TAG, "RecaptchaAppCheckProviderFactory installed.")
                    } catch (e: Throwable) {
                        Log.w(TAG, "Failed to install RecaptchaAppCheckProviderFactory: ${e.message}")
                    }
                } else {
                    Log.e(TAG, "recaptcha_site_key resource string is blank.")
                }
            } else {
                Log.e(TAG, "recaptcha_site_key string resource not found in package.")
            }
        }
    } catch (e: Throwable) {
        Log.e(TAG, "Failed to initialize Firebase App Check safely", e)
    }
}
