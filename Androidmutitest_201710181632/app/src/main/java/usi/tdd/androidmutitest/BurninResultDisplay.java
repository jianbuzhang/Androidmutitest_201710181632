package usi.tdd.androidmutitest;

import android.content.ComponentName;
import android.os.Bundle;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by usi on 2017/9/21.
 */

public class BurninResultDisplay extends TActivity {
    private List<TestItem> mItemList;
    private final String BURN_IN_RESULT="/storage/emulated/0/burninResult.log";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // TODO Auto-generated method stub
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        mItemList = getBurninItems();
        String path = this.getFilesDir().getAbsolutePath();
        //Log.d("BI", path);
        File file = new File(path+"/"+BurnInTest.BURNIN_STORAGE);
        //Log.d("BI", BurnInTest.BURNIN_STORAGE);
        //File file = new File("/storage/emulated/0/burnin.log");

        FileInputStream rstream;
        Properties rprt = new Properties();
        try {
            rstream = new FileInputStream(file);
            rprt.load(rstream);
            FileWriter fw = new FileWriter(BURN_IN_RESULT, false);
            BufferedWriter bw = new BufferedWriter(fw); //將BufferedWeiter與FileWrite物件做連結
            bw.write("Item,TestCount,PassCount,FailCount,PassRate,NeedRate,PassFail\r\n");
            boolean testResult = false;
            for(int nIdx = 0; nIdx < mItemList.size(); nIdx++)
            {
                int TestCount = Integer.parseInt(rprt.getProperty(mItemList.get(nIdx).mName+"_TestCount"));
                int PassCount = Integer.parseInt(rprt.getProperty(mItemList.get(nIdx).mName+"_PassCount"));
                int FailCount = Integer.parseInt(rprt.getProperty(mItemList.get(nIdx).mName+"_FailCount"));
                int PassRate = Integer.parseInt(mItemList.get(nIdx).mPassRate);
                String Checked = mItemList.get(nIdx).mCheck;
                float CountRate = ((float)PassCount/TestCount) * 100;
                this.appendLog(mItemList.get(nIdx).mName + ":");
                this.appendLog("---Test Count:" +Integer.toString(TestCount));
                this.appendLog("---Passed :"+Integer.toString(PassCount));
                this.appendLog("---Failed :"+Integer.toString(FailCount));
                this.appendLog("---Pass Rate :"+Double.toString(CountRate));
                bw.write(mItemList.get(nIdx).mName+",");
                bw.write(Integer.toString(TestCount)+",");
                bw.write(Integer.toString(PassCount)+",");
                bw.write(Integer.toString(FailCount)+",");
                bw.write(Double.toString(CountRate)+",");
                bw.write(PassRate+",");
                Log.d("MFG_TEST", "---Pass Rate :"+Double.toString(CountRate) );
                if (CountRate >= PassRate)
                {
                    bw.write("VARSTRING TestResult=Pass\r\n");
                    this.appendLog("---Result : PASS");
                    testResult = true;
                }
                else
                {
                    bw.write("VARSTRING TestResult=Fail\r\n");
                    this.appendLog("---Result : FAIL");
                    testResult=false;
                }
                this.appendLog("===================================");
            }

            this.mItemName="BURNIN";
            if(testResult)
            {
                bw.write("Pass\r\n");
                bw.close();
                Log.d("MFG_TEST","SUCCESSFUL TEST" );
                this.setCaption("Burn-in Result:PASS");
                this.setItemPass();

            }
            else
            {
                bw.write("Fail\r\n");
                bw.close();
                Log.d("MFG_TEST", "UUT-FAIL");
                this.setCaption("Burn-in Result:FAIL");
                this.setItemFail();
            }
        } catch (IOException e1) {
            // TODO �۰ʲ��ͪ� catch �϶�
            e1.printStackTrace();
        }
		/*
		try {
			rstream = new FileInputStream(file);
			rprt.load(rstream);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean testResult=true;
		for(int i=0;i<mItemList.size();i++){
			Log.d("MFG_TEST", mItemList.get(i).mName + "_TestCount" );

			int TestCount= Integer.parseInt(rprt.getProperty(mItemList.get(i).mName+"_TestCount"));
			int PassCount= Integer.parseInt(rprt.getProperty(mItemList.get(i).mName+"_PassCount"));
			int FailCount= Integer.parseInt(rprt.getProperty(mItemList.get(i).mName+"_FailCount"));
			int PassRate=Integer.parseInt(mItemList.get(i).mPassRate);
			String Checked=mItemList.get(i).mCheck;
			float CountRate = ((float)PassCount/TestCount) * 100;
			this.appendLog(mItemList.get(i).mName+":");
			this.appendLog("---Test Count:" +Integer.toString(TestCount));
			this.appendLog("---Passed :"+Integer.toString(PassCount));
			this.appendLog("---Failed :"+Integer.toString(FailCount));
			this.appendLog("---Pass Rate :"+Double.toString(CountRate));
			if (CountRate >= PassRate){
				this.appendLog("---Result : PASS");
			}else{
				this.appendLog("---Result : FAIL");
				testResult=false;
			}

			this.appendLog("===================================");
		}
		this.mItemName="BURNIN";
		if(testResult){
			this.setCaption("Burn-in Result:PASS");
			this.setItemPass();

		}else{
			this.setCaption("Burn-in Result:FAIL");
			this.setItemFail();
		}*/
    }

    public List<TestItem> getBurninItems(){
        List<TestItem> items=new ArrayList<TestItem>();;
        try {
            InputStream inStream = new FileInputStream(BurnInTest.mBurninConfigFile);  //Ū���ɮ�
            DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
            DocumentBuilder builder=factory.newDocumentBuilder();
            Document document=builder.parse(inStream);  //�H��榡�s��O���餤�M����ӰO����
            Element root=document.getDocumentElement();


            NodeList nodes=root.getElementsByTagName("test-item");

            if(nodes.getLength()<1){
                USILog.append("MFGUtil","No test item found!!" );
            }else{
                int count=nodes.getLength();
                for(int i=0;i<count;i++){
                    TestItem ti=new TestItem();
                    ti.mName=((Element)nodes.item(i)).getAttribute("name");
                    ti.mStartType=((Element)nodes.item(i)).getAttribute("start-type");
                    ti.mCheck = ((Element)nodes.item(i)).getAttribute("check");
                    ti.mPassRate = ((Element)nodes.item(i)).getAttribute("pass-rate");
                    if(ti.mStartType.equals(TestItem.START_TYPE_ACTIVITY)){
                        ti.mPackage=((Element)nodes.item(i)).getAttribute("package");
                        ti.mClassName=((Element)nodes.item(i)).getAttribute("classname");
                        //ti.mIntent.setClassName(ti.mPackage,ti.mPackage+ti.mClassName);
                        ti.mIntent.setComponent(new ComponentName(ti.mPackage,ti.mPackage+ti.mClassName));
                        ti.mAction=((Element)nodes.item(i)).getAttribute("action").trim();
                        ti.mIntent.setAction(ti.mAction);
                    }

                    NodeList nodesExtra=((Element)nodes.item(i)).getElementsByTagName("extra");
                    int exCount=nodesExtra.getLength();
                    for(int j=0;j<exCount;j++){
                        String key=((Element)nodesExtra.item(j)).getAttribute("key");
                        String value=((Element)nodesExtra.item(j)).getAttribute("value");
                        ti.mIntent.putExtra(key, value);
                        USILog.append(this,key+"="+value);
                    }
                    items.add(ti);
                }

            }

        } catch (Exception er) {
            USILog.append("Burn-in",er.getMessage() );
        }

        return items;
    }

}
