package usi.tdd.androidmutitest;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by usi on 2017/9/21.
 */

public class TActivity extends AppCompatActivity {
    public final static int TEST_RESULT_PASS = 0;
    public final static int TEST_RESULT_FAIL = 1;
    public final static int MESSAGE_APPEND_LOG = 1;
    public String mItemName;
    public int mTestResult;
    public TextView mTextCaption;
    public LogTextBox mTextLog;
    public String mCaption;
    public Context mCtx;
    public List<String> mLogs = new ArrayList<String>();
    public boolean mFinishOnSetTestResult = false;
    public long mDelayBeforeFinish = 1500;

    int defTimeOut = 0;
    protected PowerManager.WakeLock mWakeLock;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_burninresult);
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();
        defTimeOut = Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, 3000);
        if (Settings.System.canWrite(this)) {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 90000000);

        } else {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 200);
        }

        mTextCaption = (TextView) findViewById(R.id.T_TextCaption);
//		mTextLog=(LogTextBox)findViewById(R.id.T_TextLog);

        mCtx = this;
        Intent i = this.getIntent();
        Bundle b = i.getExtras();
        if (b != null) {
            String str = b.getString("AUTO_CLOSE");
            if (str != null) {
                if (str.toUpperCase().equals("TRUE")) {
                    mFinishOnSetTestResult = true;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 200)
            if (Settings.System.canWrite(this)) {
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 90000000);

            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 200);
            }
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        Bundle b = this.getIntent().getExtras();
        if (b != null) {
            String itemname = b.getString("ITEM_NAME");
            if (itemname != null) {
                mItemName = itemname;
            } else {
//                USILog.append(this, "No Paramter ITEM_NAME Found!!_Using default name");
                mItemName = this.getClass().getSimpleName();
            }
        } else {
//            USILog.append(this, "No Paramter ITEM_NAME Found!!_Using default name");
            mItemName = this.getClass().getSimpleName();
        }
    }


    public void appendLog(String str) {
        USILog.append(this, str);
        mLogs.add(str);
        Message message = new Message();
        message.what = TActivity.MESSAGE_APPEND_LOG;
        mHandler.sendMessage(message);
    }

    public void setCaption(String cap) {
        mCaption = cap;
        mTextCaption.setText(mCaption);
    }


    public void setItemPass() {
        // TODO Auto-generated method stub
//        USILog.appendSuccessful(mItemName);
        mTestResult = TEST_RESULT_PASS;
        Intent i = new Intent();
        Bundle b = new Bundle();
        b.putInt("TEST_RESULT", mTestResult);
        i.putExtras(b);
        this.setResult(RESULT_OK, i);
        if (mFinishOnSetTestResult) {
            new Thread(new CloseActivity()).start();
        }
    }

    public void setItemFail() {
        // TODO Auto-generated method stub
        USILog.appendUUTFail(mItemName);
        mTestResult = TEST_RESULT_FAIL;
        Intent i = new Intent();
        Bundle b = new Bundle();
        b.putInt("TEST_RESULT", mTestResult);
        i.putExtras(b);
        this.setResult(RESULT_OK, i);
        if (mFinishOnSetTestResult) {
            new Thread(new CloseActivity()).start();
        }
    }


    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_APPEND_LOG:
                    while (mLogs.size() > 0) {
                        if (mLogs.get(0) != null) {
                            //               			  mTextLog.append(mLogs.get(0));
//                			  mTextLog.append("\n");
                        }
                        mLogs.remove(0);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        this.mWakeLock.release();
        if (Settings.System.canWrite(this))
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, defTimeOut);
    }

    public class CloseActivity implements Runnable {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                Thread.sleep(mDelayBeforeFinish);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            ((TActivity) mCtx).finish();

        }

    }
}
