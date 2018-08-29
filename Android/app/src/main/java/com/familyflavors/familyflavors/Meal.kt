package com.familyflavors.familyflavorss

import android.content.Context
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import org.json.JSONException
import com.android.volley.Request
import com.android.volley.Response

class MealForListView(
    val title: String,
    val description: String,
    val imageUrl: String,
    val instructionUrl: String,
    val label: String) {

  companion object {

      data class MealJSON(val meal_latitude: Float, val meal_chef: String, val meal_tag: List<String>, val meal_theme: String, val mealname: String, val meal_image: String, val mealdes: String, val meal_longitude: Float, val mealp: String, val mealq: String)


      fun getMealsFromFile(context: Context): ArrayList<MealForListView> {
      val recipeList = ArrayList<MealForListView>()

      try {
        // Load data
        //val jsonString = loadJsonFromAsset("recipes.json", context)


        val queue = Volley.newRequestQueue(context)
        val url = StringBuilder("https://abhilasha230587.appspot.com/rest/searchMeals")
                .toString()
        var responsejson = ""
        // Request a string response from the provided URL.
        println("+++++ URL: " + url)
        var jsonString = String() //need to store volley output here
        val stringRequest = object : StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->

                    println(response)
                  val gson = Gson()
                  val meals = gson.fromJson(response, Array<MealJSON>::class.java)
                  for(meal in meals){
                    var recipe = MealForListView(title=meal.mealname, description = meal.mealdes, imageUrl = meal.meal_image, instructionUrl = "http://abhilasha230587.appspot.com/#/home/ordernow", label = "Balanced")
                    recipeList += recipe
                      println(":::::::::::::::::::::::: MEALS COUNT:"+meals.size)

                  }


                },
                Response.ErrorListener { println("ERROR VOLLEY") })

        {}
        //
          queue.add(stringRequest)
          return recipeList
        print("RECIPEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE")

      } catch (e: JSONException) {
        e.printStackTrace()
      }

        println("SIZE OF RECIPE LIST:::::::::::::::::::::::::::"+recipeList.size)
          println("RETURNINGGGGGGGGGGGGGGGGGGGG")
      return recipeList
    }

    private fun loadJsonFromAsset(filename: String, context: Context): String? {
      var json: String? = null

      try {
        val inputStream = context.assets.open(filename)
        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        json = String(buffer, Charsets.UTF_8)
      } catch (ex: java.io.IOException) {
        ex.printStackTrace()
        return null
      }

      return json
    }
  }
}