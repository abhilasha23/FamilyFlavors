package com.familyflavors.familyflavors

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

class PagerAdapter(fm: FragmentManager, internal var mNumOfTabs: Int) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment? {

        when (position) {
            0 -> return LoginFragment()
            //1 -> return ViewFragment()
            1 -> return ProfileFragment()
            2 -> return SubmitFragment()
            else -> return null
        }
    }

    override fun getCount(): Int {
        return mNumOfTabs
    }
}