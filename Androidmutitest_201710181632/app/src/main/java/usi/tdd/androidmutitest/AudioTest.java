package usi.tdd.androidmutitest;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Admin on 2016/8/24.
 */
//public class AudioTest {
public class AudioTest extends MainActivity {

    /////////////////////////////////////////////////////////////////////////////
// Controller Variable Definition
    private AudioManager g_AudioManager;
    private AudioRecord g_AudioRecorder;
    private AudioTrack g_AudioPlayer;
    private Button g_BtnRec, g_BtnPlay, g_BtnStop, g_BtnExit, g_BtnOpen;
    private Spinner g_CmbSampleRate, g_CmbTime, g_CmbVolume, g_CmbFreq;
    ;
    private CheckBox g_ChkGenerate;
    private RadioButton g_Rdo8Bit, g_Rdo16Bit, g_RdoMono, g_RdoStereo;
    private RadioGroup g_RdoGroupBPS, g_RdoGroupCHAN;
    private TextView g_StcLngTime, g_StcPosEnd, g_StcStatusMsg;
    private SeekBar g_SeekBarProgress;


    /////////////////////////////////////////////////////////////////////////////
// User Definition
    static final int MAXLENGTH = 512;
    static final int TM_PLAY = 16;
    static final int TM_REC = 17;
    static final int TM_AUTORUN = 15;
    static final int TM_LOOPBACK = 11;
    static final int UI_RECORD = 5;
    static final int UI_PLAY = 6;
    static final int UI_STOP = 7;
    static final int UI_INITIAL = 8;
    static final int CTRL_STC_LNGTIME = 20;
    static final int CTRL_BTN_STOP = 21;


    /////////////////////////////////////////////////////////////////////////////
//	Global Variable Definition
    private int g_nPlayBuffSize = 0;
    private int g_nRecBufferSize = 0;
    private int g_nCurSampleRate = 44100;
    private int g_nCurChannel = 1;
    private int g_nCurBitsPerSample = 16;
    private int g_nCurFreq = 1000;
    private int g_nCurVolume = 9;
    private int g_nActionTime = 60;
    private int g_nDefaultVolume = 0;
    private boolean g_bIsRecord = false;
    private boolean g_bIsPlaying = false;
    private boolean g_bGenerateWave = false;
    private boolean g_bhandsetOn = false;
    private String AudioRawFile = "/sdcard/record.raw";
    private String AudioWaveFile = "/sdcard/record.wav";
    private String LoopBackRawFile = "/sdcard/loopback.raw";
    private String AudioGenWaveFile = "/sdcard/generate.wav";
    private String OpenSrcRawFile = "/sdcard/opensrc.raw";

    /////////////////////////////////////////////////////////////////////////////
//	Global Variable From Parameter
    private int g_nParamTime = 5;
    private int g_nParamFreq = 1000;
    private int g_nParamSampleRate = 8000;
    private int g_nParamChannel = 1;
    private int g_nParamBitsPerSample = 16;
    private int g_nParamVolume = 9;
    private int g_nCurTimeMillis = 0;
    private String g_szParamWaveFile;
    private boolean g_bAutoTest = false;
    private boolean g_bAutoLoopback = false;
    private boolean g_bAutoPlayFile = false;
    private boolean g_bFileSetTime = false;
    private boolean g_bRecord = false;
    private boolean g_bAudioPlay = false;
    private int g_nLngMinute = 0;
    private int g_nLngSecond = 0;
    private int nAudCurrentVolume;
    private int nAudCurrentVolume1;
    private String szTempName;

    // alert dialog use

    public void AudioPlayFile(MediaPlayer mp, final AudioManager am, String szType, String szSetVolume, final MainActivity mainActivityThis) {
//        String szFilePath ;
//        if( szPath.toUpperCase().equals("SD"))
//            szFilePath = Environment.getExternalStorageDirectory().toString();
//        else
//            szFilePath = "/" + szPath ;

        try {

            int nAudMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            Log.d("MFG_TEST", "Get Max Volume: " + nAudMaxVolume);
            final int nAudCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            Log.d("MFG_TEST", "Current Volume: " + nAudCurrentVolume);
            am.setStreamVolume(AudioManager.STREAM_MUSIC, Integer.parseInt(szSetVolume), 0);  //音量效果 0:不顯示 1:顯示
            Log.d("MFG_TEST", "Set Volume to " + szSetVolume);
            mp.setVolume(1.0f, 1.0f);

//            szFilePath = szFilePath + "/" + szFile;
//            mp.setDataSource(szFilePath);
//            Log.d("MFG_TEST", "Play Audio Path: " + szFilePath + "\r\n");

            //mp.prepare();
            //Log.d( "MFG_TEST", "after mp.prepare!!");

            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    // TODO Auto-generated method stub
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, nAudCurrentVolume, 0);  //音量效果 0:不顯示 1:顯示
                    Log.d("MFG_TEST", "Play Audio SUCCESSFUL\n");
                    mainActivityThis.finish();
                }
            });

            Log.d("MFG_TEST", "Start Play Audio from " + szType + "!!\r\n");

            // Lynette:switch earpiece or speaker
            if (szType.equals("receiver")) {
                am.setSpeakerphoneOn(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    am.setMode(AudioManager.MODE_IN_COMMUNICATION);
                else
                    am.setMode(AudioManager.MODE_IN_CALL);
            } else if (szType.equals("speaker")) {
                am.setSpeakerphoneOn(true);
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            } else
                Log.d("MFG_TEST", "NO szType! UUT-FAIL\r\n");
            // Lynette===

            mp.start();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
        }
    }

    // parameter use
    public void AudioPlayFile(MediaPlayer mp, final AudioManager am, String szType, String szFile, String szPath, String szSetVolume, final MainActivity mainActivityThis) {
        String szFilePath;
        if (szPath.toUpperCase().equals("SD"))
            szFilePath = Environment.getExternalStorageDirectory().toString();
        else
            szFilePath = "/" + szPath;
        Log.d("MFG_TEST", szFilePath);

        try {

            int nAudMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            Log.d("MFG_TEST", "Get Max Volume: " + nAudMaxVolume);
            nAudCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            Log.d("MFG_TEST", "Current Volume: " + nAudCurrentVolume);
            am.setStreamVolume(AudioManager.STREAM_MUSIC, Integer.parseInt(szSetVolume), 0);  //音量效果 0:不顯示 1:顯示
            Log.d("MFG_TEST", "Set Volume to " + szSetVolume);
            nAudCurrentVolume1 = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            Log.d("MFG_TEST", "Current Volume1: " + nAudCurrentVolume1);
            mp.setVolume(1.0f, 1.0f);

            szFilePath = szFilePath + "/" + szFile;
            Log.d("MFG_TEST", "Play Audio Path: " + szFilePath + "\r\n");
            mp.setDataSource(szFilePath);
            Log.d("MFG_TEST", "Play Audio Path: " + szFilePath + "\r\n");

            mp.prepare();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    // TODO Auto-generated method stub
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, nAudCurrentVolume, 0);  //音量效果 0:不顯示 1:顯示
                    Log.d("MFG_TEST", "Play Audio SUCCESSFUL\n");
                    mainActivityThis.finish();
                }
            });
            Log.d("MFG_TEST", "Start Play Audio from " + szType + "!!\r\n");

            // Lynette:shift earpiece or speaker
            if (szType.equals("receiver")) {
                am.setSpeakerphoneOn(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    am.setMode(AudioManager.MODE_IN_COMMUNICATION);
                else
                    am.setMode(AudioManager.MODE_IN_CALL);
            } else if (szType.equals("speaker")) {
                am.setSpeakerphoneOn(true);
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            } else
                Log.d("MFG_TEST", "NO szType! UUT-FAIL\r\n");
            // Lynette===

            mp.start();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
    public void AudioLoopback(final AudioManager am, String szTime, String szFreq, String szRate, String szChannel, String szBit, String szPath)
    {
        g_AudioManager 			= am;
        g_nActionTime			= Integer.parseInt(szTime);
        g_nCurFreq				= Integer.parseInt(szFreq);
        g_nCurSampleRate		= Integer.parseInt(szRate);
        g_nCurChannel 			= Integer.parseInt(szChannel);
        g_nCurBitsPerSample		= Integer.parseInt(szBit);

        String szFilePath ;
        if( szPath.toUpperCase().equals("SD"))
            szFilePath = Environment.getExternalStorageDirectory().toString();
        else
            szFilePath = "/" + szPath;

        g_nDefaultVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.d( "MFG_TEST", "Current Volume: " + g_nDefaultVolume);

        Log.d("MFG_TEST", "Start Loopback Testing...");
        g_bhandsetOn = g_AudioManager.isWiredHeadsetOn();
        if( g_bhandsetOn )
        {
            Log.d("MFG_TEST", "Handset Status: " + g_bhandsetOn);
            g_bAutoLoopback = true;
            AudioPlay();
            AudioRec(am, szTime, szFreq, szRate, szChannel, szBit, szFilePath);
        }
        else
        {
            Log.d("MFG_TEST", "Handset Status: " + g_bhandsetOn);
            StopAudio();
        }
    }

    public void AudioPlay()
    {
        int nPlayChannelConfig	= 0;
        int nAudioEncoding 		= 0;

        //Create Audio Track
        if(g_nCurChannel == 2)
            nPlayChannelConfig= AudioFormat.CHANNEL_OUT_STEREO;
        else
            nPlayChannelConfig= AudioFormat.CHANNEL_OUT_MONO;

        if(g_nCurBitsPerSample == 8)
            nAudioEncoding = AudioFormat.ENCODING_PCM_8BIT;
        else
            nAudioEncoding = AudioFormat.ENCODING_PCM_16BIT;

        if(!g_bAutoPlayFile)
            g_nPlayBuffSize = AudioTrack.getMinBufferSize(g_nCurSampleRate,nPlayChannelConfig,nAudioEncoding);

        if(g_nPlayBuffSize < 0)
            Log.d("MFG_TEST", "AudioTrack getMinBufferSize Failed\r\n");
        else
            g_AudioPlayer = new AudioTrack(g_AudioManager.STREAM_MUSIC,g_nCurSampleRate,nPlayChannelConfig,nAudioEncoding,g_nPlayBuffSize,AudioTrack.MODE_STREAM);

        Log.d("MFG_TEST", "Start Play...");
        g_AudioPlayer.play();
        //	g_bIsRecord = false;
        g_bIsPlaying = true;
        new Thread(new PlayWaveThread()).start();
        TimerHandler.postDelayed(PLAY_TIMER,500);
    }*/

    class PlayWaveThread implements Runnable {
        private int nPlayChannelConfig = 0;
        private int nAudioEncoding = 0;
        private Message message;

        @Override
        public void run() {
            FileInputStream FileInStream = null;
            long longRawDataLen = 0;
            String szAudioFile;

            try {
                if (g_nRecBufferSize > 0)        //有錄音播錄音檔
                    FileInStream = new FileInputStream(AudioRawFile);
                else if (g_bAutoLoopback)     // Loopback 測試直接播音
                {
                    GenerateWaveFile(false);
                    FileInStream = new FileInputStream(LoopBackRawFile);
                } else if (g_bAutoPlayFile) {
                    //OpenWaveFile(g_szParamWaveFile);
                    Log.d("MFG_TEST", "Start Play " + OpenSrcRawFile + " file\r\n");
                    FileInStream = new FileInputStream(OpenSrcRawFile);
                } else {
                    if (g_bGenerateWave)
                        GenerateWaveFile(true);
                    else
                        GenerateWaveFile(false);

                    FileInStream = new FileInputStream(LoopBackRawFile);
                }

                int nRawDataLen = (int) FileInStream.getChannel().size(); // raw 實際檔案大小 (純資料內容)

                int nBlockAlign = g_nCurChannel * g_nCurBitsPerSample / 8;
                int nBytePs = nBlockAlign * g_nCurSampleRate;
                g_nLngMinute = nRawDataLen / nBytePs / 60;
                g_nLngSecond = nRawDataLen / nBytePs % 60;
                if (!g_bFileSetTime)
                    g_nActionTime = g_nLngMinute * 60 + g_nLngSecond;

                byte[] data = new byte[nRawDataLen];
                while (FileInStream.read(data) != -1) {
                    g_AudioPlayer.write(data, 0, data.length);
                }
                FileInStream.close();
                g_AudioPlayer.stop();
                g_bIsPlaying = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**/
    //以 8 bit 產生 wave 檔
    public boolean GenerateWaveFile(boolean bSaveToFile) {
        int nIdx = 0;
        int nSampleRate = g_nCurSampleRate;
        int nFreq = g_nCurFreq;
        int nActionTime = g_nActionTime;
        int nChuckSize = 0;
        int nTotalCycle = 0;


        double dSin = 2.0 * Math.PI * ((double) nFreq / (double) nSampleRate);

        //單聲道

        nTotalCycle = nActionTime * nFreq;
        nChuckSize = nSampleRate / nFreq * nTotalCycle;
        short sWave[] = new short[nChuckSize];
        for (nIdx = 0; nIdx < nChuckSize; nIdx++) {
            sWave[nIdx] = (short) (127 * (Math.sin(dSin * nIdx)));    //此例為16bit  以44個點為一弦波，取1000個
            //sDataBuf[nIdx] = (byte) (127 * Math.sin(dSin * nIdx));       //此例為8bit  以44個點為一弦波，取1000個
        }

        //雙聲道
        int nTotalDataLen = 0;
        int nAddNum = 0;

        nTotalDataLen = sWave.length * g_nCurChannel;
        nAddNum = g_nCurChannel;

        short sWave2[] = new short[nTotalDataLen];

        for (nIdx = 0; nIdx < nTotalDataLen; nIdx += nAddNum) {
            sWave2[nIdx] = sWave[nIdx / nAddNum];
            if (g_nCurChannel == 2)
                sWave2[nIdx + 1] = sWave[nIdx / nAddNum];
        }

        byte[] wave = Shorts2Bytes(sWave2);

        FileOutputStream fos = null;
        try {
            File file = new File(LoopBackRawFile);
            if (file.exists()) {
                file.delete();
            }
            fos = new FileOutputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            fos.write(wave);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (fos != null)
                fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (bSaveToFile) {
            CopyWaveFile(LoopBackRawFile, AudioGenWaveFile, g_nPlayBuffSize);

        }
        return true;
    }

    public byte[] Shorts2Bytes(short[] s) {
        byte bLength = 2;
        byte[] buf = new byte[s.length * bLength];
        for (int iLoop = 0; iLoop < s.length; iLoop++) {
            byte[] temp = getBytes(s[iLoop]);
            for (int jLoop = 0; jLoop < bLength; jLoop++) {
                buf[iLoop * bLength + jLoop] = temp[jLoop];
            }
        }
        return buf;
    }

    public byte[] getBytes(short s) {
        byte[] buf = new byte[2];
        for (int i = buf.length - 1; i >= 0; i--) {
            buf[i] = (byte) (s & 0x00ff);
            s >>= 8;
        }
        return buf;
    }

    Runnable PLAY_TIMER = new Runnable() {
        @Override
        public void run() {
            Message message = new Message();
            message.what = TM_PLAY;
            TimerHandler.sendMessage(message);
        }
    };


    public void AudioRec(final AudioManager am, String szTime, String szFreq, String szRate, String szChannel, String szBit, String szPath) {
        int nRecChannelConfig = 0;
        int nPlayChannelConfig = 0;
        int nAudioEncoding = 0;
        int nMaxVolume = 0;
        String szFilePath = null;
        g_AudioManager = am;
        g_nActionTime = Integer.parseInt(szTime);
        g_nCurFreq = Integer.parseInt(szFreq);
        g_nCurSampleRate = Integer.parseInt(szRate);
        g_nCurChannel = Integer.parseInt(szChannel);
        g_nCurBitsPerSample = Integer.parseInt(szBit);

        nMaxVolume = g_AudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        g_nDefaultVolume = g_AudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.d("MFG_TEST", "Get Max Volume: " + nMaxVolume);
        Log.d("MFG_TEST", "Get Current Volume: " + g_nDefaultVolume);

        if (szPath.toUpperCase().equals("SD"))
            szFilePath = Environment.getExternalStorageDirectory().toString();
        else {
            szFilePath = "/" + szPath;
            // Lynette
            //szFilePath = getApplicationContext().getFilesDir()
            // szFilePath = this.getCacheDir();
            // szFilePath = getBaseContext().getFilesDir().toString() + "/";
            // Lynette ===
        }
        Log.d("MFG_TEST", "szFilePath: " + szFilePath);

        if (!szPath.isEmpty()) {
            Log.d("MFG_TEST", "Rec Path is " + szFilePath);
            AudioRawFile = szFilePath + "/record.raw";
            AudioWaveFile = szFilePath + "/record.wav";
            LoopBackRawFile = szFilePath + "/loopback.raw";
            AudioGenWaveFile = szFilePath + "/generate.wav";
            OpenSrcRawFile = szFilePath + "/opensrc.raw";
            g_szParamWaveFile = szFilePath + "/" + szTempName;
        }

        //Create Audio Record
        if (g_nCurChannel == 2)
            nRecChannelConfig = AudioFormat.CHANNEL_IN_STEREO;
        else
            nRecChannelConfig = AudioFormat.CHANNEL_IN_MONO;

        if (g_nCurBitsPerSample == 8)
            nAudioEncoding = AudioFormat.ENCODING_PCM_8BIT;
        else
            nAudioEncoding = AudioFormat.ENCODING_PCM_16BIT;

        g_nRecBufferSize = AudioRecord.getMinBufferSize(g_nCurSampleRate, nRecChannelConfig, nAudioEncoding);
        if (g_nRecBufferSize < 0)
            Log.d("MFG_TEST", "AudioRecord getMinBufferSize Failed\r\n");
        else
            g_AudioRecorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, g_nCurSampleRate, nRecChannelConfig, nAudioEncoding, g_nRecBufferSize);

        Log.d("MFG_TEST", "Start Recording...\r\n");
        g_AudioRecorder.startRecording();
        g_bIsRecord = true;
        g_nCurTimeMillis = 0;
        new Thread(new RecWaveThread()).start();
        TimerHandler.postDelayed(REC_TIMER, 500);
        g_bRecord = true;
    }

    class RecWaveThread implements Runnable {
        @Override
        public void run() {
            WriteDataToFile();
            CopyWaveFile(AudioRawFile, AudioWaveFile, g_nRecBufferSize);
        }
    }

    private void WriteDataToFile() {
        byte[] bRecData = new byte[g_nRecBufferSize];
        int nReadSize = 0;
        FileOutputStream fos = null;

        try {
            File file = new File(AudioRawFile);
            if (file.exists()) {
                file.delete();
            }
            fos = new FileOutputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (g_bIsRecord == true) {
            nReadSize = g_AudioRecorder.read(bRecData, 0, g_nRecBufferSize);
            if (AudioRecord.ERROR_INVALID_OPERATION != nReadSize) {
                try {
                    fos.write(bRecData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            if (fos != null)
                fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void CopyWaveFile(String szInputFileName, String szOutputFileName, int nBuffSize) {
        FileInputStream FileInStream = null;
        FileOutputStream FileOutStream = null;
        long longRawDataLen = 0;
        byte[] data = new byte[nBuffSize];

        try {
            FileInStream = new FileInputStream(szInputFileName);
            FileOutStream = new FileOutputStream(szOutputFileName);
            longRawDataLen = FileInStream.getChannel().size();    // raw 實際檔案大小 (純資料內容)
            SetWaveHeader(FileOutStream, longRawDataLen);      // 幫 raw data 加表頭
            while (FileInStream.read(data) != -1) {
                FileOutStream.write(data);                    // 將 rawdata 寫入之後的 data 區塊
            }
            FileInStream.close();
            FileOutStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void SetWaveHeader(FileOutputStream FileOutStream, long longRawDataLen) throws IOException {
        long longRIFFChunkSize = 0;
        long longSampleRate = 0;
        long longByteRate = 0;
        int nChannels = 0;
        int nBitsPerSample = 0;
        byte[] WaveHeader = new byte[44];

        longRIFFChunkSize = longRawDataLen + 36;    //RIFF ChuckSize = 完整檔案大小 - 8，所以是 raw data + 44bytes 表頭 - 8  = raw data + 36
        longSampleRate = g_nCurSampleRate;

        if (g_nCurChannel == 1)
            nChannels = 1;
        else
            nChannels = 2;

        if (g_nCurBitsPerSample == 16)
            nBitsPerSample = 16;
        else
            nBitsPerSample = 8;

        longByteRate = (longSampleRate * nChannels * nBitsPerSample) / 8;

        //RIFF ChuckID
        WaveHeader[0] = 'R';
        WaveHeader[1] = 'I';
        WaveHeader[2] = 'F';
        WaveHeader[3] = 'F';

        //RIFF ChuckSize
        WaveHeader[4] = (byte) (longRIFFChunkSize & 0xff);
        WaveHeader[5] = (byte) ((longRIFFChunkSize >> 8) & 0xff);
        WaveHeader[6] = (byte) ((longRIFFChunkSize >> 16) & 0xff);
        WaveHeader[7] = (byte) ((longRIFFChunkSize >> 24) & 0xff);

        //RIFF Data Format
        WaveHeader[8] = 'W';
        WaveHeader[9] = 'A';
        WaveHeader[10] = 'V';
        WaveHeader[11] = 'E';

        //fmt ChuckID
        WaveHeader[12] = 'f';
        WaveHeader[13] = 'm';
        WaveHeader[14] = 't';
        WaveHeader[15] = ' ';

        //fmt ChuckSize
        WaveHeader[16] = 16;   // fmt ChuckSize 固定為16
        WaveHeader[17] = 0;
        WaveHeader[18] = 0;
        WaveHeader[19] = 0;

        //Audio Format
        WaveHeader[20] = 1;   //1 = PCM
        WaveHeader[21] = 0;

        //NumChannels
        WaveHeader[22] = (byte) nChannels;  //1 為單音，2為立體音
        WaveHeader[23] = 0;

        //SampleRate
        WaveHeader[24] = (byte) (longSampleRate & 0xff);
        WaveHeader[25] = (byte) ((longSampleRate >> 8) & 0xff);
        WaveHeader[26] = (byte) ((longSampleRate >> 16) & 0xff);
        WaveHeader[27] = (byte) ((longSampleRate >> 24) & 0xff);

        //ByteRate = (取樣率 * 頻道數 * 取樣大小)/8
        WaveHeader[28] = (byte) (longByteRate & 0xff);
        WaveHeader[29] = (byte) ((longByteRate >> 8) & 0xff);
        WaveHeader[30] = (byte) ((longByteRate >> 16) & 0xff);
        WaveHeader[31] = (byte) ((longByteRate >> 24) & 0xff);

        // BlockAlign =  (頻道數 * 取樣大小)/8
        WaveHeader[32] = (byte) ((nChannels * nBitsPerSample) / 8);
        WaveHeader[33] = 0;

        //BitsPerSample = 取樣大小為 8 or 16
        WaveHeader[34] = (byte) nBitsPerSample;
        WaveHeader[35] = 0;

        //data ChuckID
        WaveHeader[36] = 'd';
        WaveHeader[37] = 'a';
        WaveHeader[38] = 't';
        WaveHeader[39] = 'a';

        //data ChuckSize
        WaveHeader[40] = (byte) (longRawDataLen & 0xff);
        WaveHeader[41] = (byte) ((longRawDataLen >> 8) & 0xff);
        WaveHeader[42] = (byte) ((longRawDataLen >> 16) & 0xff);
        WaveHeader[43] = (byte) ((longRawDataLen >> 24) & 0xff);

        FileOutStream.write(WaveHeader, 0, 44);
    }


    Runnable REC_TIMER = new Runnable() {
        @Override
        public void run() {
            Message message = new Message();
            message.what = TM_REC;
            TimerHandler.sendMessage(message);
        }
    };


    private Handler TimerHandler = new Handler() {
        public void handleMessage(Message msg) {
            int nCurrentPosition = 0;
            int nTime = 0;

            super.handleMessage(msg);
            switch (msg.what) {
                case TM_REC:
                    Log.d("MFG_TEST", "Time " + g_nCurTimeMillis + " Sec.");
                    g_nCurTimeMillis++;
                    //限制錄音時數
                    if (g_nCurTimeMillis > g_nActionTime)
                        StopAudio();
                    else
                        TimerHandler.postDelayed(REC_TIMER, 1000);
                    break;
                case TM_PLAY:
                    nCurrentPosition = g_AudioPlayer.getPlaybackHeadPosition();
                    nTime = nCurrentPosition / g_nCurSampleRate;

                    if (nTime == g_nActionTime - 1)
                        StopAudio();
                    else
                        TimerHandler.postDelayed(PLAY_TIMER, 1000);

                    break;
            }
        }
    };

    private void StopAudio() {

        if (g_bIsRecord) {
            TimerHandler.removeCallbacks(REC_TIMER);
            g_bIsRecord = false;
            g_AudioRecorder.stop();
            g_AudioRecorder.release();
            g_AudioRecorder = null;
        }

        if (g_bIsPlaying) {
            TimerHandler.removeCallbacks(PLAY_TIMER);
            g_bIsPlaying = false;
            g_AudioPlayer.stop();
            g_AudioPlayer.release();
            g_AudioPlayer = null;
        }

        SystemClock.sleep(1000);
        g_AudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, g_nDefaultVolume, 0);

        if (g_bAutoLoopback && g_bhandsetOn)
            Log.d("MFG_TEST", "End Loopback Test!!\r\nSUCCESSFUL TEST");
        else if (g_bRecord)
            Log.d("MFG_TEST", "End Recording!!\r\nSUCCESSFUL TEST\r\n");
        else
            Log.d("MFG_TEST", "UUT-FAIL\r\n");

        finish();
    }

    ;
}
