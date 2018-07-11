package com.dubtel.mobileapi;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.iid.FirebaseInstanceId;

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

public class MainActivity extends AppCompatActivity {

    private String status = "";
    private UserLogOutTask logOutTask = null;
    private UserNotificationTask notificationTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notificationTask = new UserNotificationTask();
        notificationTask.execute((Void) null);

        Button logOutButton = (Button) findViewById(R.id.log_out_button);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOutTask = new UserLogOutTask();
                logOutTask.execute((Void) null);
            }
        });
    }

    public class UserNotificationTask extends AsyncTask<Void, Void, Boolean> {

        private final String mSessionID;

        UserNotificationTask() {
            mSessionID = SharedData.getInstance().getSession_id();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            final String authenticateString = "https://api.dubtel.com/v1/register_android_device";

            Thread t1 = new Thread(new Runnable(){
                @Override
                public void run() {
                    try {
                        try {
                            // Load CAs from an InputStream
                            CertificateFactory cf = CertificateFactory.getInstance("X.509");

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

                            String postString = "session_id=" + mSessionID + "&devicetoken=" + FirebaseInstanceId.getInstance().getToken();
                            System.out.println(FirebaseInstanceId.getInstance().getToken());
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
                                String line = null;
                                while ((line = reader.readLine()) != null) {
                                    sb.append(line + "\n");
                                }
                                result = sb.toString();
                            } finally {
                                reader.close();
                            }

                            JSONObject object = new JSONObject(result);
                            status = object.getString("status");

                        } catch (Exception e) {
                            System.out.println("ERROR " + e);
                            notificationTask = null;
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
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return false;

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            notificationTask = null;
        }

        @Override
        protected void onCancelled() {
            notificationTask = null;
        }
    }

    public class UserLogOutTask extends AsyncTask<Void, Void, Boolean> {

        private final String mSessionID, mUserID;

        UserLogOutTask() {
            mSessionID = SharedData.getInstance().getSession_id();
            mUserID = SharedData.getInstance().getUser_id();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            final String authenticateString = "https://api.dubtel.com/v1/logOut";

            Thread t1 = new Thread(new Runnable(){
                @Override
                public void run() {
                    try {
                        try {
                            // Load CAs from an InputStream
                            CertificateFactory cf = CertificateFactory.getInstance("X.509");

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

                            String postString = "session_id=" + mSessionID + "&user_id=" + mUserID;
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
                                String line = null;
                                while ((line = reader.readLine()) != null) {
                                    sb.append(line + "\n");
                                }
                                result = sb.toString();
                            } finally {
                                reader.close();
                            }

                            JSONObject object = new JSONObject(result);
                            status = object.getString("status");

                        } catch (Exception e) {
                            System.out.println("ERROR " + e);
                            logOutTask = null;
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
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return false;

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            logOutTask = null;
        }

        @Override
        protected void onCancelled() {
            logOutTask = null;
        }
    }
}