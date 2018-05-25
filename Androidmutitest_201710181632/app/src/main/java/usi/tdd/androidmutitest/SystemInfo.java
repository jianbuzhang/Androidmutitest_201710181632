package usi.tdd.androidmutitest;

import android.content.pm.PackageInfo;
import android.util.Log;
import android.view.Display;
import android.view.Surface;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;

/**
 * Created by Admin on 2016/8/24.
 */
public class SystemInfo {
    /*
     * szVersion[0][] Item Name
	 * szVersion[1][] Check Version
	 * szVersion[2][] Get Version from device
	 * szVersion[3][] ErrorCode
	 * szVersion[4][] CheckResult
	 *
	 * Log.d("MFG_TEST", "Length: " + szVersion.length); //列
	 * Log.d("MFG_TEST", "Length0: " + szVersion[0].length); //欄
	 * Log.d("MFG_TEST", "Length1: " + szVersion[1].length); //欄
	 * Log.d("MFG_TEST", "Length2: " + szVersion[2].length); //欄
	 * Log.d("MFG_TEST", "Length[0][1]: " + szVersion[0][0]);
	 * Log.d("MFG_TEST", "Length[1][0]: " + szVersion[1][0]);
	 * Log.d("MFG_TEST", "Length[1][2]: " + szVersion[1][2]); //陣列內容
	 * Log.d("MFG_TEST", "Length[1][2]: " + szVersion[1][2].length()); //陣列內容長度
	*/

    BufferedWriter buf;
    String strErrorUUT;

    public boolean CommpareVer(File fileLog, String[][] szVersion, Boolean bTestMsg) {
        int nIdx;
        boolean bRet = false;
        for (nIdx = 0; nIdx < szVersion[0].length; nIdx++) {
            if (szVersion[1][nIdx].length() > 0) {
                if (szVersion[1][nIdx].equals(szVersion[2][nIdx])) {
                    szVersion[4][nIdx] = "TRUE";
                    bRet = true;
                } else {
                    if (nIdx == 2) {
                        if (szVersion[2][2].contains(szVersion[1][2])) {
                            szVersion[4][2] = "TRUE";
                            bRet = true;
                            break;
                        }
                    }
                    bRet = false;
                    szVersion[4][nIdx] = "FALSE";
                    strErrorUUT = "ERROR-UUT:" + szVersion[3][nIdx];
                    break;
                }
            }
        }

        try {
            for (nIdx = 0; nIdx < szVersion[0].length; nIdx++)
                Log.d("MFG_TEST", "Get " + szVersion[0][nIdx] + ": " + szVersion[2][nIdx] + "\r\n");

            for (nIdx = 0; nIdx < szVersion[0].length; nIdx++) //��
            {
                if (szVersion[1][nIdx].length() > 0)
                    Log.d("MFG_TEST", "Check " + szVersion[0][nIdx] + ": " + szVersion[1][nIdx] + "\r\n");
            }

            if (bRet)
            {
                if (bTestMsg)
                    Log.d("MFG_TEST", "VersionTest TEST_PASS\r\n");
                else
                    Log.d("MFG_TEST", "VersionTest SUCCESSFUL TEST\r\n");
            }
            else {
                if (bTestMsg)
                    Log.d("MFG_TEST", "VersionTest TEST_FAIL\r\n");
                else
                    Log.d("MFG_TEST", "VersionTest Fail!!\r\nUUT-FAIL\r\n");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bRet;
    }

    public String GetISTVer(Display display) {
        String str = "";

        StringBuilder builder = new StringBuilder();
        String line;

        try {
            BufferedReader reader = new BufferedReader(new FileReader("/sys/devices/platform/sspam-spi.0/spi_master/spi0/spi0.0/ist/version"), 256);
            try {
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            Log.e("IST", "IO Exception when getting IST Ver for Device Info screen", e);
            return "Unavailable";
        }
        str = "IST FW: " + builder.toString() + "\n";

        //Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int orient = display.getRotation();
        switch (orient) {
            case Surface.ROTATION_0:
                str = str + "Direction: ROTATION_0" + "\n";
                break;

            case Surface.ROTATION_180:
                str = str + "Direction: ROTATION_180" + "\n";
                break;

            case Surface.ROTATION_270:
                str = str + "Direction: ROTATION_270" + "\n";
                break;

            case Surface.ROTATION_90:
                str = str + "Direction: ROTATION_90" + "\n";
                break;
        }
        return str;
    }

    public String GetEA() {
        StringBuilder builder = new StringBuilder();
        String line;

        try {
            BufferedReader reader = new BufferedReader(new FileReader("/sys/class/extagent/extagent/version"), 256);
            try {
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            Log.e("EA", "IO Exception when getting EA Ver for Device Info screen", e);
            return "Unavailable";
        }
        return builder.toString();
    }

    public String GetSSPAM() {
        StringBuilder builder = new StringBuilder();
        String line;

        try {
            BufferedReader reader = new BufferedReader(new FileReader("/sys/class/sspam/fpga/version"), 256);
            try {
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            Log.e("SSPAM", "IO Exception when getting SSPAM version for Device Info screen", e);
            return "Unavailable";
        }
        return builder.toString();
    }

    public String GetXLOADER() {
        Process p = null;
        StringBuilder builder = new StringBuilder();

        try {
            p = Runtime.getRuntime().exec("getprop ro.boot.xloader");
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        try {
            String xloader = in.readLine();
            builder.append(xloader);
            Log.d("", "XLOADER_VERSION = " + xloader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    // 20170124 Lynette: Add for checking version of common ES package
    private static final String PKG_DEMOS_PREFIX = "com.honeywell.demos";
    private static final String PKG_TOOLS_PREFIX = "com.honeywell.tools";
    private static final String PKG_HONEYWELL_PREFIX = "com.honeywell";
    private static final String PKG_INTERMEC_PREFIX = "com.intermec";
    private static final String PKG_LICENSE_SERVICE_PREFIX = "com.honeywell.licenseservice";
    private static final String PKG_DATACOLLECTION_SERVICE_PREFIX = "com.intermec.datacollectionservice";

    public Boolean getESPkgInfo(MainActivity mainActivity, String szVer, boolean bPrint) {
        Boolean bChkResult = true;
        StringBuffer result = new StringBuffer();
        StringBuffer demosInfo = new StringBuffer();
        StringBuffer toolsInfo = new StringBuffer();
        StringBuffer appsInfo = new StringBuffer();

        List<PackageInfo> pkgInfo = mainActivity.getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < pkgInfo.size(); i++) {
            PackageInfo info = pkgInfo.get(i);
            if (info.packageName.contains(PKG_TOOLS_PREFIX)) {
                toolsInfo.append(getAppName(info.packageName, PKG_TOOLS_PREFIX.length() + 1) + ": " + info.versionName);
                if (info.packageName.indexOf("provisioner") > 0) {
                    toolsInfo.append("\n");
                    continue;
                }
                if (info.packageName.indexOf("launcher") > 0) {

                    toolsInfo.append("\n");
                    continue;
                }

                if (!info.versionName.equals(szVer)) {
                    if (bPrint)
                        toolsInfo.append("  ===  Compare with toolsInfo (" + szVer + ") failed! ===\r\n");
                    bChkResult = false;
                } else
                    toolsInfo.append("\n");
            } else if (info.packageName.contains(PKG_DEMOS_PREFIX)) {
                demosInfo.append(getAppName(info.packageName, PKG_DEMOS_PREFIX.length() + 1) + ": " + info.versionName);
                if (!info.versionName.equals(szVer)) {
                    if (bPrint)
                        demosInfo.append("  ===  Compare with demosInfo (" + szVer + ") failed! ===\r\n");
                    bChkResult = false;
                } else
                    demosInfo.append("\n");
            } else if (info.packageName.equals(PKG_LICENSE_SERVICE_PREFIX)) {
                toolsInfo.append(getAppName(info.packageName, PKG_HONEYWELL_PREFIX.length() + 1) + ": " + info.versionName + "\n");
            } else if (info.packageName.equals(PKG_DATACOLLECTION_SERVICE_PREFIX)) {
                toolsInfo.append(getAppName(info.packageName, PKG_INTERMEC_PREFIX.length() + 1) + ": " + info.versionName + "\n");
            } else
                appsInfo.append(info.packageName + ": " + info.versionName + "\n");
        }


        result.append("\nPOWER TOOLS AND DEMOS INFO\n");
        result.append("-------------------------\n");
        result.append(toolsInfo);
        result.append(demosInfo);
        // Log.d(TAG, "appsInfo:" + appsInfo);
        if (bPrint)
            Log.d("MFG_TEST", result.toString() + "\n");
        return bChkResult;
    }

    private String getAppName(String pkgName, int subIndex) {
        if (pkgName != null && pkgName.length() != 0)
            return new String(pkgName.substring(subIndex).toUpperCase(Locale.ENGLISH));
        else
            return "null";
    }
    // Lynette end
}
