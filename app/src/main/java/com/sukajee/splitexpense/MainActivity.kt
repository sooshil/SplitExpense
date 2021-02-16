package com.sukajee.splitexpense

import android.R.attr
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.*
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import java.util.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, DrawerLocker {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var buttonEditPhoto: Button
    private lateinit var imageViewUserPic: ImageView

    companion object {
        private const val PICK_IMAGE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val storageRef = FirebaseStorage.getInstance().reference

        
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragments_container) as NavHostFragment
        navController = navHostFragment.findNavController()


        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        appBarConfiguration = AppBarConfiguration(
                setOf(R.id.profileFragment, R.id.usersContributionDetailsFragment, R.id.loginFragment, R.id.joinCreateCircleFragment),
                drawer_layout
        )
        setSupportActionBar(toolbar)

        setupActionBarWithNavController(navController, appBarConfiguration)
        //bottom_navigation.setupWithNavController(navController)
        nav_view.setupWithNavController(navController)

        navigationView.setNavigationItemSelectedListener(this@MainActivity)

        buttonEditPhoto = navigationView.getHeaderView(0).findViewById(R.id.buttonEditPhoto)
        imageViewUserPic = navigationView.getHeaderView(0).findViewById(R.id.imageViewUserPic)
        buttonEditPhoto.setOnClickListener {
            val intentGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intentGallery, PICK_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            val imageUri: Uri? = data?.data
            Glide.with(this).load(imageUri).into(imageViewUserPic)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.nav_profile) {
            navController.navigate(R.id.profileFragment)
            Objects.requireNonNull(supportActionBar)?.title = "Profile"
        } else if (item.itemId == R.id.nav_settle) {
            navController.navigate(R.id.doSettlementFragment)
            Objects.requireNonNull(supportActionBar)?.title = "Contribution Details"
        } else if (item.itemId == R.id.nav_logout) {
            navController.navigate(R.id.logoutFragment)
            Objects.requireNonNull(supportActionBar)?.title = "Logout"
        } else if (item.itemId == R.id.nav_leave_circle) {
            Toast.makeText(this, "You clicked Share", Toast.LENGTH_SHORT).show()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.menu_terms) {
            val action = NavGraphDirections.actionGlobalTermsFragment()
            navController.navigate(action)
            true
        } else {
            item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun lockDrawer() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        toolbar.setNavigationIcon(null)
    }

    override fun unlockDrawer() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    override fun enableBackButton() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //actionBar?.setDisplayHomeAsUpEnabled(true)
    }

}