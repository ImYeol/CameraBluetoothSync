package thealphalabs.defaultcamera.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.ivbaranov.rxbluetooth.RxBluetooth;

import java.util.Set;

import rx.Subscription;
import thealphalabs.defaultcamera.data.bluetooth.BluetoothHelper;
import thealphalabs.defaultcamera.model.BluetoothPictureInfo;

/**
 * Created by yeol on 17. 5. 1.
 */

/**
 * https://github.com/IvBaranov/RxBluetooth/
 * https://github.com/eove/android-rxbluetooth-service
 * https://github.com/eove/RxBluetooth/blob/master/app/src/main/java/com/github/ivbaranov/rxbluetooth/example/BluetoothService.java
 */

public class BluetoothClientService extends Service implements BluetoothHelper{

    private static final String TAG = "BluetoothClientService";

    public static final String RECENT_DEVICE="bluetooth.recent.address";
    public static final String RECENT_DEVICE_NAME="bluetooth.recent.name";

    private RxBluetooth rxBluetooth;
    private Subscription connectSubscription;
    private ConnectionHelper connHelper;
    private IBinder mBinder = new btBinder();

    public class btBinder extends Binder {
        public BluetoothClientService getService(){
            return BluetoothClientService.this;
        }

        public boolean isConnected(){
            if(connHelper != null){
                return connHelper.isConnected();
            } else {
                return false;
            }
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "BluetoothService started!");
        rxBluetooth = new RxBluetooth(this);
        connHelper = BluetoothConnectionHelper.getInstance(this);
        if (!rxBluetooth.isBluetoothAvailable()) {
            // handle the lack of bluetooth support
            Log.d(TAG, "Bluetooth is not supported!");
        } else {
            // check if bluetooth is currently enabled and ready for use
            if (!rxBluetooth.isBluetoothEnabled()) {
                Log.d(TAG, "Bluetooth should be enabled first!");
            } else {

                connHelper.registerAclConnectedReceiver(this);

                if(connHelper.isConnected()){
                    Log.d(TAG,"connHelper.isConnected : "+connHelper.isConnected());

                } else {
                    Log.d(TAG,"connHelper.isConnected : "+connHelper.isConnected());
                    connectRecentDevice();
                }
                /*connectSubscription = rxBluetooth.observeAclEvent() //
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.computation())
                        .subscribe(new Action1<AclEvent>() {
                            @Override public void call(AclEvent aclEvent) {
                                switch (aclEvent.getAction()) {
                                    case BluetoothDevice.ACTION_ACL_CONNECTED:
                                        //...
                                        Log.d(TAG,"Connected Event : "+aclEvent.getBluetoothDevice().getName());
                                        if( !connHelper.isConnected()){
                                            Log.d(TAG,"try to connect to server");
                                            connHelper.connectToServer(aclEvent.getBluetoothDevice());
                                        } else {
                                            Log.d(TAG,"already connected");
                                        }
                                        //saveRecentDevice(aclEvent.getBluetoothDevice().getAddress());
                                        break;
                                    case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED:
                                        //...
                                        break;
                                    case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                                        //...
                                        Log.d(TAG,"disConnected Event : "+aclEvent.getBluetoothDevice().getName());
                                        if(connHelper.isConnected()){
                                            connHelper.clear();
                                        }
                                        //saveRecentDevice(aclEvent.getBluetoothDevice().getAddress());
                                        break;
                                }
                            }
                        });*/
            }
        }

    }

    @Override
    public boolean onUnbind(Intent intent) {
        connHelper.unRegisterAclConnectedReceiver(this);
        return super.onUnbind(intent);
    }

    @Override
    public void onTrimMemory(int level) {
        Log.d(TAG,"onTrimMemory");
        super.onTrimMemory(level);
    }


    public void connectRecentDevice(){
        //getRecentDevice2();
        String address = getRecentDevice2();
        BluetoothDevice btDevice = null;
        if(address == ""){
            Set<BluetoothDevice> pairedDevices= rxBluetooth.getBondedDevices();
            if(pairedDevices.size() > 0){
                for(BluetoothDevice device : pairedDevices){
                    btDevice = device;
                }
                Log.d(TAG, "Random connected device: "+ btDevice.getName());
            }
        }
        else {
            btDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
        }

        connHelper.connectToServer(btDevice);
    }

    public String getRecentDevice2(){
        Context otherAppContext = null;
        try{
            otherAppContext =
                    createPackageContext("thealphalabs.areumlauncher",0);
        }catch(PackageManager.NameNotFoundException e){
            // log
            e.printStackTrace();
        }
        // getting Shared preference from other application
        SharedPreferences pref
                = otherAppContext.getSharedPreferences("prefs", Context.MODE_WORLD_READABLE | Context.MODE_MULTI_PROCESS);

        Log.d(TAG,"SharedPref device name : " + pref.getString(RECENT_DEVICE_NAME,""));
        //Log.d(TAG,pref.getString(RECENT_DEVICE,""));
        return pref.getString(RECENT_DEVICE, "");
    }
    // save preference
    public void saveRecentDevice(BluetoothDevice device){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(RECENT_DEVICE, device.getAddress());
        editor.putString(RECENT_DEVICE_NAME, device.getName());
        editor.commit();
    }

    // remove preference
    public void removeRecentDevice(){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(RECENT_DEVICE);
        editor.commit();
    }

    // clear preference
    public void removeAllPreferences(){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "BluetoothService stopped!");
        rxBluetooth.cancelDiscovery();
        connHelper.clear();
        unsubscribe(connectSubscription);
    }

    private static void unsubscribe(Subscription subscription) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"onBind Call");
        return mBinder;
    }


    @Override
    public boolean sendImageData(BluetoothPictureInfo picture) {
        if(connHelper == null){
            Log.d(TAG,"connHelper is null");
            connHelper = BluetoothConnectionHelper.getInstance(this);
        }
        Log.d(TAG,"sendImageData, isConnected: " + connHelper.isConnected());
        if(!connHelper.isConnected()){
            return false;
        } else {
            Log.d(TAG,"BluetoothConnection is not null");
            connHelper.sendPictureToService(picture);

            return true;
        }
    }
}
