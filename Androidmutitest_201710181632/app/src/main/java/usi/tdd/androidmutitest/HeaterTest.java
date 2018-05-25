package usi.tdd.androidmutitest;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Admin on 2017/2/16.
 */

public class HeaterTest {

    public boolean HEATER_PRESENT() {
        StringBuilder builder = new StringBuilder();
        String line;
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/sys/class/misc/heater/present"), 256);
            try {
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            Log.d("MFG_TEST", e.toString());
            return false;
        }
        Log.d("MFG_TEST", builder.toString());
        if (builder.toString().equals("1"))
            return true;
        else
            return false;
    }

    /*
    * /sys/class/misc/heater/operating_mode
    * Read/Write -  1 means manual control mode;
    *               0 means the auto control mode,
    * there is a thread to read the temperature scan_temp_input and
    * turn on the heater when the temperature is below to trip_point.
    * The heater will be turned off after 165 seconds when it is turned on.
    */
    public boolean Write_OperatingMode(boolean bOnOff) {
        String szOff;
        if (bOnOff)
            szOff = "1";
        else
            szOff = "0";

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("/sys/class/misc/heater/operating_mode", false));
            bw.write(szOff);
            bw.close();

        } catch (IOException e) {
            Log.d("MFG_TEST", e.toString());
            return false;
        }
        return true;
    }

    public String Read_OperatingMode() {
        StringBuilder builder = new StringBuilder();
        String line;

        try {
            BufferedReader reader = new BufferedReader(new FileReader("/sys/class/misc/heater/operating_mode"), 256);
            try {
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            Log.d("MFG_TEST", e.toString());
            return "Read_OperatingMode_Unavailable";
        }
        return builder.toString();
    }

    /*
    * /sys/class/misc/heater/on_duration
    * Read/Write – the on duration in seconds,
    * the heater will be turned off in on_duration after the heater is turned on.
    */
    public boolean Write_ON_DURATION(String szOnDuration) {
        try {
            FileWriter fw = new FileWriter("/sys/class/misc/heater/on_duration", false);
            BufferedWriter bw = new BufferedWriter(fw); //將BufferedWriter與FileWrite物件做連結
            bw.write(szOnDuration);
            bw.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /*
    * /sys/class/misc/heater/power_state
    * Read/Write -  1 means the heaters are turned on;
    *               0 means the heaters are turned off.
    * Write is only supported in manual control mode.
    * The heaters will be turned off and power_state will be set to 0
    * after 165 seconds when the heaters are turned on(power_state is set to 1).
    * */
    public boolean Write_PowerState(boolean bOnOff) {
        String szOff;
        if (bOnOff)
            szOff = "1";
        else
            szOff = "0";

        try {
            FileWriter fw = new FileWriter("/sys/class/misc/heater/power_state", false);
            BufferedWriter bw = new BufferedWriter(fw); //將BufferedWriter與FileWrite物件做連結
            bw.write(szOff);
            bw.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public String Read_PowerState() {
        StringBuilder builder = new StringBuilder();
        String line;

        try {
            BufferedReader reader = new BufferedReader(new FileReader("/sys/class/misc/heater/power_state"), 256);
            try {
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            return "Read_PowerState_Unavailable";
        }
        return builder.toString();
    }


}
