package com.dubtel.mobileapi;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    SharedData sharedData = SharedData.getInstance();

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    View focusView = null;
    Boolean cancel = false;

    private String status = "", session = "", user = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button mSignUpButton = (Button) findViewById(R.id.sign_up_button);
        mSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || TextUtils.isEmpty(username)) {
            mPasswordView.setError("One of the fields is missing.");
            focusView = mPasswordView;
            cancel = true;
        }
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mAuthTask = new UserLoginTask(username, password);
            mAuthTask.execute((Void) null);
    }

    private void myError() {
        focusView = mPasswordView;
        mPasswordView.setError("Username or password is incorrect.");
        focusView.requestFocus();
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            final String authenticateString = "https://api.rapidsdk.com/v1/login";

            Thread t1 = new Thread(new Runnable(){
                @Override
                public void run() {
                    try {
                        try {


                            String postString = "username=" + mEmail + "&password=" + mPassword;
                            byte[] postData = postString.getBytes(StandardCharsets.UTF_8);

// Tell the URLConnection to use a SocketFactory from our SSLContext
                            URL url = new URL(authenticateString);
                            HttpsURLConnection urlConnection =
                                    (HttpsURLConnection)url.openConnection();
                            urlConnection.setDoOutput(true);
                            urlConnection.setRequestProperty ("Authorization", "Basic " + SharedData.getInstance().getToken());
                            urlConnection.setRequestMethod("POST");

                            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                            wr.write(postData);
                            wr.flush();
                            wr.close();

                            InputStream in = urlConnection.getInputStream();

                            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                            StringBuilder sb = new StringBuilder();
                            String result = null;
                            try {
                                sb.append(reader.readLine());
                                result = sb.toString();
                            } finally {
                                reader.close();
                            }

                            JSONObject object = new JSONObject(result);
                            status = object.getString("status");
                            if (status.equals("Success")) {
                                session = object.getString("session_id");
                                user = object.getString("user_id");
                            }

                        } catch (Exception e) {
                            System.out.println("ERROR " + e);
                            mAuthTask = null;
                        }
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            t1.start();

            try {
                t1.join();
                if (status.equals("Success")) {
                    sharedData.setSession_id(session);
                    sharedData.setUser_id(user);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return false;

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            if (status.equals("")) {
                myError();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }
}

