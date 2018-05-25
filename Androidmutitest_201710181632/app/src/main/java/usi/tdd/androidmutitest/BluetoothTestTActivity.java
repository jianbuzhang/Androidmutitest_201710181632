package usi.tdd.androidmutitest;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.IBluetooth;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import net.vidageek.mirror.dsl.Mirror;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@SuppressLint("NewApi")
public class BluetoothTestTActivity extends TActivity {
	private BluetoothAdapter mBT = BluetoothAdapter.getDefaultAdapter();
	private List<BluetoothDevice> mBTDevice;
	private List<String> mBTDeviceRSSI;
	
	 // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_CONNECTION_LOST = 6;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    
    private String mConnectedDeviceName = null;
    private BluetoothChatService mChatService = null;
    private StringBuffer mOutStringBuffer;
    
	private Thread mTriansmitThread;
    String CliMAC;
	private boolean bRet = false, bResult = false;

	public static final String SECURE_SETTINGS_BLUETOOTH_ADDRESS = "bluetooth_address";
	public String mBTaddress = null;

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Intent intent = this.getIntent();
		String action = intent.getAction();
		this.appendLog( "ACTION: "+ action);
		if( !mBT.isEnabled()){
			mBT.enable();
		}
		long startTime=System.currentTimeMillis();
		while( mBT.getState() != BluetoothAdapter.STATE_ON){
			((TActivity)mCtx).appendLog( "Enabling Bluetooth");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(!IsActive)
				return;
			if((System.currentTimeMillis()-startTime)>80000){
				BluetoothTestTActivity.this.appendLog("BT Enableing time out!!");
				BluetoothTestTActivity.this.setItemFail();
				return;
			}
		}
		((TActivity)mCtx).appendLog("Bluetooth ready");
		if( mBT.getAddress()!=null) {
			mBTaddress = mBT.getAddress();
			if( "02:00:00:00:00:00".equalsIgnoreCase(mBTaddress)) {
				try {
					ContentResolver mContentResolver = mCtx.getContentResolver();
					mBTaddress = Settings.Secure.getString( mContentResolver, SECURE_SETTINGS_BLUETOOTH_ADDRESS);
					mBTaddress = mBTaddress.toUpperCase();
				} catch (Exception e) {
					mBTaddress =  getBtAddressViaReflection().toUpperCase();
				}
				USILog.append( this, "Reflection VARSTRING BTMAC= \'"+getBtAddressViaReflection().toUpperCase()+"\'\n");
			} else {
				mBTaddress = mBT.getAddress().toUpperCase();
			}
			USILog.append( this, "VARSTRING BTMACCON= \'"+mBTaddress+"\'\n");
		}
		((TActivity)mCtx).appendLog("BT_MAC:"+mBTaddress);

		// 取得目前已經配對過的裝置
		Set<BluetoothDevice > setPairedDevices = mBT.getBondedDevices ();
		// 如果已經有配對過的裝置
///*
// 		ListView Olalist = (ListView) this.findViewById(R.id.listview1);
// 		String[] item = new String[] {/*"Ola的家", "魔獸世界","星海爭霸2","凱蘭迪亞傳
//		奇","Ola Query簡介","蟲族秒滅心法","Ola MapGuide教學","Ola jQuery教學","Ola
//		Android教學"*/};
//		ArrayAdapter mArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_
//		expandable_list_item_1,item);
//		Olalist.setAdapter(mArrayAdapter);
//*/
		if (setPairedDevices .size() > 0) {
			// 把裝置名稱以及MAC Address印出來
//			for (BluetoothDevice device : setPairedDevices ) {
				//mArrayAdapter .add(device .getName() + "\n" + device .getAddress ());
//			}
		}

		if(	action.equals("BT_PWR_OFF")) {
			this.setCaption( "Bluetooth Power OFF Test");
			mBT.disable();
			mFinishOnSetTestResult = true;
			((TActivity)mCtx).setItemPass();
			//SystemClock.sleep(1500);
			//((TActivity)mCtx).finish();
		}
		if(	action.equals("BT_PWR_ON")) {
			this.setCaption( "Bluetooth Power ON Test");
			int nIdx;
			for( nIdx = 0; nIdx < 5; nIdx++) {
				mBT.enable();
				SystemClock.sleep(2000);
				bRet = mBT.enable();
				if( bRet )
					break;
			}
			mFinishOnSetTestResult = true;
			((TActivity)mCtx).setItemPass();
			//SystemClock.sleep(1500);
			//((TActivity)mCtx).finish();
		}
		if(action.equals("BT_QUERY")){
			this.setCaption("Bluetooth Device Query Test");
			new Thread(new StartDiscovery()).start();			
		}
		if(action.equals("BT_CONNECT")){
			this.setCaption("Bluetooth Transmit Test");
			Bundle b = intent.getExtras();
			CliMAC = b.getString("CliMAC");
			if( CliMAC == null ){
				this.appendLog("No CliMAC Parameter");
				((TActivity)mCtx).setItemFail();
				return;
			}
			mTriansmitThread=new Thread(new StartConnect());
			mTriansmitThread.start();
		}
		if(action.equals("BT_GET_MAC")){	
			this.setCaption("Bluetooth Get MAC Test");
			((TActivity)mCtx).setItemPass();
		}
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
		registerReceiver(mPairingRequestReceiver, filter);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		IBluetooth ib =getIBluetooth();
		try {
			if(null != CliMAC ) {
				ib.removeBond(CliMAC);
			} //else {
				//ib.removeBond("00:1A:7D:DA:71:15");
			//}
			mBT.disable();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(mTriansmitThread!=null){
			IsActive=false;
			mTriansmitThread.interrupt();
			mTriansmitThread = null;
		}
		unregisterReceiver(mPairingRequestReceiver);
		super.onDestroy();
	}
	
	//private static IBluetoothA2dp getIBluetoothA2dp() {
	private IBluetooth getIBluetooth() {
		//IBluetoothA2dp ibta = null;
		IBluetooth ibt = null;
		try {
		    //final Class serviceManager = Class.forName("android.os.ServiceManager");
			Class c2 = Class.forName("android.os.ServiceManager");
			//final Method getService = serviceManager.getDeclaredMethod("getService", String.class);
		    Method m2 = c2.getDeclaredMethod("getService",String.class);
		    //final IBinder iBinder = (IBinder) getService.invoke(null, "bluetooth_a2dp");
    		IBinder b = (IBinder) m2.invoke(null, "bluetooth");
    		USILog.append(BluetoothTestTActivity.this.getClass().getSimpleName(), "Test2: " + b.getInterfaceDescriptor());
    		//final Class iBluetoothA2dp = Class.forName("android.bluetooth.IBluetoothA2dp");
		    Class c3 = Class.forName("android.bluetooth.IBluetooth");
		    //final Class[] declaredClasses = iBluetoothA2dp.getDeclaredClasses();
		    Class[] s2 = c3.getDeclaredClasses();
		    //final Class c = declaredClasses[0];
		    Class c = s2[0];
		    //final Method asInterface = c.getDeclaredMethod("asInterface",IBinder.class);
		    Method m = c.getDeclaredMethod("asInterface",IBinder.class);
		    //asInterface.setAccessible(true);
		    m.setAccessible(true);
		    //ibta = (IBluetoothA2dp) asInterface.invoke(null, iBinder);
		    ibt = (IBluetooth) m.invoke(null, b);
		} catch (Exception e) {
		    Log.e("flowlab", "Erroraco!!! " + e.getMessage());
		    USILog.append(BluetoothTestTActivity.this.getClass().getSimpleName(), "Erroraco!!! " + e.getMessage());
		}
		return ibt;
	}
	
/*
//	Finally got this working on 4.2. See the details here: http://code.google.com/p/a2dp-connect2/
//	It is quite different from 4.1 and before. First call connect to the interface like this:
//	public static void getIBluetoothA2dp(Context context) {
//		Intent i = new Intent(IBluetoothA2dp.class.getName());
//		if (context.bindService(i, mConnection, Context.BIND_AUTO_CREATE)) {
//		} else {
//			// Log.e(TAG, "Could not bind to Bluetooth A2DP Service");
//		}
//	}
//
//	When the interface is returned it will call back to this:
//
//	public static ServiceConnection mConnection = new ServiceConnection() {
//		@Override
//		public void onServiceConnected(ComponentName name, IBinder service) {
//			ibta2 = IBluetoothA2dp.Stub.asInterface(service);
//		}
//		@Override
//		public void onServiceDisconnected(ComponentName name) {
//			// TODO Auto-generated method stub
//		}
//	};
//
*/

/*
//	private void testBluetoothA2dp(BluetoothDevice device) {
//		// TODO Auto-generated method stub
//		// TODO Auto-generated method stub
//		IBluetoothA2dp ibta = getIBluetoothA2dp();
//		try {
//			Log.d("Felix", "Here: " + ibta.getSinkPriority(device));
//			ibta.connectSink(device);
//		} catch (RemoteException e) {
//			// * TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	interface IBluetoothA2dp {
//		boolean connectSink(in BluetoothDevice device); // Pre API 11 only
//		boolean disconnectSink(in BluetoothDevice device); // Pre API 11 only
//		boolean connect(in BluetoothDevice device); // API 11 and up only
//		boolean disconnect(in BluetoothDevice device); // API 11 and up only
//		boolean suspendSink(in BluetoothDevice device); // all
//		boolean resumeSink(in BluetoothDevice device); // all
//		BluetoothDevice[] getConnectedSinks(); // change to Set<> once AIDL	supports, pre API 11 only
//		BluetoothDevice[] getNonDisconnectedSinks(); // change to Set<> once AIDL supports,
//		int getSinkState(in BluetoothDevice device);
//		boolean setSinkPriority(in BluetoothDevice device, int priority); // Pre API 11 only
//		boolean setPriority(in BluetoothDevice device, int priority); // API 11 and up only
//		int getPriority(in BluetoothDevice device); // API 11 and up only
//		int getSinkPriority(in BluetoothDevice device); // Pre API 11 only
//		boolean isA2dpPlaying(in BluetoothDevice device); // API 11 and up only
//	}
//	
//	private IBluetoothA2dp getIBluetoothA2dp() {
//		IBluetoothA2dp ibta = null;
//		try {
//			Class c2 = Class.forName("android.os.ServiceManager");
//			Method m2 = c2.getDeclaredMethod("getService", String.class);
//			IBinder b = (IBinder) m2.invoke(null, "bluetooth_a2dp");
//			Log.d("Felix", "Test2: " + b.getInterfaceDescriptor());
//			Class c3 = Class.forName("android.bluetooth.IBluetoothA2dp");
//			Class[] s2 = c3.getDeclaredClasses();
//			Class c = s2[0];
//			// printMethods(c);
//			Method m = c.getDeclaredMethod("asInterface", IBinder.class);
//			m.setAccessible(true);
//			ibta = (IBluetoothA2dp) m.invoke(null, b);
//		} catch (Exception e) {
//			Log.e("flowlab", "Erroraco!!! " + e.getMessage());
//		}
//		return ibta;
//	}	
*/
	boolean IsActive=true;
	public class StartConnect implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			if( mBT.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
				Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 10);
				startActivity(discoverableIntent);
			}//ANR debug
		
//			IBluetooth mBtService = getIBluetooth();
//			try {
//				mBtService.setDiscoverableTimeout(1000);
//			} catch (RemoteException e) {
//				// TODO Auto-generated catch block
//				((TActivity)mCtx).appendLog(e.toString());
//				e.printStackTrace();
//			}
		    
			mChatService = new BluetoothChatService(((TActivity)mCtx), mHandler);

	     // Initialize the buffer for outgoing messages
			mOutStringBuffer = new StringBuffer("");
			mChatService.start();
			
//			BluetoothSocket socket=null;
//			mBT.cancelDiscovery();
//		//	BluetoothDevice dev1=mBT.getRemoteDevice("88:9F:FA:EC:2F:93");	
//			BluetoothDevice dev1=mBT.getRemoteDevice("00:13:EF:F0:DB:EB");
//			try {
//		
//		 // 	socket =dev1.createRfcommSocketToServiceRecord(UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66"));
//				socket =dev1.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
//			//	socket =dev1.createRfcommSocketToServiceRecord(UUID.fromString("E4F000FC-572B-42b5-BCA2-0F71694DF71B"));
//				socket.connect();
//				OutputStream tmpOut = socket.getOutputStream();
//				InputStream tmpIn=socket.getInputStream();
//			
//				String aa="1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd";
//			//	(1048576/8)/1000
//				for(int i=0;i<10;i++){2
//					//((TActivity)mCtx).appendLog("Send Text:"+aa);
//					tmpOut.write(aa.getBytes());
//					tmpOut.flush();				   
//					byte[] buffer = new byte[2048];
//					 
//					int count=tmpIn.read(buffer);	
//					String recstr=new String(buffer).substring(0,count);
//					if(recstr.equals(aa))
//						((TActivity)mCtx).appendLog("Receive Text:"+Integer.toString(i));
//					else
//						((TActivity)mCtx).appendLog("Receive Text:"+recstr);
//				}
//				
//				socket.close();
//				((TActivity)mCtx).setItemPass();
//				
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				((TActivity)mCtx).appendLog(e.toString());
//				e.printStackTrace();
//			}
		}		
	}
	
	private void transmitTest() {		
		
		try {
			OutputStream tmpOut = mChatService.ssocket.getOutputStream();
			InputStream tmpIn = mChatService.ssocket.getInputStream();
			String testStr= "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd12345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcd123456789";
		
			//(1048576/8)/1000
			((TActivity)mCtx).appendLog("Start transmit test");
			//for(int i=0;i<1500;i++){				
			for(int i=0;i<500;i++){	
				//((TActivity)mCtx).appendLog("Send Text:"+aa);
				tmpOut.write(testStr.getBytes());
				tmpOut.flush();						
				byte[] buffer = new byte[2048];					
				int count=tmpIn.read(buffer);					
				String recstr=new String(buffer).substring(0,count);
				if(recstr.equals(testStr)){
					if( (i % 100)==0)
					 ((TActivity)mCtx).appendLog("Receive Count:"+Integer.toString(i));
				}else{
					((TActivity)mCtx).appendLog("Receive Text:"+recstr);
					tmpOut.close();
					tmpIn.close();
					mChatService.stop();
					((TActivity)mCtx).setItemFail();
					return;
					
				}
			}
			tmpOut.close();
			tmpIn.close();
			mChatService.stop();
			((TActivity)mCtx).setItemPass();
			SystemClock.sleep(1000);//ANR debug
		} catch (IOException e) {
			// TODO Auto-generated catch block
			((TActivity)mCtx).appendLog(e.toString());
			mChatService.stop();
			((TActivity)mCtx).setItemFail();
			e.printStackTrace();
		}
	}
	
	 private void sendMessage(String message) {
	        // Check that we're actually connected before trying anything
	        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {	            
	            ((TActivity)mCtx).appendLog("not connected!!");
	            return;
	        }

	        // Check that there's actually something to send
	        if (message.length() > 0) {
	            // Get the message bytes and tell the BluetoothChatService to write
	            byte[] send = message.getBytes();
	            mChatService.write(send);

	            // Reset out string buffer to zero and clear the edit text field
	            mOutStringBuffer.setLength(0);
	        }
	    }
	
	// The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                
                switch (msg.arg1) {
                case BluetoothChatService.STATE_CONNECTED:                   
                    ((TActivity)mCtx).appendLog("connecte from "+ mConnectedDeviceName
                    		+ "/tremote RSSI "+ getIntent().getExtras().getShort(BluetoothDevice.EXTRA_RSSI));
                    ((BluetoothTestTActivity)mCtx).transmitTest();                   
                    break;
                case BluetoothChatService.STATE_CONNECTING:
                	((TActivity)mCtx).appendLog("connecting...."
                			+ "/tremote RSSI "+ getIntent().getExtras().getShort(BluetoothDevice.EXTRA_RSSI));                    
                    break;
                case BluetoothChatService.STATE_LISTEN:
                	((TActivity)mCtx).appendLog("Server listening...."
                			+ "/tremote RSSI "+ getIntent().getExtras().getShort(BluetoothDevice.EXTRA_RSSI));
                	break;
                case BluetoothChatService.STATE_NONE:  	     
                	
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                //mConversationArrayAdapter.add("Me:  " + writeMessage);
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);      
                ((TActivity)mCtx).appendLog(readMessage);
                //if(!IsClient)
               // ((BluetoothTestTActivity)mCtx).sendMessage(readMessage);
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_CONNECTION_LOST:
            	((TActivity)mCtx).appendLog("Connection lost!!");
            	mChatService.stop();
            	((TActivity)mCtx).setItemFail();
                break;    
                
            }
        }
    };
	
	public class StartDiscovery implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			mBTDevice=new ArrayList<BluetoothDevice>();
			mBTDeviceRSSI=new ArrayList<String>();
			IntentFilter foundFilter=new IntentFilter(BluetoothDevice.ACTION_FOUND);
			foundFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
			registerReceiver(onBTDeviceFound,foundFilter);
			IntentFilter finishedFilter=new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
			registerReceiver(onBTDiscoveryFinished,finishedFilter);
			mBT.startDiscovery();
		}
	}
	
	private BroadcastReceiver onBTDeviceFound =new BroadcastReceiver(){
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			// 當收尋到裝置時
			//if (BluetoothDevice .ACTION_FOUND.equals (intent .getAction())) {
			// 取得藍芽裝置這個物件
			BluetoothDevice dev=arg1.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			mBTDevice.add(dev);
			String SRSSI = Integer.toString(arg1.getExtras().getShort( BluetoothDevice.EXTRA_RSSI));
			mBTDeviceRSSI.add(SRSSI);
			if(mBTDevice.size() >= 3)
			{
				unregisterReceiver(onBTDiscoveryFinished);
				unregisterReceiver(this);	
				((TActivity)mCtx).appendLog("BT device Discovery Finished!!"+ "\nBluetooth device Query PASS");
					for(int i=0;i<(mBTDevice.size());i++){
						((TActivity)mCtx).appendLog("Name: " + mBTDevice.get(i).getName()+ "\tAddress: " + mBTDevice.get(i).getAddress()
								+ "\tRSSI: " + mBTDeviceRSSI.get(i) + " db\tdistance: " + CalcDistByRSSI(mBTDeviceRSSI.get(i)));
					}				
					
					((TActivity)mCtx).setItemPass();
					mBT.disable();													
			}
		}		
	};
	
	private BroadcastReceiver onBTDiscoveryFinished =new BroadcastReceiver(){
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			unregisterReceiver(onBTDeviceFound);
			unregisterReceiver(this);	
			((TActivity)mCtx).appendLog("BT Discovery Finished!!");
			if(mBTDevice.size()>0){
				((TActivity)mCtx).appendLog("Bluetooth Query PASS");
				for(int i=1;i<mBTDevice.size();i++){
					((TActivity)mCtx).appendLog("Name: " + mBTDevice.get(i).getName()+ "\tAddress: " + mBTDevice.get(i).getAddress()
							+ "\tRSSI: " + mBTDeviceRSSI.get(i) + " db\tdistance: " + CalcDistByRSSI(mBTDeviceRSSI.get(i)));
				}				
				
				((TActivity)mCtx).setItemPass();
				mBT.disable();
			}else{				
				mBT.disable();
				((TActivity)mCtx).appendLog("Bluetooth Query FAIL");
				((TActivity)mCtx).setItemFail();
			}					
		}		
	};
	
	private double CalcDistByRSSI( String rssi) {
		int iRssi = Math.abs(Integer.valueOf(rssi));
		double power = (iRssi -59)/(10*2.0);
		return Math.pow(10, power);
		
//		d = 10^((abs(RSSI) - A) / (10 * n))
//				其中：
//				    d - 计算所得距离
//				    RSSI - 接收信号强度（负值）
//				    A - 发射端和接收端相隔1米时的信号强度
//				    n - 环境衰减因子
	}

	private static String getBtAddressViaReflection() {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		Object bluetoothManagerService = new Mirror().on(bluetoothAdapter).get().field("mService");
		if( bluetoothManagerService == null) {
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

	private String TAG="BTTA";
	private final BroadcastReceiver mPairingRequestReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
				try {
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					int pin=intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY", 0000);
					//the pin in case you need to accept for an specific pin
					Log.d(TAG, "Start Auto Pairing. PIN = " + intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY",0000));
					byte[] pinBytes;
					pinBytes = (""+pin).getBytes("UTF-8");
					device.setPin(pinBytes);
					//setPairing confirmation if neeeded
					device.setPairingConfirmation(true);

					try {
						Log.d(TAG, "Start Pairing... with: " + device.getName());
						device.createBond();
						Log.d(TAG, "Pairing finished.");
					} catch (Exception e) {
						Log.e(TAG, e.getMessage());
					}
				} catch (Exception e) {
					Log.e(TAG, "Error occurs when trying to auto pair");
					e.printStackTrace();
				}
			}
		}
	};
}
