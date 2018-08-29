package com.familyflavors.familyflavors



import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

val MIME_TYPE = "image/jpeg"

class ProfileFragment : Fragment() {

    var profile: Profile? = null
    val PROFILE_UPDATE = 1
    val CHOOSE_PHOTO = 2

    val MEDIA_TYPE_JPEG: MediaType = MediaType.get("image/jpeg")
    private val client = OkHttpClient()
    private val TEMP_IMAGE_NAME = "tempImage"

    companion object {
        val FIRST_NAME = "com.familyflavors.familyflavors.FIRST_NAME"
        val LAST_NAME = "com.familyflavors.familyflavors.LAST_NAME"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.profile_fragment, container, false)
        // so we get to presuppose that we're logged in
        // then what we want to do is to retrieve the profile data, if it exists

        val queue = Volley.newRequestQueue(this.context)
        val url = StringBuilder("https://abhilasha230587.appspot.com/rest/getProfile?").append("email=").append(account?.email)
                .toString()
        // Request a string response from the provided URL.
        println("+++++ URL: " + url)
        val stringRequest = object : StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->
                    textView.text = ""
                    val gson = Gson()
                    profile = gson.fromJson(response, Profile::class.java)
                    updateUI()
                },
                Response.ErrorListener { textView.text = "" })
        {}
        // Add the request to the RequestQueue.
        queue.add(stringRequest)

        /*val url2 = StringBuilder("https://abhilasha230587.appspot.com/rest/CreateProfile")
                .toString()
        // Request a string response from the provided URL.
        println("+++++++++++++ URL2: " + url2)
        val stringRequest2 = object : StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->
                    println(response)
                    val gson2 = Gson()
                    val jobj:JSONObject = gson2.fromJson(response, JSONObject::class.java)
                    profile?.uploadURL = jobj.get("upload_url").toString()
                    updateUI()
                },
                Response.ErrorListener { println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! That didn't work!") })
        {}

        queue.add(stringRequest2)*/

        /*val button = view.findViewById<Button>(R.id.mapButton)

        button.setOnClickListener {
            val intent = Intent(this.activity, MapsActivity::class.java)
            startActivityForResult(intent, 101)
        }*/

        return view
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                PackageManager.PERMISSION_DENIED -> (AlertDialog.Builder(this@ProfileFragment.activity!!)
                        .setTitle("Runtime Permissions Required")
                        .setMessage(Html.fromHtml("<p>To enable the app to access photos please click \"Allow\" on the runtime permissions popup.</p>"))
                        .setNeutralButton("Okay") { dialog, which ->
                            if (ContextCompat.checkSelfPermission(activity!!,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(this@ProfileFragment.activity!!,
                                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                        1)
                            }
                        }
                        .show()
                        .findViewById<View>(android.R.id.message) as TextView).movementMethod = LinkMovementMethod.getInstance()
                PackageManager.PERMISSION_GRANTED -> { }
            }
        }
        updateProfileButton.setOnClickListener{
            // launch activity for updated profile
            val intentUpdateProfile = Intent(this@ProfileFragment.activity, UpdateProfileActivity::class.java)
            //start activity
            intentUpdateProfile.putExtra(ProfileFragment.FIRST_NAME, profile?.firstname)
            intentUpdateProfile.putExtra(ProfileFragment.LAST_NAME, profile?.lastname)
            startActivityForResult(intentUpdateProfile, PROFILE_UPDATE)
        }

        changePhotoButton.setOnClickListener{
            // launch intent to get new photo and send it to the upload URL

            // https://stackoverflow.com/a/36929403
            val intentChooseNewPhoto = Intent(Intent.ACTION_PICK)
            intentChooseNewPhoto.type = "image/*"
            intentChooseNewPhoto.putExtra("crop", "true")
            intentChooseNewPhoto.putExtra("scale", true)
            intentChooseNewPhoto.putExtra("outputX", 256)
            intentChooseNewPhoto.putExtra("outputY", 256)
            intentChooseNewPhoto.putExtra("aspectX", 1)
            intentChooseNewPhoto.putExtra("aspectY", 1)
            intentChooseNewPhoto.putExtra("return-data", true)
            intentChooseNewPhoto.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            intentChooseNewPhoto.putExtra("return-data", true)
            startActivityForResult(intentChooseNewPhoto, CHOOSE_PHOTO)
        }
    }



    fun updateUI(){
        fullnameTextView.text = StringBuilder(profile?.firstname).append(" ").append(profile?.lastname)
        emailTextView.text = profile?.email
        Picasso.get().load(profile?.imageURL).into(profilePhotoImageView);
        // create Picasso.Builder object
        //val picassoBuilder = Picasso.Builder(this.activity!!)

        // Picasso.Builder creates the Picasso object to do the actual requests
        //val picasso = picassoBuilder.build()
        //println("##############################" + profile?.imageURL)
        //picasso.load(profile?.imageURL).into(profilePhotoImageView);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PROFILE_UPDATE) {
            if (resultCode == Activity.RESULT_OK) {
                profile?.firstname = data!!.getStringExtra(FIRST_NAME)
                profile?.lastname = data.getStringExtra(LAST_NAME)
                //TODO: Add a Volley post request to update this info if we want

                updateUI()
            }
        }
        if(requestCode == CHOOSE_PHOTO) {
            if(resultCode == Activity.RESULT_OK){
                val queue = Volley.newRequestQueue(this.context)

                val url2 = StringBuilder("https://abhilasha230587.appspot.com/rest/CreateProfile")
                        .toString()
                // Request a string response from the provided URL.
                println("+++++++++++++ URL2: " + url2)
                val stringRequest2 = object : StringRequest(Request.Method.GET, url2,
                        Response.Listener<String> { response ->
                            println(response)
                            val gson2 = Gson()
                            val uploadurl = gson2.fromJson(response, URL::class.java)

                            //val gson2 = Gson()
                            //val jobj:JSONObject = gson2.fromJson(response, JSONObject::class.java)
                            //println(jobj)
                            profile?.uploadURL = uploadurl.upload_url
                            println(profile?.uploadURL)
                            updateUI()


                            val extras = data?.getExtras()
                            val newProfilePic = extras?.getParcelable<Bitmap>("data")
                            val bytes = ByteArrayOutputStream()
                            newProfilePic?.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
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
                            executePost(file, profile?.uploadURL!!, account?.email!!).execute()
                        },
                        Response.ErrorListener { println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! That didn't work!") })
                {}

                queue.add(stringRequest2)



            }
        }
    }
}

class executePost(val file: File?, val uploadURL: String, val email: String) : AsyncTask<Void, Void, String>(){
    val MEDIA_TYPE_JPG: MediaType = MediaType.get("image/jpg")
    val client = OkHttpClient()

    override fun doInBackground(vararg p0: Void?): String {
        println("*******: url: " + uploadURL)

        // from this okhttp recipe:
        // https://github.com/square/okhttp/blob/master/samples/guide/src/main/java/okhttp3/recipes/PostMultipart.java

        val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)//.addFormDataPart("Foo", "Bar")
                .addFormDataPart("file", "profilephoto.jpg", RequestBody.create(MEDIA_TYPE_JPG, file))
                .addFormDataPart("email", email)
                .build()

        val request = okhttp3.Request.Builder()
                .url(uploadURL)
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


data class Profile(var email: String, var firstname: String, var lastname: String,
                   var imageURL: String, var uploadURL: String?)

data class URL(var upload_url: String)