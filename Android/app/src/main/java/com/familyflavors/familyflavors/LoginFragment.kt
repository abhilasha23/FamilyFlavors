package com.familyflavors.familyflavors

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.familyflavors.familyflavorss.MealForListView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.login_fragment.*


class LoginFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.login_fragment, container, false)

        //Button for list view because I can't figure out how to embed it into a view fragment
        view.findViewById<Button>(R.id.view_meals_button).setOnClickListener {
            val intent = Intent(context, MealsActivity::class.java)
            startActivity(intent);
        }

        //Button for map activity that clair is developing.
        view.findViewById<Button>(R.id.view_map_activity).setOnClickListener {
            val intent = Intent(context, MapsActivity::class.java)
            startActivity(intent);
        }



        //Button to be taken to MapsActivity that Clair is developing

        // sign in code based on https://developers.google.com/identity/sign-in/android/sign-in
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        // the server_client_id, defined in strings, is retrieved from the Google API console credentials page
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build()
        // Build a GoogleSignInClient with the options specified by gso.
        val mGoogleSignInClient = GoogleSignIn.getClient(this.activity!!, gso);
        view.findViewById<SignInButton>(R.id.sign_in_button).setOnClickListener {
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        /*val providers = Arrays.asList(AuthUI.IdpConfig.GoogleBuilder().build())

        // Create and launch sign-in intent
        findViewById<Button>(R.id.sign_in_button).setOnClickListener {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN)
        }*/


        // volley code based on example at https://developer.android.com/training/volley/simple
        /*val responseTextView = view.findViewById<TextView>(R.id.responseContent)
        view.findViewById<Button>(R.id.requestButton).setOnClickListener {
            //only signed in users can make a request
            if (account == null) {
                Toast.makeText(view.context, "Sign in first!", Toast.LENGTH_SHORT).show()
            } else {
                // Instantiate the RequestQueue.
                responseTextView.text = "Request in progress ... "
                val queue = Volley.newRequestQueue(this.context)
                val url = "https://abhilasha230587.appspot.com"
                //val url = "https://auth-dot-aiutamiapad.appspot.com"
                // Request a string response from the provided URL.
                println ("***** token in login: " + token)
                val stringRequest = object : StringRequest(Request.Method.POST, url,
                        Response.Listener<String> { response ->
                            Log.d("LoginFragment", "*******\n " + response + "\n*********")
                            responseTextView.text = response
                        },
                        Response.ErrorListener { responseTextView.text = "That didn't work!" }) {
                    override fun getParams(): Map<String, String> = mapOf("username" to username!!,
                            "token" to token!!)
                }


                // Add the request to the RequestQueue.
                queue.add(stringRequest)


            }
        }*/

        view.findViewById<Button>(R.id.sign_out_button).setOnClickListener {
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(this.activity!!, object : OnCompleteListener<Void> {
                        override fun onComplete(task: Task<Void>) {
                            val textView = view.findViewById<TextView>(R.id.userNameText)
                            textView.text = "User is: not signed in"
                            account = null
                            username = null
                            token = null
                        }
                    })
        }
        return view;
    }

    // sign in code based on https://developers.google.com/identity/sign-in/android/sign-in
    fun updateUI(){
        val textView = this.view?.findViewById<TextView>(R.id.userNameText)
        val stringToAdd = StringBuilder(account?.displayName ?: "not logged in")
        var stringToAdd2 = StringBuilder(", ")
        if(account?.idToken != null) {
            stringToAdd2.append("got token")
        }
        textView?.text = "User is: " + stringToAdd + stringToAdd2

    }

    /*fun updateUI(user: FirebaseUser?){
        val textView = findViewById<TextView>(R.id.userNameText)
        val stringToAdd = StringBuilder(user?.displayName ?: "not logged in")
        val stringToAdd2 = StringBuilder(", ").append(user?.getIdToken(true) ?: "no token")
        textView.text = "User is: " + stringToAdd + stringToAdd2

    }*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // sign in code based on https://developers.google.com/identity/sign-in/android/sign-in
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                account = task.getResult(ApiException::class.java)
                username = account?.displayName
                token = account?.idToken

                // Signed in successfully, show authenticated UI.
                loggedIn = true
                updateUI()
            } catch (e: ApiException) {
                // The ApiException status code indicates the detailed failure reason.
                // Please refer to the GoogleSignInStatusCodes class reference for more information.
                Log.w("MainActivity", "signInResult:failed code=" + e.statusCode)
                loggedIn = false
                updateUI()
            }
        }

        /*val response = IdpResponse.fromResultIntent(data)

        if (resultCode == Activity.RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            updateUI(user)
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            Log.w("MainActivity", "signInResult:failed code=" + resultCode)
            updateUI(null)
        }*/
    }

    override fun onStart() {
        super.onStart()

        account = GoogleSignIn.getLastSignedInAccount(this.activity)
        username = account?.displayName
        token = account?.idToken
        loggedIn = true
        updateUI()
    }


}

