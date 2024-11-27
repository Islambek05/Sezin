package com.example.sezin.user.login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.sezin.MainActivity
import com.example.sezin.R
import com.example.sezin.user.AuthUtils
import com.example.sezin.user.register.RegisterActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Инициализация Firebase Auth
        auth = Firebase.auth

        val emailOrPhone = findViewById<EditText>(R.id.email_or_phone)
        val passwordField = findViewById<EditText>(R.id.password)
        val otpField = findViewById<EditText>(R.id.otp_code)
        val loginButton = findViewById<Button>(R.id.login_button)
        val sendOtpButton = findViewById<Button>(R.id.send_otp_button)
        val signupText = findViewById<TextView>(R.id.signup_prompt)

        // Скрыть поля по умолчанию
        passwordField.visibility = View.GONE
        otpField.visibility = View.GONE
        sendOtpButton.visibility = View.GONE

        // Слушатель изменений текста
        emailOrPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString().trim()

                when {
                    Patterns.EMAIL_ADDRESS.matcher(input).matches() -> {
                        // Показывать поле для пароля, если введен email
                        passwordField.visibility = View.VISIBLE
                        otpField.visibility = View.GONE
                        sendOtpButton.visibility = View.GONE
                    }
                    Patterns.PHONE.matcher(input).matches() -> {
                        // Показывать поле для ввода кода и кнопку "Отправить код", если введен телефон
                        passwordField.visibility = View.GONE
                        otpField.visibility = View.VISIBLE
                        sendOtpButton.visibility = View.VISIBLE
                    }
                    else -> {
                        // Скрывать все дополнительные поля, если формат неверный
                        passwordField.visibility = View.GONE
                        otpField.visibility = View.GONE
                        sendOtpButton.visibility = View.GONE
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Обработка кнопки входа
        loginButton.setOnClickListener {
            val input = emailOrPhone.text.toString().trim()

            if (input.isEmpty()) {
                showToast("Please enter email or phone")
                return@setOnClickListener
            }

            if (Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
                val password = passwordField.text.toString().trim()
                if (password.isEmpty()) {
                    showToast("Please enter your password")
                    return@setOnClickListener
                }
                signInWithEmail(input, password)
            } else if (Patterns.PHONE.matcher(input).matches()) {
                val otpCode = otpField.text.toString().trim()
                if (otpCode.isEmpty()) {
                    showToast("Please enter the OTP code")
                    return@setOnClickListener
                }
                signInWithPhone(otpCode)
            } else {
                showToast("Invalid email or phone number")
            }
        }

        // Отправка OTP-кода
        sendOtpButton.setOnClickListener {
            val phoneNumber = emailOrPhone.text.toString().trim()

            if (phoneNumber.isEmpty() || !Patterns.PHONE.matcher(phoneNumber).matches()) {
                showToast("Please enter a valid phone number")
                return@setOnClickListener
            }

            sendOtp(phoneNumber)
        }

        // Переход к экрану регистрации
        signupText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // Вход через email
    private fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToMainActivity()
                } else {
                    showToast("Password incorrect")
                }
            }
    }

    // Отправка OTP-кода
    private fun sendOtp(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(phoneAuthCallbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // Callback для обработки OTP
    private val phoneAuthCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            showToast("Verification failed: ${e.message}")
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            this@LoginActivity.verificationId = verificationId
            showToast("OTP sent to your phone number")
        }
    }

    // Вход с помощью OTP-кода
    private fun signInWithPhone(otpCode: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId ?: "", otpCode)
        signInWithPhoneCredential(credential)
    }

    private fun signInWithPhoneCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToMainActivity()
                } else {
                    showToast("Login failed: ${task.exception?.message}")
                }
            }
    }

    // Переход в MainActivity
    private fun navigateToMainActivity() {
        AuthUtils.setLoggedIn(this)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Уведомление Toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
