package org.ole.planet.myplanet.lite.network

import org.ole.planet.myplanet.lite.profile.StoredCredentials
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

abstract class BaseRepository(
    protected val client: OkHttpClient = NetworkClient.okHttpClient,
    protected val moshi: Moshi = NetworkClient.moshi
) {
    protected val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    protected fun normalizeUrl(baseUrl: String): String {
        val normalized = baseUrl.trim().trimEnd('/')
        if (normalized.isEmpty()) {
            throw IOException("Missing server base URL")
        }
        return normalized
    }

    protected fun Request.Builder.addAuth(
        credentials: StoredCredentials?,
        sessionCookie: String?
    ): Request.Builder {
        credentials?.let {
            addHeader("Authorization", Credentials.basic(it.username, it.password))
        }
        sessionCookie?.takeIf { it.isNotBlank() }?.let {
            addHeader("Cookie", it)
        }
        return this
    }

    protected fun <T> T.toJsonRequestBody(adapter: JsonAdapter<T>): RequestBody {
        return adapter.toJson(this).toRequestBody(JSON_MEDIA_TYPE)
    }
}
