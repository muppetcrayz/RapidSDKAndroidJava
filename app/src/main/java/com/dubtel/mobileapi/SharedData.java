package com.dubtel.mobileapi;

import android.app.Application;

import java.util.Base64;

/**
 * Created by kundan on 6/23/2015.
 */
public class SharedData {

    private static SharedData instance = new SharedData();

    // Getter-Setters
    public static SharedData getInstance() {
        return instance;
    }

    public static void setInstance(SharedData instance) {
        SharedData.instance = instance;
    }

    private String session_id;
    private String user_id;
    private String apiKey = "4abc7598e1f28e394d57f50396c92a160671b575776ed10d885281eb94db7259";
    private String apiSecret = "eb1f101fe943b41526ae1c5e54089834badbab836e87f73ee1b6047e30a41c68";
    private String var = apiKey + ":" + apiSecret;
    byte[] x = Base64.getEncoder().encode(var.getBytes());
    private String token = new String(x);

    private SharedData() {

    }

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getToken() { return token; }

}