package com.google.codelab.sdclibrary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()

        Handler(Looper.getMainLooper()).postDelayed({
            checkUserStatusAndRedirect()
        }, 3000) //3 seconds
    }

    private fun checkUserStatusAndRedirect() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this, GetStartedActivity::class.java)
            startActivity(intent)
        }
        finish()
    }
}