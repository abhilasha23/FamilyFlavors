package com.familyflavors.familyflavors



import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.widget.Button
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.profile_fragment.*
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.EditText
import android.widget.TextView
import com.familyflavors.familyflavors.R.id.*
import com.google.gson.JsonObject
import com.familyflavors.familyflavors.UpdateProfileActivity
import kotlinx.android.synthetic.main.profile_fragment.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlinx.android.synthetic.main.submit_fragment.*

//val MIME_TYPE = "image/jpeg"

class SubmitFragment : Fragment() {

    //var meal: Meal? = null

    val MEDIA_TYPE_JPEG: MediaType = MediaType.get("image/jpeg")
    private val client = OkHttpClient()
    private val TEMP_IMAGE_NAME = "tempImage"

    val CHOOSE_PHOTO = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.submit_fragment, container, false)

        val button = view.findViewById<Button>(R.id.addImageButton)

        button.setOnClickListener {
            // https://stackoverflow.com/a/36929403
            val intentChoosePhoto = Intent(Intent.ACTION_PICK)
            intentChoosePhoto.type = "image/*"
            intentChoosePhoto.putExtra("crop", "true")
            intentChoosePhoto.putExtra("scale", true)
            intentChoosePhoto.putExtra("outputX", 256)
            intentChoosePhoto.putExtra("outputY", 256)
            intentChoosePhoto.putExtra("aspectX", 1)
            intentChoosePhoto.putExtra("aspectY", 1)
            intentChoosePhoto.putExtra("return-data", true)
            intentChoosePhoto.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            intentChoosePhoto.putExtra("return-data", true)
            startActivityForResult(intentChoosePhoto, CHOOSE_PHOTO)
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                PackageManager.PERMISSION_DENIED -> (AlertDialog.Builder(this@SubmitFragment.activity!!)
                        .setTitle("Runtime Permissions Required")
                        .setMessage(Html.fromHtml("<p>To enable the app to access photos please click \"Allow\" on the runtime permissions popup.</p>"))
                        .setNeutralButton("Okay") { dialog, which ->
                            if (ContextCompat.checkSelfPermission(activity!!,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(this@SubmitFragment.activity!!,
                                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                        1)
                            }
                        }
                        .show()
                        .findViewById<View>(android.R.id.message) as TextView).movementMethod = LinkMovementMethod.getInstance()
                PackageManager.PERMISSION_GRANTED -> {
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == CHOOSE_PHOTO) {
            if(resultCode == Activity.RESULT_OK){
                //TODO: Add code to create new meal object using values from textboxes
                var titleedit = titleEditText.text.toString()
                var descriptionedit = descriptionEditText.text.toString()
                var priceedit = priceEditText.text.toString()
                var quantityedit = quantityEditText.text.toString()
                var themeedit = themeEditText.text.toString()
                var tagsedit = tagsEditText.text.toString()

                var meal = Meal(title =  titleedit, description = descriptionedit, imageURL = "", price = priceedit, quantity = quantityedit, latitude = "21.50",
                        longitude = "21.50",tags = tagsedit, theme = themeedit, uploadURL = "")


                val queue = Volley.newRequestQueue(this.context)

                val url = StringBuilder("https://abhilasha230587.appspot.com/rest/CreateMeal")
                        .toString()
                // Request a string response from the provided URL.
                println("+++++++++++++ URL: " + url)
                val stringRequest2 = object : StringRequest(Request.Method.GET, url,
                        Response.Listener<String> { response ->
                            println(response)
                            val gson = Gson()
                            val uploadurl = gson.fromJson(response, URL::class.java)
                            meal!!.uploadURL = uploadurl.upload_url

                            //val gson2 = Gson()
                            //val jobj:JSONObject = gson2.fromJson(response, JSONObject::class.java)
                            //println(jobj)
                            //profile?.uploadURL = uploadurl.upload_url
                            //println(profile?.uploadURL)
                            //updateUI()


                            val extras = data?.getExtras()
                            val newMealPic = extras?.getParcelable<Bitmap>("data")
                            val bytes = ByteArrayOutputStream()
                            newMealPic?.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                            // https://stackoverflow.com/a/45524289
                            // this should really be implemented as an async task, but I'm tired.
                            val file: File?
                            val path_external = StringBuilder(Environment.getExternalStorageDirectory().toString()).append(File.separator).append("temporary_file.jpg")
                            file = File(path_external.toString())

                            println("####HERE IS THE EMAIL####" + account?.email)

                            try {
                                val fo = FileOutputStream(file)
                                fo.write(bytes.toByteArray())
                                fo.flush()
                                fo.close()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                            executePost2(file, meal, account?.email!!).execute()
                        },
                        Response.ErrorListener { println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! That didn't work!") })
                {}

                queue.add(stringRequest2)
            }
        }
    }
}

class executePost2(val file: File?, val meal: Meal, val email: String) : AsyncTask<Void, Void, String>(){
    val MEDIA_TYPE_JPG: MediaType = MediaType.get("image/jpg")
    val client = OkHttpClient()

    override fun doInBackground(vararg p0: Void?): String {
        println("*******: url: " + meal.uploadURL)

        // from this okhttp recipe:
        // https://github.com/square/okhttp/blob/master/samples/guide/src/main/java/okhttp3/recipes/PostMultipart.java

        val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)//.addFormDataPart("Foo", "Bar")
                .addFormDataPart("file", "profilephoto.jpg", RequestBody.create(MEDIA_TYPE_JPG, file))
                .addFormDataPart("email", email)
                .addFormDataPart("title", meal.title)
                .addFormDataPart("description", meal.description)
                .addFormDataPart("price", meal.price.toString())
                .addFormDataPart("quantity", meal.quantity.toString())
                .addFormDataPart("theme", meal.theme)
                .addFormDataPart("tags", meal.tags)
                .addFormDataPart("latitude", meal.latitude.toString())
                .addFormDataPart("longitude", meal.longitude.toString())
                //TODO: Check to see if we need to cast price, quantity, latiutude, and longitude back to floats and ints on the backend
                .build()

        val request = okhttp3.Request.Builder()
                .url(meal.uploadURL)
                .post(requestBody)
                .addHeader("MimeType", MIME_TYPE)
                .addHeader("Content-Type", "multipart/form-data")
                .build()

        println(" ****** REQUEST ******\n" + request)

        val response = client.newCall(request).execute()

        return response.toString()
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        println("********* RESULT: " + result)

    }
}


data class Meal(var title: String, var description: String, var price: String, var quantity: String, var theme: String, var tags: String,
                   var latitude: String, var longitude: String, var imageURL: String, var uploadURL: String?)

//data class URL(var upload_url: String)