package com.bzrudski.nuptiallog

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.bzrudski.nuptiallog.management.SessionManager
import com.bzrudski.nuptiallog.management.UrlManager
import com.bzrudski.nuptiallog.models.users.Session
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity(), SessionManager.LoginObserver {

    companion object {
        private val LOG_TAG = LoginActivity::class.java.simpleName
    }

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameEditText = (findViewById<TextInputLayout>(R.id.username_field)).editText!!
        passwordEditText = (findViewById<TextInputLayout>(R.id.password_field)).editText!!

        SessionManager.loginObserver = this
    }

    interface LoginScreenObserver {
        fun loginScreenHasLoggedIn()
    }

    private var observer: LoginScreenObserver? = null

    private fun dismissKeyboard(view: View){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager

        inputManager?.let {
            it.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    fun triggerLogin(view: View) {
        dismissKeyboard(view)

        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()

        Log.d(LOG_TAG, "About to begin logging in...")
        SessionManager.login(username, password)
    }

    fun launchCreateAccount(view: View) {
        dismissKeyboard(view)

        val createAccountUrl = Uri.parse(UrlManager.getCreateAccountURL().toString())
        val intent = Intent(Intent.ACTION_VIEW, createAccountUrl)

        if (intent.resolveActivity(packageManager) != null) {
            Log.d(LOG_TAG, "About to launch the web browser to create account")
            startActivity(intent)
        } else {
            Log.d(LOG_TAG, "Unable to launch web browser to create account")
        }

    }
    fun launchForgotPassword(view: View) {
        dismissKeyboard(view)

        val createAccountUrl = Uri.parse(UrlManager.getPasswordResetURL().toString())
        val intent = Intent(Intent.ACTION_VIEW, createAccountUrl)

        if (intent.resolveActivity(packageManager) != null) {
            Log.d(LOG_TAG, "About to launch the web browser to reset password")
            startActivity(intent)
        } else {
            Log.d(LOG_TAG, "Unable to launch web browser to reset password")
        }
    }

    override fun loggedIn(session: Session) {
        Log.d(LOG_TAG, "Successfully logged in!")
        setResult(RESULT_OK)
        finish()
    }

    override fun loggedInWithError(error: SessionManager.LoginError) {
        Log.d(LOG_TAG, "Error logging in:")

        val alertTitle: String
        val alertMessage: String

        val okAction: (DialogInterface, Int) -> Unit = {
            dialogInterface: DialogInterface, which: Int ->
        }

        val okText = getString(R.string.ok)


        var negativeAction: (DialogInterface, Int) -> Unit = {_, _ ->}
        var negativeText: String? = null
        var positiveAction: (DialogInterface, Int) -> Unit = {_, _ ->}
        var positiveText: String? = null

        when (error) {
            SessionManager.LoginError.EmptyUsernameOrPassword -> {
                Log.d(LOG_TAG, "Empty username or password")
                alertTitle = getString(R.string.empty_user_pass_title)
                alertMessage = getString(R.string.empty_user_pass_message)

                negativeText = okText
                negativeAction = okAction
            }
            SessionManager.LoginError.IncorrectCredentials -> {
                Log.d(LOG_TAG, "Incorrect credentials")
                alertTitle = getString(R.string.incorrect_credentials_title)
                alertMessage = getString(R.string.incorrect_credentials_message)

                negativeText = okText
                negativeAction = okAction
            }
            SessionManager.LoginError.ForbiddenAccess -> {
                Log.d(LOG_TAG, "Forbidden Access")
                alertTitle = getString(R.string.forbidden_access_title)
                alertMessage = getString(R.string.forbidden_access_message)

                negativeText = okText
                negativeAction = okAction
            }
            SessionManager.LoginError.JsonParseError -> {
                Log.d(LOG_TAG, "Error parsing response")
                alertTitle = getString(R.string.json_parse_title)
                alertMessage = getString(R.string.json_parse_message)

                negativeText = okText
                negativeAction = okAction
            }
            SessionManager.LoginError.NoResponse -> {
                Log.d(LOG_TAG, "No response from server")
                alertTitle = getString(R.string.no_response_title)
                alertMessage = getString(R.string.no_response_message)

                negativeText = okText
                negativeAction = okAction

                positiveText = getString(R.string.try_again)
                positiveAction = {
                    dialogInterface, which ->
                    triggerLogin(findViewById(R.id.login_button))
                }
            }
            is SessionManager.LoginError.OtherLoginError -> {
                Log.d(LOG_TAG, "Other login error (${error.status})")
                alertTitle = getString(R.string.other_login_error_title)
                alertMessage = getString(R.string.other_login_error_message, error.status)

                negativeText = okText
                negativeAction = okAction
            }
        }

        runOnUiThread {
            val alertDialog = AlertDialog.Builder(this).also {
                it.setTitle(alertTitle)
                it.setMessage(alertMessage)
                it.setNegativeButton(negativeText, negativeAction)

                if (positiveText != null){
                    it.setPositiveButton(positiveText, positiveAction)
                }

            }.show()
        }
    }
}