package com.bzrudski.nuptiallog

import android.util.Base64
import android.util.Log
import com.bzrudski.nuptiallog.management.AuthenticationCredential
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL
import kotlin.Exception

/**
 * Define common tasks for web interaction
 */
object WebInt {
    enum class HttpMethods {
        GET,POST,PUT,PATCH,DELETE
    }

    private const val TIMEOUT = 10_000
    private val LOG_TAG = WebInt::class.java.simpleName


    fun request(
        method:HttpMethods,
        url:URL,
        body:String?=null,
        authentication: AuthenticationCredential? = null,
        timeout: Int = TIMEOUT,
        headers:HashMap<String, String> = HashMap(),
        json:Boolean=true,
        callback: (status:Int, responseData:String) -> Unit,
        errorHandler: (Exception) -> Unit
    ){

        Log.d(LOG_TAG, "About to start web request with url $url")

        var urlConnection: HttpURLConnection? = null

        try {
            urlConnection = (url.openConnection() as HttpURLConnection)
            urlConnection.requestMethod = method.toString()

            for (headerKey in headers.keys){
                urlConnection.setRequestProperty(headerKey, headers[headerKey])
            }

            authentication?.let{
                urlConnection.setRequestProperty("Authorization", it.authorizationString)
            }

            if (json) urlConnection.setRequestProperty("Content-Type", "application/json")

            urlConnection.readTimeout = timeout

            if (method == HttpMethods.POST || method == HttpMethods.PUT){
                urlConnection.doOutput = true
                val outputStream = BufferedOutputStream(urlConnection.outputStream)
                val streamWriter = BufferedWriter(OutputStreamWriter(outputStream))

                val stringToWrite = body!!

                urlConnection.setFixedLengthStreamingMode(stringToWrite.toByteArray().size)

                streamWriter.write(stringToWrite)
                streamWriter.close()
            }

            urlConnection.connect()

            val inputStream = BufferedInputStream(urlConnection.inputStream)
            val reader = BufferedReader(InputStreamReader(inputStream))

            val contentList = reader.readLines()

            val contentBuilder = StringBuilder()

            for (line in contentList) {
                contentBuilder.append(line)
                contentBuilder.append("\n")
            }

            reader.close()

            val content = contentBuilder.toString()

            val statusCode = urlConnection.responseCode

            callback(statusCode, content)

        } catch (e: Exception) {
            e.printStackTrace()
            errorHandler(e)
        } finally {
            urlConnection?.disconnect()
        }
    }
}