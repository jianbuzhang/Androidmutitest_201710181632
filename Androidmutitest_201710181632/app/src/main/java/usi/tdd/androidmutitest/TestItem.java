package usi.tdd.androidmutitest;

import android.content.Intent;

/**
 * Created by usi on 2017/9/21.
 */

public class TestItem {
    public static final String START_TYPE_ACTIVITY="ACTIVITY";
    public static final String START_TYPE_SERVICE="SERVICE";
    public static final String START_TYPE_BROADCAST="BROADCAST";

    String mName;   // test item name
    String mStartType; // Activity , Service ,broadcast
    String mPackage;
    String mClassName;
    String mAction;	 //for broadcast
    String mCheck;
    String mNeedCheck;
    String mPassRate;
    Intent mIntent;
    String mStartCommand;
    int TestCount=0;
    int PassCount=0;
    int FailCount=0;

    public TestItem(String name,String startType){
        mName=name;
        mStartType=startType;
        mIntent =new Intent();
    }
    public TestItem(){
        mIntent =new Intent();
    }
    public void addPassCount(){
        PassCount++;
        TestCount++;
    }
    public void addFailCount(){
        FailCount++;
        TestCount++;
    }

}
