package com.quickblox.sample.groupchatwebrtc.main;


import android.app.Application;

import com.sendbird.android.SendBird;

public class BaseApplication extends Application {

    private static final String APP_ID = "E236FA3C-C3D8-49D6-A0A0-5ED01BC4BCF6"; // US-1 Demo
    public static final String VERSION = "3.0.31";

    @Override
    public void onCreate() {
        super.onCreate();
        SendBird.init(APP_ID, getApplicationContext());
    }
}
