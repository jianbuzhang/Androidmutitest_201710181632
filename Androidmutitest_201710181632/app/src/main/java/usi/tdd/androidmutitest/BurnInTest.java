package usi.tdd.androidmutitest;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static usi.tdd.androidmutitest.WifiHandler.TAG;
/**
 * Created by usi on 2017/9/19.
 */

public class BurnInTest extends Activity {

    private BluetoothAdapter mBT = BluetoothAdapter.getDefaultAdapter();
    private WifiManager mWifiManager;
    private WifiInfo mWifiInfo;
    public static  boolean recoding=false;
    public static final String mBurninConfigFile = "/storage/emulated/0/BurninConfig.xml";
    private List<TestItem> mItemList;
    private long mBurninTime;
    private int mItemDelay, mCycleDelay;
    private String mRunType = "false";
    private long mBurninStartTime;
    private String[] mListStrings;
    private int iPassRecoder[];
    private int iFailRecoder[];
    private int iPassRate = 100;
    EditText edText;
    ListView list;
    TextView tv_LoopCount;
    TextView tv_RunTime;
    //private String address = "172.18.41.101";// �s�u��ip
    //private int port = 4000;// �s�u��port
    //DataReceiver dataReceiver;
    int levels[];
    int delay = 0;
    String pk_name = "";
    String cls_name = "";
    String param_value = "";
    //USILog usilog = new USILog(this);
    Button btn_test;
    Button btn_stop;
    Button btn_exit;
    boolean bReturnFlag = false;
    boolean bStopManual = false;
    int iCount = 0;
    int iItemCount = 0;
    int iLoopCount = 0;
    boolean mStartTest = true;
    boolean mIsTesting = false;
    Thread thRunTestItems;
    Thread thItemDelayTime,thCycleDelayTime;
    LinearLayout background;
    String strResult = "FAIL";
    BurninLogRecorder burninLogRecorder = new BurninLogRecorder(this);
    protected PowerManager.WakeLock mWakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        iniUI();

        //************************************************************************
        //1. BT/WLAN Power Always Open
        if (!mBT.isEnabled()) {
            mBT.enable();
        }

        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
        /*
        mWifiManager = new WifiAdmin()
        mWifiAdmin=new WifiAdmin(BurnInTest.this);
        mWifiAdmin.openWifi();*/
        //2. Screen always On, CPU always on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        //************************************************************************


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        9527);
            }
        } else {
            burnIN();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 9527: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    burnIN();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void burnIN() {
        new Thread(burninLogRecorder).start();

        mBurninStartTime = System.currentTimeMillis();
        final CheckBox chbox = (CheckBox) findViewById(R.id.burnin_checkBox1);
        mItemList = getBurninItems();
        Log.d("mItemList", "" + mItemList.size());
        list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        list.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_multiple_choice, mListStrings));

        iPassRecoder = new int[iItemCount];
        iFailRecoder = new int[iItemCount];

        for (int i = 0; i < iItemCount; i++) {
            if (mItemList.get(i).mCheck.equals("true")) {
                list.setItemChecked(i, true);
            }
            iPassRecoder[i] = 0;
            iFailRecoder[i] = 0;

        }
        list.setSelection(0);

        chbox.setOnClickListener(new CheckBox.OnClickListener() {
            public void onClick(View v) {
                if (chbox.isChecked()) {
                    edText.append("Select All....\r\n");
                    for (int i = 0; i < iItemCount; i++) {
                        list.setItemChecked(i, true);
                    }
                } else {
                    edText.append("Non Select ....\r\n");
                    for (int i = 0; i < iItemCount; i++) {
                        list.setItemChecked(i, false);
                    }
                }
            }

        });

        btn_test.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View arg0) {
                iCount = 0;
                iLoopCount = 1;
                mStartTest = true;
                USILog.append(this, "BurnIn Test Start.\r\n");
                tv_LoopCount.setText("Loop = 1");
                while (!list.isItemChecked(iCount)) {
                    if (iCount < iItemCount) {
                        iCount = iCount + 1;
                    } else if (iCount >= iItemCount) {
                        iCount = 0;
                        iLoopCount = iLoopCount + 1;
                    }
                }

                thItemDelayTime = new Thread(new ItemDelayTime());
                thItemDelayTime.start();

                thRunTestItems = new Thread(new RunTestItems());
                thRunTestItems.start();
            }
        });


        btn_stop.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View arg0) {
                bStopManual = true;

                edText.append("BurnIn Test Stop.");
                mStartTest = false;
                long cst = System.currentTimeMillis();
                long bt = cst - mBurninStartTime;
                long lHH = 0;
                long lMM = 0;
                long lSS = 0;
                lHH = bt / 1000 / 3600;
                lMM = (bt / 1000 - (lHH * 3600)) / 60;
                lSS = (bt / 1000 - (lHH * 3600) - (lMM * 60));
                tv_RunTime.setText("RunTime: " + lHH + "h - " + lMM + "m - " + lSS + "s");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < iItemCount; i++) {
                    if (list.isItemChecked(i)) {
                        float CountRate = ((float) iPassRecoder[i] / (iPassRecoder[i] + iFailRecoder[i])) * 100;
                        USILog.append(this, mListStrings[i] + " CountRate : " + CountRate);
                        USILog.append(this, mListStrings[i] + " Pass times : " + iPassRecoder[i]);
                        USILog.append(this, mListStrings[i] + " Fail times : " + iFailRecoder[i]);
                        iPassRate = Integer.parseInt(mItemList.get(i).mPassRate.toString());
                        if (CountRate >= iPassRate) {
                            if (strResult.equals("FAIL")) {
                                break; //���ݭn�A���
                            }
                            strResult = "PASS";
                            USILog.append(this, mListStrings[i] + "Set : " + iPassRate + "(>=) Pass Rate : " + CountRate);
                        } else {
                            strResult = "FAIL";
                            USILog.append(this, mListStrings[i] + "Set : " + iPassRate + "(<) Pass Rate : " + CountRate);
                            break; //���ݭn�A���
                        }
                    }
                }

                if (strResult.equals("PASS")) {
                    new AlertDialog.Builder(BurnInTest.this).setTitle("Test Result")
                            .setMessage("BurnIn Test PASS!")
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialoginterface, int i) {
                                            finish();
                                        }
                                    }).show();
                } else {
                    new AlertDialog.Builder(BurnInTest.this).setTitle("Test Result")
                            .setMessage("BurnIn Test FAIL!")
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialoginterface, int i) {
                                            finish();
                                        }
                                    }).show();
                }
                onStop();

            }
        });

        btn_exit.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View arg0) {
                mStartTest = false;
                thRunTestItems.stop();
                //thRunTestItems.destroy();

                thItemDelayTime.stop();
                thCycleDelayTime.stop();
                //thDelayTime.destroy();

                USILog.append(this, "BurnIn Test Close.\r\n");
                finish();

            }
        });

        //�P�_�O�_�ѼƦ۰ʰ���
        if (mRunType.equals("true")) {
            btn_test.performClick();
            edText.append("Start Auto Run BurnIn Test....\r\n");
        } else
            edText.append("Please Manual to Click Test....\r\n");
    }

    private void iniUI() {
        setContentView(R.layout.main_burnin);

        String szPath = this.getFilesDir().getAbsolutePath();
        File file = new File(szPath + "/" + BurnInTest.BURNIN_STORAGE);
        Log.d("MFG_TEST", "Summary LogFile: " + file);
        if (file.exists())
            file.delete();

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();
        list = (ListView) findViewById(R.id.list);
        edText = (EditText) findViewById(R.id.burnin_userText);
        btn_test = (Button) findViewById(R.id.burnin_btn_Test);
        btn_stop = (Button) findViewById(R.id.burnin_btn_Stop);
        btn_exit = (Button) findViewById(R.id.burnin_btn_Exit);
        tv_LoopCount = (TextView) findViewById(R.id.burnin_tV_LoopCount);
        tv_RunTime = (TextView) findViewById(R.id.burnin_tV_RunTime);
        edText.setText("");
        tv_LoopCount.setText("Loop = 0");
        tv_RunTime.setText("RunTime: 0h - 0m - 0s");
        burninLogRecorder.mIsRecord = true;
    }

    public List<TestItem> getBurninItems() {
        List<TestItem> items = new ArrayList<TestItem>();
        ;
        try {
            Log.d("getBurninItems: ", "beread");
            InputStream inStream = new FileInputStream(mBurninConfigFile);
            Log.d("getBurninItems: ", "read");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inStream);
            Element root = document.getDocumentElement();
            mBurninTime = Long.parseLong(root.getAttribute("time-minute")) * 60000;
            mItemDelay = Integer.parseInt(root.getAttribute("ItemDelay"));
            mCycleDelay = Integer.parseInt(root.getAttribute("CycleDelay"));
            mRunType = root.getAttribute("auto-run").toString();

            NodeList nodes = root.getElementsByTagName("test-item");

            if (nodes.getLength() < 1) {
                USILog.append("MFGUtil", "No test item found!!");
            } else {
                int count = nodes.getLength();
                iItemCount = count;
                mListStrings = new String[count];
                for (int i = 0; i < count; i++) {
                    TestItem ti = new TestItem();
                    ti.mName = ((Element) nodes.item(i)).getAttribute("name");
                    mListStrings[i] = i + 1 + "." + ((Element) nodes.item(i)).getAttribute("name");
                    ti.mStartType = ((Element) nodes.item(i)).getAttribute("start-type");
                    ti.mCheck = ((Element) nodes.item(i)).getAttribute("check");
                    ti.mNeedCheck = ((Element) nodes.item(i)).getAttribute("needcheck");
                    ti.mPassRate = ((Element) nodes.item(i)).getAttribute("pass-rate");
                    if (ti.mStartType.equals(TestItem.START_TYPE_ACTIVITY)) {
                        ti.mPackage = ((Element) nodes.item(i)).getAttribute("package");
                        ti.mClassName = ((Element) nodes.item(i)).getAttribute("classname");
                        //ti.mIntent.setClassName(ti.mPackage,ti.mPackage+ti.mClassName);
                        ti.mIntent.setComponent(new ComponentName(ti.mPackage, ti.mPackage + ti.mClassName));
                        ti.mAction = ((Element) nodes.item(i)).getAttribute("action").trim();
                        ti.mIntent.setAction(ti.mAction);
                    }

                    NodeList nodesExtra = ((Element) nodes.item(i)).getElementsByTagName("extra");
                    int exCount = nodesExtra.getLength();
                    for (int j = 0; j < exCount; j++) {
                        String key = ((Element) nodesExtra.item(j)).getAttribute("key");
                        String value = ((Element) nodesExtra.item(j)).getAttribute("value");
                        ti.mIntent.putExtra(key, value);
                        USILog.append(this, key + "=" + value);
                    }
                    items.add(ti);
                }

            }

        } catch (Exception er) {
            USILog.append("Burn-in", er.getMessage());
        }

        return items;
    }

    public class RunTestItems implements Runnable {
        @Override
        public void run() {
            while (mStartTest) {
                if (!mIsTesting) {
                    if (list.isItemChecked(iCount)) {
                        if (mItemList.get(iCount).mStartType.equals(TestItem.START_TYPE_ACTIVITY)) {
                            if(!recoding)
                            {
                                new Thread(burninLogRecorder).start();
                            }
                            recoding=false;
                            mIsTesting = true;
                            startActivityForResult(mItemList.get(iCount).mIntent, 1);
                        }
                    }
                }

            }
        }

    }

    public class ItemDelayTime implements Runnable {

        @Override
        public void run() {
            if (mIsTesting) {
                try {
                    USILog.append(this, "Item Delay :" + mItemDelay);
                    Thread.sleep(mItemDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mIsTesting = false;
            }

        }

    }

    public class CycleDelayTime implements Runnable {

        @Override
        public void run() {
            if (mIsTesting) {
                try {
                    USILog.append(this, "Cycle Delay :" + (mCycleDelay + mItemDelay));
                    Thread.sleep(mCycleDelay + mItemDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mIsTesting = false;
            }

        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mItemList.get(iCount).mNeedCheck.equals("true")) {
            Bundle bdata = data.getExtras();
            if (bdata != null) {
                String burninLog = bdata.getString("BURNIN_LOG");
                if (burninLog != null)
                    USILog.append(this, burninLog);

                int iResult = data.getIntExtra("TEST_RESULT", -1);
                USILog.append(this, "TEST_RESULT: " + iResult);

                if (iResult == 0) //PASS
                {
                    testItemPass();

                } else if (iResult == 1) //FAIL
                {
                    testItemFail();
                } else if (iResult == -1) //FAIL
                {
                    USILog.append(this, "iResult -1");
                    testItemFail();
                }

            } else {
                USILog.append(this, "onActivityResult : data.getExtras() is null.");
            }
        } else if (mItemList.get(iCount).mNeedCheck.equals("false"))

        {
            testItemPass();
        } else {
            USILog.append(this, "onActivityResult :mNeedCheck is null.");

        }


        long cst = System.currentTimeMillis();
        long bt = cst - mBurninStartTime;
        long lHH = 0;
        long lMM = 0;
        long lSS = 0;
        lHH = bt / 1000 / 3600;
        lMM = (bt / 1000 - (lHH * 3600)) / 60;
        lSS = (bt / 1000 - (lHH * 3600) - (lMM * 60));
        tv_RunTime.setText("RunTime: " + lHH + "h - " + lMM + "m - " + lSS + "s");

        if (bt > mBurninTime) {
            Log.d("MFG_TEST", "Stop BurnIn Test");
            edText.append("Stop BurnIn Test");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mStartTest = false;
            burnInResultSave();


            for (int i = 0; i < iItemCount; i++) {
                if (list.isItemChecked(i)) {
                    float CountRate = ((float) iPassRecoder[i] / (iPassRecoder[i] + iFailRecoder[i])) * 100;
                    USILog.append(this, mListStrings[i] + " CountRate : " + CountRate);
                    USILog.append(this, mListStrings[i] + " Pass times : " + iPassRecoder[i]);
                    USILog.append(this, mListStrings[i] + " FAIL times : " + iFailRecoder[i]);
                    iPassRate = Integer.parseInt(mItemList.get(i).mPassRate.toString());
                    if (CountRate >= iPassRate) {
                        if (strResult.equals("FAIL")) {
                            break;
                        }
                        strResult = "PASS";
                        USILog.append(this, mListStrings[i] + "Set : " + iPassRate + "(>=) Pass Rate : " + CountRate);
                    } else {
                        strResult = "FAIL";
                        USILog.append(this, mListStrings[i] + "Set : " + iPassRate + "(<) Pass Rate : " + CountRate);
                        break;
                    }
                }
            }


            if (strResult.equals("PASS")) {
                new AlertDialog.Builder(BurnInTest.this).setTitle("Test Result")
                        .setMessage("BurnIn Test Finished!!")
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialoginterface, int i) {
                                        finish();
                                    }
                                }).show();
            } else {
                new AlertDialog.Builder(BurnInTest.this).setTitle("Test Result")
                        .setMessage("BurnIn Test Finished!")
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialoginterface, int i) {
                                        finish();
                                    }
                                }).show();
            }
        }

        System.gc();
        Log.d("onActivityResult: ",iCount+","+iItemCount);
        if (iCount < iItemCount-1) {
            iCount = iCount + 1;
            thItemDelayTime = new Thread(new ItemDelayTime());
            thItemDelayTime.start();
        }else if (iCount < iItemCount) {
            iCount = iCount + 1;
        } else if (iCount >= iItemCount) {
            iCount = 0;
            iLoopCount = iLoopCount + 1;

        }

        while (!list.isItemChecked(iCount)) {
            if (iCount < iItemCount) {
                iCount = iCount + 1;

            } else if (iCount >= iItemCount) {
                iCount = 0;
                iLoopCount = iLoopCount + 1;
                thCycleDelayTime = new Thread(new CycleDelayTime());
                thCycleDelayTime.start();
            }
        }
    }

    private void testItemFail() {
        iFailRecoder[iCount] = iFailRecoder[iCount] + 1;
        mItemList.get(iCount).addFailCount();
        edText.append(mListStrings[iCount] + " FAIL TEST : " + iFailRecoder[iCount] + "\r\n");
    }

    private void testItemPass() {
        iPassRecoder[iCount] = iPassRecoder[iCount] + 1;
        mItemList.get(iCount).addPassCount();
        edText.append(mListStrings[iCount] + " SUCCESSFUL TEST : " + iPassRecoder[iCount] + "\r\n");
        tv_LoopCount.setText("Loop = " + iLoopCount);
    }

    public static String BURNIN_STORAGE = "BurnIn_Summary";

    public void burnInResultSave() {
        USILog.append(this, "Save Burn-in result");
        try {
            String path = this.getFilesDir().getAbsolutePath();
            File file = new File(path + "/" + BURNIN_STORAGE);
            Properties properties = new Properties();
            for (int i = 0; i < mItemList.size(); i++) {
                properties.setProperty(mItemList.get(i).mName + "_TestCount", Integer.toString(mItemList.get(i).TestCount));
                properties.setProperty(mItemList.get(i).mName + "_PassCount", Integer.toString(mItemList.get(i).PassCount));
                properties.setProperty(mItemList.get(i).mName + "_FailCount", Integer.toString(mItemList.get(i).FailCount));
            }
            FileOutputStream stream = new FileOutputStream(file, false);
            properties.store(stream, "");
            //properties.store(mOutputStream, "");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        burninLogRecorder.mIsRecord = false;
        finish();
        super.onDestroy();
        USILog.append(this, "BurnIn onDestroy()");
    }


}
