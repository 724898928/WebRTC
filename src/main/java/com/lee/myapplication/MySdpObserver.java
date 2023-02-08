package com.lee.myapplication;

import android.util.Log;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

public class MySdpObserver implements SdpObserver {

    
    private static final String TAG = MySdpObserver.class.getSimpleName();

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Log.d(TAG, " MySdpObserver onCreateSuccess sessionDescription->" + sessionDescription);

    }

    @Override
    public void onSetSuccess() {
        Log.d(TAG, "onSetSuccess ==  ");
    }

    @Override
    public void onCreateFailure(String s) {
        Log.d(TAG, "onCreateFailure ==  " + s);
    }

    @Override
    public void onSetFailure(String s) {
        Log.d(TAG, "onSetFailure ==  " + s);
    }
}
