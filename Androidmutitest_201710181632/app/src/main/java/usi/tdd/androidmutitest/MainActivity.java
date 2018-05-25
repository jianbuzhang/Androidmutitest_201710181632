package usi.tdd.androidmutitest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.AidcManager.CreatedCallback;
import com.honeywell.aidc.BarcodeReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.os.Build.VERSION_CODES.M;
import static usi.tdd.androidmutitest.Setting.pass;

//import android.graphics.drawable.Drawable;

public class MainActivity extends TActivity {

    // SysInfo test - Add by Lynette:Only for Phoenix project
    public static final String[][] PROPERTY_KEYWORDS = {
            //MFG data related property
            //{"ro.hsm.extconf.num", "CONFIG_NUM"},
            //{"ro.hsm.extpart.num", "PART_NUM"},
            {"ro.hsm.assembly.date", "ASSEMBLY_DATE"},
            //{"ro.hsm.asset.num", "ID_ASSET_NUMBER"},
            {"ro.hsm.bt.addr", "BLUETOOTH_DEVICE_ADDRESS"},
            // {"ro.hsm.cal.accel.zero_offset", "ID_CAL_ACCEL_ZERO_OFFSET"},
            //{"ro.hsm.cal.gyro.zero_offset", "ID_CAL_GYRO_ZERO_OFFSET"},
            //{"ro.hsm.calibration.accelerator", "ID_CALIBRATION_ACCELERATOR"},
            //{"ro.hsm.calibration.gyro", "ID_CALIBRATION_GYRO"},
            //{"ro.hsm.calibration.magnet", "ID_CALIBRATION_MAGNET"},
            //{"ro.hsm.camera.enable_stamp", "ID_CAMERA_ENABLE_STAMP"},
            //{"ro.hsm.extserial.num", "EXTSERIAL_NUM"},
            //{"ro.hsm.file.id", "FILE_ID"},
            //{"ro.hsm.hw.has.gps", "HW_GPS"},
            //{"ro.hsm.hw.rev", "HW_REV"},
            //{"ro.hsm.mfg.ver", "MFG_VER"},
            //{"ro.hsm.mm.feature", "MM_FEATURE"},
            //{"ro.hsm.odm.num", "ODM_NUM"},
            //{"ro.hsm.reset.reason", "RESET_REASON"},
            //{"ro.hsm.imei.num", "IMEI_NUMBER"},
            //{"ro.hsm.meid.num", "MEID_NUMBER"},
            //{"ro.hsm.model.num", "ID_MODEL_NUMBER"},
            //{"ro.hsm.extserial.num", "ID_EX_SERIAL_NUMBER"},
            // Normal property
            //{"ro.hsm.ver.nonhlos", "NON HLOS images version"},
            //{"ro.hsm.ver.appsbl", "Application bootloader version"},
            //{"ro.hsm.ver.kernel", "linux kernel image version"},
            //{"ro.hsm.ver.recovery", "Recovery image version"},
            //{"ro.hsm.ver.system", "Android revision number"},
            //{"ro.hsm.wireless.feature", "WIRELESS_FEATURE"},
            //{"ro.hsm.wlan.addr", "ID_WLAN_MAC_ADDRESS"},
    };
    // End SysInfo test
    MainActivity mainActivityThis;
    String szBattStatus, szItemName, g_szQuery, g_szRSSI, g_szMax, g_szMin;
    String g_szSSID, g_szIP, g_szPingCount;
    String g_strStorage, g_szCliMAC, mCameraId, g_szWiFiAP, g_szWiFiPW, g_szWiFiDisconnect;
    String g_szWiFiAPMode;
    int g_nTimeDelay, g_nPassRate, g_nRandom, g_nMIN_SNR, g_nTTFF, g_nSAT_NUM;
    int g_nBrightLevel = 0, g_nBrightMax = 4;
    boolean g_bRet, bTestResult, g_bScanFinished, g_bIsBTActive, g_bWiFiConnection;
    static boolean g_bBoardViberateGoing = false;
    int[] g_nBrightness = {1, 50, 90, 140, 255}; // base on project
    File fileLogPath;
    Timer timer = new Timer();
    Sensor g_SenDef;
    SensorEventListener g_SensorListen;
    SensorManager g_sm;

    //Kevin 20170921
    int nBurnInDisCount;
    //Kevin

    public KeyDefine mTop[];
    public KeyDefine mLeft[];
    public KeyDefine mRight[];
    public KeyDefine mCenter[][];
    public KeyDefine mCurrentKey = null;
    public String TEST_TYPE = "O"; //N normal ; O order

    private int CAMERA_REQUEST = 1888;
    private HandlerThread mThread_RTC, mThread_Wifi, mThread_RSSI, mThread_Ping, mThread_FileSys, mThread_BT;
    private Handler mRTC_Handler, mWifi_Handler, mRSSI_Handler, mPing_Handler, mFileSys_Handler, mBT_Handler;
    private WifiManager g_mWifiManager;
    private CameraManager mCameraManager;


    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_CONNECTION_LOST = 6;
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private BluetoothAdapter g_mBT = BluetoothAdapter.getDefaultAdapter();
    private List<BluetoothDevice> g_mBTDevice;
    private String mConnectedDeviceName = null;
    private BluetoothChatService mChatService = null;
    private StringBuffer mOutStringBuffer;
    private static MainActivity g_sIntance;
    private static BarcodeReader barcodeReader;
    private AidcManager manager;


    //Kevin default of parameters

    //Keypad Test
    String g_szKeyType = "CN75AN5";

    //Audio Play Test - Speaker & Receiver
    String g_szAudType = "speaker";
    String g_szAudVol = "9";
    String g_szAudVolR = "15";

    //Audio Rec Test - Microphone
    String g_szRecTime = "3";
    String g_szRecFreq = "1000";
    String g_szRecRate = "8000";
    String g_szRecChannel = "1";
    String g_szRecSample = "16";
    String g_szRecPath = "storage/emulated/0";
    //String g_szRecFile = "record.wav"; // not able to modify
    // replay file path reuse g_szRecPath, volume reuse g_AudVol
    // need not to replay, user needs to click speaker test to play recorded result manually.

    //Touch Panel Test
    final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    //System Size
    String g_szSysStorage = "Flash";
    String g_szSysSize = "9625255936";

    //File Access
    String g_szFileType = "Find";
    String g_szFilePath = "storage/emulated/0/MFGDefine.xml";

    //GPS
    String g_szGpsSnr = "40";
    String g_szGpsTtff = "84000";
    String g_szGpsSat = "3";
    String szChkNMEA = "0";
	//20171225 for clear GPS location data
    String szClearData = "0";
    //Kevin End
    Intent cameraIntent;

    @SuppressLint("WifiManagerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Setting.mainActivity = this;
        mainActivityThis = MainActivity.this;
        Intent intent = this.getIntent();
        String action = intent.getAction();
        fileLogPath = this.getCacheDir();

        Log.d("MFG_TEST", "multi test\r\n");

        if (action == "android.intent.action.MAIN") {

            requestWindowFeature(Window.FEATURE_NO_TITLE);

        }

        if (action != "DisplayTest" && action != "Touch" && action != "Keypad" && action != "Camera" && action != "BurnIn") {
            inilayout(savedInstanceState);

        }
        if (action == "CommandAgentTest") {
            Log.d("MFG_TEST", "action is: " + action + "\r\n");
            Log.d("MFG_TEST", "CommandAgentTestPASS\r\n");
            finish();
        } else if (action == "permission") {
            Log.d("MFG_TEST", "action is: " + action + "\r\n");
            setTitle("permission Testing...");
            getAllPermission();
        } else if (action == "Version") {
            Bundle bundle = intent.getExtras();
            String VersionCheck = bundle.getString("versionName");
            if (VersionCheck == null){
                Log.d("MFG_TEST", "Need version input for comparison!! UUT-FAIL\r\n");
                finish();
            }
            String appVersion;
            PackageManager packageManager = this.getPackageManager();
            try {
                PackageInfo info = packageManager.getPackageInfo(this.getPackageName(), 0);
                appVersion = info.versionName; //版本名
                Log.d("MFG_TEST", "appVersion = " + appVersion + "\r\nversionName = " + VersionCheck + "\r\n");
                if (appVersion.equals(VersionCheck)){
                    Log.d("MFG_TEST", "Version check pass!! Successful Test!!\r\n");
                } else {
                    Log.d("MFG_TEST", "Version check fail!! UUT-FAIL\r\n");
                }
                finish();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

        } else if (action == "getCurrentTime") {
            Log.d("MFG_TEST", "action is: " + action + "\r\n");
            setTitle("getCurrentTime Testing...");
            Log.d("MFG_TEST", "VARREAL TIME1 = " + System.currentTimeMillis() + "\r\n");
            finish();
        } else if (action == "Vibrate") {
            Log.d("MFG_TEST", "action is: " + action + "\r\n");
            setTitle("Vibrator Testing...");
            myVibratorTest();
        } else if (action == "BoardVibrate") {
            Bundle bundle = intent.getExtras();
            g_bBoardViberateGoing = bundle.getString("go").equals("ON");
            Log.d("MFG_TEST", "action is: " + action + "\r\n");
            setTitle("BoardVibrate Testing...");
            boardVibratorTest();
            finish();
        } else if (action == "BurnInVibrate") {
            int vTime = 2000;
            Vibrator myVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
            myVibrator.vibrate(vTime);
            try {
                Thread.sleep(vTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finish();
        } else if (action == "BarcodeTest") {
            //allen barcode
            AidcManager.create(this, new CreatedCallback() {

                @Override
                public void onCreated(AidcManager aidcManager) {
                    manager = aidcManager;
                    barcodeReader = manager.createBarcodeReader();
                }
            });
            //allen barcode
            Intent barcodeIntent = new Intent();
            barcodeIntent.setClass(MainActivity.this, AutomaticBarcodeActivity.class);
            Bundle bundle = intent.getExtras();
            barcodeIntent.putExtra("Data", bundle.getString("Data","ABCDEF"));
			//20171002 - Check
            barcodeIntent.putExtra("Auto", bundle.getString("Auto","false"));
			//20171002
            startActivity(barcodeIntent);
        }
        else if (action == "BarcodeTestTXT") {
            Bundle bundle = intent.getExtras();
            final String g_sData=bundle.getString("Data","ABCDEF");
            final EditText editText=(EditText)findViewById(R.id.editTextScanInput);
            editText.requestFocus();
            TextView.OnEditorActionListener onEditorActionListener=new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if(keyEvent.getAction()==KeyEvent.ACTION_DOWN && keyEvent.getScanCode()==28)
                    {
                        Log.d("MFG_TEST", "Scan Input:"+editText.getText().toString()+"\r\n");
                        if(editText.getText().toString().equals(g_sData))
                        {
                            Log.d("MFG_TEST", "SUCCESSFUL TEST\r\n");
                            editText.setText("");
                        }
                        else
                        {
                            Log.d("MFG_TEST", "UUT-FAIL\r\n");
                        }
                        return true;
                    }
                    return false;
                }
            };
            editText.setOnEditorActionListener(onEditorActionListener);
        }else if (action == "WiFiConnect") {
            Log.d("MFG_TEST", "action is: " + action + "\r\n");

            Bundle bundle = intent.getExtras();
            g_szWiFiAP = bundle.getString("AP");
            g_szWiFiPW = bundle.getString("PW");
            g_szWiFiDisconnect = bundle.getString("Disconnect");
            g_szWiFiAPMode = bundle.getString("APMode");
            Log.d("MFG_TEST", "g_szWiFiAP: " + g_szWiFiAP);
            Log.d("MFG_TEST", "g_szWiFiPW: " + g_szWiFiPW);
            Log.d("MFG_TEST", "g_szWiFiDisconnect: " + g_szWiFiDisconnect);
            Log.d("MFG_TEST", "g_szWiFiAPMode: " + g_szWiFiAP);

            WifiConnectTest(g_szWiFiAP, g_szWiFiPW, g_szWiFiDisconnect, g_szWiFiAPMode);
            finish();

        } else if (action == "BatteryInfo") {
            Log.d("MFG_TEST", "action is: " + action + "\r\n");

            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                szBattStatus = bundle.getString("Status", "Pass");
            } else
                szBattStatus = "Pass";
            registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

            finish();
        } else if (action == "RTC_TEST") {
            Log.d("MFG_TEST", "action is: " + action + "\r\n");
            //Log.d("MFG_TEST", "RTC Test Start\r\n");

            Bundle bundle = intent.getExtras();
            szItemName = bundle.getString("ItemName");

            g_nTimeDelay = Integer.parseInt(bundle.getString("TIME_DELAY"));
            if (g_nTimeDelay > 0) {
                mThread_RTC = new HandlerThread("RTCTest");
                mThread_RTC.start();
                mRTC_Handler = new Handler(mThread_RTC.getLooper());
                mRTC_Handler.post(RTCTest_Thread);
            }
            finish();
        } else if (action == "GetDateTime"){
            Log.d("MFG_TEST", "action is: " + action + "\r\n");
            Bundle bundle = intent.getExtras();
            String strCumulative = bundle.getString("Times","");

            strCumulative = "/storage/emulated/0/" + strCumulative + ".txt";

            Calendar ca = Calendar.getInstance();
            SimpleDateFormat dfAll = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String strDate = "RTC_TIME=" + dfAll.format(ca.getTime());
            Log.d("MFG_TEST", "Date Time: " + strDate);

            try{
                FileOutputStream output = new FileOutputStream(strCumulative);
                output.write(strDate.getBytes());
                output.close();
            } catch (Exception e){
                e.printStackTrace();
            }
            finish();
        } else if (action == "WLAN") {
            Log.d("MFG_TEST", "action is: " + action + "\r\n");
            //Log.d("MFG_TEST", "WLAN Test Start\r\n");

            Bundle bundle = intent.getExtras();
            String strChkMAC = bundle.getString("ChkMAC", "");
            String strEnable = bundle.getString("EnPwr", "");
            String strDis = bundle.getString("DisPwr", "");
            String strRead = bundle.getString("Read", "");
            g_szQuery = bundle.getString("Query", "");
            szItemName = bundle.getString("ItemName", "NoName");
            g_szRSSI = bundle.getString("RSSI", "");
            g_szMax = bundle.getString("Max", "");
            g_szMin = bundle.getString("Min", "");
            g_szSSID = bundle.getString("SSID", "");
            g_szIP = bundle.getString("IP", "");
            g_szPingCount = bundle.getString("PingCount", "5");
            String szTemp = bundle.getString("PassRate", "80");
            g_nPassRate = Integer.parseInt(szTemp);

            g_mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            WifiTest wifiTest = new WifiTest();
            if ((!g_szSSID.isEmpty()) && (!g_szIP.isEmpty())) {
                mThread_Ping = new HandlerThread("WifiPing");
                mThread_Ping.start();
                mPing_Handler = new Handler(mThread_Ping.getLooper());
                mPing_Handler.post(WifiPing_Thread);
            } else if (!g_szRSSI.isEmpty()) {
                mThread_RSSI = new HandlerThread("WifiRSSI");
                mThread_RSSI.start();
                mRSSI_Handler = new Handler(mThread_RSSI.getLooper());
                mRSSI_Handler.post(WifiRSSI_Thread);
            } else if (!g_szQuery.isEmpty()) {
                mThread_Wifi = new HandlerThread("WifiQuery");
                mThread_Wifi.start();
                mWifi_Handler = new Handler(mThread_Wifi.getLooper());
                mWifi_Handler.post(WifiQuery_Thread);
            } else if (strChkMAC.length() == 12) {
                bTestResult = wifiTest.checkWifiMAC(fileLogPath, szItemName, strChkMAC, g_mWifiManager);
                finish();
            } else if (strDis.toUpperCase().equals("TRUE")) {
                bTestResult = wifiTest.DisWifi(fileLogPath, szItemName, g_mWifiManager);
                finish();
            } else if (strEnable.toUpperCase().equals("TRUE")) {
                bTestResult = wifiTest.EnableWifi(fileLogPath, szItemName, g_mWifiManager);
                finish();
            } else if (strRead.toUpperCase().equals("TRUE")) {
                wifiTest.ReadWifiMAC(g_mWifiManager);
                finish();
            }
        } else if (action == "FileSystem") {
            Bundle bundle = intent.getExtras();
            g_strStorage = bundle.getString("Storage", ""); //SD, Flash

            Log.d("MFG_TEST", "action is: " + action + " ,Storage is: " + g_strStorage + "\r\n");

            if (g_strStorage != null) {
                mThread_FileSys = new HandlerThread("FileSysTest");
                mThread_FileSys.start();
                mFileSys_Handler = new Handler(mThread_FileSys.getLooper());
                mFileSys_Handler.post(FileSys_Thread);
            }

        } else if (action == "SystemSize") {
            Bundle bundle = intent.getExtras();
            String strStorage = bundle.getString("Storage"); //SD, Flash, <Path>
            String strSize = bundle.getString("Size");
            Log.d("MFG_TEST", "action is: " + action + ", Storage is: " + strStorage + "\r\n");

            FileSystemTest FSTest = new FileSystemTest();
            if (strStorage != null && strSize != null)
                FSTest.FileSystemSizeCheck(strStorage, strSize, mainActivityThis);
            finish();
        } else if (action == "FileAccess") {
            Bundle bundle = intent.getExtras();
            String szType = bundle.getString("Type");
            String szPath = bundle.getString("Path");
            Log.d("MFG_TEST", "action is: " + action + ", Type is: " + szType);

            FileSystemTest FSTest = new FileSystemTest();
            FSTest.FileAccess(szType, szPath);
            finish();
        } else if (action == "Backlight") {
            Log.d("MFG_TEST", "action is: " + action + "\r\n");
            setTitle("Brightness Testing...");
            try {
                android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
            } catch (Settings.SettingNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                Random random = new Random();
                g_nRandom = random.nextInt(2);
            } else
                g_nRandom = 4;
            Log.d("MFG_TEST", "Start Brightness Testing...\r\n");
            timer.schedule(BrightTask, 0, 1000); //設定timer(BrightTask執為執內容，0代表立即開始，間格1ms執行一次)
        } else if (action == "Sensor") {
            // Kevin 20170915
            Bundle bundle = intent.getExtras();
            String szType = bundle.getString("Type"); // GYROscope, ACCelerometer, PREssure, MAGnetic, PROximity, LIGht
            Log.d("MFG_TEST", "Sensor Test - " + szType);
            if (szType.isEmpty()) {
                Log.d("MFG_TEST", "Wrong Parameters! \r\nUUT-FAIL\r\n");
                finish();
            }

            g_sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            Sensor mSensor = null;

            if (szType.equals("GYRO")) {
                List<Sensor> list = g_sm.getSensorList(Sensor.TYPE_GYROSCOPE);
                for (Sensor s : list) {
                    if (s.toString().indexOf("\"LSM6DSM Gyroscope\"") >= 0) {
                        Log.d("MFG_TEST", s.toString());
                        mSensor = s;
                    }
                }
            } else if (szType.equals("ACC")) {
                List<Sensor> list = g_sm.getSensorList(Sensor.TYPE_ACCELEROMETER);
                for (Sensor s : list) {
                    if (s.toString().indexOf("\"LSM6DSM Accelerometer\"") >= 0) {
                        Log.d("MFG_TEST", s.toString());
                        mSensor = s;
                    }
                }
            } else if (szType.equals("PRE")) {
                List<Sensor> list = g_sm.getSensorList(Sensor.TYPE_PRESSURE);
                for (Sensor s : list) {
                    if (s.toString().indexOf("\"LPS22HB Barometer\"") >= 0) {
                        Log.d("MFG_TEST", s.toString());
                        mSensor = s;
                    }
                }
            } else if (szType.equals("MAG")) {
                List<Sensor> list = g_sm.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
                for (Sensor s : list) {
                    if (s.toString().indexOf("\"AK09915 Magnetometer\"") >= 0) {
                        Log.d("MFG_TEST", s.toString());
                        mSensor = s;
                    }
                }
            } else if (szType.equals("PRO")) {
                List<Sensor> list = g_sm.getSensorList(Sensor.TYPE_PROXIMITY);
                for (Sensor s : list) {
                    Log.d("MFG_TEST", s.toString());
                    if (s.toString().indexOf("\"APDS-9930/QPDS-T930 Proximity & Light\"") >= 0) {
                        Log.d("MFG_TEST", s.toString());
                        mSensor = s;
                    }
                    if (s.toString().indexOf("\"TMx490x PROX\"") >= 0) {
                        Log.d("MFG_TEST", s.toString());
                        mSensor = s;
                    }
                }
            } else if (szType.equals("LIG")) {
                List<Sensor> list = g_sm.getSensorList(Sensor.TYPE_LIGHT);
                for (Sensor s : list) {
                    if (s.toString().indexOf("\"APDS-9930/QPDS-T930 Proximity & Light\"") >= 0) {
                        Log.d("MFG_TEST", s.toString());
                        mSensor = s;
                    }
                    if (s.toString().indexOf("\"TMx490x ALS\"") >= 0) {
                        Log.d("MFG_TEST", s.toString());
                        mSensor = s;
                    } else {
                    }
                }
            } else {
                Log.d("MFG_TEST", "Wrong Type! \r\nUUT-FAIL\r\n ");
                finish();
            }

            if (mSensor == null) {
                Log.d("MFG_TEST", "Selected sensor is not supported in this device.\r\nUUT-FAIL\r\n");
                finish();
            }

            g_SensorListen = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    StringBuilder sb = new StringBuilder();
                    for (float value : event.values){
                        sb.append(String.valueOf(value)).append(" ");
                    }
                    Log.d("MFG_TEST","SENSOR_DATA:  " + sb.toString());
                    Log.d("MFG_TEST", "SUCCESSFUL TEST");
                    g_sm.unregisterListener(g_SensorListen);
                    finish();
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    // TODO Auto-generated method stub
                }
            };
            g_sm.registerListener(g_SensorListen, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
            //Kevin
        } else if (action == "BurnIn") {
            Bundle bundle = intent.getExtras();
            String szFunc = bundle.getString("Func");
            Log.d("MFG_TEST", "BurnIn Start!!");
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            //Disable Adaptive brightness
            android.provider.Settings.System.putInt(getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            if (szFunc.toUpperCase().equals("DISPLAY_TEST")) {
                Log.d("MFG_TEST", "DISPLAY_TEST Start!!");
                requestWindowFeature(Window.FEATURE_NO_TITLE); //MUST need at "super.onCreate" before, if not will get error
                super.onCreate(savedInstanceState);
                setContentView(R.layout.display_color); //不可換 不然linearlayout會找不到
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

                //LinearLayout background = (LinearLayout)findViewById(R.id.display_color);

                final AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);

                nBurnInDisCount = 0;
                timer.schedule(BurnInDisplay, 0, 2000); //設定timer(BrightTask執為執內容，0代表立即開始，間格1ms執行一次)

		    	/*final DisplayTest displayTest = new DisplayTest();
                displayTest.BurnInDisplay( dlg, background, mainActivityThis);*/

            } else if (szFunc.toUpperCase().equals("AUDIO_PLAY")) {
                inilayout(savedInstanceState);
                Log.d("MFG_TEST", "Audio_Play Start!!");
                //final MediaPlayer mp = new MediaPlayer();
                MediaPlayer mp = null;
                AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

                mp = MediaPlayer.create(this, R.raw.v11k8b);
                mp.start();
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        // TODO Auto-generated method stub
                        mp.stop();
                        mp.reset();
                        mp.release();
                        mp = null;
                        USILog.append(this, "Speaker Playing Stop");
                        //USILog.append(this, "SpeakerTest SUCCESSFUL TEST");

                    }
                });
                this.setItemPass();
                finish();
            } else if (szFunc.toUpperCase().equals("BARCODE_TEST")) {
                inilayout(savedInstanceState);
                Log.d("MFG_TEST", "Barcode  Start!!");
                //allen barcode
                AidcManager.create(this, new CreatedCallback() {

                    @Override
                    public void onCreated(AidcManager aidcManager) {
                        manager = aidcManager;
                        barcodeReader = manager.createBarcodeReader();
                    }
                });
                //allen barcode
                Intent barcodeIntent = new Intent();
                barcodeIntent.setClass(MainActivity.this, AutomaticBarcodeActivity.class);
                barcodeIntent.putExtra("Data", bundle.getString("Data","ABCDEF"));
				// 20171002 - check 
                barcodeIntent.putExtra("Auto", bundle.getString("Auto","false"));
                startActivity(barcodeIntent);
				//20171002
            } else if (szFunc.toUpperCase().equals("CAMERA_TEST")) {
                inilayout(savedInstanceState);
                Log.d("MFG_TEST", "Camera  Start!!");
                Intent CameraIntent = new Intent();
                CameraIntent.setClass(MainActivity.this, AndroidCameraPreview.class);
                startActivity(CameraIntent);
                finish();
            }
        } else if (action == "BT") {
            Bundle bundle = intent.getExtras();
            String szChkMAC = bundle.getString("ChkMAC", "");
            String szEnPwr = bundle.getString("EnPwr", "");
            String szRead = bundle.getString("Read", "");
            szItemName = bundle.getString("ItemName", "NoName");
            g_szCliMAC = bundle.getString("CliMAC", "");
            String szConnect = bundle.getString("Connect", "");
            g_szQuery = bundle.getString("Query", "");
            String szDisPwr = bundle.getString("DisPwr", "");

            BTTest BTTest = new BTTest();
            if (szConnect.toUpperCase().equals("TRUE") && g_szCliMAC != null) {
                g_mBT.disable();
                SystemClock.sleep(3000);
                mThread_BT = new HandlerThread("BTConnect");
                mThread_BT.start();
                mBT_Handler = new Handler(mThread_BT.getLooper());
                mBT_Handler.post(BTConnect_Thread);
            } else if (szRead.toUpperCase().equals("TRUE")) {
                BTTest.ReadBTMAC(mainActivityThis);
            } else if (szChkMAC.contains(":")) {
                bTestResult = BTTest.chkBTMAC(fileLogPath, szItemName, szChkMAC);
            } else if (szEnPwr.toUpperCase().equals("TRUE")) {
                bTestResult = BTTest.EnBTPwr(fileLogPath, szItemName);
            } else if (szDisPwr.toUpperCase().equals("TRUE")) {
                bTestResult = BTTest.DisBTPwr(fileLogPath, szItemName);
            } else {
                if (!g_szQuery.isEmpty()) {
                    Log.d("MFG_TEST", "BT Query Start");
                    mThread_BT = new HandlerThread("BTQuery");
                    mThread_BT.start();
                    mBT_Handler = new Handler(mThread_BT.getLooper());
                    mBT_Handler.post(BTQuery_Thread);
                } else {
                    finish();

                }
            }
        } else if (action == "SysInfo") {
            Calendar ca = Calendar.getInstance();
            SimpleDateFormat dfAll = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String strDate = dfAll.format(ca.getTime());
            Log.d("MFG_TEST", "Date & Time: " + strDate);

            /*
            SimpleDateFormat dfYear = new SimpleDateFormat("yyyy");
            String strYear = dfYear.format(ca.getTime());
            //Log.d("MFG_TEST", strYear + "\r\n");
            if(!strYear.equals("2017"))
                Log.d("MFG_TEST", "The ta\r\n");
            */

            // 20161024 Lynette:sysInfo sample from HSM
            // 20170124 Lynette:add for checking version of common ES package
            String result = "SERVICE_PACK: " + (Build.DISPLAY.contains(".SP") ? Build.DISPLAY : "No Service Pack") + "\n"
                    + "RELEASE: " + Build.VERSION.RELEASE + "\n";
            for (int i = 0; i < PROPERTY_KEYWORDS.length; i++) {
                result += PROPERTY_KEYWORDS[i][1] + ": " + SystemPropertyAccess.getProperty(PROPERTY_KEYWORDS[i][0], null) + "\n";
            }
            Log.d("MFG_TEST", result);
            // Lynette ===

            Bundle bundle = intent.getExtras();
            // 20171017 Lynette: Modify for check two version
              String szTestMsg = bundle.getString("TestMsg", "FALSE");
            boolean bTestMsg = false;
            if (szTestMsg.toUpperCase().equals("TRUE"))
                bTestMsg = true;
            // 20171017 Lyentte End===

            // 20161125 Lynette:Modify to meet requirement of phoenix project
            /*
            String [][] szVersion = {{"BuildNumber", "ESN"     , "XLDR"    , "EA"      , "SSPAM"   , "IST"  , "Language", "Type", "Radio"   }, //item name
                                     {""           , ""        , ""        , ""        , ""        , ""     , ""        , ""    , ""        }, //check value
                                     {""           , ""        , ""        , ""        , ""        , ""     , ""        , ""    , ""        }, // get value
                                     {"VER01-05"   , "WRUUT-02", "VER01-16", "VER01-03", "VER01-13", "ACT01", "VER01-15", ""    , "VER02-01"}, //error code
                                     {""           , ""        , ""        , ""        , ""        , ""     , ""        , ""    , ""        }}; //Test result
            szVersion[1][0] = bundle.getString("BuildNumber", "" );
            szVersion[1][1] = bundle.getString("ESN", "");
            szVersion[1][2] = bundle.getString("XLDR", "");
            szVersion[1][3] = bundle.getString("EA", "");
            szVersion[1][4] = bundle.getString("SSPAM", "");
            szVersion[1][5] = bundle.getString("IST", "");
            szVersion[1][6] = bundle.getString("Language", "");
            szVersion[1][7] = bundle.getString("Type","");
            szVersion[1][8] = bundle.getString("Radio","");

            Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            SystemInfo sysInfo = new SystemInfo();
            szVersion[2][0] = Build.DISPLAY;
            szVersion[2][1] = Build.SERIAL;
            szVersion[2][2] = Build.BOOTLOADER;
            szVersion[2][3] = sysInfo.GetEA();//
            szVersion[2][4] = sysInfo.GetSSPAM();//
            szVersion[2][5] = sysInfo.GetISTVer(display);//
            szVersion[2][6] = getResources().getConfiguration().locale.toString();
            szVersion[2][7] = Build.TYPE; //eng or user mode
            szVersion[2][8] = Build.getRadioVersion();
            */

            String[][] szVersion = {{"BuildNumber", "ESN", "XLDR", "Language", "Type", "Radio", "CONFIG_NUM", "INCREMENTAL"},  // item name
                    {"", "", "", "", "", "", "", ""},  // check value
                    {"", "", "", "", "", "", "", ""},  // get value
                    {"VER01-05", "WRUUT-02", "VER01-16", "VER01-15", "", "VER02-01", "", "HHP15"},  // error code
                    {"", "", "", "", "", "", "", ""}}; // Test result
            szVersion[1][0] = bundle.getString("BuildNumber", "");
            szVersion[1][1] = bundle.getString("ESN", "");
            szVersion[1][2] = bundle.getString("XLDR", "");
            szVersion[1][3] = bundle.getString("Language", "");
            szVersion[1][4] = bundle.getString("Type", "");
            szVersion[1][5] = bundle.getString("Radio", "");
            szVersion[1][6] = bundle.getString("CONFIG_NUM", ""); // Add for phoenix project
            szVersion[1][7] = bundle.getString("INCREMENTAL", "");// Add for phoenix project

            SystemInfo sysInfo = new SystemInfo();
            szVersion[2][0] = Build.DISPLAY;
            szVersion[2][1] = Build.SERIAL;
            szVersion[2][2] = Build.BOOTLOADER;
            szVersion[2][3] = getResources().getConfiguration().locale.toString();
            szVersion[2][4] = Build.TYPE; //eng or user mode
            szVersion[2][5] = Build.getRadioVersion();
            szVersion[2][6] = SystemPropertyAccess.getProperty("ro.hsm.extconf.num", null);// Add for phoenix project
            szVersion[2][7] = Build.VERSION.INCREMENTAL; // Add for phoenix project

            g_bRet = sysInfo.CommpareVer(fileLogPath, szVersion, bTestMsg);
            finish();
        } else if (action == "CommonESPkg") // 20170124 Lynette: Add for confirm whether common pkg upgrade or not
        {
            Bundle bundle = intent.getExtras();
            String szVer = bundle.getString("Ver");
            int nTimeDur = Integer.parseInt(bundle.getString("TIME", "300000"));

            String szTestMsg = bundle.getString("TestMsg", "FALSE");
            boolean bTestMsg = false;
            if (szTestMsg.toUpperCase().equals("TRUE"))
                bTestMsg = true;

            SystemInfo ESInfo = new SystemInfo();
            //g_bRet = ESInfo.getESPkgInfo(mainActivityThis, szVer, true);
            int nIndex = 0;
            g_bRet = false;

            do {
                if ((nIndex % 60000) == 0) {
                    Log.d("MFG_TEST", "[" + Integer.toString(nIndex / 1000) + "]");
                    g_bRet = ESInfo.getESPkgInfo(mainActivityThis, szVer, true);
                    if (bTestMsg || g_bRet)
                        break;
                } else {
                    Log.d("MFG_TEST", "[" + Integer.toString(nIndex / 1000) + "] Wait for installation completed...");
                    g_bRet = ESInfo.getESPkgInfo(mainActivityThis, szVer, false);
                }

                if (nIndex >= nTimeDur)
                    break;

                nIndex += 10000;
                try {
                    Thread.sleep(10000); // sleep 10 sec
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (!g_bRet);

            if ((nIndex % 60000) != 0)
                g_bRet = ESInfo.getESPkgInfo(mainActivityThis, szVer, true);

            if (g_bRet)
                Log.d("MFG_TEST", "Check ES Version: SUCCESSFUL TEST\r\nAPK STOP\r\n");
            else if (bTestMsg)
                Log.d("MFG_TEST", "Check ES Version: Need reboot to upgrade.\r\nAPK STOP\r\n");
            else
                Log.d("MFG_TEST", "Check ES Version: Checked Fail!\r\nUUT-FAIL\r\n");

            finish();
        } else if (action == "DisplayTest") {
            Log.d("MFG_TEST", "Display");
            Bundle bundle = intent.getExtras();
            final String strFunc = bundle.getString("Func");

            requestWindowFeature(Window.FEATURE_NO_TITLE); //MUST need at "super.onCreate" before, if not will get error
            super.onCreate(savedInstanceState);
            getWindow().getDecorView().setSystemUiVisibility(flags);
            final View decorView = getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        decorView.setSystemUiVisibility(flags);
                    }
                }
            });
            setContentView(R.layout.display_color); //不可換 不然linearlayout會找不到

            LinearLayout background = (LinearLayout) findViewById(R.id.display_color);
            final DisplayTest displayTest = new DisplayTest();

            final AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
            displayTest.RunDisplay(strFunc, dlg, background, mainActivityThis);
        } else if (action == "Touch") {
            Bundle bundle = intent.getExtras();
            String szType = bundle.getString("Type");
            Log.d("MFG_TEST", "Touch Panel Test - " + szType);

            requestWindowFeature(Window.FEATURE_NO_TITLE); //MUST need at "super.onCreate" before, if not will get error
            super.onCreate(savedInstanceState);
            getWindow().getDecorView().setSystemUiVisibility(flags);
            final View decorView = getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        decorView.setSystemUiVisibility(flags);
                    }
                }
            });

            if (szType.toUpperCase().equals("POINT"))
                setContentView(new PanelPoint(this));
            else if (szType.toUpperCase().equals("LINE"))
                setContentView(new PanelLine(this));
        } else if (action == "AudioPlay") {
            Bundle bundle = intent.getExtras();
            String szType = bundle.getString("Type", g_szAudType); // receiver or speaker(default)
            String szVolume = bundle.getString("Vol", g_szAudVol); // default=9
            String szFile = bundle.getString("File");
            String szPath = bundle.getString("Path");

            Log.d("MFG_TEST", "Audio Test - " + szType);
            setTitle("Audio Testing...");

            //final MediaPlayer mp = MediaPlayer.create(MainActivity.this, R.raw.v11k8b);
            final MediaPlayer mp = new MediaPlayer();
            AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

            AudioTest AudPlay = new AudioTest();
            AudPlay.AudioPlayFile(mp, am, szType, szFile, szPath, szVolume, mainActivityThis);
            //else
            //    AudPlay.AudioPlayFile(mp, am, szType, szVolume, mainActivityThis);
        } else if (action == "AudioRec") {
            // Log.d("MFG_TEST", "action is: " + action + "\r\n");
            Log.d("MFG_TEST", "AudioRec Test Start\r\n");
            setTitle("AudioRec Testing...");

            Bundle bundle = intent.getExtras();
            String szTime = bundle.getString("Time", g_szRecTime);
            String szFreq = bundle.getString("Freq", g_szRecFreq);
            String szRate = bundle.getString("Rate", g_szRecRate);
            String szChannel = bundle.getString("Channel", g_szRecChannel);
            String szBit = bundle.getString("Sample", g_szRecSample);
            String szPath = bundle.getString("Path", g_szRecPath);

            AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            AudioTest AudRec = new AudioTest();
            AudRec.AudioRec(am, szTime, szFreq, szRate, szChannel, szBit, szPath);
            finish();
        } else if (action == "NewAudioRec") {

            {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //adb shell am start -n usi.tdd.androidmutitest/.MainActivity -a AudioPlay -e File AudioTest.wav -e Path storage/emulated/0 -e Vol 12

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String szType = "speaker"; // receiver or speaker(default)
                                String szVolume = "12"; // default=9
                                String szFile = "AudioTest.wav";
                                String szPath = "storage/emulated/0";
                                MediaPlayer mp = new MediaPlayer();
                                AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                                AudioTest AudPlay = new AudioTest();
                                AudPlay.AudioPlayFile(mp, am, szType, szFile, szPath, szVolume, mainActivityThis);
                            }
                        });

                    }
                }).start();
            }
            Log.d("MFG_TEST", "AudioRec Test Start\r\n");
            setTitle("AudioRec Testing...");

            Bundle bundle = intent.getExtras();
            String szTime = bundle.getString("Time", g_szRecTime);
            String szFreq = bundle.getString("Freq", g_szRecFreq);
            String szRate = bundle.getString("Rate", g_szRecRate);
            String szChannel = bundle.getString("Channel", g_szRecChannel);
            String szBit = bundle.getString("Sample", g_szRecSample);
            String szPath = bundle.getString("Path", g_szRecPath);

            AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            AudioTest AudRec = new AudioTest();
            AudRec.AudioRec(am, szTime, szFreq, szRate, szChannel, szBit, szPath);
            finish();
        }/*
        else if( action == "AudioLoopback")
        {
            Bundle bundle = intent.getExtras();
            String szTime = bundle.getString("Time", "5");
            String szFreq = bundle.getString("Freq", "1000");
            String szRate = bundle.getString("Rate", "8000");
            String szChannel = bundle.getString("Channel", "1");
            String szBit = bundle.getString("Sample", "16");
            String szPath = bundle.getString("Path", "Internal");

            AudioManager am = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

            AudioTest AudLoopback = new AudioTest();
            AudLoopback.AudioLoopback(am, szTime, szFreq, szRate, szChannel, szBit, szPath);
            finish();
        }*/ else if (action == "Keypad") {
            Log.d("MFG_TEST", "action is: " + action + "\r\n");
            Log.d("MFG_TEST", "Keypad Test Start\r\n");
            Bundle bundle = intent.getExtras();
            g_szKeyType = bundle.getString("Type", "");
            if (g_szKeyType != null) {
                requestWindowFeature(Window.FEATURE_NO_TITLE); //MUST need at "super.onCreate" before, if not will get error
                super.onCreate(savedInstanceState);
                // 20161028 Lynette:Modify for full screen
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getSupportActionBar().hide();
                // Lynette ===
                setContentView(R.layout.keypad);
                InitialKeyTest(g_szKeyType);
            } else
                Log.d("MFG_TEST", "No Key Type Parameter!!\r\nUUT-FAIL\r\n");
        }

        else if (action == "Camera") {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.camera);
            Log.d("MFG_TEST", "action is: " + action + "\r\n");

            Bundle bundle = intent.getExtras();
            //String szLight = bundle.getString("FLASH", "OFF");
            String szPath = bundle.getString("Path", "SD");
            if (szPath.toUpperCase().equals("SD")) {
                szPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                //szPath = Environment.getExternalStorageDirectory().toString();
            } else {
                szPath = "/" + szPath;
                Log.d("MFG_TEST", "Path Failed!!\r\n");
            }
            Log.d("MFG_TEST", "szPath is: " + szPath + "\r\n");
            fileLogPath = new File(szPath + "/" + "Photo.jpg");

            cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File tmpFile = new File(Environment.getExternalStorageDirectory(),"image.jpg");
            Uri outputFileUri = Uri.fromFile(tmpFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            //startActivityForResult(intent, 0);


            //ImageView imageView = (ImageView) this.findViewById(R.id.imageViewPic);
            Button photoButton = (Button) this.findViewById(R.id.buttonTakePic);
            photoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                Manifest.permission.CAMERA)) {
                        } else {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    9527);
                        }
                    } else {
                        testCamera();
                    }
                }
            });
            // Click to camera test
            photoButton.performClick();
            Log.d("MFG_TEST", "photoButton.performClick();");
			//20171002 - Kevin add for PCBA test
        } else if (action == "PCBAFlashLight") {
            Log.d("MFG_TEST", "action is: " + action + "\r\n");
            setTitle("FlashLight Testing...");

            Bundle bundle = intent.getExtras();
            final String szLight = bundle.getString("Type");

            //bLightOn = true;
            Boolean isFlashAvailable = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
            if (!isFlashAvailable) {
                Log.d("MFG_TEST", "Your device doesn't support flash light!\r\nUUT-FAIL\n\r");
                finish();
            }
            mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            try {
                mCameraId = mCameraManager.getCameraIdList()[0];
                Log.d("MFG_TEST", "mCameraId is: " + mCameraId + "\r\n");

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (szLight.toUpperCase().equals("ON")) {
                turnOnFlashLight();

               finish();
            } else {
                turnOffFlashLight();
                finish();
            }
			//20171002 - Kevin
        } else if (action == "FlashLight") {
            Log.d("MFG_TEST", "action is: " + action + "\r\n");
            Log.d("MFG_TEST", "FlashLight Test Start\r\n");
            setTitle("FlashLight Testing...");

            Bundle bundle = intent.getExtras();
            final String szLight = bundle.getString("Type");

            //bLightOn = true;
            Boolean isFlashAvailable = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
            if (!isFlashAvailable) {
                Log.d("MFG_TEST", "Your device doesn't support flash light!\r\nUUT-FAIL\n\r");
                finish();
            }
            mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            try {
                mCameraId = mCameraManager.getCameraIdList()[0];
                Log.d("MFG_TEST", "mCameraId is: " + mCameraId + "\r\n");

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (szLight.toUpperCase().equals("ON")) {
                turnOnFlashLight();
				
                final AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                DisplayTest DPTest = new DisplayTest();
                DPTest.EndPopDialog(dlg, action, "FlashLightTest is OK ?", mainActivityThis);
            } else {
                turnOffFlashLight();
                finish();
            }
        } else if (action == "GPS") {
            // Get parameter from LaunchCmd, and copy to GPSIntent
            Log.d("MFG_TEST", "action is: " + action + "\r\n");
            Bundle bundle = intent.getExtras();
            g_szGpsSnr = bundle.getString("MIN_SNR", g_szGpsSnr);
            g_szGpsTtff = bundle.getString("TTFF", g_szGpsTtff);
            g_szGpsSat = bundle.getString("SAT_NUM", g_szGpsSat);

            //20170428 Lynette: Add for checking NMEA
            szChkNMEA = bundle.getString("ChkNMEA", szChkNMEA);
			
            szClearData = bundle.getString("ClearData", szClearData);

            Intent GPSIntent = new Intent();
            GPSIntent.setClass(MainActivity.this, GPSTest.class);
            GPSIntent.putExtra("MIN_SNR", Integer.parseInt(g_szGpsSnr));
            GPSIntent.putExtra("TTFF", Integer.parseInt(g_szGpsTtff));
            GPSIntent.putExtra("SAT_NUM", Integer.parseInt(g_szGpsSat));
            GPSIntent.putExtra("ChkNMEA", Integer.parseInt(szChkNMEA));
            GPSIntent.putExtra("ClearData", Integer.parseInt(szClearData));
            //GPSIntent.putExtra("ChkNMEA", szChkNMEA);
            startActivityForResult(GPSIntent, 1);
            //20171002 - Allen remove, Kevin adds back
            MainActivity.this.finish();
        } else if (action=="IMSI") {
            TelephonyManager telephonyManager = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
            String szGetIMSI = telephonyManager.getSubscriberId();
            if(szGetIMSI==null)
            {
                szGetIMSI="null";
            }
            Log.d("MFG_TEST", "VARSTRING szGetIMSI=' " + szGetIMSI.replaceAll(" ","")+"'");
            Log.d("MFG_TEST", "Check Finish");
            finish();
        }
        else if (action == "PHONEMSG") {
            Bundle bundle = intent.getExtras();
            String szType = bundle.getString("Type", "");
            Log.d("MFG_TEST", "szType: " + szType);

            g_bRet = false;
            TelephonyManager telephonyManager = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
            if (szType.equals("IMEI")) {
                String szChkIMEI = bundle.getString("Num", "");
                //Log.d("MFG_TEST", "szChkIMEI: " + szChkIMEI);
                if (szChkIMEI.length() == 15)// Phone Type
                {
                    String[] phoneTypeArray = {"NONE", "GSM", "CDMA"};
                    Log.d("MFG_TEST", "Phone Type is:" + phoneTypeArray[telephonyManager.getPhoneType()]);
                    String szGetIMEI = telephonyManager.getDeviceId();
                    Log.d("MFG_TEST", "Get IMEI is: " + szGetIMEI);
                    Log.d("MFG_TEST", "Chk IMEI is: " + szChkIMEI);
                    g_bRet = szGetIMEI.equals(szChkIMEI);
                }
            } else if (szType.equals("IMSI")) {
                String szChkIMSI = bundle.getString("Num", "");
                Log.d("MFG_TEST", "VARSTRING szChkIMSI= " + szChkIMSI);

                String szGetIMSI = telephonyManager.getSubscriberId();
                Log.d("MFG_TEST", "VARSTRING szGetIMSI=' " + szGetIMSI.replaceAll(" ","")+"'");

                g_bRet = szChkIMSI.equals(szGetIMSI);
            } else if (szType.equals("MCC")) {
                String szChkMCC = bundle.getString("Num", "");
                Log.d("MFG_TEST", "szChkMCC= " + szChkMCC);

                String szGetIMSI = telephonyManager.getSubscriberId();
                String szGetMCC = szGetIMSI.substring(0, 3);
                Log.d("MFG_TEST", "SIM IMSI= " + szGetMCC);

                g_bRet = szChkMCC.equals(szGetMCC);
            } else if (szType.equals("SIMID")) {
                String szChkSIM = bundle.getString("Num", "");


                String szGetSIM = telephonyManager.getSimSerialNumber();

                Log.d("MFG_TEST", "Simcard= " + szGetSIM);
                g_bRet = szGetSIM.equals(szChkSIM);

                Log.d("MFG_TEST", "szChkSIM: " + szChkSIM);
                String[] simStateArray = {"UNKNOWN", "ABSENT", "PIN_REQUIRED", "PUK_REQUIRED", "NETWORK_LOCKED", "READY"};
                Log.d("MFG_TEST", "VARSTRING SIM_State = '" + simStateArray[telephonyManager.getSimState()] + "'\r\n");

                if (szChkSIM.equals("STATE")) {
                    finish();
                    System.exit(0);
                } else {
                }
            } else {
                Log.d("MFG_TEST", "Parameter error!\r\nUUT-FAIL\r\n");
                finish();
            }

            if (g_bRet)
                Log.d("MFG_TEST", "SUCCESSFUL TEST\r\n");
            else
                Log.d("MFG_TEST", "Check Fail!\r\nUUT-FAIL\r\n");
            finish();
        }
        // Lynette===
    }

    private void getAllPermission() {
      for(String per:Setting.perList)
      {
          Log.d("MFG_TEST", "check permission:"+per+"\r\n");
          if (ContextCompat.checkSelfPermission(this,
                  per)
                  != PackageManager.PERMISSION_GRANTED) {
              if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                      per)) {
              } else {
                  ActivityCompat.requestPermissions(this,
                          new String[]{per},
                          9958);
                  Log.d("MFG_TEST", "getting permission:"+per+"\r\n");
                  return;
              }
          }
      }
        Log.d("MFG_TEST", "SUCCESSFUL TEST\r\n");
        return;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 9527: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    testCamera();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case 9958:{
                Log.d("MFG_TEST", "check permission: on result\r\n");
                getAllPermission();
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void testCamera() {
        Log.d("MFG_TEST", "Camera Test Start\r\n");
        //Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    private void inilayout(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // End Create ==========================================================================================

    // Kevin 20170915
    @Override
    protected void onPause() {
        super.onPause();
        //解除感應器註冊
        if (g_sm != null)
            g_sm.unregisterListener(g_SensorListen);
    }
    // Kevin 20170915

    static BarcodeReader getBarcodeObject() {
        return barcodeReader;
    }


    // Kevin alertDialog
    public void alertDialog_keypad(View v) {
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        Log.d("MFG_TEST", "Enter alter dialog to input parameter(s) for keypad test...");
        LayoutInflater inflater = getLayoutInflater();
        final View customView = inflater.inflate(R.layout.alert_keypad, null);
        TextView type = (TextView) customView.findViewById(R.id.etType);
        type.setText(g_szKeyType);
        builder.setView(customView)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dig, int id) {
                        EditText edit = (EditText) customView.findViewById(R.id.etType);
                        g_szKeyType = edit.getText().toString().toUpperCase();
                        Log.d("MFG_TEST", "Keypad Type = " + g_szKeyType);
                        //
                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        getSupportActionBar().hide();
                        setContentView(R.layout.keypad);
                        InitialKeyTest(g_szKeyType);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dig, int id) {
                        //return to main
                    }
                });

        builder.setCancelable(true);
        android.support.v7.app.AlertDialog ad = builder.create();
        ad.show();
    }

    public void alertDialog_speaker(View v) {
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        Log.d("MFG_TEST", "Enter alter dialog to input parameter(s) for speaker test...");
        LayoutInflater inflater = getLayoutInflater();
        final View customView = inflater.inflate(R.layout.alert_audioplay, null);

        TextView vol = (TextView) customView.findViewById(R.id.etVolume);
        vol.setText(g_szAudVol);

        builder.setView(customView)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dig, int id) {
                        EditText edit3 = (EditText) customView.findViewById(R.id.etVolume);
                        g_szAudVol = edit3.getText().toString();
                        Log.d("MFG_TEST", "Speaker Volume = " + g_szAudVol);

                        // 20170412 Change for audio team
                        //final MediaPlayer mp = MediaPlayer.create(MainActivity.this, R.raw.oct2415sweep);
                        final MediaPlayer mp = MediaPlayer.create(MainActivity.this, R.raw.v11k8b);

                        AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                        AudioTest AudPlay = new AudioTest();
                        AudPlay.AudioPlayFile(mp, am, g_szAudType, g_szAudVol, mainActivityThis);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dig, int id) {
                        //return to main
                    }
                });

        builder.setCancelable(true);
        android.support.v7.app.AlertDialog ad = builder.create();
        ad.show();
    }

    public void alertDialog_receiver(View v) {
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        g_szAudType = "receiver";
        Log.d("MFG_TEST", "Enter alter dialog to input parameter(s) for receiver test...");
        LayoutInflater inflater = getLayoutInflater();
        final View customView = inflater.inflate(R.layout.alert_audioplayrec, null);

        TextView vol = (TextView) customView.findViewById(R.id.etVolume);
        vol.setText(g_szAudVolR);

        builder.setView(customView)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dig, int id) {
                        EditText edit3 = (EditText) customView.findViewById(R.id.etVolume);
                        g_szAudVolR = edit3.getText().toString();
                        Log.d("MFG_TEST", "Receiver Volume = " + g_szAudVolR);

                        final MediaPlayer mp = MediaPlayer.create(MainActivity.this, R.raw.v11k8b);
                        // 20170412 Change for audio team
                        //final MediaPlayer mp = MediaPlayer.create(MainActivity.this, R.raw.oct2415sweep);
                        AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                        AudioTest AudPlay = new AudioTest();
                        AudPlay.AudioPlayFile(mp, am, g_szAudType, g_szAudVol, mainActivityThis);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dig, int id) {
                        //return to main
                    }
                });

        builder.setCancelable(true);
        android.support.v7.app.AlertDialog ad = builder.create();
        ad.show();
    }

    public void alertDialog_microphone(View v) {
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        Log.d("MFG_TEST", "Enter alter dialog to input parameter(s) for microphone test...");
        LayoutInflater inflater = getLayoutInflater();
        final View customView = inflater.inflate(R.layout.alert_audiorec, null);

        TextView time = (TextView) customView.findViewById(R.id.etTime);
        time.setText(g_szRecTime);
        TextView freq = (TextView) customView.findViewById(R.id.etFreq);
        freq.setText(g_szRecFreq);
        TextView rate = (TextView) customView.findViewById(R.id.etRate);
        rate.setText(g_szRecRate);
        TextView channel = (TextView) customView.findViewById(R.id.etChannel);
        channel.setText(g_szRecChannel);
        TextView sample = (TextView) customView.findViewById(R.id.etSample);
        sample.setText(g_szRecSample);
        TextView path = (TextView) customView.findViewById(R.id.etPath);
        path.setText(g_szRecPath);

        builder.setView(customView)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dig, int id) {
                        EditText edit1 = (EditText) customView.findViewById(R.id.etTime);
                        g_szRecTime = edit1.getText().toString();
                        EditText edit2 = (EditText) customView.findViewById(R.id.etFreq);
                        g_szRecFreq = edit2.getText().toString();
                        EditText edit3 = (EditText) customView.findViewById(R.id.etRate);
                        g_szRecRate = edit3.getText().toString();
                        EditText edit4 = (EditText) customView.findViewById(R.id.etChannel);
                        g_szRecChannel = edit4.getText().toString();
                        EditText edit5 = (EditText) customView.findViewById(R.id.etSample);
                        g_szRecSample = edit5.getText().toString();
                        EditText edit6 = (EditText) customView.findViewById(R.id.etPath);
                        g_szRecPath = edit6.getText().toString();

                        Log.d("MFG_TEST", "Microphone Time = " + g_szRecTime);
                        Log.d("MFG_TEST", "Microphone Freq = " + g_szRecFreq);
                        Log.d("MFG_TEST", "Microphone Rate = " + g_szRecRate);
                        Log.d("MFG_TEST", "Microphone Channel = " + g_szRecChannel);
                        Log.d("MFG_TEST", "Microphone Sample = " + g_szRecSample);
                        Log.d("MFG_TEST", "Microphone Path = " + g_szRecPath);
                        //
                        AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                        AudioTest AudRec = new AudioTest();
                        AudRec.AudioRec(am, g_szRecTime, g_szRecFreq, g_szRecRate, g_szRecChannel, g_szRecSample, g_szRecPath);
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dig, int id) {
                        //return to main
                    }
                });

        builder.setCancelable(true);
        android.support.v7.app.AlertDialog ad = builder.create();
        ad.show();
    }

    public void alertDialog_GPSt(View v) {
    }

    public void alertDialog_systemSize(View v) {
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        Log.d("MFG_TEST", "Enter alter dialog to input parameter(s) for system size test...");
        LayoutInflater inflater = getLayoutInflater();
        final View customView = inflater.inflate(R.layout.alert_systemsize, null);

        TextView storage = (TextView) customView.findViewById(R.id.etStorage);
        storage.setText(g_szSysStorage);
        TextView size = (TextView) customView.findViewById(R.id.etSize);
        size.setText(g_szSysSize);

        builder.setView(customView)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dig, int id) {
                        EditText edit1 = (EditText) customView.findViewById(R.id.etStorage);
                        g_szSysStorage = edit1.getText().toString();
                        EditText edit2 = (EditText) customView.findViewById(R.id.etSize);
                        g_szSysSize = edit2.getText().toString();
                        Log.d("MFG_TEST", "System Storage = " + g_szSysStorage);
                        Log.d("MFG_TEST", "System Size = " + g_szSysSize);

                        FileSystemTest FSTest = new FileSystemTest();
                        FSTest.FileSystemSizeCheck(g_szSysStorage, g_szSysSize, mainActivityThis);
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dig, int id) {
                        //return to main
                    }
                });

        builder.setCancelable(true);
        android.support.v7.app.AlertDialog ad = builder.create();
        ad.show();
    }

    public void alertDialog_barcode(View v) {
        //allen barcode
        AidcManager.create(this, new CreatedCallback() {

            @Override
            public void onCreated(AidcManager aidcManager) {
                manager = aidcManager;
                barcodeReader = manager.createBarcodeReader();
            }
        });
        //allen barcode
        Intent barcodeIntent = new Intent();

        barcodeIntent.setClass(MainActivity.this, AutomaticBarcodeActivity.class);
		//20171002 - Check
        barcodeIntent.putExtra("Auto","true");
        barcodeIntent.putExtra("Data","ABCDEF");
		//20171002

        startActivity(barcodeIntent);
    }

    public void alertDialog_fileAccess(View v) {
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        Log.d("MFG_TEST", "Enter alter dialog to input parameter(s) for file access test...");
        LayoutInflater inflater = getLayoutInflater();
        final View customView = inflater.inflate(R.layout.alert_fileaccess, null);

        TextView path = (TextView) customView.findViewById(R.id.etPath);
        path.setText(g_szFilePath);
        TextView type = (TextView) customView.findViewById(R.id.etType);
        type.setText(g_szFileType);

        builder.setView(customView)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dig, int id) {
                        EditText edit1 = (EditText) customView.findViewById(R.id.etPath);
                        g_szFilePath = edit1.getText().toString();
                        EditText edit2 = (EditText) customView.findViewById(R.id.etType);
                        g_szFileType = edit2.getText().toString();
                        Log.d("MFG_TEST", "File Access Type = " + g_szFileType);
                        Log.d("MFG_TEST", "File Access Path = " + g_szFilePath);

                        FileSystemTest FSTest = new FileSystemTest();
                        FSTest.FileAccess(g_szFileType, g_szFilePath);
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dig, int id) {
                        //return to main
                    }
                });

        builder.setCancelable(true);
        android.support.v7.app.AlertDialog ad = builder.create();
        ad.show();
    }

    public void alertDialog_GPS(View v) {
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        Log.d("MFG_TEST", "Enter alter dialog to input parameter(s) for GPS test...");
        LayoutInflater inflater = getLayoutInflater();
        final View customView = inflater.inflate(R.layout.alert_gps, null);

        TextView snr = (TextView) customView.findViewById(R.id.etSNR);
        snr.setText(g_szGpsSnr);
        TextView ttff = (TextView) customView.findViewById(R.id.etTtff);
        ttff.setText(g_szGpsTtff);
        TextView satNum = (TextView) customView.findViewById(R.id.etSatNum);
        satNum.setText(g_szGpsSat);

        builder.setView(customView)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dig, int id) {
                        EditText edit1 = (EditText) customView.findViewById(R.id.etSNR);
                        g_szGpsSnr = edit1.getText().toString();
                        EditText edit2 = (EditText) customView.findViewById(R.id.etTtff);
                        g_szGpsTtff = edit2.getText().toString();
                        EditText edit3 = (EditText) customView.findViewById(R.id.etSatNum);
                        g_szGpsSat = edit3.getText().toString();

                        Log.d("MFG_TEST", "GPS MIN SNR = " + g_szGpsSnr);
                        Log.d("MFG_TEST", "GPS TTFF = " + g_szGpsTtff);
                        Log.d("MFG_TEST", "GPS SAT NUM = " + g_szGpsSat);

                        Intent GPSIntent = new Intent();
                        GPSIntent.setClass(MainActivity.this, GPSTest.class);
                        GPSIntent.putExtra("MIN_SNR", Integer.parseInt(g_szGpsSnr));
                        GPSIntent.putExtra("TTFF", Integer.parseInt(g_szGpsTtff));
                        GPSIntent.putExtra("SAT_NUM", Integer.parseInt(g_szGpsSat));

                        startActivity(GPSIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dig, int id) {
                        //return to main
                    }
                });

        builder.setCancelable(true);
        android.support.v7.app.AlertDialog ad = builder.create();
        ad.show();
    }

    public void button_vibrator(View v) {
        myVibratorTest();
    }

    //

    // Vibrator Test
    void myVibratorTest() {
        Vibrator myVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);

        Random random = new Random();
        g_nRandom = random.nextInt(2);
        switch (g_nRandom) {
            case 0://Vibrate One
                myVibrator.vibrate(500);
                ShowVibratorResult();
                break;
            case 1:
                myVibrator.vibrate(500);
                SystemClock.sleep(800);
                myVibrator.vibrate(500);
                ShowVibratorResult();
                break;
        }
    }

    // Vibrator Test
    void boardVibratorTest() {
        final Vibrator myVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (g_bBoardViberateGoing) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                myVibrator.vibrate(5000);
                            }
                        });
                        Thread.sleep(4999);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        ;


    }

    void ShowVibratorResult() {
        LinearLayout layout = new LinearLayout(mainActivityThis);
        RadioGroup radioGroup = new RadioGroup(mainActivityThis);

        final RadioButton btnVibOne = new RadioButton(mainActivityThis);
        btnVibOne.setText("vibrate one time, 震動一次");
        btnVibOne.setTextColor(Color.BLUE);
        btnVibOne.setTextSize((float) 35);
        ;
        radioGroup.addView(btnVibOne);

        final RadioButton btnVibTwo = new RadioButton(mainActivityThis);
        btnVibTwo.setText("vibrate two times, 震動二次");
        btnVibTwo.setTextColor(Color.BLUE);
        btnVibTwo.setTextSize((float) 35);
        ;
        radioGroup.addView(btnVibTwo);

        final RadioButton btnVibNoChange = new RadioButton(mainActivityThis);
        btnVibNoChange.setText("No change 沒有變化");
        btnVibNoChange.setTextColor(Color.BLUE);
        btnVibNoChange.setTextSize((float) 35);
        ;
        radioGroup.addView(btnVibNoChange);

        layout.setBackgroundColor(Color.WHITE);
        layout.addView(radioGroup);
        mainActivityThis.setContentView(layout);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // TODO Auto-generated method stub

                if (checkedId == btnVibOne.getId()) {
                    Log.d("MFG_TEST", "Choose \"vibrate one time, 震動一次\" \r\n");
                    if (g_nRandom == 0)
                        Log.d("MFG_TEST", "Vibrator Test SUCCESSFUL TEST\r\n");
                    else
                        Log.d("MFG_TEST", "Vibrator Test Fail!!\r\nUUT-FAIL\r\n");
                    finish();
                } else if (checkedId == btnVibTwo.getId()) {
                    Log.d("MFG_TEST", "Choose \"vibrate two times, 震動二次\" \r\n");
                    if (g_nRandom == 1)
                        Log.d("MFG_TEST", "Vibrator Test SUCCESSFUL TEST\r\n");
                    else
                        Log.d("MFG_TEST", "Vibrator Test Fail!!\r\nUUT-FAIL\r\n");
                    finish();
                } else if (checkedId == btnVibNoChange.getId()) {
                    Log.d("MFG_TEST", "Choose \"No change 沒有變化\" \r\n");
                    Log.d("MFG_TEST", "Vibrator Test Fail!!\r\nUUT-FAIL\r\n");
                    finish();
                }
            }
        });
    }
    // End Vibrator Test

    // BatteryInfo Test
    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        String strBatteryStatus, strBatteryHealth, strBatteryPlug;

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            int nLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            float nTemperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            int nVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
            String strTechnology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);

            switch (intent.getIntExtra("status", 0)) {
                case BatteryManager.BATTERY_STATUS_CHARGING:
                    strBatteryStatus = "Charging";
                    break;
                case BatteryManager.BATTERY_STATUS_DISCHARGING:
                    strBatteryStatus = "Discharging";
                    break;
                case BatteryManager.BATTERY_STATUS_FULL:
                    strBatteryStatus = "Full";
                    break;
                case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                    strBatteryStatus = "Not_Charging";
                    break;
                case BatteryManager.BATTERY_STATUS_UNKNOWN:
                    strBatteryStatus = "Unknown";
                    break;
                default:
                    strBatteryStatus = "Unknown";
                    break;
            }

            switch (intent.getIntExtra("health", 0)) {
                case BatteryManager.BATTERY_HEALTH_DEAD:
                    strBatteryHealth = "Dead";
                    break;
                case BatteryManager.BATTERY_HEALTH_GOOD:
                    strBatteryHealth = "Good";
                    break;
                case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                    strBatteryHealth = "Over_Voltage";
                    break;
                case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                    strBatteryHealth = "Overheat";
                    break;
                case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                    strBatteryHealth = "Unknown";
                    break;
                case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                    strBatteryHealth = "Unspecified_Failure";
                    break;
                default:
                    strBatteryHealth = "Unknown";
                    break;
            }
            switch (intent.getIntExtra("plugged", 0)) {
                case BatteryManager.BATTERY_PLUGGED_AC:
                    strBatteryPlug = "AC";
                    break;
                case BatteryManager.BATTERY_PLUGGED_USB:
                    strBatteryPlug = "USB";
                    break;
                default:
                    strBatteryPlug = "Unknown";
                    break;
            }

            // print all status
            if (nLevel > 100)
                Log.d("MFG_TEST", "Battery Level: Unknown\r\n");
            else
                Log.d("MFG_TEST", "Battery Level: " + nLevel + "\r\n");

            Log.d("MFG_TEST", "Battery Status: " + strBatteryStatus + "\r\n");
            Log.d("MFG_TEST", "Battery Health: " + strBatteryHealth);
            Log.d("MFG_TEST", "Battery Plug: " + strBatteryPlug);
            Log.d("MFG_TEST", "Battery Temperature: " + nTemperature / 10 + "\r\n");
            Log.d("MFG_TEST", "Battery Voltage: " + nVoltage + " mA\r\n");
            Log.d("MFG_TEST", "Battery technology: " + strTechnology + "\r\n");

            // compare with parameter
            if (szBattStatus.toUpperCase().equals("BATTERY_ACPOWER")) {
                if (strBatteryPlug.equals("AC") && !strBatteryStatus.equals("Unknown"))
                    Log.d("MFG_TEST", "SUCCESSFUL TEST\r\n");
                else
                    Log.d("MFG_TEST", "Need Both Battery & AC are detected.\r\nUUT-FAIL\r\n");
            }
            if (szBattStatus.toUpperCase().equals("BATTERY_USB")) {
                if (strBatteryPlug.equals("USB") && !strBatteryStatus.equals("Unknown"))
                    Log.d("MFG_TEST", "SUCCESSFUL TEST\r\n");
                else
                    Log.d("MFG_TEST", "Need Both Battery & USB are detected.\r\nUUT-FAIL\r\n");
            } else if (szBattStatus.toUpperCase().equals("BATTERY")) {
                if (strBatteryStatus.equals("Discharging"))
                    Log.d("MFG_TEST", "SUCCESSFUL TEST\r\n");
                else
                    Log.d("MFG_TEST", "Battery detected only.\r\nUUT-FAIL\r\n");
            } else if (szBattStatus.toUpperCase().equals("Pass")) {
                Log.d("MFG_TEST", "SUCCESSFUL TEST\r\n");

            }
            unregisterReceiver(batteryReceiver);
            //ChkLog.WriteTestResult(fileLogPath, szBattStatus, bBatteryTestResult);
        }
    };
    // End Battery Test

    // RTC Test
    private Runnable RTCTest_Thread = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            RTCTest rtcTest = new RTCTest();
            g_bRet = rtcTest.GetRTCTest(fileLogPath, szItemName, g_nTimeDelay);
            // ChkLog.WriteTestResult(fileLogPath, szItemName, bRet);
        }
    };
    // End RTC Test

    // Wifi/WLAN Test
    private Runnable WifiQuery_Thread = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            int nIdx = 0;
            g_bRet = false;
            BufferedWriter buf;

            if (!g_mWifiManager.isWifiEnabled()) {
                g_mWifiManager.setWifiEnabled(true);
            }
            while (!g_mWifiManager.isWifiEnabled()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            Pattern pattern = Pattern.compile("[0-9]*");
            Matcher isNum = pattern.matcher(g_szQuery);

            registerReceiver(WifiQueryReceiver, new IntentFilter(g_mWifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            g_mWifiManager.startScan();

            while (!g_bScanFinished) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            unregisterReceiver(WifiQueryReceiver);

            List<ScanResult> WifiList = g_mWifiManager.getScanResults();
            int nCount = WifiList.size();

            if (isNum.matches()) //g_szQuery 為數字
            {
                int nNumChk = Integer.parseInt(g_szQuery);
                //20171205 Kevin modify to skip chinese characters and list the number of check only, not all of the AP in case of PUSIGB fail
                Log.d("MFG_TEST", "WLAN Query List Count: " + nCount + "\r\n");
                if (nCount > 0) {
                    nIdx = 0;
                    int num = 1;
                    while((num <= nNumChk) && (nIdx <= nCount)) {
                        if (WifiList.get(nIdx).SSID.toString().matches("[\\u4E00-\\u9FA5]+") == false && WifiList.get(nIdx).SSID.toString().length() > 1) {
                            Log.d("MFG_TEST", num + ". SSID: " + WifiList.get(nIdx).SSID.toString()
                                    + "\tBSSID: " + WifiList.get(nIdx).BSSID.toString()
                                    + "\tLevel: " + WifiList.get(nIdx).level
                                    + "\tFreq: " + WifiList.get(nIdx).frequency + "\r\n");
                            num = num + 1;
                        }
                        nIdx = nIdx + 1;
                    }

                    Log.d("MFG_TEST", "List first " + nNumChk + " above!!");
                }


                if (nCount >= nNumChk) {
                    Log.d("MFG_TEST", "SUCCESSFUL TEST\r\n");
                    pass();
                } else {
                    Log.d("MFG_TEST", "UUT-FAIL\r\n");
                }
            } else {
                for (nIdx = 0; nIdx < nCount; nIdx++) {
                    if (g_szQuery.toUpperCase().equals(WifiList.get(nIdx).SSID.toString())) {
                        g_bRet = true;
                        break;
                    }
                }

                if (g_bRet)
                    Log.d("MFG_TEST", "Find SSID " + g_szQuery + " AP Successful!!\r\nSUCCESSFUL TEST\r\n");
                else
                    Log.d("MFG_TEST", "Find SSID " + g_szQuery + " AP Fail!!\r\nUUT-FAIL\r\n");
                finish();
            }

        }
    };

    private BroadcastReceiver WifiQueryReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            g_bScanFinished = true;
        }
    };

    private Runnable WifiPing_Thread = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            WifiTest wifiTest = new WifiTest();
            wifiTest.PingtoAP(g_mWifiManager, g_szSSID, g_szIP, g_szPingCount, g_nPassRate);
            finish();
        }
    };

    private Runnable WifiLink_Thread = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            WifiTest wifiTest = new WifiTest();

            //Log.d("MFG_TEST", "fileLogPath: " + fileLogPath );
            //Log.d("MFG_TEST", "g_mWifiManager: " + g_mWifiManager);
            //Log.d("MFG_TEST", "g_szRSSI: " + g_szRSSI );
            //Log.d("MFG_TEST", "g_szMax: " + g_szMax );
            //Log.d("MFG_TEST", "szItemName: " + szItemName);
            Log.d("MFG_TEST", "\n");

            g_bRet = wifiTest.ManualLink(g_mWifiManager, g_szRSSI, g_bWiFiConnection);
            if (g_bRet)
                Log.d("MFG_TEST", "true");
            else
                Log.d("MFG_TEST", "false");

            finish();
        }
    };

    private Runnable WifiRSSI_Thread = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            WifiTest wifiTest = new WifiTest();

            //Log.d("MFG_TEST", "fileLogPath: " + fileLogPath );
            //Log.d("MFG_TEST", "g_mWifiManager: " + g_mWifiManager);
            //Log.d("MFG_TEST", "g_szRSSI: " + g_szRSSI );
            //Log.d("MFG_TEST", "g_szMax: " + g_szMax );
            //Log.d("MFG_TEST", "szItemName: " + szItemName);
            Log.d("MFG_TEST", "\n");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    List<WifiConfiguration> list = g_mWifiManager.getConfiguredNetworks();
                    for( WifiConfiguration i : list ) {
                        g_mWifiManager.removeNetwork(i.networkId);
                        g_mWifiManager.saveConfiguration();
                    }
                }
            });
            g_bRet = wifiTest.LinkRSSI(g_mWifiManager, g_szRSSI, g_szMax, g_szMin, szItemName);
            if (g_bRet)
                Log.d("MFG_TEST", "true");
            else
                Log.d("MFG_TEST", "false");

            finish();
        }
    };
    // End Wifi/WLAN Test

    // File System test
    private Runnable FileSys_Thread = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            FileSystemTest FSTest = new FileSystemTest();
            g_bRet = FSTest.FileSystemTest(fileLogPath, g_strStorage, mainActivityThis);
            if (g_bRet) {
                pass();
            } else
                Setting.fail();
        }
    };
    // End FileRTCTest System test

    // Kevin 20170921 - for burn in use
    private TimerTask BurnInDisplay = new TimerTask() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            Message message = new Message();
            message.what = 2;
            handler.sendMessage(message);
        }
    };
    // Kevin

    // Backlight Intensity test
    private TimerTask BrightTask = new TimerTask() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            Message message = new Message();
            message.what = g_nRandom;
            handler.sendMessage(message);
        }
    };
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0://由暗變亮
                    SetBacklgith(g_nBrightness[g_nBrightLevel]);
                    if (g_nBrightLevel >= g_nBrightMax) {
                        SystemClock.sleep(1500);
                        SetBacklgith(g_nBrightness[3]);
                        ShowBacklightResult();
                        timer.cancel();
                    } else
                        g_nBrightLevel++;
                    break;
                case 1://由亮變暗
                    SetBacklgith(g_nBrightness[g_nBrightMax]);
                    if (g_nBrightMax <= 0) {
                        SystemClock.sleep(1000);
                        SetBacklgith(g_nBrightness[3]);
                        ShowBacklightResult();
                        timer.cancel();
                    } else
                        g_nBrightMax--;
                    break;

                case 2:
                    //Kevin 20170921 - for BurnIn use
                    RunBurnInDisplay(nBurnInDisCount);
                    nBurnInDisCount++;
                    //Kevin
                    break;
                case 3:
                    timer.cancel();
                    SystemClock.sleep(g_nTimeDelay * 1000);
                    finish();
                case 4://由暗變亮
                    SetBacklgith(g_nBrightness[g_nBrightLevel]);
                    if (g_nBrightLevel >= g_nBrightMax) {
                        SystemClock.sleep(1500);
                        SetBacklgith(g_nBrightness[3]);
                        timer.cancel();
                        finish();
                    } else
                        g_nBrightLevel++;
                    break;
            }
        }
    };

    //Kevin 20170921 - for burn in use
    void RunBurnInDisplay(int nBurnInDisplay) {
        LinearLayout g_background = (LinearLayout) findViewById(R.id.display_color);
        switch (nBurnInDisplay) {
            case 0:
                g_background.setBackgroundColor(Color.RED);
                Log.d("MFG_TEST", "Color = Red");
                break;
            case 1:
                g_background.setBackgroundColor(Color.GREEN);
                Log.d("MFG_TEST", "Color = Green");
                break;
            case 2:
                g_background.setBackgroundColor(Color.BLUE);
                Log.d("MFG_TEST", "Color = Blue");
                break;
            case 3:
                g_background.setBackgroundColor(Color.BLACK);
                Log.d("MFG_TEST", "Color = Black");
                break;
            case 4:
                g_background.setBackgroundColor(Color.WHITE);
                Log.d("MFG_TEST", "Color = White");
                break;
            case 5:
                g_background.setOrientation(LinearLayout.VERTICAL);
                TextView[] colorBar = new TextView[5];

                for (int nIdx = 0; nIdx < colorBar.length; nIdx++) {
                    colorBar[nIdx] = new TextView(mainActivityThis);
                    g_background.addView(colorBar[nIdx], nIdx, new LinearLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
                }

                int[] bla2w = {Color.BLACK, Color.WHITE};
                colorBar[0].setBackgroundDrawable(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, bla2w));

                int[] r2w = {Color.BLACK, Color.RED};
                colorBar[1].setBackgroundDrawable(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, r2w));

                int[] g2w = {Color.BLACK, Color.GREEN};
                colorBar[2].setBackgroundDrawable(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, g2w));

                int[] blu2w = {Color.BLACK, Color.BLUE};
                colorBar[3].setBackgroundDrawable(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, blu2w));

                int[] b2g = {Color.BLACK, Color.GRAY};
                colorBar[4].setBackgroundDrawable(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, b2g));
                Log.d("MFG_TEST", "Color = Color-Parent");
                break;
            case 6:
                Log.d("MFG_TEST", "Timer cancel");
                timer.cancel();
                //Kevin
                this.setItemPass();
                //finish();
                break;
        }
    }
    //Kevin

    void SetBacklgith(int nBackLightLevel) {
        WindowManager.LayoutParams LP = getWindow().getAttributes();
        LP.screenBrightness = nBackLightLevel / (float) 255;
        getWindow().setAttributes(LP);
        Log.d("MFG_TEST", "nBackLightLevel is: " + nBackLightLevel + "\r\n");
    }

    void ShowBacklightResult() {
        LinearLayout layout = new LinearLayout(mainActivityThis);
        RadioGroup radioGroup = new RadioGroup(mainActivityThis);

        final RadioButton btnBrightToDark = new RadioButton(mainActivityThis);
        btnBrightToDark.setText("Bright->Dark 由亮變暗");
        btnBrightToDark.setTextColor(Color.BLUE);
        btnBrightToDark.setTextSize((float) 35);
        ;
        radioGroup.addView(btnBrightToDark);

        final RadioButton btnBrightToBright = new RadioButton(mainActivityThis);
        btnBrightToBright.setText("Drak->Bright 由暗變亮");
        btnBrightToBright.setTextColor(Color.BLUE);
        btnBrightToBright.setTextSize((float) 35);
        ;
        radioGroup.addView(btnBrightToBright);

        final RadioButton btnBrightNoChange = new RadioButton(mainActivityThis);
        btnBrightNoChange.setText("No change 沒有變化");
        btnBrightNoChange.setTextColor(Color.BLUE);
        btnBrightNoChange.setTextSize((float) 35);
        ;
        radioGroup.addView(btnBrightNoChange);

        layout.setBackgroundColor(Color.WHITE);
        layout.addView(radioGroup);
        mainActivityThis.setContentView(layout);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // TODO Auto-generated method stub

                if (checkedId == btnBrightToDark.getId()) {
                    if (g_nRandom == 1)
                        Log.d("MFG_TEST", "Brightness Test SUCCESSFUL TEST\r\n");
                    else
                        Log.d("MFG_TEST", "Brightness Test Fail!!\r\nUUT-FAIL\r\n");
                    finish();
                } else if (checkedId == btnBrightToBright.getId()) {
                    if (g_nRandom == 0)
                        Log.d("MFG_TEST", "Brightness Test SUCCESSFUL TEST\r\n");
                    else
                        Log.d("MFG_TEST", "Brightness Test Fail!!\r\nUUT-FAIL\r\n");
                    finish();
                } else if (checkedId == btnBrightNoChange.getId()) {
                    Log.d("MFG_TEST", "Brightness Test Fail!!\r\nUUT-FAIL\r\n");
                    finish();
                }
            }
        });
    }
    // End Backlight Intensity test

    // Bluetooth test
    private Runnable BTQuery_Thread = new Runnable() {
        @Override
        public void run() {

            // TODO Auto-generated method stub
            int nIdx;
            g_bRet = false;
            BufferedWriter buf;
            g_mBT.disable();

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            if (!g_mBT.isEnabled()) {
                // Lynette
                for (nIdx = 0; nIdx < 5; nIdx++) {
                    g_mBT.enable();
                    SystemClock.sleep(2000);
                    g_bRet = g_mBT.enable();
                    if (g_bRet)
                        break;
                }
                // Lynette ===
                Log.d("MFG_TEST", "Open BT successfully");
            } else
                Log.d("MFG_TEST", "BT is Enable!!");

            long startTime = System.currentTimeMillis();

            while (g_mBT.getState() != BluetoothAdapter.STATE_ON) {
                Log.d("MFG_TEST", "BT Doesn't Ready!!");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if ((System.currentTimeMillis() - startTime) > 80000) {
                    Log.d("MFG_TEST", "BT Enable Time Out!!\r\nUUT-FAIL\r\n");
                    return;
                }
            }

            g_mBTDevice = new ArrayList<BluetoothDevice>();
            IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(onBTDeviceFound, foundFilter);
            IntentFilter finishedFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(onBTDiscoveryFinished, finishedFilter);
            g_mBT.startDiscovery();

            //Log.d("MFG_TEST", "HI~~~~");
            //g_mBTDevice = new ArrayList<BluetoothDevice>();
            //IntentFilter intentBTFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            //intentBTFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            //intentBTFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            //intentBTFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            //registerReceiver(onBTDeviceFound,intentBTFilter);
            //g_mBT.startDiscovery();
            //Log.d("MFG_TEST", "YEAH~~~~");


        }
    };
    private BroadcastReceiver onBTDeviceFound = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            BluetoothDevice dev = arg1.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            g_mBTDevice.add(dev);

            Pattern pattern = Pattern.compile("[0-9]*");
            Matcher isNum = pattern.matcher(g_szQuery);

            if (isNum.matches()) {
                int nNumChk = Integer.parseInt(g_szQuery);

                if (g_mBTDevice.size() >= nNumChk) {
                    unregisterReceiver(onBTDiscoveryFinished);
                    unregisterReceiver(this);

                    for (int nIdx = 0; nIdx < (g_mBTDevice.size()); nIdx++) {
                        Log.d("MFG_TEST", nIdx + 1 + ": " + g_mBTDevice.get(nIdx).getName());
                    }
                    Log.d("MFG_TEST", "SUCCESSFUL TEST");
                    pass();
                    g_mBT.disable();
                }
            }
        }
    };

    private BroadcastReceiver onBTDiscoveryFinished = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            unregisterReceiver(onBTDeviceFound);
            unregisterReceiver(this);

            Pattern pattern = Pattern.compile("[0-9]*");
            Matcher isNum = pattern.matcher(g_szQuery);

            if (isNum.matches()) {
                int nNumChk = Integer.parseInt(g_szQuery);

                // print the BT units info
                for (int nIdx = 0; nIdx < g_mBTDevice.size(); nIdx++)
                    Log.d("MFG_TEST", nIdx + 1 + ": " + g_mBTDevice.get(nIdx).getName());

                if (g_mBTDevice.size() > nNumChk) {
                    Log.d("MFG_TEST", "SUCCESSFUL TEST");
                    pass();
                } else {
                    Log.d("MFG_TEST", "Bluetooth Query FAIL\r\nUUT-FAIL\r\n");
//                    Setting.fail();
                }
                g_mBT.disable();
            }
        }
    };

    private Runnable BTConnect_Thread = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (!g_mBT.isEnabled()) {
                g_mBT.enable();
                SystemClock.sleep(3000);
            }

            while (g_mBT.getState() != BluetoothAdapter.STATE_ON) {
                Log.d("MFG_TEST", "Enabling Bluetooth");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (!g_bIsBTActive)
                    return;
            }
            Log.d("MFG_TEST", "Bluetooth Ready");
            Log.d("MFG_TEST", "Get BT MAC: " + g_mBT.getAddress());

            mChatService = new BluetoothChatService(mainActivityThis, mHandler);

            // Initialize the buffer for outgoing messages
            mOutStringBuffer = new StringBuffer("");
            mChatService.start();
        }
    };

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:

                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            Log.d("MFG_TEST", "Connect From " + mConnectedDeviceName);
                            BTTransmitTest();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            Log.d("MFG_TEST", "Connecting....");

                            break;
                        case BluetoothChatService.STATE_LISTEN:
                            Log.d("MFG_TEST", "Server Listening....");
                            break;
                        case BluetoothChatService.STATE_NONE:

                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.d("MFG_TEST", readMessage);
                    //if(!IsClient)
                    // ((BluetoothTestTActivity)mCtx).sendMessage(readMessage);
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_CONNECTION_LOST:
                    Log.d("MFG_TEST", "Connection Lost!!");
                    mChatService.stop();
                    Log.d("MFG_TEST", "UUT-FAIL\r\n");
                    finish();
                    break;
            }
        }
    };

    private void BTTransmitTest() {
        try {
            OutputStream tmpOut = mChatService.ssocket.getOutputStream();
            InputStream tmpIn = mChatService.ssocket.getInputStream();
            String testStr = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd123456789";

            //(1048576/8)/1000
            Log.d("MFG_TEST", "Start Transmit Test");
            for (int i = 0; i < 500; i++) {
                tmpOut.write(testStr.getBytes());
                tmpOut.flush();
                byte[] buffer = new byte[2048];
                int count = tmpIn.read(buffer);
                String recstr = new String(buffer).substring(0, count);
                if (recstr.equals(testStr)) {
                    if ((i % 100) == 0)
                        Log.d("MFG_TEST", "Receive Count:" + Integer.toString(i));
                } else {
                    Log.d("MFG_TEST", "Receive Text:" + recstr);
                    mChatService.stop();
                    Log.d("MFG_TEST", "UUT-FAIL\r\n");
                    finish();
                    return;
                }
            }
            tmpOut.close();
            tmpIn.close();
            Log.d("MFG_TEST", "SUCCESSFUL TEST");
            SystemClock.sleep(1000);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.d("MFG_TEST", e.toString());
            mChatService.stop();
            Log.d("MFG_TEST", "UUT-FAIL\r\n");
            e.printStackTrace();
        }

        /*
        IBluetooth IB = getIBluetooth();
        try {
            //ib.removeBond("00:1A:7D:DA:71:15");
            IB.removeBond(g_szCliMAC);
            //g_mBT.disable();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/

        mChatService.stop();
    }
    // End Bluetooth test

    // Keypad Test
    private void InitialKeyTest(String szType) {
        // TODO Auto-generated method stub
        TableRow mRow_top = (TableRow) findViewById(R.id.key_row);
        Button btnFail = (Button) findViewById(R.id.btnFail);
        LinearLayout mLine_Left = (LinearLayout) findViewById(R.id.keyLeft);
        LinearLayout mLine_Right = (LinearLayout) findViewById(R.id.keyRight);
        LinearLayout mLine_Center = (LinearLayout) findViewById(R.id.keyCenter);

        btnFail.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Log.d("MFG_TEST", "KeypadTest Fail\r\nUUT-FAIL\r\n");
                finish();
            }
        });

        mTop = GetDefine.getKeys(szType, "top");

        if (mTop != null) {
            for (int nIdx = 0; nIdx < mTop.length; nIdx++) {
                mTop[nIdx].btn = new Button(this);
                mTop[nIdx].btn.setText(mTop[nIdx].caption);
                mTop[nIdx].btn.setTextSize(12);
                //mTop[nIdx].btn.setBackgroundResource(R.id.btnFail);
                mRow_top.addView(mTop[nIdx].btn, mTop[nIdx].width, mTop[nIdx].heigth);
            }
        }

        mLeft = GetDefine.getKeys(szType, "left");
        if (mLeft != null) {
            for (int nIdx = 0; nIdx < mLeft.length; nIdx++) {
                mLeft[nIdx].btn = new Button(this);
                mLeft[nIdx].btn.setText(mLeft[nIdx].caption);
                mLeft[nIdx].btn.setTextSize(12);
                //mLeft[nIdx].btn.setBackgroundResource(R.layout.button1);
                mLine_Left.addView(mLeft[nIdx].btn, mLeft[nIdx].width, mLeft[nIdx].heigth);
            }
        }

        mRight = GetDefine.getKeys(szType, "right");
        if (mRight != null) {
            for (int nIdx = 0; nIdx < mRight.length; nIdx++) {
                mRight[nIdx].btn = new Button(this);
                mRight[nIdx].btn.setText(mRight[nIdx].caption);
                mRight[nIdx].btn.setTextSize(12);
                //mRight[i].btn.setBackgroundResource(R.layout.button1);
                mLine_Right.addView(mRight[nIdx].btn, mRight[nIdx].width, mRight[nIdx].heigth);
            }
        }

        mCenter = GetDefine.getCenterKeys(szType);
        for (int nIdx = 0; nIdx < mCenter.length; nIdx++) {
            TableRow trTmp = new TableRow(this);
            trTmp.setGravity(0x11);
            for (int j = 0; j < mCenter[nIdx].length; j++) {
                mCenter[nIdx][j].btn = new Button(this);
                mCenter[nIdx][j].btn.setText(mCenter[nIdx][j].caption);
                mCenter[nIdx][j].btn.setTextSize(12);
                //mCenter[nIdx][j].btn.setBackgroundResource(R.layout.botton1);

                if (!mCenter[nIdx][j].img.equals("")) {
                    int resID = getResources().getIdentifier(mCenter[nIdx][j].img, "drawable", "usi.tdd.mfg");
                    Drawable draw = this.getResources().getDrawable(resID);
                    mCenter[nIdx][j].btn.setCompoundDrawablesWithIntrinsicBounds(null, null, null, draw);
                }
                trTmp.addView(mCenter[nIdx][j].btn, mCenter[nIdx][j].width, mCenter[nIdx][j].heigth);
            }
            mLine_Center.addView(trTmp);
        }
        setUnPressKey();
    }

    public void setUnPressKey() {
        mCurrentKey = null;
        int nIdx = 0;

        if (mTop != null) {
            for (nIdx = 0; nIdx < mTop.length; nIdx++) {
                if (mTop[nIdx].btn.isEnabled()) {
                    mCurrentKey = mTop[nIdx];
                    mTop[nIdx].btn.setFocusableInTouchMode(true);
                    mTop[nIdx].btn.requestFocus();
                    return;
                }
            }
        }

        if (mLeft != null) {
            for (nIdx = 0; nIdx < mLeft.length; nIdx++) {
                if (mLeft[nIdx].btn.isEnabled()) {
                    mCurrentKey = mLeft[nIdx];
                    mCurrentKey.btn.setFocusableInTouchMode(true);
                    mCurrentKey.btn.requestFocus();
                    return;
                }
            }
        }

        if (mRight != null) {
            for (nIdx = 0; nIdx < mRight.length; nIdx++) {
                if (mRight[nIdx].btn.isEnabled()) {
                    mCurrentKey = mRight[nIdx];
                    mCurrentKey.btn.setFocusableInTouchMode(true);
                    mCurrentKey.btn.requestFocus();
                    return;
                }
            }
        }

        if (mCenter != null) {
            for (nIdx = 0; nIdx < mCenter.length; nIdx++) {
                for (int j = 0; j < mCenter[nIdx].length; j++) {
                    if (mCenter[nIdx][j].btn.isEnabled()) {
                        mCurrentKey = mCenter[nIdx][j];
                        mCurrentKey.btn.setFocusableInTouchMode(true);
                        mCurrentKey.btn.requestFocus();
                        return;
                    }
                }
            }
        }
    }

    //    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//
////        event.getKeyCode()== KeyEvent.KEYCODE_ENTER;
//
//        //if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
//
//        // TODO Auto-generated method stub
//        if( g_szKeyType != null )
//        {
//            //Log.d("MFG_TEST", /*"Caption: " + mCurrentKey.caption +*/ "Key Code: " + Integer.toString(keyCode) + ", Scan Code: "+Integer.toString(event.getScanCode()));
//
//            Log.d("MFG_TEST", "Key_Down, " + "Scan Code: "+Integer.toString(event.getScanCode()));
//
//            if("O".equals(TEST_TYPE)){
//                if(mCurrentKey.code==event.getScanCode()){
//                    mCurrentKey.btn.setEnabled(false);
//                    setUnPressKey();
//                    if(mCurrentKey==null){
//                        Log.d("MFG_TEST", "SUCCESSFUL TEST");
//                        this.finish();
//                    }
//                }
//            }
//            if("N".equals("TEST_TYPE")){
//                if(setKeyPressed(event.getScanCode())){
//                    Log.d("MFG_TEST", "SUCCESSFUL TEST");
//                    this.finish();
//                }
//            }
//            return true;
//        }
//        else
//            return super.onKeyDown(event.getKeyCode(), event);
//    }
    // Lynette
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (g_szKeyType != null) {
            //Log.d("MFG_TEST", /*"Caption: " + mCurrentKey.caption +*/ "Key Code: " + Integer.toString(keyCode) + ", Scan Code: "+Integer.toString(event.getScanCode()));

            Log.d("MFG_TEST", "Key_Up, " + "Scan Code: " + Integer.toString(event.getScanCode()));

            if ("O".equals(TEST_TYPE) && mCurrentKey != null) {
                if (mCurrentKey.code == event.getScanCode()) {
                    mCurrentKey.btn.setEnabled(false);
                    setUnPressKey();
                    if (mCurrentKey == null) {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.d("MFG_TEST", "SUCCESSFUL TEST");
                        this.finish();
                    }
                }
                return true;

            }
            if ("N".equals("TEST_TYPE") && mCurrentKey != null) {
                if (setKeyPressed(event.getScanCode())) {
                    Log.d("MFG_TEST", "SUCCESSFUL TEST");
                    this.finish();
                }
                return true;

            }

        }
        return super.onKeyUp(keyCode, event);
    }

//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//        // TODO Auto-generated method stub
//        //Log.d("MFG_TEST", "dispatchKeyEvent 1 key = " + event.getKeyCode() + " event =  " + event.getAction());
//        // if(event.getAction()==KeyEvent.ACTION_DOWN){
//        //    Log.d("MFG_TEST", "dispatchKeyEvent 2 ACTION_DOWN" + event.getKeyCode() );
//           // return true;
//        //}
//        //if(event.getAction()==KeyEvent.ACTION_UP){
//        //    Log.d("MFG_TEST", "dispatchKeyEvent 3 ACTION_UP" + event.getKeyCode() );
//            //return false;
//        //}
//        return super.dispatchKeyEvent(event);
//    }
    // Lynette===

    public boolean setKeyPressed(int keycode) {
        int nIdx = 0;
        boolean IsAllKeyPress = true;

        if (mTop != null) {
            for (nIdx = 0; nIdx < mTop.length; nIdx++) {
                if (keycode == mTop[nIdx].code)
                    mTop[nIdx].btn.setEnabled(false);
                if (mTop[nIdx].btn.isEnabled())
                    IsAllKeyPress = false;
            }
        }

        if (mLeft != null) {
            for (nIdx = 0; nIdx < mLeft.length; nIdx++) {
                if (keycode == mLeft[nIdx].code)
                    mLeft[nIdx].btn.setEnabled(false);
                if (mLeft[nIdx].btn.isEnabled())
                    IsAllKeyPress = false;
            }
        }

        if (mRight != null) {
            for (int i = 0; i < mRight.length; i++) {
                if (keycode == mRight[i].code)
                    mRight[i].btn.setEnabled(false);
                if (mRight[i].btn.isEnabled())
                    IsAllKeyPress = false;
            }
        }

        if (mCenter != null) {
            for (nIdx = 0; nIdx < mCenter.length; nIdx++) {
                for (int j = 0; j < mCenter[nIdx].length; j++) {
                    if (keycode == mCenter[nIdx][j].code)
                        mCenter[nIdx][j].btn.setEnabled(false);
                    if (mCenter[nIdx][j].btn.isEnabled())
                        IsAllKeyPress = false;
                }
            }
        }

        return IsAllKeyPress;
    }
    // End Keypad test

    // Camera Test
    // 拍照後顯示圖片，拍照後回傳監聽式
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("MFG_TEST", "onActivityResult\r\n");
        if (requestCode == 1) {
            if(resultCode==1)
                mainActivityThis.setItemPass();
            else
                mainActivityThis.setItemFail();
        } else {
            ImageView iv = (ImageView) findViewById(R.id.imageViewPic);

            if (resultCode == RESULT_OK) {
                Bitmap bmp = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/image.jpg");
                iv.setImageBitmap(bmp);

                FileOutputStream fOut;
                try {
                    fOut = new FileOutputStream(fileLogPath.toString());
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                    fOut.flush();
                    fOut.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.d("MFG_TEST", "Picture is saved in: " + fileLogPath.toString() + "\r\n");
                Log.d("MFG_TEST", "SUCCESSFUL TEST");
                mainActivityThis.finish();
            } else {
                Log.d("MFG_TEST", "Camera Test Fail!!\r\nUUT-FAIL\r\n");
                mainActivityThis.finish();
                //Log.d("MFG_TEST", "resultCode is: " + resultCode + "\r\n");
                //覆蓋原來的Activity
                //super.onActivityResult(requestCode, resultCode, data);
            }
        }

    }
    // End Camera Test

    // FlashLight Test
    public void turnOnFlashLight() {
        try {
            if (Build.VERSION.SDK_INT >= M)
                mCameraManager.setTorchMode(mCameraId, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("MFG_TEST", "Turn on FlashLight\r\n");
    }

    public void turnOffFlashLight() {
        try {
            if (Build.VERSION.SDK_INT >= M)
                mCameraManager.setTorchMode(mCameraId, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("MFG_TEST", "Turn off FlashLight\r\nSUCCESSFUL TEST\r\n");
    }
    // End FlashLight Test

    //WiFiConnectTest
    public void WifiConnectTest(String networkSSID, String networkPass, String g_szWiFiDisconnect, String g_szWiFiAPMode) {

        WifiHandler wifiHandler = new WifiHandler(this);
        wifiHandler.enableWifi();
        int apMode = 0;
        if (g_szWiFiAPMode == null) {
            Log.d("MFG_TEST", "WiFiConnect  APMode setting  fail. UUT-FAIL\r\n");
            return;
        }
        if (g_szWiFiAPMode.equals("WEP")) {
            apMode = 1;
        } else if (g_szWiFiAPMode.equals("WAP")) {
            apMode = 2;
        } else if (g_szWiFiAPMode.equals("OPEN_NETWORK")) {
            apMode = 3;
        } else {
            Log.d("MFG_TEST", "WiFiConnect  APMode setting  fail. UUT-FAIL\r\n");
            return;
        }

        boolean result = wifiHandler.connectToSelectedNetwork(networkSSID, networkPass, apMode);
        if (result) {
            Log.d("MFG_TEST", "WiFiConnect Successful\r\n");

            if (g_szWiFiDisconnect != null) {
                if (g_szWiFiDisconnect.equals("true")) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    boolean dis = wifiHandler.disconnectFromWifi();
                    //wifiHandler.disableWifi();
                    if (dis) {
                        Log.d("MFG_TEST", "WiFiConnect disconnect Successful\r\n");
                    } else {
                        Log.d("MFG_TEST", "WiFiConnect 1 disconnect fail. UUT-FAIL\r\n");
                        return;

                    }
                } else if (g_szWiFiDisconnect.equals("false")) {
                } else {
                    Log.d("MFG_TEST", "WiFiConnect 2 disconnect setting  fail. UUT-FAIL\r\n");
                    return;

                }
            } else {
                Log.d("MFG_TEST", "WiFiConnect 3 disconnect setting  fail. UUT-FAIL\r\n");
                return;

            }

        } else {
            Log.d("MFG_TEST", "WiFiConnect  UUT-FAIL\r\n");
            return;

        }
    }
}
