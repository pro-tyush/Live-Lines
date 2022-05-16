package com.example.livelinesalpha

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.livelinesalpha.databinding.ActivityMainBinding
import com.example.livelinesalpha.extensions.Extensions.toast
import com.example.livelinesalpha.utils.FirebaseUtils.firebaseAuth
import com.example.livelinesalpha.ui.login.CreateAccountActivity
import com.example.livelinesalpha.ui.login.SignInActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val SPLASH_SCREEN_TIME_OUT = 2000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_graph, R.id.navigation_news
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }
    private fun logout(){
        firebaseAuth.signOut()
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }

    fun logout(item: android.view.MenuItem) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Are you sure you want to logout?")
            .setNegativeButton("No") { dialog, which ->
                // Respond to negative button press
            }
            .setPositiveButton("Yes") { dialog, which ->
                logout()
            }
            .show()
    }


}