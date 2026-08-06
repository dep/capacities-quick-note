package com.dnnypck.capacitiesquicknotepro.data.network

import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

suspend fun postToTarget(
    url: String,
    headers: Map<String, String>,
    body: String
): Result<String> = withContext(Dispatchers.IO) {
    try {
        val response = KtorHttpClient.client.post(url) {
            headers {
                headers.forEach { (key, value) ->
                    append(key, value)
                }
            }
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        if (response.status.value in 200..299) {
            Result.success(response.bodyAsText())
        } else {
            val errorBody = response.bodyAsText()
            Result.failure(Exception(friendlyHttpError(response.status.value, response.status.description, errorBody)))
        }
    } catch (e: Exception) {
        Result.failure(Exception(friendlyNetworkError(e), e))
    }
}

/**
 * Turns an HTTP status code into a clear, actionable message so the user knows
 * exactly what went wrong and how to fix it, while still including the raw
 * response from Capacities for troubleshooting.
 */
private fun friendlyHttpError(status: Int, description: String, responseBody: String): String {
    val explanation = when (status) {
        400 -> "Bad request (400). The note or space details were rejected by Capacities. Double-check your Space ID in Settings."
        401 -> "Unauthorized (401). Your API key is missing, invalid, or expired. Open Settings and paste a fresh API key from Capacities → Settings → Capacities API."
        403 -> "Forbidden (403). This API key doesn't have permission for this space. Confirm the key and Space ID belong to the same account."
        404 -> "Not found (404). The Space ID appears to be incorrect. Check it in Capacities → Settings → Space settings and update it in the app."
        413 -> "Note too large (413). Try sending a shorter note."
        429 -> "Rate limited (429). You've made too many requests. Wait a moment and try again."
        in 500..599 -> "Capacities server error ($status). This is a problem on Capacities' side, not your settings. Please try again in a little while."
        else -> "Request failed ($status ${description.ifBlank { "" }}).".trim()
    }

    return buildString {
        append(explanation)
        val trimmedBody = responseBody.trim()
        if (trimmedBody.isNotEmpty()) {
            append("\n\nServer response: ")
            append(trimmedBody)
        }
    }
}

/**
 * Turns a low-level network exception into a plain-language explanation.
 */
private fun friendlyNetworkError(e: Throwable): String {
    return when (e) {
        is UnknownHostException ->
            "Can't reach Capacities. Check your internet connection and try again."
        is ConnectException ->
            "Couldn't connect to Capacities. Check your internet connection and try again."
        is SocketTimeoutException ->
            "The request timed out. Your connection may be slow — please try again."
        else ->
            "Something went wrong while sending your note: ${e.message ?: e.javaClass.simpleName}"
    }
}
