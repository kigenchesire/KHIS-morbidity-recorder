package com.google.codelab.sdclibrary

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var registerBtn: Button
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var haveAccountTextView: TextView
    private lateinit var emailErrorTextView: TextView
    private lateinit var passwordErrorTextView: TextView
    private lateinit var confirmPasswordErrorTextView: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        registerBtn = findViewById(R.id.registerBtn)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        haveAccountTextView = findViewById(R.id.haveAccountTextView)
        emailErrorTextView = findViewById(R.id.emailErrorTextView)
        passwordErrorTextView = findViewById(R.id.passwordErrorTextView)
        confirmPasswordErrorTextView = findViewById(R.id.confirmPasswordErrorTextView)
        progressBar = findViewById(R.id.progressBar)

        auth = FirebaseAuth.getInstance()

        haveAccountTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        registerBtn.setOnClickListener{
            if (emailEditText.text.isEmpty()){
                emailEditText.requestFocus()
                emailErrorTextView.visibility = View.VISIBLE
            }else if (passwordEditText.text.isEmpty()){
                passwordEditText.requestFocus()
                passwordErrorTextView.visibility = View.VISIBLE
            }else if(confirmPasswordEditText.text.isEmpty()){
                confirmPasswordEditText.requestFocus()
                passwordErrorTextView.visibility = View.VISIBLE
            }else {
                progressBar.visibility = View.VISIBLE
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            progressBar.visibility = View.GONE
                            Toast.makeText(baseContext, "Authentication successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, FacilityActivity::class.java))
                        } else {
                            // If sign in fails, display a message to the user.
                            progressBar.visibility = View.GONE
                            Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        }
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

