package usi.tdd.androidmutitest;

import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by Lynette on 2016/10/25.
 * Only For Phoenix Project
 */

public class SystemPropertyAccess {
    // private static final String TAG = SystemPropertyAccess.class.getSimpleName();

    public static String getProperty(String name, String defaultValue) {
        @SuppressWarnings("rawtypes")
        Class SystemProperties;
        try {
            SystemProperties = Class.forName("android.os.SystemProperties");

            // Parameters Types
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[2];
            paramTypes[0] = String.class;
            paramTypes[1] = String.class;

            @SuppressWarnings("unchecked")
            Method get = SystemProperties.getMethod("get", paramTypes);

            // Parameters
            Object[] params = new Object[2];
            params[0] = name;
            params[1] = defaultValue;

            return (String) get.invoke(SystemProperties, params);
        } catch (Exception e) {
            Log.d("MFG_TEST", "Failed to get system property", e);
            // TODO Auto-generated catch block
            return null;
        }

    }

    public static void setProperty(String name, String value) {
        @SuppressWarnings("rawtypes")
        Class SystemProperties;
        try {
            SystemProperties = Class.forName("android.os.SystemProperties");

            // Parameters Types
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[2];
            paramTypes[0] = String.class;
            paramTypes[1] = String.class;

            @SuppressWarnings("unchecked")
            Method set = SystemProperties.getMethod("set", paramTypes);

            // Parameters
            Object[] params = new Object[2];
            params[0] = name;
            params[1] = value;

            set.invoke(SystemProperties, params);
        } catch (Exception e) {
            Log.d("MFG_TEST", "Failed to set system property", e);
        }
    }
}
