package usi.tdd.androidmutitest;

import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;

/**
 * Created by Admin on 2016/8/24.
 */
public class RTCTest {
    BufferedWriter buf;
    long preTimeDiffer, dutTimeMillis, TimeDiff, Timeover;
    boolean bRet;

    public boolean GetRTCTest(File fileLog, String szItemName, final int nTimeDelay) {
        //preTimeDiffer = System.currentTimeMillis(); //會受時間修改所影響
        preTimeDiffer = SystemClock.elapsedRealtime(); //1st 從開機開始計算，包含休眠時間
        try {
            Thread.sleep(nTimeDelay);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //dutTimeMillis = System.currentTimeMillis(); //會受時間修改所影響
        dutTimeMillis = SystemClock.elapsedRealtime(); //2st 從開機開始計算，包含休眠時間
        //SystemClock.elapsedRealtime()
        TimeDiff = Math.abs(dutTimeMillis - preTimeDiffer);
        Timeover = TimeDiff - nTimeDelay;

        if (TimeDiff <= nTimeDelay && TimeDiff > 2000)
            bRet = false;
        else
            bRet = true;

        Log.d("MFG_TEST", "DUT 1st Time Millisecound Start: " + preTimeDiffer + "\r\n");
        Log.d("MFG_TEST", "DUT 2nd Time Millisecound End: " + dutTimeMillis + "\r\n");
        Log.d("MFG_TEST", "Time Difference: " + TimeDiff + "\r\n");
        Log.d("MFG_TEST", "Time Delay: " + nTimeDelay + "\r\n");
        if (bRet)
        {
            Log.d("MFG_TEST", "RTCTest SUCCESSFUL TEST\r\n");
//            Setting.pass();
        }
        else
        {
            Log.d("MFG_TEST", "RTCTest Fail\r\n");
//            Setting.fail();
        }

        return bRet;
    }
}
