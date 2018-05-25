package usi.tdd.androidmutitest;

import android.util.Log;

/**
 * Created by usi on 2017/9/19.
 */

public class USILog {
    static final String  MFG_TAG="MFG_TEST";// for FCT test use
    static final String  BURN_IN_TAG="BURN_IN_TEST";// for burn in control use
	// 20171002 - Check
    public static final String BURN_IN_LOG_FILE="/storage/emulated/0/burnin.log";
	// 20171002
    public static final String ITEM_TEST_PASS="SUCCESSFUL TEST";
    public static final String ITEM_TEST_FAIL="UUT-FAIL";
    //LOG.D: save logs by using Log.d("TAG","Msg");
    //File: save logs to files by using app as a file name
    static String LogType="LOG.D";
    static boolean IsDebug=true;

    public static void appendSuccessful(String itemName){
        Log.d(MFG_TAG, itemName+" "+ITEM_TEST_PASS);
    }

    public static void appendUUTFail(String itemName){
        Log.d(MFG_TAG, itemName+" "+ITEM_TEST_FAIL);
    }

    public static void append(String app,String msg){
        if(LogType.equals("LOG.D")){
            Log.d(MFG_TAG, app+":"+msg);
        }
    }

    public static void append(Object app,String msg){
        if(LogType.equals("LOG.D")){
            Log.d(MFG_TAG, app.getClass().getSimpleName()+":"+msg);
        }
    }

    public static void appendDebug(String className,String msg){
        if(IsDebug){
            Log.d(MFG_TAG, className+":"+msg);
        }
    }

    public static void appendBurnin(String className,String msg){
        Log.d(BURN_IN_TAG, className+":"+msg);
    }
    public static void appendBurnin(Object app,String msg){
        if(LogType.equals("LOG.D")){
            Log.d(BURN_IN_TAG, app.getClass().getSimpleName()+":"+msg);
        }
    }

}
