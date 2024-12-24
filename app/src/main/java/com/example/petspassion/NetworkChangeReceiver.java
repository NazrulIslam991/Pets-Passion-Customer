package com.example.petspassion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private NetworkListener networkListener;

    public NetworkChangeReceiver(NetworkListener listener) {
        this.networkListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                // Notify listener that network is connected
                networkListener.onNetworkConnected();
            } else {
                // Notify listener that network is disconnected
                networkListener.onNetworkDisconnected();
            }
        }
    }

    public interface NetworkListener {
        void onNetworkConnected();
        void onNetworkDisconnected();
    }
}
