package com.dubtel.mobileapi;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
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
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class SignUpActivity extends AppCompatActivity {

    private EditText firstName, lastName, email, password;
    private Button signUpButton, cancelButton;

    private String status = "";
    private UserSignUpTask signUpTask = null;
    Boolean cancel = false;
    View focusView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firstName = (EditText) findViewById(R.id.first_name);
        lastName = (EditText) findViewById(R.id.last_name);
        email = (EditText) findViewById(R.id.email_sign_up);
        password = (EditText) findViewById(R.id.password_sign_up);

        signUpButton = (Button) findViewById(R.id.submit_sign_up);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignUp();
            }
        });

        cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void attemptSignUp() {

        String firstNameText = firstName.getText().toString();
        String lastNameText = lastName.getText().toString();
        String emailText = email.getText().toString();
        String passwordText = password.getText().toString();

        if (TextUtils.isEmpty(firstNameText) || TextUtils.isEmpty(lastNameText) || TextUtils.isEmpty(emailText) || TextUtils.isEmpty(passwordText)) {
            password.setError("One of the fields is missing.");
            focusView = password;
            focusView.requestFocus();
            cancel = true;
        }

            signUpTask = new UserSignUpTask(firstNameText,lastNameText,emailText,passwordText);
            signUpTask.execute((Void) null);

    }

    private void myError() {
        focusView = firstName;
        firstName.setError("Unable to register.");
        focusView.requestFocus();
    }

    public class UserSignUpTask extends AsyncTask<Void, Void, Boolean> {

        private final String mFirstName, mLastName, mEmail, mPassword;

        UserSignUpTask(String firstName, String lastName, String email, String password) {
            mFirstName = firstName;
            mLastName = lastName;
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            final String authenticateString = "https://api.dubtel.com/v1/register";

            Thread t1 = new Thread(new Runnable(){
                @Override
                public void run() {
                    try {
                        try {
                            // Load CAs from an InputStream
                            CertificateFactory cf = CertificateFactory.getInstance("X.509");

                            // I/System.out: java.lang.ClassCastException: android.content.res.AssetManager$AssetInputStream cannot be cast to java.io.FileInputStream

                            InputStream fis = getResources().openRawResource(R.raw.mycert);

                            InputStream caInput = new BufferedInputStream(fis);
                            Certificate ca;
                            try {
                                ca = cf.generateCertificate(caInput);
                            } finally {
                                caInput.close();
                            }

// Create a KeyStore containing our trusted CAs
                            String keyStoreType = KeyStore.getDefaultType();
                            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                            keyStore.load(null, null);
                            keyStore.setCertificateEntry("ca", ca);

// Create a TrustManager that trusts the CAs in our KeyStore
                            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                            tmf.init(keyStore);

// Create an SSLContext that uses our TrustManager
                            SSLContext context = SSLContext.getInstance("TLS");
                            context.init(null, tmf.getTrustManagers(), null);

                            String postString = "firstname=" + mFirstName + "&lastname=" + mLastName + "&email=" + mEmail + "&password=" + mPassword;
                            byte[] postData = postString.getBytes(StandardCharsets.UTF_8);

// Tell the URLConnection to use a SocketFactory from our SSLContext
                            URL url = new URL(authenticateString);
                            HttpsURLConnection urlConnection =
                                    (HttpsURLConnection)url.openConnection();
                            urlConnection.setDoOutput(true);
                            urlConnection.setSSLSocketFactory(context.getSocketFactory());
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
                                System.out.println(result);
                            } finally {
                                reader.close();
                            }

                            JSONObject object = new JSONObject(result);
                            status = object.getString("status");

                        } catch (Exception e) {
                            System.out.println("ERROR " + e);
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
                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return false;

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            signUpTask = null;
            myError();
        }

        @Override
        protected void onCancelled() {
            signUpTask = null;
        }
    }
}

