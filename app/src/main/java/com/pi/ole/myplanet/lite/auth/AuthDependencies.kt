/**
 * Author: Walfre López Prado
 * Email: loppra@plataformasinformaticas.com
 * Creation date: 2025-11-07
 */

package com.pi.ole.myplanet.lite.auth

import android.content.Context
import com.pi.ole.myplanet.lite.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object AuthDependencies {
    @Volatile
    private var authServiceOverride: AuthService? = null

    fun provideAuthService(context: Context, baseUrl: String = BuildConfig.PLANET_BASE_URL): AuthService {
        return authServiceOverride ?: createAuthService(context.applicationContext, baseUrl)
    }

    fun overrideAuthService(service: AuthService?) {
        authServiceOverride = service
    }

    private fun createAuthService(context: Context, baseUrl: String): AuthService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(client)
            .build()

        val tokenStorage = SecureTokenStorage(context)
        val api = retrofit.create(AuthApi::class.java)
        return NetworkAuthService(api, tokenStorage)
    }
}
