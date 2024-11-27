package com.example.sezin.user.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.sezin.MainActivity
import com.example.sezin.R
import com.example.sezin.user.AuthUtils
import com.example.sezin.user.login.LoginActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = true
        setContentView(R.layout.activity_register)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        val signupButton = findViewById<Button>(R.id.signup_button)
        val loginText = findViewById<TextView>(R.id.login_prompt)

        val emailOrPhone = findViewById<EditText>(R.id.email_or_phone)
        val password = findViewById<EditText>(R.id.password)
        val confirmPassword = findViewById<EditText>(R.id.confirm_password)

        // Наблюдатели для включения кнопки регистрации
        emailOrPhone.addTextChangedListener { validateFields(emailOrPhone, password, confirmPassword, signupButton) }
        password.addTextChangedListener { validateFields(emailOrPhone, password, confirmPassword, signupButton) }
        confirmPassword.addTextChangedListener { validateFields(emailOrPhone, password, confirmPassword, signupButton) }

        // Обработка кнопки регистрации
        signupButton.setOnClickListener {
            val emailOrPhoneInput = emailOrPhone.text.toString().trim()
            val passwordInput = password.text.toString().trim()
            val confirmPasswordInput = confirmPassword.text.toString().trim()

            if (!areFieldsValid(emailOrPhoneInput, passwordInput, confirmPasswordInput)) {
                showToast("Please enter all fields")
                return@setOnClickListener
            }

            if (passwordInput != confirmPasswordInput) {
                showToast("Passwords do not match")
                return@setOnClickListener
            }

            when {
                Patterns.EMAIL_ADDRESS.matcher(emailOrPhoneInput).matches() -> {
                    registerWithEmail(emailOrPhoneInput, passwordInput)
                }
                Patterns.PHONE.matcher(emailOrPhoneInput).matches() -> {
                    registerWithPhone(emailOrPhoneInput)
                }
                else -> {
                    showToast("Please enter a valid email or phone number")
                }
            }
        }

        // Обработка ссылки "Уже есть аккаунт? Войти"
        loginText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    // Регистрация через email
    private fun registerWithEmail(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    saveUserData(user)
                } else {
                    logError(task.exception, "Authentication failed")
                    showToast("Authentication failed: ${task.exception?.message}")
                }
            }
    }

    // Регистрация через номер телефона
    private fun registerWithPhone(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(phoneAuthCallbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // Callback для аутентификации через номер телефона
    private val phoneAuthCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            logError(e, "Verification failed")
            showToast("Verification failed: ${e.message}")
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            showToast("Code sent to your phone number")
            promptForVerificationCode(verificationId)
        }
    }

    // Подтверждение кода из SMS
    private fun promptForVerificationCode(verificationId: String) {
        val input = EditText(this).apply {
            hint = "Enter verification code"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        AlertDialog.Builder(this)
            .setTitle("Verification Code")
            .setView(input)
            .setPositiveButton("Verify") { _, _ ->
                val code = input.text.toString().trim()
                if (code.isNotEmpty()) {
                    val credential = PhoneAuthProvider.getCredential(verificationId, code)
                    signInWithPhoneCredential(credential)
                } else {
                    showToast("Code cannot be empty")
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    // Вход с подтвержденными учетными данными
    private fun signInWithPhoneCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    saveUserData(user)
                } else {
                    logError(task.exception, "Sign-in failed")
                    showToast("Sign-in failed: ${task.exception?.message}")
                }
            }
    }

    private fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                } else {
                    showToast("Google sign in failed: ${task.exception?.message}")
                }
            }
    }

    // Сохранение данных пользователя в Firestore
    private fun saveUserData(user: FirebaseUser?) {
        user?.let {
            val userData = mapOf(
                "email" to (user.email ?: ""),
                "phoneNumber" to (user.phoneNumber ?: "")
            )

            db.collection("users").document(user.uid)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener {
                    updateUI(user)
                    showToast("User data saved successfully")
                }
                .addOnFailureListener { e ->
                    logError(e, "Error saving user data")
                    showToast("Error saving user data: ${e.message}")
                }
        } ?: run {
            showToast("User is not authenticated")
        }
    }

    // Обновление UI
    private fun updateUI(user: FirebaseUser?) {
        if (user == null) {
            showToast("Authentication failed")
            return
        }

        AuthUtils.setLoggedIn(this)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    // Проверка, что поля заполнены
    private fun validateFields(
        emailOrPhone: EditText,
        password: EditText,
        confirmPassword: EditText,
        signupButton: Button
    ) {
        signupButton.isEnabled = emailOrPhone.text.isNotEmpty() &&
                password.text.isNotEmpty() &&
                confirmPassword.text.isNotEmpty()
    }

    // Проверка валидации полей
    private fun areFieldsValid(vararg fields: String): Boolean {
        return fields.none { it.isEmpty() }
    }

    // Показать сообщение Toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Логирование ошибок
    private fun logError(e: Exception?, message: String) {
        FirebaseCrashlytics.getInstance().recordException(e ?: Exception("Unknown error"))
        Log.e("RegisterActivity", message, e)
    }
}