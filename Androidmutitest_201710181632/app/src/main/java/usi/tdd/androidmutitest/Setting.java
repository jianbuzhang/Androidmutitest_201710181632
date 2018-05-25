package usi.tdd.androidmutitest;

import android.Manifest;

/**
 * Created by allen on 2017/9/26.
 */

public class Setting {
    static public TActivity mainActivity;
    static public String[] perList = {
            Manifest.permission.VIBRATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.RECORD_AUDIO,
//            Manifest.permission.FMOUNTLASHLIGHT,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_SETTINGS,
//            Manifest.permission.android.permission.READ,
//            Manifest.permission.WRITE_INTERNAL_STORAGE,
//            Manifest.permission.READ_INTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.READ_PHONE_STATE,
//            Manifest.permission.INTERACT_ACROSS_USERS_FULL,
//            Manifest.permission.INTERACT_ACROSS_USERS
    };

    static public void pass() {
        mainActivity.setItemPass();
        mainActivity.finish();
    }

    static public void fail() {
        mainActivity.setItemFail();
        mainActivity.finish();
    }
}
