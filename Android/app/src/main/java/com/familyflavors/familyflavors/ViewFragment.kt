package com.familyflavors.familyflavors



import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.profile_fragment.*
import kotlinx.android.synthetic.main.view_fragment.*
import android.support.v7.app.AppCompatActivity
import android.widget.BaseAdapter
import android.widget.TextView
import com.familyflavors.familyflavors.R.id.textView


class ViewFragment : Fragment() {
    var responseJSON = ""



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)




        return inflater.inflate(R.layout.view_fragment, container, false)
    }

    var list: ListView? = null


}