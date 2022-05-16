package com.example.livelinesalpha.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import com.example.livelinesalpha.MainActivity
import com.example.livelinesalpha.R
import com.example.livelinesalpha.extensions.Extensions.toast
import com.example.livelinesalpha.utils.FirebaseUtils.firebaseAuth
//import com.example.livelinesalpha.ui.views
//import com.example.livelinesalpha.ui.views.HomeActivity



import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : AppCompatActivity() {
    lateinit var signInEmail: String
    lateinit var signInPassword: String
    lateinit var signInInputsArray: Array<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        signInInputsArray = arrayOf(etSignInEmail, etSignInPassword)
        btnCreateAccount2.setOnClickListener {
            startActivity(Intent(this, CreateAccountActivity::class.java))
            finish()
        }

        btnSignIn.setOnClickListener {
            signInUser()
        }
    }

    private fun notEmpty(): Boolean = signInEmail.isNotEmpty() && signInPassword.isNotEmpty()

    private fun signInUser() {
        signInEmail = etSignInEmail.text.toString().trim()
        signInPassword = etSignInPassword.text.toString().trim()

        if (notEmpty()) {
            firebaseAuth.signInWithEmailAndPassword(signInEmail, signInPassword)
                .addOnCompleteListener { signIn ->
                    if (signIn.isSuccessful) {
                        startActivity(Intent(this, MainActivity::class.java))
                        toast("Signed In")
                        finish()
                    } else {
                        toast("Auth Failed")
                    }
                }
        } else {
            signInInputsArray.forEach { input ->
                if (input.text.toString().trim().isEmpty()) {
                    input.error = "${input.hint} is Required"
                }
            }
        }
    }
}