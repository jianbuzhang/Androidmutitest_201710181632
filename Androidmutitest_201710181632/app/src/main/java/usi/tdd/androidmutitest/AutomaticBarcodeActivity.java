package usi.tdd.androidmutitest;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.ScannerNotClaimedException;
import com.honeywell.aidc.ScannerUnavailableException;
import com.honeywell.aidc.TriggerStateChangeEvent;
import com.honeywell.aidc.UnsupportedPropertyException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutomaticBarcodeActivity extends Activity implements BarcodeReader.BarcodeListener,
        BarcodeReader.TriggerListener {

    private com.honeywell.aidc.BarcodeReader barcodeReader;
    private ListView barcodeList;
    private  String g_sData,g_sAuto;
    private boolean g_bHasTestResult=false;
    public String Big2D, C39_5mil, C39_10mil, C39_20percent,EX20,S6603ER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);
        Intent intent = getIntent();
        g_sData = intent.getStringExtra("Data");
        //20171206 Kevin get barcode data from MFGDefine.xml
        EX20="";
        S6603ER="ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890AbCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		try {
            Big2D = GetDefine.getValue("scan-barcode", "QR", "BigTwoD");
            C39_5mil = GetDefine.getValue("scan-barcode", "C39", "FiveMIL");
            C39_10mil = GetDefine.getValue("scan-barcode", "C39", "TenMIL");
            C39_20percent = GetDefine.getValue("scan-barcode", "C39", "TwentyPercent");
        } catch (Exception e) {
            Log.d("MFG_TEST", e.toString());
            Log.d("MFG_TEST", "Initial Parameter Fail!!");
            this.finish();
        }
		
		if(g_sData.equals("Big2D")) {
			g_sData=Big2D;
		}
		if(g_sData.equals("C39_5mil"))	{
			g_sData=C39_5mil;
		}
		if(g_sData.equals("C39_10mil"))	{
			g_sData=C39_10mil;
		}
        if(g_sData.equals("C39_20percent"))	{
            g_sData=C39_20percent;
        }
        if(g_sData.equals("EX20"))	{
            g_sData=EX20;
        }
        if(g_sData.equals("6603ER"))	{
            g_sData=S6603ER;
        }
        //20171206 Kevin

        g_sAuto = intent.getStringExtra("Auto");
        // set lock the orientation
        // otherwise, the onDestory will trigger when orientation changes
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        

        // get bar code instance from MainActivity
        barcodeReader = MainActivity.getBarcodeObject();

        if (barcodeReader != null) {

            // register bar code event listener
            barcodeReader.addBarcodeListener(this);

            // set the trigger mode to client control
            try {
                barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                        BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
            } catch (UnsupportedPropertyException e) {
                Toast.makeText(this, "Failed to apply properties", Toast.LENGTH_SHORT).show();
            }
            // register trigger state change listener
            barcodeReader.addTriggerListener(this);

            Map<String, Object> properties = new HashMap<String, Object>();
//            // Set Symbologies On/Off
//            properties.put(BarcodeReader.PROPERTY_CODE_128_ENABLED, true);
//            properties.put(BarcodeReader.PROPERTY_GS1_128_ENABLED, true);
//            properties.put(BarcodeReader.PROPERTY_QR_CODE_ENABLED, true);
//            properties.put(BarcodeReader.PROPERTY_CODE_39_ENABLED, true);
//            properties.put(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true);
//            properties.put(BarcodeReader.PROPERTY_UPC_A_ENABLE, true);
//            properties.put(BarcodeReader.PROPERTY_EAN_13_ENABLED, false);
//            properties.put(BarcodeReader.PROPERTY_AZTEC_ENABLED, false);
//            properties.put(BarcodeReader.PROPERTY_CODABAR_ENABLED, false);
//            properties.put(BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED, false);
//            properties.put(BarcodeReader.PROPERTY_PDF_417_ENABLED, false);
//            // Set Max Code 39 barcode length
            properties.put(BarcodeReader.PROPERTY_CODE_39_MAXIMUM_LENGTH, 50);
//            // Turn on center decoding
//            properties.put(BarcodeReader.PROPERTY_CENTER_DECODE, true);
//            // Enable bad read response
//            properties.put(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, true);
//            // Apply the settings
            barcodeReader.setProperties(properties);
        }

        // get initial list
        barcodeList = (ListView) findViewById(R.id.listViewBarcodeData);

        new Thread(new Runnable() {
            @Override
            public void run() {

                Log.v("barcodescan", "doscan");
                try {
                    g_bHasTestResult=false;
                    Thread.sleep(500);
                    if(g_sAuto.equals("true"))
                    {
                        barcodeReader.aim(true);
                        barcodeReader.light(true);
                        barcodeReader.decode(true);
                    }
                    Thread.sleep(120000);
                    if(!g_bHasTestResult)
                    {
                        if(g_sAuto.equals("true"))
                        {
                            barcodeReader.aim(false);
                            barcodeReader.light(false);
                            barcodeReader.decode(false);
                        }
                        Log.d("MFG_TEST", "UUT-FAIL   time out\r\n");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                Setting.fail();
                                AutomaticBarcodeActivity.this.finish();
                            }
                        });

                    }
                } catch (ScannerNotClaimedException localScannerNotClaimedException) {
                    localScannerNotClaimedException.printStackTrace();
                    Log.d("MFG_TEST", "UUT-FAIL   time out\r\n");

                } catch (ScannerUnavailableException localScannerUnavailableException) {
                    localScannerUnavailableException.printStackTrace();
                    Log.d("MFG_TEST", "UUT-FAIL   time out\r\n");

                } catch (Exception localException) {
                    localException.printStackTrace();
                    Log.d("MFG_TEST", "UUT-FAIL   time out\r\n");

                }



            }
        }).start();
    }

    @Override
    public void onBarcodeEvent(final BarcodeReadEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                g_bHasTestResult=true;
                // update UI to reflect the data
                List<String> list = new ArrayList<String>();
                list.add("Barcode data: " + event.getBarcodeData());
                list.add("Check  data: " +g_sData);
                list.add("Check result: " +event.getBarcodeData().equals(g_sData));
                list.add("Character Set: " + event.getCharset());
                list.add("Code ID: " + event.getCodeId());
                list.add("AIM ID: " + event.getAimId());
                list.add("Timestamp: " + event.getTimestamp());
                Log.d("MFG_TEST","Barcode data: " + event.getBarcodeData());
                Log.d("MFG_TEST","Check  data: " +g_sData);
                Log.d("MFG_TEST","Check result: " +event.getBarcodeData().equals(g_sData));
                Log.d("MFG_TEST","Code ID: " + event.getCodeId());
                Log.d("MFG_TEST","AIM ID: " + event.getAimId());
                Log.d("MFG_TEST","Timestamp: " + event.getTimestamp());
                if(event.getBarcodeData().equals(g_sData))
                {
                    Log.d("MFG_TEST", "SUCCESSFUL TEST\r\n");
                    list.add("MFG_TEST : "+ "SUCCESSFUL TEST\r\n");
                    Setting.mainActivity.finish();
//                    Setting.pass();
                }
                else
                {
                    Log.d("MFG_TEST", "UUT-FAIL\r\n");
                    list.add("MFG_TEST : "+ "UUT-FAIL\r\n");
                    Setting.mainActivity.finish();
//                    Setting.fail();
                }
                    finish();
                final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
                        AutomaticBarcodeActivity.this, android.R.layout.simple_list_item_1, list);

                barcodeList.setAdapter(dataAdapter);
            }
        });
    }

    // When using Automatic Trigger control do not need to implement the
    // onTriggerEvent function
    @Override
    public void onTriggerEvent(TriggerStateChangeEvent event) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onResume() {
        super.onResume();
        if (barcodeReader != null) {
            try {
                barcodeReader.claim();
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                Toast.makeText(this, "Scanner unavailable", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (barcodeReader != null) {
            // release the scanner claim so we don't get any scanner
            // notifications while paused.
            barcodeReader.release();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (barcodeReader != null) {
            // unregister barcode event listener
            barcodeReader.removeBarcodeListener(this);

            // unregister trigger state change listener
            barcodeReader.removeTriggerListener(this);
        }
    }
}
