package com.google.codelab.sdclibrary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var loginBtn: Button
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var noAccountTextView: TextView
    private lateinit var emailErrorTextView: TextView
    private lateinit var passwordErrorTextView: TextView
    private lateinit var progressBar2: ProgressBar

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginBtn = findViewById(R.id.loginBtn)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        noAccountTextView = findViewById(R.id.noAccountTextView)
        passwordErrorTextView = findViewById(R.id.passwordForgotTextView)
        emailErrorTextView = findViewById(R.id.emailErrorTextView)
        progressBar2 = findViewById(R.id.progressBar2)

        auth = FirebaseAuth.getInstance()

        noAccountTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        loginBtn.setOnClickListener{
            if (emailEditText.text.isEmpty()){
                emailEditText.requestFocus()
                emailErrorTextView.visibility = View.VISIBLE
            }else if (passwordEditText.text.isEmpty()){
                passwordEditText.requestFocus()
                passwordErrorTextView.visibility = View.VISIBLE
            }else{
                progressBar2.visibility = View.VISIBLE
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            progressBar2.visibility = View.GONE
                            startActivity(Intent(this, MainActivity::class.java))
                        } else {
                            progressBar2.visibility = View.GONE
                            Toast.makeText(baseContext,"Authentication failed.",Toast.LENGTH_SHORT,).show()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(baseContext, exception.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}