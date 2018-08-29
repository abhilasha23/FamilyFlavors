package com.familyflavors.familyflavors

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.familyflavors.familyflavors.ProfileFragment
import com.familyflavors.familyflavors.R
import kotlinx.android.synthetic.main.activity_update_profile.*

class UpdateProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_profile)

        val intent = intent
        val current_first_name = intent.getStringExtra(ProfileFragment.FIRST_NAME)
        val current_last_name = intent.getStringExtra(ProfileFragment.LAST_NAME)

        firstnameEditText.setText(current_first_name)
        lastnameEditText.setText(current_last_name)

        updateButton.setOnClickListener(View.OnClickListener {
            val result = Intent()
            result.putExtra(ProfileFragment.FIRST_NAME, firstnameEditText.text.toString())
            result.putExtra(ProfileFragment.LAST_NAME, lastnameEditText.text.toString())
            setResult(Activity.RESULT_OK, result)
            finish()
        })

    }

}
