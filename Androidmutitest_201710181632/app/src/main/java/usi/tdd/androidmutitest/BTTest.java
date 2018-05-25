package usi.tdd.androidmutitest;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import net.vidageek.mirror.dsl.Mirror;

import java.io.BufferedWriter;
import java.io.File;

/**
 * Created by Admin on 2016/8/24.
 */
public class BTTest {
    final String SECURE_SETTINGS_BLUETOOTH_ADDRESS = "bluetooth_address";
    String strGetBTMAC;
    boolean bRet, bBTEnable, bResult;
    BufferedWriter buf;
    BluetoothAdapter mBT = BluetoothAdapter.getDefaultAdapter();

    void ReadBTMAC(Context context) {
        while (!mBT.isEnabled()) {
            mBT.enable();
            SystemClock.sleep(3000);
        }

        if (mBT.isEnabled()) {
            strGetBTMAC = getBtAddressViaReflection().toUpperCase();
            if (strGetBTMAC != null && !strGetBTMAC.equals("02:00:00:00:00:00")) {
                Log.d("MFG_TEST", "Get BT MAC is: " + strGetBTMAC + "\r\n");
                Log.d("MFG_TEST", "VARSTRING GET_BTMAC = '" + strGetBTMAC + "'\r\n");
                Log.d("MFG_TEST", "Read BT MAC SUCCESSFUL\r\n");
            } else if (strGetBTMAC.equals("02:00:00:00:00:00")) {
                try {
                    ContentResolver mContentResolver = context.getContentResolver();
                    strGetBTMAC = Settings.Secure.getString(mContentResolver, SECURE_SETTINGS_BLUETOOTH_ADDRESS);

                    Log.d("MFG_TEST", "Get BT MAC is: " + strGetBTMAC + "\r\n");
                    Log.d("MFG_TEST", "VARSTRING GET_BTMAC = '" + strGetBTMAC + "'\r\n");
                    Log.d("MFG_TEST", "Read BT MAC SUCCESSFUL\r\n");

                } catch (Exception e) {
                }
            } else
                Log.d("MFG_TEST", "Read BT MAC Fail!!\r\nUUT-FAIL\r\n");
            /*
            strGetBTMAC = mBT.getAddress().toUpperCase().toString();
            if( strGetBTMAC != null && !strGetBTMAC.equals("02:00:00:00:00:00"))
            {
                Log.d("MFG_TEST", "Get BT MAC is: " + strGetBTMAC + "\r\n");
                Log.d("MFG_TEST", "VARSTRING GET_BTMAC = '" + strGetBTMAC + "'\r\n");
                Log.d("MFG_TEST", "Read BT MAC SUCCESSFUL\r\n");
            }
            else if(strGetBTMAC.equals("02:00:00:00:00:00"))
            {
                try {
                    ContentResolver mContentResolver = context.getContentResolver();
                    strGetBTMAC = Settings.Secure.getString(mContentResolver, SECURE_SETTINGS_BLUETOOTH_ADDRESS);

                    Log.d("MFG_TEST", "Get BT MAC is: " + strGetBTMAC + "\r\n");
                    Log.d("MFG_TEST", "VARSTRING GET_BTMAC = '" + strGetBTMAC + "'\r\n");
                    Log.d("MFG_TEST", "Read BT MAC SUCCESSFUL\r\n");

                } catch (Exception e) {
                }
            }
            else
                Log.d("MFG_TEST", "Read BT MAC Fail!!\r\nUUT-FAIL\r\n");
                */
        } else {
            Log.d("MFG_TEST", "BT Power Doesn't Enable & Enable Fail!!");
            Log.d("MFG_TEST", "Get MAC Error!!\r\nUUT-FAIL\r\n");
        }
    }

    boolean chkBTMAC(File fileLog, String szItemName, String strChkMAC) {
        if (mBT.isEnabled()) {
            strGetBTMAC = getBtAddressViaReflection().toUpperCase();
            bRet = strGetBTMAC.equals(strChkMAC);

            Log.d("MFG_TEST", "Get BT is: " + strGetBTMAC + "\r\n");
            Log.d("MFG_TEST", "DUT BT is: " + strChkMAC + "\r\n");
            if (bRet)
                Log.d("MFG_TEST", "Check BT_MAC SUCCESSFUL\r\n");
            else
                Log.d("MFG_TEST", "Check BT_MAC Fail!!\r\nUUT-FAIL\r\n");

        } else {
            mBT.enable();
            SystemClock.sleep(3000);
            if (mBT.isEnabled()) {
                strGetBTMAC = mBT.getAddress().toUpperCase().toString();
                bRet = strGetBTMAC.equals(strChkMAC);

                Log.d("MFG_TEST", "Get BT is: " + strGetBTMAC + "\r\n");
                Log.d("MFG_TEST", "DUT BT is: " + strChkMAC + "\r\n");
                if (bRet)
                    Log.d("MFG_TEST", "Check BT_MAC SUCCESSFUL TEST\r\n");
                else
                    Log.d("MFG_TEST", "Check BT_MAC Fail!!\r\nUUT-FAIL\r\n");
            } else {
                Log.d("MFG_TEST", "BT Power Doesn't Enable & Enable Fail!!");
                Log.d("MFG_TEST", "Get MAC Error!!\r\nUUT-FAIL\r\n");
            }
        }

        return bRet;
    }

    boolean DisBTPwr(File fileLog, String szItemName) {
        bRet = mBT.disable();
        SystemClock.sleep(1500);
        if (bRet) {
            Log.d("MFG_TEST", "Set BT Power OFF!!\r\n");
            Log.d("MFG_TEST", "Disable BT Power!!\r\nSUCCESSFUL TEST\r\n");
        } else {
            Log.d("MFG_TEST", "Set BT Power Fail!!\r\n");
            Log.d("MFG_TEST", "Disable BT Power Fail\r\n");
        }
        return bRet;
    }

    boolean EnBTPwr(File fileLog, String szItemName) {
        int nIdx;
        for (nIdx = 0; nIdx < 5; nIdx++) {
            mBT.enable();
            SystemClock.sleep(2000);
            bRet = mBT.enable();
            if (bRet)
                break;
        }

        if (bRet && nIdx <= 5) {
            Log.d("MFG_TEST", "Set BT Power ON!!\r\n");
            Log.d("MFG_TEST", "Enable BT Power!!\r\nSUCCESSFUL TEST\r\n");
        } else {
            Log.d("MFG_TEST", "Set BT Power Fail!!\r\n");
            Log.d("MFG_TEST", "Enable BT Power Fail\r\n");
        }
        return bRet;
    }

    private static String getBtAddressViaReflection() {
        Log.d("MFG_TEST", "1111111");
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Object bluetoothManagerService = new Mirror().on(bluetoothAdapter).get().field("mService");
        if (bluetoothManagerService == null) {
            //Log.w(TAG, "couldn't find bluetoothManagerService");
            return null;
        }
        Object address = new Mirror().on(bluetoothManagerService).invoke().method
                ("getAddress").withoutArgs();
        if (address != null && address instanceof String) {
            //Log.w(TAG, "using reflection to get the BT MAC address: " + address);
            return (String) address;
        } else {
            return null;
        }
    }
}
