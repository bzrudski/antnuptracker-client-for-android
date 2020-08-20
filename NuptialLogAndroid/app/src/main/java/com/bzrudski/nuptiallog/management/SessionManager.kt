package com.bzrudski.nuptiallog.management

import android.app.Application
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.bzrudski.nuptiallog.WebInt
import com.bzrudski.nuptiallog.models.users.Device
import com.bzrudski.nuptiallog.models.users.Role
import com.bzrudski.nuptiallog.models.users.Session
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

object SessionManager {

    var session: Session? = null

    val isLoggedIn: Boolean get() = session != null

    // region DEVICE TOKEN
    // PUT IN DEVICE TOKEN... EVENTUALLY
    // endregion DEVICE TOKEN

    // region OBSERVER SETUP
    var loginObserver: LoginObserver? = null
    var logoutObserver: LogoutObserver? = null
    var verifyObserver: VerifySessionObserver? = null
    var sessionClearObserver: SessionClearObserver? = null

    //endregion OBSERVER SETUP

    // region LOGIN
    sealed class LoginError {
        object EmptyUsernameOrPassword : LoginError()
        object IncorrectCredentials : LoginError()
        object ForbiddenAccess : LoginError()
        object JsonParseError : LoginError()
        object NoResponse : LoginError()
        data class OtherLoginError(val status: Int) : LoginError()
    }

    interface LoginObserver {
        fun loggedIn(session: Session)
        fun loggedInWithError(error: LoginError)
    }

    data class LoginResponse(
        val token: String,
        @SerializedName("professional") val isProfessional: Boolean,
        val description: String,
        val institution: String,
        val deviceID: Long
    )

    fun login(username: String, password: String) {
        val url = UrlManager.getLoginURL()

        if (username.isEmpty() || password.isEmpty()) {
            loginObserver?.loggedInWithError(LoginError.EmptyUsernameOrPassword)
            return
        }

        // DEAL WITH DEVICE TOKENS
        val deviceInfo = Device.getInfoForDevice()
        val body = if (deviceInfo.isNotEmpty()) FlightAppManager.gson.toJson(deviceInfo) else null
        val credentials = BasicAuth(username, password)

        Thread {
            WebInt.request(
                WebInt.HttpMethods.POST,
                url,
                body,
                authentication = credentials,
                callback = { status, responseData ->

                    when (status) {
                        401 -> {
                            loginObserver?.loggedInWithError(LoginError.IncorrectCredentials)
                            return@request
                        }
                        403 -> {
                            loginObserver?.loggedInWithError(LoginError.ForbiddenAccess)
                            return@request
                        }
                        200 -> {
                        }
                        else -> {
                            loginObserver?.loggedInWithError(LoginError.OtherLoginError(status))
                            return@request
                        }
                    }

                    try {
                        val transaction =
                            FlightAppManager.gson.fromJson(responseData, LoginResponse::class.java)

                        Device.deviceID = transaction.deviceID

                        val session = Session(
                            username,
                            transaction.isProfessional,
                            transaction.description,
                            transaction.institution,
                            transaction.token
                        )

                        this.session = session
                        loginObserver?.loggedIn(session)

                    } catch (e: JsonSyntaxException) {
                        e.printStackTrace()
                        loginObserver?.loggedInWithError(LoginError.JsonParseError)
                    }
                },
                errorHandler = {
                    loginObserver?.loggedInWithError(LoginError.NoResponse)
                })
        }.start()
    }
    // endregion LOGIN

    // region LOGOUT
    sealed class LogoutError {
        object AuthError : LogoutError()
        object NoResponse : LogoutError()
        data class OtherLogoutError(val status: Int) : LogoutError()
    }

    interface LogoutObserver {
        fun loggedOut()
        fun loggedOutWithError(error: LogoutError)
    }

    fun logout(session: Session){
        val url = UrlManager.getLogoutURL()

        Thread {
            WebInt.request(
                WebInt.HttpMethods.POST,
                url,
                authentication = session.authentication,
                callback = { status, responseData ->

                    if (status == 401 || status == 403) {
                        logoutObserver?.loggedOutWithError(LogoutError.AuthError)
                        return@request
                    }

                    if (status != 200 && status != 204) {
                        logoutObserver?.loggedOutWithError(LogoutError.OtherLogoutError(status))
                        return@request
                    }

                    logoutObserver?.loggedOut()
                    clearSession()
                },
                errorHandler = {
                    logoutObserver?.loggedOutWithError(LogoutError.NoResponse)
                })
        }.start()
    }
    // endregion LOGOUT

    // region VERIFY
    sealed class VerificationError {
        object NoResponse: VerificationError()
        object InvalidCredentials: VerificationError()
        object JsonError: VerificationError()
        data class OtherVerificationError(val status: Int): VerificationError()
    }

    interface VerifySessionObserver {
        fun sessionVerified(session: Session, isValid: Boolean, responseData: Map<String, String?>?)
        fun sessionVerifiedWithError(error: VerificationError)
    }

    fun verify(session: Session) {
        val url = UrlManager.getVerifyURL()
        val headers = HashMap<String, String>()
        headers["deviceID"] = Device.deviceID.toString()

        Thread {
            WebInt.request(
                WebInt.HttpMethods.POST,
                url,
                authentication = session.authentication,
                headers = headers,
                callback = { status, responseData ->

                    if (status == 401) {
                        // CLEAR CREDENTIALS
                        verifyObserver?.sessionVerified(session, false, null)
                        return@request
                    }

                    if (status != 200) {
                        verifyObserver?.sessionVerifiedWithError(
                            VerificationError.OtherVerificationError(
                                status
                            )
                        )
                        return@request
                    }

                    try {
                        val typeToken = object : TypeToken<HashMap<String, String>>() {}.type
                        val responseDictionary =
                            FlightAppManager.gson.fromJson<HashMap<String, String>>(
                                responseData,
                                typeToken
                            )
                        this.session = session
                        verifyObserver?.sessionVerified(session, true, responseDictionary)
                    } catch (e: JsonSyntaxException) {
                        verifyObserver?.sessionVerifiedWithError(VerificationError.JsonError)
                    }

                },
                errorHandler = {
                    verifyObserver?.sessionVerifiedWithError(VerificationError.NoResponse)
                })
        }.start()
    }
    // endregion VERIFY

    // region CREDENTIAL STORAGE
    fun saveCredentials(context: Context){

        if (session == null){
            return
        }

        val masterKey = MasterKey.Builder(context).build()
        val filename = "CRED_STORE"

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            filename,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val editor = sharedPreferences.edit()

        editor.putString(Session.USERNAME_KEY, session!!.username)
            .putString(Session.INSTITUTION_KEY, session!!.institution)
            .putString(Session.DESCRIPTION_KEY, session!!.description)
            .putBoolean(Session.PROFESSIONAL_KEY, (session!!.role == Role.MYRMECOLOGIST))
            .putLong(Device.DEVICE_ID_KEY, Device.deviceID)
            .apply()
    }

    fun loadCredentials(context: Context){
        val masterKey = MasterKey.Builder(context).build()
        val filename = "CRED_STORE"

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            filename,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val username = sharedPreferences.getString(Session.USERNAME_KEY, null)
        val token = sharedPreferences.getString(Session.TOKEN_KEY, null)
        val isProfessional = sharedPreferences.getBoolean(Session.PROFESSIONAL_KEY, false)
        val description = sharedPreferences.getString(Session.DESCRIPTION_KEY, null)
        val institution = sharedPreferences.getString(Session.INSTITUTION_KEY, null)

        val deviceID = sharedPreferences.getLong(Device.DEVICE_ID_KEY, -1)

        if (username.isNullOrBlank() || token.isNullOrBlank() || description == null || institution == null || deviceID == (-1).toLong()){
            clearCredentials(context)
            return
        }

        Device.deviceID = deviceID

        val session = Session(
            username = username,
            token = token,
            isProfessional = isProfessional,
            description = description,
            institution = institution
        )

        verify(session)

    }

    fun clearCredentials(context: Context){

        val masterKey = MasterKey.Builder(context).build()
        val filename = "CREDSTORE"

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            filename,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val editor = sharedPreferences.edit()

        editor.remove(Session.USERNAME_KEY)
        editor.remove(Session.TOKEN_KEY)
        editor.remove(Session.PROFESSIONAL_KEY)
        editor.remove(Session.DESCRIPTION_KEY)
        editor.remove(Session.INSTITUTION_KEY)
        editor.remove(Device.DEVICE_ID_KEY)

        editor.apply()
    }

    // endregion CREDENTIAL STORAGE

    // region CLEAR SESSION

    interface SessionClearObserver {
        fun sessionCleared()
    }

    fun clearSession(){
        session = null
    }

    // endregion CLEAR SESSION

}