package com.familyflavors.familyflavors

import android.os.Bundle
import android.widget.ListView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import android.support.v7.app.AppCompatActivity

//REFERENCE LINK FOR ListView with Kotlin: https://www.raywenderlich.com/155-android-listview-tutorial-with-kotlin
//TUTORIAL BY Joe Howard
//ADAPTATIONS: JSON IS LOADED FROM API RATHER THAN FROM A LOCAL JSON FILE AND SOME OTHER SMALL CHANGES

class MealsActivity : AppCompatActivity() {

    class MealJson(val meal_latitude: Float, val meal_chef: String, val meal_tag: List<String>, val meal_theme: String, val mealname: String, val meal_image: String, val mealdes: String, val meal_longitude: Float, val mealp: String, val mealq: String)

    class MealForListView(val mealname: String, val mealchef: String) {
        companion object {

        }
    }

    var mealList = ArrayList<com.familyflavors.familyflavorss.MealForListView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meals)

        //START VOLLEY//
        val queue = Volley.newRequestQueue(applicationContext)
        val url = StringBuilder("https://abhilasha230587.appspot.com/rest/searchMeals")
                .toString()
        val stringRequest = object : StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->

                    println(response)
                    val gson = Gson()
                    val meals = gson.fromJson(response, Array<MealJson>::class.java)
                    for(meal in meals){
                        var mealobj = com.familyflavors.familyflavorss.MealForListView(title=meal.mealname, description = meal.mealdes, imageUrl = meal.meal_image+"?dl=0", instructionUrl = "http://abhilasha230587.appspot.com/#/home/ordernow", label = meal.meal_theme)
                        mealList.add(mealobj)
                        println(":::::::::::::::::::::::: MEALS COUNT:"+meals.size)
                    }

                    var listView = findViewById<ListView>(R.id.meal_list_view)
                    val adapter = MealAdapter(this, mealList)
                    listView.adapter = adapter

                    val context = this
                    listView.setOnItemClickListener { _, _, position, _ ->
                        val selectedMeal = mealList[position]

                        val detailIntent = MealDetailActivity.newIntent(context, selectedMeal)
                    }

                },
                Response.ErrorListener { println("ERROR VOLLEY") })
        {}
        queue.add(stringRequest)
        println("::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::mealLIST COUNT:"+mealList.size)
        //START VOLLEY//








    }
}
