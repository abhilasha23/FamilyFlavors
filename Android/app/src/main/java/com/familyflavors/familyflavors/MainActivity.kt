package com.familyflavors.familyflavors

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignInAccount


val RC_SIGN_IN = 9001

// OK, This is bad form. I've just made global variables for my fragments to communicate
// Ideally, we'd create some interfaces within the fragments and have the MainActivity implement them
// and we'd directly communicate with the fragments from the MainActivity when needed.
var account: GoogleSignInAccount? = null
var username: String? = null
var token: String? = null

var loggedIn: Boolean = false

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)

        val tabLayout = findViewById(R.id.tab_layout) as TabLayout
        tabLayout.addTab(tabLayout.newTab().setText("Login"))
        //tabLayout.addTab(tabLayout.newTab().setText("View"))
        tabLayout.addTab(tabLayout.newTab().setText("Profile"))
        tabLayout.addTab(tabLayout.newTab().setText("Submit"))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL

        val viewPager = findViewById(R.id.pager) as ViewPager
        val adapter = PagerAdapter(supportFragmentManager, tabLayout.tabCount)
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if(loggedIn){
                    viewPager.currentItem = tab.position
                }
                else {
                    Toast.makeText(this@MainActivity, "Please login first!", Toast.LENGTH_SHORT).show()
                    viewPager.currentItem = 0
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
}
