package usi.tdd.androidmutitest;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Iterator;

/**
 * Created by Admin on 2016/9/6.
 */
public class GPSTest extends MainActivity {

    LocationManager locationManager;
    Location loc;
    TextView txtAltitude, txtLongtitude, txtLatitude, txtFixTime, txtNMEA; // getAltitude海拔/getLongitude經度/getLatitude緯度
    SurfaceView sView;
    SurfaceHolder surfaceHolder;
    Iterator it;
    GpsStatus.Listener listener;
    long mStartTime;
    boolean mIsTTFFPass = false;
    boolean mIsSatNumberPass = false;
    // 20170428 Lynette : Ad for only checking NMEA
    boolean bChkNMEA = false;



    protected PowerManager.WakeLock mWakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        // 20161129 Lynette
        requestWindowFeature(Window.FEATURE_NO_TITLE); //MUST need at "super.onCreate" before, if not will get error
        super.onCreate(savedInstanceState);
        setTitle("GPS Testing...");
        setContentView(R.layout.gps); //不可換 不然linearlayout會找不到
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getSupportActionBar().hide();
        // Lynette====

        Intent intent = GPSTest.this.getIntent();
        g_nMIN_SNR = intent.getIntExtra("MIN_SNR", 40);
        g_nTTFF = intent.getIntExtra("TTFF", 84000);
        g_nSAT_NUM = intent.getIntExtra("SAT_NUM", 3);
        // 20170428 Lynette : Add for only checking NMEA
        int nChkNMEA = intent.getIntExtra("ChkNMEA", 0);
        Log.d("MFG_TEST", "ChkNMEA =" + nChkNMEA);
        if (nChkNMEA == 1) {
            Log.d("MFG_TEST", "ChkNMEA =1");
            bChkNMEA = true;
        } else
            bChkNMEA = false;
        //Log.d("MFG_TEST","ChkNMEA =0");
        //String szChk = intent.getExtras().getString("ChkNMEA");

        //Log.d("MFG_TEST","g_nMIN_SNR =" + String.valueOf(g_nMIN_SNR));
        //Log.d("MFG_TEST","g_nTTFF =" + String.valueOf(g_nTTFF));
        //Log.d("MFG_TEST","g_nSAT_NUM =" + String.valueOf(g_nSAT_NUM));

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();

        txtAltitude = (TextView) findViewById(R.id.gps_txt_altitude);
        txtLongtitude = (TextView) findViewById(R.id.gps_txt_longtitude);
        txtLatitude = (TextView) findViewById(R.id.gps_txt_latitude);
        txtLatitude = (TextView) findViewById(R.id.gps_txt_latitude);
        txtFixTime = (TextView) findViewById(R.id.gps_txt_fixtime);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        loc = null;

        int nClearData = intent.getIntExtra("ClearData", 0);

        Log.d("MFG_TEST", "ClearData =" + nClearData);
        if(nClearData == 1) {
            locationManager.sendExtraCommand(locationManager.GPS_PROVIDER, "delete_aiding_data", null);
            finishAffinity();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //如果沒有授權使用定位就會跳出來這個
            // TODO: Consider calling

            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            requestLocationPermission(); // 詢問使用者開啟權限
            Log.d("MFG_TEST", "YES, return");

            //return;
        }


        // 20161128 Lynette:Add for showing NMEA
        txtNMEA = (TextView) findViewById(R.id.txtNMEA);
        // 20161128 Lynette end


        // Lynette:Check whether GPS turns on or not
        if (!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            Log.d("MFG_TEST", "Open Location first!!\r\n");
            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(myIntent);
        }
        int nCal = 0;
        while (!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            try {
                Thread.sleep(1000);
                nCal++;
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Log.d("MFG_TEST", "wait for GPS enable..." + nCal + "\r\n");

            if (nCal == 20) {
                Log.d("MFG_TEST", "GPS doesn't enable at specific time.\r\nUUT-FAIL\r\n");
                //20171002 - Allen added, Kevin remove first for debugging
                //Setting.fail();
                finishAffinity();
                return;
            }
        }
        // Lynette end
        Log.d("MFG_TEST", "GPS enable!!\r\n");

        sView = (SurfaceView) findViewById(R.id.gps_SurfaceView);
        surfaceHolder = sView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            public void surfaceDestroyed(SurfaceHolder arg0) {
            }

            public void surfaceCreated(SurfaceHolder arg0) {
                draw(drawAction.create);
            }

            public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
            }
        });

        mStartTime = System.currentTimeMillis();
    }

    private void requestLocationPermission() {
        // 如果裝置版本是6.0（包含）以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 取得授權狀態，參數是請求授權的名稱
            int hasPermission = checkSelfPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION);

            // 如果未授權
            if (hasPermission != PackageManager.PERMISSION_GRANTED) {

                Log.d("MFG_TEST", "No permission");
                // 請求授權
                //     第一個參數是請求授權的名稱
                //     第二個參數是請求代碼
                //requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_FINE_LOCATION_PERMISSION);
            } else {
                // 啟動地圖與定位元件
                Log.d("MFG_TEST", "Got the permission");
                return;

            }
        }
    }

    private void request_updates() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // GPS is enabled on device so lets add a loopback for this locationmanager
            locationManager.addGpsStatusListener(listener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            // 20161128 Lynette:Add for showing NMEA
            locationManager.addNmeaListener(nmeaListener);
			// 20171002 - Check
//            if(bChkNMEA)
//            {
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            Thread.sleep(g_nTTFF-500);
//                            if(bChkNMEA)
//                            {
//                                bChkNMEA=false;
//                                locationManager.removeNmeaListener(nmeaListener);
//                                Thread.sleep(500);
//                                Setting.fail();
//                                setResult(0);
//                                Log.d("MFG_TEST", "GPS Test Time out!!\r\nUUT-FAIL\r\n");
//                                GPSTest.this.finish();
//                            }
//
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//
//                    }
//                }).start();
//            }
			// 20171002
            // Lynette end
        }
    }

    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Each time the location is changed we assign loc
            loc = location;
            txtLongtitude.setText("Longtitude : " + Double.toString(location.getLongitude()));
            txtLatitude.setText("Latitude : " + Double.toString(location.getLatitude()));
            txtAltitude.setText("Altitude : " + Double.toString(location.getAltitude()));
        }

        // Need these even if they do nothing. Can't remember why.
        public void onProviderDisabled(String arg0) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    // 20161128 Lynette:Add for showing NMEA
    GpsStatus.NmeaListener nmeaListener = new GpsStatus.NmeaListener() {
        public void onNmeaReceived(long timestamp, String nmea) {
            //check nmea's checksum
            //Log.d("MFG_TEST", nmea);
            txtNMEA.setText(nmea);

            // 20170428 Lynette : Add for Check NMEA & compare Test Time
            int nIndex = nmea.indexOf("GPGGA", 0);
            //Log.d("MFG_TEST", String.valueOf(nIndex));
            if ((nIndex >= 1) && bChkNMEA) {
                //bChkNMEA=false;
                Intent intent = GPSTest.this.getIntent();
                int nChkNMEA = intent.getIntExtra("ChkNMEA", 0);
                Log.d("MFG_TEST", bChkNMEA + "," + nChkNMEA + "\r\n");

                Log.d("MFG_TEST", "GPS Check NMEA SUCCESSFUL TEST\r\n");
                //20171002 - Allen added, Kevin removes for debugging
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        setResult(1);
//                    }
//                });
                GPSTest.this.finish();
//                finishAffinity();
            }

            //Log.d("MFG_TEST", "System.currentTimeMillis()= " + System.currentTimeMillis());
            //Log.d("MFG_TEST", "mStartTime= " + mStartTime);
            //Log.d("MFG_TEST", "g_nTTFF= " + g_nTTFF);
//20171002 - Allen remove and Kevin add back due to GPS test fail
            if (((System.currentTimeMillis() - mStartTime) * 0.001) > (g_nTTFF)) {
                //20171002 - Allen added, Kevin removes for debugging
                //Setting.fail();
                //setResult(0);
                Log.d("MFG_TEST", "GPS Test Time out!!\r\nUUT-FAIL\r\n");
                locationManager.sendExtraCommand(locationManager.GPS_PROVIDER, "delete_aiding_data", null);
                finishAffinity();
                System.exit(0);
                return;
            }
//20171002 
            // Lynette end
        }
    };
    // Lynette end


    // 列舉狀態
    enum drawAction {
        create, drawGPSBar, clear,
    }

    // 更新畫面
    void draw(drawAction action) {
        Canvas canvas = null;
        if (this.isFinishing())
            return;

        try {
            // 鎖定
            canvas = surfaceHolder.lockCanvas(null);

            synchronized (surfaceHolder) {
                // 依照目前的狀態做不同的繪圖
                switch (action) {
                    // 在程式一開始的時候畫
                    case create:
                        drawBackground(canvas);
                        break;

                    // 畫出右向轉90度的圖片
                    case drawGPSBar:
                        drawBar(canvas);
                        break;

                    // 清除畫面
                    case clear:
                        canvas.drawColor(Color.BLUE);
                        break;
                }
            }
        } finally {
            if (canvas != null) {
                // 解除鎖定
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    void drawBackground(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);

        //canvas.drawColor(0xff111F77);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(new RectF(0, 0, sView.getWidth(), sView.getHeight()), 16.0f, 16.0f, paint);
        paint.setColor(0xff080837);
        canvas.drawRoundRect(new RectF(1, 1, sView.getWidth() - 1, sView.getHeight() - 1), 16.0f, 16.0f, paint);

        // 畫白線
        Paint paint1 = new Paint();
        paint1.setStrokeWidth(1);
        paint1.setColor(Color.WHITE);
        canvas.drawLine(5, sView.getHeight() - 40, sView.getWidth() - 5, sView.getHeight() - 40, paint1);

//	    paint1.setTextSize(18);
//	      paint1.setAntiAlias(true);
//	      paint1.setTypeface(Typeface.MONOSPACE);
//	      canvas.drawText("1234567890",50,50,paint1);
//	      paint1.setTypeface(Typeface.SANS_SERIF);
//	      canvas.drawText("1234567890",50,70,paint1);
//	      paint1.setTypeface(Typeface.DEFAULT_BOLD);
//	      canvas.drawText("1234567890",50,90,paint1);
//	      paint1.setTypeface(Typeface.SERIF);
//	      canvas.drawText("1234567890",50,110,paint1);
//	      paint1.setTypeface(Typeface.DEFAULT);
//	      canvas.drawText("1234567890",50,130,paint1);
    }

    void drawBar(Canvas canvas) {
        int left = 8;
        int top = 0;
        int right = 40;
        int bottom = sView.getHeight() - 40;
        int gap = right - left + 8;

        //canvas.drawColor(0xff111F77);
        drawBackground(canvas);
        int satCount = 0;
        while (it.hasNext()) {

            GpsSatellite oSat = (GpsSatellite) it.next();
            top = (int) oSat.getSnr();
            if (top > g_nMIN_SNR)
                satCount++;
            // Check number of satellites in list to determine fix state
            Paint paint = new Paint();
            // 去鋸齒
            paint.setAntiAlias(true);

            // 設定paint的顏色
            paint.setColor(Color.RED);
            if (top > 10)
                paint.setColor(0xffFBA506);
            if (top > 20)
                paint.setColor(Color.YELLOW);
            if (top > 30)
                paint.setColor(0xff99FB06);
            if (top > 40)
                paint.setColor(Color.GREEN);
            // 設定paint的style為STROKE：空心的
            paint.setStyle(Paint.Style.FILL);
            // 設定paint的外框寬度
            canvas.drawRect(left, bottom - top * 2, right, bottom, paint);

            Paint paint1 = new Paint();
            paint1.setStrokeWidth(2);
            paint1.setColor(Color.WHITE);
            canvas.drawLine(left - 1, bottom, left - 1, bottom - top * 2, paint1);
            canvas.drawLine(right + 1, bottom, right + 1, bottom - top * 2, paint1);
            canvas.drawLine(left - 2, bottom - top * 2, right + 2, bottom - top * 2, paint1);

            paint.setTextSize(18);
            paint.setAntiAlias(true);
            paint.setTypeface(Typeface.DEFAULT);
            canvas.drawText(Integer.toString(top), left + 2, bottom - top * 2 - 5, paint);
            paint.setTextSize(15);
            canvas.drawText(Integer.toString(oSat.getPrn()), left + 7, sView.getHeight() - 18, paint);

            left = left + gap;
            right = right + gap;
        }

        Log.d("MFG_TEST", "satCount is: " + satCount + "\r\n");
        Log.d("MFG_TEST", "g_nSAT_NUM is: " + g_nSAT_NUM + "\r\n");

        if (satCount >= g_nSAT_NUM) {
            mIsSatNumberPass = true;
            Log.d("MFG_TEST", "Get GPS Sat number PASS\r\n");

            if (mIsSatNumberPass && mIsTTFFPass) {
                //Setting.pass();
                Log.d("MFG_TEST", "GPS SUCCESSFUL TEST\r\n");
                //USILog.append(GPSTest.this, "GPS TEST SUCCESSFUL");
                locationManager.removeGpsStatusListener(listener);
                locationManager.removeUpdates(locationListener);
                locationManager = null;
                finishAffinity();
                return;
            }
        }
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        // mStartTime=System.currentTimeMillis();

        listener = new GpsStatus.Listener() {
            @Override
            public void onGpsStatusChanged(int event) {
			//20171002 - Check 
            //    if(locationManager!=null)
            //    {
			//20171002
                    GpsStatus status = locationManager.getGpsStatus(null);
                    switch (event) {
                        case GpsStatus.GPS_EVENT_STARTED:
                            Log.d("MFG_TEST", "GPS Test Start\r\n");
                            break;
                        case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                            Iterable<GpsSatellite> sats = status.getSatellites();
                            it = sats.iterator();
                            //if(action !=null && action.equals("GPS_FIX_TEST")){
                            if ((System.currentTimeMillis() - mStartTime) > (g_nTTFF + 3000)) {
                                Log.d("MFG_TEST", "GPS_EVENT_SATELLITE_STATUS time is: " + (System.currentTimeMillis() - mStartTime));
                                Log.d("MFG_TEST", "First Fix Time out!!\r\nUUT-FAIL\r\n");
                                locationManager.removeGpsStatusListener(listener);
                                locationManager.removeUpdates(locationListener);
                                locationManager = null;

                                //Setting.fail();
                                finishAffinity();
                                return;
                            }
                            //}
                            draw(drawAction.drawGPSBar);
                            break;
                        case GpsStatus.GPS_EVENT_FIRST_FIX:
                            Log.d("MFG_TEST", "GPS First Fix!\r\n");
                            int ttff = status.getTimeToFirstFix();
                            txtFixTime.setText("Time first fix : " + Integer.toString(ttff));
                            if (ttff < g_nTTFF) {
                                mIsTTFFPass = true;
                                Log.d("MFG_TEST", "GPS_EVENT_FIRST_FIX time is: " + (System.currentTimeMillis() - mStartTime));
                                Log.d("MFG_TEST", "GPS TTFF PASS\r\n");
                                //finishAffinity();
                                //return;
                            }
                            break;
                        // Lynette
                        case GpsStatus.GPS_EVENT_STOPPED:
                            Log.d("MFG_TEST", "定位结束");
                            break;
				//20171002			
                //}

                    // Lynette ===
                }
            }
        };
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        9527);
            }
        } else {
            request_updates();
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
                    request_updates();

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

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        try {
            locationManager.removeGpsStatusListener(listener);
            locationManager.removeUpdates(locationListener);
            locationManager.removeNmeaListener(nmeaListener);

            locationManager = null;
            this.mWakeLock.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestart() {
        // TODO Auto-generated method stub
        super.onRestart();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }
}
