package com.example.sezin.user.register

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sezin.MainActivity
import com.example.sezin.R
import com.example.sezin.serversezin.DatabaseHelper
import com.example.sezin.user.User
import com.example.sezin.user.login.LoginActivity
import com.example.sezin.user.AuthUtils

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val signupButton = findViewById<Button>(R.id.signup_button)
        val loginText = findViewById<TextView>(R.id.login_prompt)

        signupButton.setOnClickListener {
            val firstname = findViewById<EditText>(R.id.firstname).text.toString().trim()
            val lastname = findViewById<EditText>(R.id.lastname).text.toString().trim()
            val email = findViewById<EditText>(R.id.email).text.toString().trim()
            val phoneNumber = findViewById<EditText>(R.id.phone_number).text.toString().trim()
            val password = findViewById<EditText>(R.id.password).text.toString().trim()
            val confirmPassword = findViewById<EditText>(R.id.confirm_password).text.toString().trim()

            if (password.isEmpty() || confirmPassword.isEmpty() || firstname.isEmpty() || lastname.isEmpty() || email.isEmpty() || phoneNumber.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password == confirmPassword) {
                val user = User(firstname, lastname, email, phoneNumber, password.hashCode())
                val dbHelper = DatabaseHelper()

                if (dbHelper.isUserExists(email, phoneNumber)) {
                    Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (dbHelper.saveUser(user)) {
                    AuthUtils.setLoggedIn(this, true)
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("openProfile", true)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Failed to save user", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }


        loginText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
