package com.bzrudski.nuptiallog.management

import com.bzrudski.nuptiallog.WebInt
import com.bzrudski.nuptiallog.models.users.Session

object SessionManager {

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
        object EmptyUsernameOrPassword: LoginError()
        object IncorrectCredentials: LoginError()
        object JsonParseError: LoginError()
        object NoResponse: LoginError()
        data class OtherLoginError(val status:Int): LoginError()
    }

    interface LoginObserver {
        fun loggedIn(session: Session)
        fun loggedInWithError(error: LoginError)
    }

    fun login(username: String, password: String, deviceInfo: Map<String, String?> = HashMap()){
        val url = UrlManager.getLoginURL()

        if (username.isEmpty() || password.isEmpty()){
            loginObserver?.loggedInWithError(LoginError.EmptyUsernameOrPassword)
            return
        }

        // DEAL WITH DEVICE TOKENS

        val body = if (deviceInfo.isNotEmpty()) FlightAppManager.gson.toJson(deviceInfo) else null
        val credentials = BasicAuth(username, password)

        WebInt.request(WebInt.HttpMethods.POST, url, body, authentication = credentials, callback = {
            status, responseData ->
        }, errorHandler = {

        })

    }
    // endregion LOGIN

    // region LOGOUT
    sealed class LogoutError {
        object AuthError: LogoutError()
        object NoResponse: LogoutError()
        data class OtherLogoutError(val status:Int): LogoutError()
    }

    interface LogoutObserver {
        fun loggedOut()
        fun loggedOutWithError(error: LogoutError)
    }
    // endregion LOGOUT

    // region VERIFY
    sealed class VerificationError {
        object NoResponse
        object InvalidCredentials
        object JsonError
        data class OtherVerificationError(val status: Int)
    }

    interface VerifySessionObserver {
        fun sessionVerified(session: Session, isValid: Boolean, responseData: Map<String, String?>?)
        fun sessionVerifiedWithError(error: VerificationError)
    }
    // endregion VERIFY

    // region CLEAR SESSION

    interface SessionClearObserver {
        fun sessionCleared()
    }

    // endregion CLEAR SESSION

}