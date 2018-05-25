package usi.tdd.androidmutitest;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * Created by usi on 2017/9/19.
 */

class BurninLogRecorder implements Runnable{
    Context mCtx;
    public boolean mIsRecord=true;
    public BurninLogRecorder(Context ctx){
        mCtx=ctx;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        Log.d("RUNIN", "Start Burnin Log Recorder Thread");

        Runtime localRuntime = Runtime.getRuntime();
        String[] args = {"logcat", "-s", USILog.MFG_TAG + ":D"};
        String[] clear = {"logcat", "-c"};
        InputStream in=null, clearStream = null;
        try {
            in = localRuntime.exec(args).getInputStream();
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(in));
            String s = null;
            while(mIsRecord){
                BurnInTest.recoding=true;
                if ((s = stdInput.readLine()) != null) {
                    File logFile = new File(USILog.BURN_IN_LOG_FILE);
                    if (!logFile.exists()){
                        try{
                            logFile.createNewFile();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }

                    try {
                        Date date = new Date(System.currentTimeMillis());
                        DateFormat dateFormat = new DateFormat();
                        BufferedWriter buf = new BufferedWriter(new FileWriter(USILog.BURN_IN_LOG_FILE, true),8192);
                        if(s.indexOf(":")!=-1){
                            buf.append(dateFormat.format("yyyy-MM-dd hh:mm:ss", date).toString());
                            buf.append(new String(s.substring(s.indexOf(":"), s.length())));
                        }else
                            buf.append(s);
                        buf.newLine();
                        buf.close();
                    }catch(Exception e){
                        Log.d("myTag", e.toString());
                    }

                }
                clearStream = localRuntime.exec(clear).getInputStream();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            Log.d("RUNIN", e1.toString());
        }

        Log.d("RUNIN", "Thread Stop");
    }


}
