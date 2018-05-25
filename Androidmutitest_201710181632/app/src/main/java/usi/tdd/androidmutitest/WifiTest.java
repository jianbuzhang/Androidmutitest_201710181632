package usi.tdd.androidmutitest;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by Admin on 2016/8/24.
 */
public class WifiTest {
    String strGetWifiMAC;
    boolean bRet, bWifiEnable;
    public WifiInfo mWifiInfo;

    public boolean DisWifi(File fileLog, String szItemName, WifiManager mWifiManager) {
        bRet = mWifiManager.setWifiEnabled(false);
        SystemClock.sleep(2000);
        if (bRet) {
            Log.d("MFG_TEST", "Disable Wifi Power!!\r\nSUCCESSFUL TEST\r\n");
        } else {
            Log.d("MFG_TEST", "Disable Wifi Power Fail!!\r\nUUT-FAIL\r\n");
        }
        return bRet;
    }

    public boolean EnableWifi(File fileLog, String szItemName, WifiManager mWifiManager) {

        bRet = mWifiManager.setWifiEnabled(true);
        if (bRet)
            Log.d("MFG_TEST", "Enable WLAN Power SUCCESSFUL TEST\r\n");
        else
            Log.d("MFG_TEST", "Set WLAN Power On Fail!!\r\n");
        return bRet;
    }

    public void ReadWifiMAC(WifiManager mWifiManager) {
        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();

        if (mWifiManager.isWifiEnabled()) {
            bWifiEnable = true;
            //strGetWifiMAC = mWifiInfo.getMacAddress().toUpperCase().toString();
            // 20161116 Lynette
            StringBuilder builder = new StringBuilder();
            String line;
            try {
                BufferedReader reader = new BufferedReader(new FileReader("//sys/class/net/wlan0/address"), 256);
                try {
                    while ((line = reader.readLine()) != null)
                        builder.append(line);
                } finally {
                    strGetWifiMAC = builder.toString().toUpperCase();

                    reader.close();
                }
            } catch (IOException e) {
                strGetWifiMAC = "MAC_error";
            }
            // Lynette===

            if (strGetWifiMAC != null) {
                Log.d("MFG_TEST", "Get MAC is: " + strGetWifiMAC + "\r\n");
                Log.d("MFG_TEST", "VARSTRING GET_WLANMAC = '" + strGetWifiMAC + "'\r\n");
                Log.d("MFG_TEST", "Read Wifi MAC SUCCESSFUL\r\n");
            } else
                Log.d("MFG_TEST", "Read Wifi MAC Fail!!\r\nUUT-FAIL\r\n");
        } else {
            bRet = mWifiManager.setWifiEnabled(true);
            SystemClock.sleep(3500);
            if (mWifiManager.isWifiEnabled()) {
                bWifiEnable = true;
                strGetWifiMAC = mWifiInfo.getMacAddress().toUpperCase().toString();

                if (strGetWifiMAC != null) {
                    Log.d("MFG_TEST", "Get MAC is: " + strGetWifiMAC + "\r\n");
                    Log.d("MFG_TEST", "VARSTRING GET_WLANMAC = '" + strGetWifiMAC + "'\r\n");
                    Log.d("MFG_TEST", "Read Wifi MAC SUCCESSFUL\r\n");
                } else
                    Log.d("MFG_TEST", "Read Wifi MAC Fail!!\r\nUUT-FAIL\r\n");
            } else {
                Log.d("MFG_TEST", "Wifi Power Doesn't Enable & Enable Fail!!\r\n");
                Log.d("MFG_TEST", "Get Wifi MAC Fail!!\r\nUUT-FAIL\r\n");
            }
        }
    }

    public boolean checkWifiMAC(File fileLog, String szItemName, String strChkMAC, WifiManager mWifiManager) {
        //bRet = mWifiManager.setWifiEnabled(false);
        //SystemClock.sleep(2000);
        //bRet = mWifiManager.setWifiEnabled(true);
        //SystemClock.sleep(3500);

        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();

        if (mWifiManager.isWifiEnabled()) {
            bWifiEnable = true;
            //strGetWifiMAC = mWifiInfo.getMacAddress().toUpperCase().toString();
            // 20161116 Lynette
            StringBuilder builder = new StringBuilder();
            String line;
            try {
                BufferedReader reader = new BufferedReader(new FileReader("//sys/class/net/wlan0/address"), 256);
                try {
                    while ((line = reader.readLine()) != null)
                        builder.append(line);
                } finally {
                    strGetWifiMAC = builder.toString().toUpperCase();
                    reader.close();
                }
            } catch (IOException e) {
                strGetWifiMAC = "MAC_error";
            }
            // ignore ":"
            String[] splittedStr = strGetWifiMAC.split(":");
            strGetWifiMAC = "";
            for (String s : splittedStr) {
                strGetWifiMAC += s;
            }
            // Lynette===
            bRet = strGetWifiMAC.equals(strChkMAC);

            Log.d("MFG_TEST", "Get MAC is: " + strGetWifiMAC + "\r\n");
            Log.d("MFG_TEST", "Chk MAC is: " + strChkMAC + "\r\n");
            if (bRet)
                Log.d("MFG_TEST", "Check Wifi MAC SUCCESSFUL TEST\r\n");
            else
                Log.d("MFG_TEST", "Check WIfi MAC Fail!!\r\nUUT-FAIL\r\n");
        } else {
            bRet = mWifiManager.setWifiEnabled(true);
            SystemClock.sleep(3500);

            if (mWifiManager.isWifiEnabled()) {
                checkWifiMAC(fileLog, szItemName, strChkMAC, mWifiManager);
            } else {
                Log.d("MFG_TEST", "Wifi Power Doesn't Enable & Enable Fail!!\r\n");
                Log.d("MFG_TEST", "Get Wifi MAC Fail!!\r\nUUT-FAIL\r\n");
            }
        }

        if (bWifiEnable && bRet)
            return true;
        else
            return false;
    }



    public boolean PingtoAP(WifiManager g_mWifiManager, String szSSID, String szIP, String szPingCount, int nPassRate) {
        String szMAC, szMAC2, szPingCmd, szResult = "";
        int nCount, nMAC;

        if (!g_mWifiManager.isWifiEnabled())
            g_mWifiManager.setWifiEnabled(true);

        while (!g_mWifiManager.isWifiEnabled()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        WifiConfiguration wcon = new WifiConfiguration();
        wcon.SSID = "\"".concat(szSSID).concat("\"");
        wcon.status = WifiConfiguration.Status.DISABLED;
        wcon.priority = 40;
        wcon.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wcon.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        wcon.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        wcon.allowedAuthAlgorithms.clear();
        wcon.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wcon.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wcon.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40); //AP加密模式為WEP
        wcon.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104); //AP加密模式為WEP
        wcon.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wcon.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

        int wID = g_mWifiManager.addNetwork(wcon);
        if (wID != -1) {
            g_mWifiManager.enableNetwork(wID, true);
            Log.d("MFG_TEST", "Connecting....");
        } else
            Log.d("MFG_TEST", "Enable Wlan Failed!!");

        nCount = 0;
        mWifiInfo = g_mWifiManager.getConnectionInfo();
        // 20170525 Lynette
        //ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        //State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        // Lynette end
        while (!"COMPLETED".equals(mWifiInfo.getSupplicantState().toString())) {
            nCount++;
            if (nCount >= 15) {
                Log.d("MFG_TEST", "Link AP Error!!\r\nUUT-FAIL\r\n");
                return false;
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mWifiInfo = g_mWifiManager.getConnectionInfo();
        }

        mWifiInfo = g_mWifiManager.getConnectionInfo();
        nMAC = mWifiInfo.getIpAddress();
        szMAC = (nMAC & 0xFF) + "." + ((nMAC >> 8) & 0xFF) + "." + ((nMAC >> 16) & 0xFF) + "." + ((nMAC >> 24) & 0xFF);
        szMAC2 = (nMAC & 0xFF) + "." + ((nMAC >> 8) & 0xFF);

        nCount = 0;
        while ("0.0.0.0".equals(szMAC.toString()) || "169.254".equals(szMAC2.toString())) {
            nCount++;
            if (nCount >= 20) {
                Log.d("MFG_TEST", "Get IP Addr Error!!\r\nUUT-FAIL\r\n");
                return false;
            }

            Log.d("MFG_TEST", "Fetch Ip....");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mWifiInfo = g_mWifiManager.getConnectionInfo();
            nMAC = mWifiInfo.getIpAddress();
            szMAC = (nMAC & 0xFF) + "." + ((nMAC >> 8) & 0xFF) + "." + ((nMAC >> 16) & 0xFF) + "." + ((nMAC >> 24) & 0xFF);
            szMAC2 = (nMAC & 0xFF) + "." + ((nMAC >> 8) & 0xFF);
        }

        nCount = 0;
        while (!(mWifiInfo.getRssi() < 0) || !(mWifiInfo.getRssi() > -200)) {
            nCount++;
            if (nCount >= 15) {
                Log.d("MFG_TEST", "Get RSSI Error!!\r\nUUT-FAIL\r\n");
                return false;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mWifiInfo = g_mWifiManager.getConnectionInfo();
        }
        Log.d("MFG_TEST", "Start Ping Test...\r\n");

        szPingCmd = "ping -c " + szPingCount + " " + szIP; //-c ping 次數 -w 整體Timeout時間，單位秒
        Log.d("MFG_TEST", "Cmd: " + szPingCmd);
        try {
            Process process = Runtime.getRuntime().exec(szPingCmd);
            process.waitFor();
            InputStream inStream = process.getInputStream();
            byte[] reData = new byte[1024];

            while (inStream.read(reData) != -1) {
                szResult = szResult + new String(reData);
                //Log.d("MFG_TEST", szResult);
            }
            inStream.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }

        int nTransmitCount = 0;
        int nReceiverCount = 0;

        String[] arrayStr = szResult.split("\\n+");
        for (int nIdx = 0; nIdx < arrayStr.length - 1; nIdx++) {
            Log.d("MFG_TEST", arrayStr[nIdx]);
            if (arrayStr[nIdx].indexOf("transmitted,") != -1) {
                String szTemp = arrayStr[nIdx];
                if (szTemp.indexOf("errors") != -1)
                    return false;

                nTransmitCount = Integer.parseInt(szTemp.split(",")[0].replace("packets transmitted", "").trim());
                nReceiverCount = Integer.parseInt(szTemp.split(",")[1].replace("received", "").trim());
            }
        }
        int nPingCount = Integer.parseInt(szPingCount);
        float fPassRate = (int) ((nReceiverCount / (float) nPingCount) * 100);
        if (nReceiverCount > 0) {
            Log.d("MFG_TEST", "Ping " + szPingCount + " times, Pass " + nReceiverCount + " times, Pass Rate: "
                    + String.valueOf(fPassRate) + "%");
        }

        if (fPassRate >= nPassRate)
            Log.d("MFG_TEST", "SUCCESSFUL TEST\r\n");
        else
            Log.d("MFG_TEST", "UUT-FAIL\r\n");

        wID = g_mWifiManager.getConnectionInfo().getNetworkId();
        g_mWifiManager.disconnect();
        g_mWifiManager.removeNetwork(wID);
        g_mWifiManager.saveConfiguration();

        return true;
    }

    public boolean LinkRSSI(WifiManager g_mWifiManager, String szSSID, String szMax, String szMin, String szItemName) {
        int nRSSIMax = 0,nRSSIMin=0, nMAC, nCount, nRSSI;
        //boolean bRet = true;
        String szMAC, szMAC2;

        if (!szMax.isEmpty())
            nRSSIMax = Integer.parseInt(szMax);
        else {
            Log.d("MFG_TEST", "RSSIMax parameter is unavailable!\r\nUUT-FAIL\r\n");
            return false;
        }
        if (!szMin.isEmpty())
            nRSSIMin = Integer.parseInt(szMin);
        else {
            Log.d("MFG_TEST", "RSSIMax parameter is unavailable!\r\nUUT-FAIL\r\n");
            return false;
        }
        if (!g_mWifiManager.isWifiEnabled())
            g_mWifiManager.setWifiEnabled(true);

        while (!g_mWifiManager.isWifiEnabled()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        WifiConfiguration wcon = new WifiConfiguration();
        wcon.allowedAuthAlgorithms.clear();
        wcon.allowedGroupCiphers.clear();
        wcon.allowedKeyManagement.clear();
        wcon.allowedPairwiseCiphers.clear();
        wcon.allowedProtocols.clear();
        wcon.SSID = "\"" + szSSID + "\"";
        //wcon.wepKeys[0] = "";
        wcon.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        //wcon.wepTxKeyIndex = 0;
        wcon.status = WifiConfiguration.Status.DISABLED;
        wcon.priority = 40;

        WifiConfiguration wifiFound = null;

        List<WifiConfiguration> existingConfigs = g_mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals(wcon.SSID)) {
                wifiFound = existingConfig;
            }
        }
        //Log.d("MFG_TEST", "wifiFound: " + wifiFound.toString() );

		/*wcon.SSID="\"".concat(szSSID).concat("\"");
        wcon.status = WifiConfiguration.Status.DISABLED;
		wcon.priority = 40;
		wcon.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		wcon.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
		wcon.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
		wcon.allowedAuthAlgorithms.clear();
		wcon.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
		wcon.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
		wcon.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40); //AP加密模式為WEP
		wcon.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104); //AP加密模式為WEP
		wcon.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		wcon.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		wcon.wepKeys[0] = "";*/

        Log.d("MFG_TEST", "LinkAP: " + szSSID);
        //SystemClock.sleep(2000);
        int wID = 0;

        for (int nTest = 0; nTest < 3; nTest++) {
            Log.d("MFG_TEST", "wID_1: " + Integer.toString(wID));
            g_mWifiManager.addNetwork(wcon);

            if (wID != -1) {
                g_mWifiManager.disconnect();
                g_mWifiManager.enableNetwork(wID, true);
                g_mWifiManager.reconnect();
                Log.d("MFG_TEST", "Connecting....");
                break;
            } else {
                Log.d("MFG_TEST", "Add Network Failed!! TestCount: " + nTest);
            }

            Log.d("MFG_TEST", "wID_2: " + Integer.toString(wID));
            SystemClock.sleep(2000);
        }

        Log.d("MFG_TEST", "wID_3: " + Integer.toString(wID));
        SystemClock.sleep(10000);
        nCount = 0;


        mWifiInfo = g_mWifiManager.getConnectionInfo();
		/*while( !"COMPLETED".equals( mWifiInfo.getSupplicantState().toString()) )
		{
			Log.d("MFG_TEST","WifiInfo" + mWifiInfo.getSupplicantState().toString() + ", Sleep Count: " + nCount );
			nCount++;
			if( nCount >= 15 )
			{
				Log.d("MFG_TEST", "Link AP Error!!\r\nUUT-FAIL\r\n");
				return false;
			}

			SystemClock.sleep(2000);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mWifiInfo = g_mWifiManager.getConnectionInfo();
		}*/

        SystemClock.sleep(2000);
        mWifiInfo = g_mWifiManager.getConnectionInfo();

        nMAC = mWifiInfo.getIpAddress();
        szMAC = (nMAC & 0xFF) + "." + ((nMAC >> 8) & 0xFF) + "." + ((nMAC >> 16) & 0xFF) + "." + ((nMAC >> 24) & 0xFF);
        szMAC2 = (nMAC & 0xFF) + "." + ((nMAC >> 8) & 0xFF);

        nCount = 0;
        while ("0.0.0.0".equals(szMAC.toString()) || "169.254".equals(szMAC2.toString())) {
            nCount++;
            if (nCount >= 20) {
                Log.d("MFG_TEST", "Get IP Addr Error!!\r\nUUT-FAIL\r\n");
                return false;
            }

            Log.d("MFG_TEST", "Fetch Ip...." + nCount);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mWifiInfo = g_mWifiManager.getConnectionInfo();
            nMAC = mWifiInfo.getIpAddress();
            szMAC = (nMAC & 0xFF) + "." + ((nMAC >> 8) & 0xFF) + "." + ((nMAC >> 16) & 0xFF) + "." + ((nMAC >> 24) & 0xFF);
            szMAC2 = (nMAC & 0xFF) + "." + ((nMAC >> 8) & 0xFF);
        }

        //SystemClock.sleep(2000);
        nCount = 0;
        while (!(mWifiInfo.getRssi() < 0) || !(mWifiInfo.getRssi() > -200)) {
            nCount++;
            if (nCount >= 15) {
                Log.d("MFG_TEST", "Get RSSI Error!!\r\nUUT-FAIL\r\n");
                return false;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mWifiInfo = g_mWifiManager.getConnectionInfo();
        }

        Log.d("MFG_TEST", "LinkUpAP: " + mWifiInfo.getSSID());

        // 20170524 Lynette
        StringBuilder builder = new StringBuilder();
        String line;
        try {
            BufferedReader reader = new BufferedReader(new FileReader("//sys/class/net/wlan0/address"), 256);
            try {
                while ((line = reader.readLine()) != null)
                    builder.append(line);
            } finally {
                strGetWifiMAC = builder.toString().toUpperCase();
                reader.close();
            }
        } catch (IOException e) {
            strGetWifiMAC = "MAC_error";
        }
        Log.d("MFG_TEST", "DUT MAC Addr: " + strGetWifiMAC);
        // Lynette end
        Log.d("MFG_TEST", "IP Addr: " + szMAC.toString());
        Log.d("MFG_TEST", "Speed: " + mWifiInfo.getLinkSpeed());
        Log.d("MFG_TEST", "RSSI: " + mWifiInfo.getRssi());

        nRSSI = mWifiInfo.getRssi();
        if (nRSSI >= nRSSIMax && nRSSI <=nRSSIMin)
            Log.d("MFG_TEST", "SUCCESSFUL TEST\r\n");
        else
            Log.d("MFG_TEST", "UUT-FAIL\r\n");

        wID = g_mWifiManager.getConnectionInfo().getNetworkId();
        g_mWifiManager.disconnect();
        g_mWifiManager.removeNetwork(wID);
        g_mWifiManager.saveConfiguration();

        // 20170524 Lynette
        g_mWifiManager.removeNetwork(wcon.networkId);
        Log.d("MFG_TEST", "Disconnect!");
        // 20170524 Lynette end

        return true;
    }

    public boolean ManualLink(WifiManager g_mWifiManager, String szSSID, boolean Connection) {
        int nMAC, nCount;
        String szMAC, szMAC2;

        if (!g_mWifiManager.isWifiEnabled())
            g_mWifiManager.setWifiEnabled(true);

        while (!g_mWifiManager.isWifiEnabled()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        WifiConfiguration wcon = new WifiConfiguration();
        wcon.allowedAuthAlgorithms.clear();
        wcon.allowedGroupCiphers.clear();
        wcon.allowedKeyManagement.clear();
        wcon.allowedPairwiseCiphers.clear();
        wcon.allowedProtocols.clear();
        wcon.SSID = "\"" + szSSID + "\"";
        wcon.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wcon.status = WifiConfiguration.Status.DISABLED;
        wcon.priority = 40;

        WifiConfiguration wifiFound = null;
        List<WifiConfiguration> existingConfigs = g_mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals(wcon.SSID)) {
                wifiFound = existingConfig;
            }
        }
        Log.d("MFG_TEST", "LinkAP: " + szSSID);

        int wID = 0;
        for (int nTest = 0; nTest < 3; nTest++) {
            Log.d("MFG_TEST", "wID_1: " + Integer.toString(wID));
            g_mWifiManager.addNetwork(wcon);

            if (wID != -1) {
                g_mWifiManager.disconnect();
                g_mWifiManager.enableNetwork(wID, true);
                g_mWifiManager.reconnect();
                Log.d("MFG_TEST", "Connecting....");
                break;
            } else {
                Log.d("MFG_TEST", "Add Network Failed!! TestCount: " + nTest);
            }

            Log.d("MFG_TEST", "wID_2: " + Integer.toString(wID));
            SystemClock.sleep(2000);
        }

        Log.d("MFG_TEST", "wID_3: " + Integer.toString(wID));
        SystemClock.sleep(10000);


        mWifiInfo = g_mWifiManager.getConnectionInfo();
        SystemClock.sleep(2000);
        mWifiInfo = g_mWifiManager.getConnectionInfo();

        nMAC = mWifiInfo.getIpAddress();
        szMAC = (nMAC & 0xFF) + "." + ((nMAC >> 8) & 0xFF) + "." + ((nMAC >> 16) & 0xFF) + "." + ((nMAC >> 24) & 0xFF);
        szMAC2 = (nMAC & 0xFF) + "." + ((nMAC >> 8) & 0xFF);

        nCount = 0;
        while ("0.0.0.0".equals(szMAC.toString()) || "169.254".equals(szMAC2.toString())) {
            nCount++;
            if (nCount >= 20) {
                Log.d("MFG_TEST", "Get IP Addr Error!!\r\nUUT-FAIL\r\n");
                return false;
            }

            Log.d("MFG_TEST", "Fetch Ip...." + nCount);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            mWifiInfo = g_mWifiManager.getConnectionInfo();
            nMAC = mWifiInfo.getIpAddress();
            szMAC = (nMAC & 0xFF) + "." + ((nMAC >> 8) & 0xFF) + "." + ((nMAC >> 16) & 0xFF) + "." + ((nMAC >> 24) & 0xFF);
            szMAC2 = (nMAC & 0xFF) + "." + ((nMAC >> 8) & 0xFF);
        }

        //nCount = 0;
        Log.d("MFG_TEST", "LinkUpAP: " + mWifiInfo.getSSID());

        // 20170524 Lynette
        StringBuilder builder = new StringBuilder();
        String line;
        try {
            BufferedReader reader = new BufferedReader(new FileReader("//sys/class/net/wlan0/address"), 256);
            try {
                while ((line = reader.readLine()) != null)
                    builder.append(line);
            } finally {
                strGetWifiMAC = builder.toString().toUpperCase();
                reader.close();
            }
        } catch (IOException e) {
            strGetWifiMAC = "MAC_error";
        }
        Log.d("MFG_TEST", "DUT MAC Addr: " + strGetWifiMAC);
        // Lynette end

        Log.d("MFG_TEST", "IP Addr: " + szMAC.toString());

        wID = g_mWifiManager.getConnectionInfo().getNetworkId();
        Log.d("MFG_TEST", "wID: " + wID);
        if(!Connection){
            g_mWifiManager.disconnect();
            g_mWifiManager.removeNetwork(wID);
            g_mWifiManager.saveConfiguration();

            // 20170524 Lynette
            g_mWifiManager.removeNetwork(wcon.networkId);
            Log.d("MFG_TEST", "Disconnect!");
            // 20170524 Lynette end
        }


        return true;
    }
}
