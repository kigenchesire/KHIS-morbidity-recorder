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
import com.google.codelab.sdclibrary.entity.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.logging.Logger

class FacilityActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var nameErrorTextView: TextView
    private lateinit var facilityEditText: EditText
    private lateinit var facilityErrorTextView: TextView
    private lateinit var saveBtn: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_facility)

        nameEditText = findViewById(R.id.nameEditText)
        nameErrorTextView = findViewById(R.id.nameErrorTextView)
        facilityEditText = findViewById(R.id.facilityEditText)
        facilityErrorTextView = findViewById(R.id.facilityErrorTextView)

        auth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.getReference("users")

        saveBtn.setOnClickListener{
            if (nameEditText.text.isEmpty()){
                nameEditText.requestFocus()
                nameErrorTextView.visibility = View.VISIBLE
            }else if (facilityEditText.text.isEmpty()){
                facilityEditText.requestFocus()
                facilityErrorTextView.visibility = View.VISIBLE
            }else{
                progressBar.visibility = View.VISIBLE
                val fullNames = nameEditText.text.toString()
                val facility = facilityEditText.text.toString()
                val userData = UserData(fullNames, facility)

                databaseReference.child(auth.currentUser!!.uid).setValue(userData)
                    .addOnCompleteListener { saveTask ->
                        if (saveTask.isSuccessful) {
                            progressBar.visibility = View.GONE
                            Toast.makeText(baseContext, "Data updated successfully.", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                        } else {
                            progressBar.visibility = View.GONE
                            Toast.makeText(baseContext, "Data update failed", Toast.LENGTH_SHORT).show()
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