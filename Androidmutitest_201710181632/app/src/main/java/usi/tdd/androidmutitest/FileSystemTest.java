package usi.tdd.androidmutitest;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * Created by Admin on 2016/8/24.
 */
public class FileSystemTest {
    final String str1 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    File fileDataDir;
    StatFs statFs;
    long nBlockSize, nTotalBlocks, nAvailaBlock/*, MBSize*/;

    public boolean FileSystemTest(File fileLog, String strStorage, MainActivity mainActivityThis) {
        String strPath = null;
        try {
            // 20161027Lynette: Modify SD path
            if (strStorage.equals("SD")) {
                String[] storagePaths = getStoragePaths(mainActivityThis);
                String sdcardRoot = null;
                if (storagePaths != null) {
                    for (String path : storagePaths) {
                        if ((!path.contains("IPSM")) && (!path.contains("emulated"))) {
                            sdcardRoot = path;
                            // 20171016 Lynette: Add SD path for
                            Log.d("MFG_TEST", "VARSTRING SD_PATH = '" + sdcardRoot + "'\r\n");
                            // 20161027Lynette: Modify SD path
                            break;
                        }
                    }
                }

                if (sdcardRoot != null) {
                    String status = Environment.getExternalStorageState(new File(sdcardRoot));
                    if (status.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
                        status = Environment.MEDIA_MOUNTED;
                        Log.d("MFG_TEST", "SD Card Status: " + "READONLY\r\nUUT-FAIL\r\n");
                    }
                    if (status.equals(Environment.MEDIA_MOUNTED)) {
                        try {
                            //strPath = sdcardRoot;
                            strPath = Environment.getExternalStorageDirectory().toString();
                            Log.d("MFG_TEST", "START SD Card File System test");
                        } catch (IllegalArgumentException e) {// this can occur if the SD card is removed, but we haven't received the ACTION_MEDIA_REMOVED Intent yet.
                            Log.d("MFG_TEST", "SD Card Status: " + "REMOVED_Exception\r\nUUT-FAIL\r\n");

                            return false;
                        }
                    } else {
                        Log.d("MFG_TEST", "SD Card Status: " + "REMOVED_No Mounted\r\nUUT-FAIL\r\n");
                        return false;
                    }
                } else {
                    Log.d("MFG_TEST", "SD Card Status: " + "REMOVED_Null\r\nUUT-FAIL\r\n");
                    return false;
                }
            }// Lynette ===
            else {
                strPath = fileLog.toString();
                Log.d("MFG_TEST", "START Internal Storage File System test");
            }

            // file system check
            Log.d("MFG_TEST", "strPath is: " + strPath + "\r\n");
            File fileDirectory1 = new File(strPath + "/dir1/");
            if (!fileDirectory1.exists()) {
                Log.d("MFG_TEST", "Create Directory 'dir1'\r\n");
                if (fileDirectory1.mkdirs())
                    Log.d("MFG_TEST", "Create Directory " + fileDirectory1.getPath() + " Done!!\r\n");
                else {
                    Log.d("MFG_TEST", "Create Directory " + fileDirectory1.getPath() + " Error!!\r\n");
                    return false;
                }
            }
            File fileDirectory2 = new File(strPath + "/dir1/dir2/");
            if (!fileDirectory2.exists()) {
                Log.d("MFG_TEST", "Create Directory 'dir2'\r\n");

                if (fileDirectory2.mkdirs())
                    Log.d("MFG_TEST", "Create Directory " + fileDirectory2.getPath() + " Done!!\r\n");
                else {
                    Log.d("MFG_TEST", "Create Directory " + fileDirectory2.getPath() + " Error!!\r\n");
                    return false;
                }
            }

            // write
            Log.d("MFG_TEST", "Create File 'Test.cfg'\r\n");
            File file = new File(strPath + "/dir1/dir2/test.cfg");
            Properties wprt = new Properties();
            for (int nIdx = 1; nIdx <= 100; nIdx++)
                wprt.put("str" + Integer.toString(nIdx), str1);

            try {
                FileOutputStream stream = new FileOutputStream(file, false);
                wprt.store(stream, "");
                Log.d("MFG_TEST", "Write test.cfg to " + fileDirectory2.getPath() + " Done\r\n");
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("MFG_TEST", e.toString());
                return false;
            }

            //read
            Log.d("MFG_TEST", "Read Data from 'test.cfg'\r\n");
            Properties rprt = new Properties();
            try {
                FileInputStream rstream = new FileInputStream(file);
                rprt.load(rstream);
                Log.d("MFG_TEST", "Read data from " + file.getPath() + " Done\r\n");
                rstream.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.d("MFG_TEST", e.toString());
                return false;
            }

            // check file content
            if (str1.equals(rprt.get("str1").toString()))
                Log.d("MFG_TEST", "Check Write Data Done!!\r\n");
            else {
                Log.d("MFG_TEST", "Check Write Data Error!!\r\n");
                return false;
            }

            // remove file
            Log.d("MFG_TEST", "Delete test.cfg\r\n");
            if (new File(strPath + "/dir1/dir2/test.cfg").delete())
                Log.d("MFG_TEST", "Delete Test.cfg Done!!\r\n");
            else {
                Log.d("MFG_TEST", "Delete Test.cfg Error!!\r\n");
                return false;
            }

            Log.d("MFG_TEST", "Delete directory 'dir2'\r\n");
            if (fileDirectory2.delete())
                Log.d("MFG_TEST", "Delete Directory 'dir2' Done!!\r\n");
            else {
                Log.d("MFG_TEST", "Delete Directory 'dir2' Error!!\r\n");
                return false;
            }

            Log.d("MFG_TEST", "Delete Directory 'dir1'\r\n");
            if (fileDirectory1.delete())
                Log.d("MFG_TEST", "Delete Directory 'dir1' Done!!\r\n");
            else {
                Log.d("MFG_TEST", "Delete Directory 'dir1' Error!!\r\n");
                return false;
            }

            testPass();
            return true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    private void testPass() {
        Log.d("MFG_TEST", "SUCCESSFUL TEST\r\n");
    }

    public void FileSystemSizeCheck(String strStorage, String strSize, MainActivity mainActivityThis) {
        if (strStorage.equals("RAM")) {
            Log.d("MFG_TEST", "START RAM File System Size Test");
            //StringBuilder builder = new StringBuilder();
            String line, strGetRamSize;
            try {
                BufferedReader reader = new BufferedReader(new FileReader("/proc/meminfo"), 256);
                try {
                    while ((line = reader.readLine()) != null) {
                        int nIdx = line.lastIndexOf("MemTotal:");
                        if (nIdx != -1) {
                            strGetRamSize =new String( line.substring(9, line.length()));
                            strGetRamSize=new String(strGetRamSize.toLowerCase().replace("kb", "").replace(" ", ""));
                            long getRamSize = (Long.parseLong(strGetRamSize)) / 1024;
                            strGetRamSize = String.valueOf(getRamSize);

                            Log.d("MFG_TEST", "Read DUT RAM size: " + getRamSize + " MB\r\n");
                            Log.d("MFG_TEST", "Expected RAM size: " + strSize + " MB\r\n");

                            if (strGetRamSize.equals(strSize)) {
                                Log.d("MFG_TEST", "FileSystem RAM Size SUCCESSFUL TEST\r\n");
                                Toast.makeText(mainActivityThis, "FileSystem RAM Size SUCCESSFUL TEST", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.d("MFG_TEST", "FileSystem RAM Size Fail!!\r\nUUT-FAIL\r\n");
                                Toast.makeText(mainActivityThis, "FileSystem RAM Size Fail!!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } finally {
                    reader.close();
                }
            } catch (IOException e) {
                Log.e("memInfo", "IO Exception when getting EA Ver for Device Info screen", e);
            }
            return;
        } else if (strStorage.toUpperCase().equals("SD")) {
            String[] storagePaths = getStoragePaths(mainActivityThis);
            String sdcardRoot = null;
            if (storagePaths != null) {
                for (String path : storagePaths) {
                    //Log.d("MFG_TEST", "SD_path: " + path);
                    if ((!path.contains("IPSM")) && (!path.contains("emulated"))) {
                        sdcardRoot = path;
                        break;
                    }
                }
            }

            if (sdcardRoot != null) {
                //String status = Environment.getStorageState(new File(sdcardRoot));
                String status = Environment.getExternalStorageState(new File(sdcardRoot));

                if (status.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
                    status = Environment.MEDIA_MOUNTED;
                    Log.d("MFG_TEST", "SD Card Status: " + "READONLY" + "\r\n");
                }
                if (status.equals(Environment.MEDIA_MOUNTED)) {
                    try {
                        statFs = new StatFs(sdcardRoot);
                        Log.d("MFG_TEST", "fileDataDir is: " + sdcardRoot + "\r\n");

                    } catch (IllegalArgumentException e) {// this can occur if the SD card is removed, but we haven't received the ACTION_MEDIA_REMOVED Intent yet.
                        Log.d("MFG_TEST", "SD Card Status: " + "REMOVED_Exception\r\nUUT-FAIL\r\n");
                        Toast.makeText(mainActivityThis, "REMOVED_Exception!!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    Log.d("MFG_TEST", "SD Card Status: " + "REMOVED_No Mounted\r\nUUT-FAIL\r\n");
                    Toast.makeText(mainActivityThis, "REMOVED_No Mounted!!", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                Log.d("MFG_TEST", "SD Card Status: " + "REMOVED_Null\r\nUUT-FAIL\r\n");
                Toast.makeText(mainActivityThis, "REMOVED_Null!!", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (strStorage.equals("Flash")) {
            // Internal Storage
            fileDataDir = Environment.getDataDirectory();
            statFs = new StatFs(fileDataDir.getPath());
            Log.d("MFG_TEST", "fileDataDir is: " + fileDataDir.toString() + "\r\n");
        } else if (strStorage.toUpperCase().equals("IPSM")) {
            String IPSM_ROOT_19 = "/storage/IPSM";
            File ipsm = new File(IPSM_ROOT_19);
            if (ipsm.exists()) {
                statFs = new StatFs(IPSM_ROOT_19);
            } else {
                Log.d("MFG_TEST", "IPSM Folder doesn't exist!\r\nUUT-FAIL\r\n");
                Toast.makeText(mainActivityThis, "IPSM Folder doesn't exist!!", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d("MFG_TEST", "fileDataDir is: " + IPSM_ROOT_19 + "\r\n");
        }
        // 獲取每個block的SIZE
        nBlockSize = statFs.getBlockSizeLong();
        // 獲取BLOCK總數
        nTotalBlocks = statFs.getBlockCountLong();
        // 獲取可供程式使用的Block的數量
        nAvailaBlock = statFs.getAvailableBlocksLong();

        long nTotalSize = (nTotalBlocks * nBlockSize);
        long nAvailaSize = (nAvailaBlock * nBlockSize);

        Log.d("MFG_TEST", "FileSystem Size Test\r\n");
        Log.d("MFG_TEST", "Available size: " + nAvailaSize + "\r\n");
        Log.d("MFG_TEST", "Total size: " + nTotalSize + "\r\n");
        Log.d("MFG_TEST", "Expected size: " + strSize + "\r\n");

        if (Long.toString(nTotalSize).equals(strSize)) {
            testPass();
            Toast.makeText(mainActivityThis, "IPSM SUCCESSFUL TEST!!", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("MFG_TEST", "FileSystem Size Failed!\r\nUUT-FAIL\r\n");
            Toast.makeText(mainActivityThis, "IPSM Size Failed!!", Toast.LENGTH_SHORT).show();
        }
    }

    // 20161024Lynette: by sysInfo, from HSM's Sample code
    public static String[] getStoragePaths(Context context) {
        StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            return (String[]) sm.getClass().getMethod("getVolumePaths").invoke(sm);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }
    // Lynette ===

    // Lynette:for File Access
    public void FileAccess(String szType, String szPath) {
        try {
            //IPSM?
            //File fileDataDir = Environment.getRootDirectory();
            //Log.d("MFG_TEST", "fileDataDir is: " + fileDataDir.toString() + "\r\n");
            //szPath = Environment.getRootDirectory().toString() + "/" + "Hello.wav";

            File FileAccessPath = new File(szPath);
            Log.d("MFG_TEST", "szPath is: " + szPath + "\r\n");

            if (szType.equals("Find")) {
                if (FileAccessPath.exists())
                    testPass();
                else
                    Log.d("MFG_TEST", "NOT EXIST\r\nUUT-FAIL\r\n");
            } else if (szType.equals("Delete")) {
                if (FileAccessPath.exists()) {
                    if (FileAccessPath.isDirectory())
                        deleteFile(FileAccessPath);

                    if (FileAccessPath.delete())
                        testPass();
                    else
                        Log.d("MFG_TEST", "Wrong!\r\nUUT-FAIL\r\n");
                } else
                    Log.d("MFG_TEST", "File is not exist\r\nSUCCESSFUL TEST\r\n");
            } else if (szType.equals("ReadTxt")) {
                // /storage/emulated/0/honeywell/sysinfo/sysinfo.txt
                FileInputStream FItSeam = new FileInputStream(FileAccessPath);
                BufferedReader reader = new BufferedReader(new InputStreamReader(FItSeam));
                Log.d("MFG_TEST", "========== Read txt file start ==========");
                String line;
                while ((line = reader.readLine()) != null)
                    Log.d("MFG_TEST", line);
                reader.close();
                Log.d("MFG_TEST", "========== Read txt file finish ==========");
            } else
                Log.d("MFG_TEST", "szType is wrong!\r\nUUT-FAIL\r\n");
        } catch (Exception e) {
            Log.d("MFG_TEST", e.toString());
        }

    }

    // Recursive delete all files in Folder
    public void deleteFile(File f) {
        String[] fList = f.list();
        for (int i = 0; i < fList.length; i++) {
            //Log.d("MFG_TEST", f.getAbsolutePath() + "/" + fList[i]);
            File filePoint = new File(f.getAbsolutePath() + "/" + fList[i]);
            if (filePoint.isDirectory()) {
                deleteFile(filePoint);
                if (!filePoint.delete()) {
                    Log.d("MFG_TEST", "Delete " + f.getAbsolutePath() + "/" + fList[i] + "Delete Test.cfg Error!\r\nUUT-FAIL\r\n");
                    return;
                }
            } else {
                if (!filePoint.delete()) {
                    Log.d("MFG_TEST", "Delete " + f.getAbsolutePath() + "/" + fList[i] + "Delete Test.cfg Error!\r\nUUT-FAIL\r\n");
                    return;
                }
            }
        }
    }
    // Lynette===


}
