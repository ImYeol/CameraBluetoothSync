package thealphalabs.defaultcamera.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.ivbaranov.rxbluetooth.BluetoothConnection;
import com.github.ivbaranov.rxbluetooth.RxBluetooth;
import com.github.ivbaranov.rxbluetooth.events.AclEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Set;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
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

    private RxBluetooth rxBluetooth;
    private Subscription connectSubscription;
    private ConnectionHelper connHelper;
    private IBinder mBinder = new btBinder();

    public class btBinder extends Binder {
        public BluetoothClientService getService(){
            return BluetoothClientService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "BluetoothService started!");
        rxBluetooth = new RxBluetooth(this);
        connHelper = BluetoothConnectionHelper.getInstance();

        if (!rxBluetooth.isBluetoothAvailable()) {
            // handle the lack of bluetooth support
            Log.d(TAG, "Bluetooth is not supported!");
        } else {
            // check if bluetooth is currently enabled and ready for use
            if (!rxBluetooth.isBluetoothEnabled()) {
                Log.d(TAG, "Bluetooth should be enabled first!");
            } else {
                Set<BluetoothDevice> pairedDevices= rxBluetooth.getBondedDevices();
                BluetoothDevice btDevice= null;
                if(pairedDevices.size() > 0){
                    for(BluetoothDevice device : pairedDevices){
                        btDevice = device;
                    }
                    Log.d(TAG, "connected device: "+ btDevice.getName());
                }
                connHelper.connectToServer(btDevice);
                connectSubscription = rxBluetooth.observeAclEvent() //
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.computation())
                        .subscribe(new Action1<AclEvent>() {
                            @Override public void call(AclEvent aclEvent) {
                                switch (aclEvent.getAction()) {
                                    case BluetoothDevice.ACTION_ACL_CONNECTED:
                                        //...
                                        /*Set<BluetoothDevice> pairedDevices= rxBluetooth.getBondedDevices();
                                        BluetoothDevice btDevice= null;
                                        if(pairedDevices.size() > 0){
                                            for(BluetoothDevice device : pairedDevices){
                                                btDevice = device;
                                            }
                                            Log.d(TAG, "connected device: "+ btDevice.getName());
                                        }
                                        connHelper.connectToServer(btDevice);*/

                                        break;
                                    case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED:
                                        //...
                                        break;
                                    case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                                        //...
                                        connHelper.clear();
                                        break;
                                }
                            }
                        });
            }
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand !!!!!!!!!!!!!!!!11");
        /*if(rxBluetooth == null){
            rxBluetooth = new RxBluetooth(this);
            connHelper = BluetoothConnectionHelper.getInstance();

            if (!rxBluetooth.isBluetoothAvailable()) {
                // handle the lack of bluetooth support
                Log.d(TAG, "Bluetooth is not supported!");
            } else {
                // check if bluetooth is currently enabled and ready for use
                if (!rxBluetooth.isBluetoothEnabled()) {
                    Log.d(TAG, "Bluetooth should be enabled first!");
                } else {
                    Set<BluetoothDevice> pairedDevices= rxBluetooth.getBondedDevices();
                    BluetoothDevice btDevice= null;
                    if(pairedDevices.size() > 0){
                        for(BluetoothDevice device : pairedDevices){
                            btDevice = device;
                        }
                        Log.d(TAG, "connected device: "+ btDevice.getName());
                    }
                    connHelper.connectToServer(btDevice);
                    connectSubscription = rxBluetooth.observeAclEvent() //
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.computation())
                            .subscribe(new Action1<AclEvent>() {
                                @Override public void call(AclEvent aclEvent) {
                                    switch (aclEvent.getAction()) {
                                        case BluetoothDevice.ACTION_ACL_CONNECTED:
                                            //...
                                            Set<BluetoothDevice> pairedDevices= rxBluetooth.getBondedDevices();
                                            BluetoothDevice btDevice= null;
                                            if(pairedDevices.size() > 0){
                                                for(BluetoothDevice device : pairedDevices){
                                                    btDevice = device;
                                                }
                                                Log.d(TAG, "connected device: "+ btDevice.getName());
                                            }
                                            connHelper.connectToServer(btDevice);

                                            break;
                                        case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED:
                                            //...
                                            break;
                                        case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                                            //...
                                            connHelper.clear();
                                            break;
                                    }
                                }
                            });
                }
            }
        }*/
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
        return mBinder;
    }


    @Override
    public boolean sendImageData(BluetoothPictureInfo picture) {
        if(connHelper == null){
            Log.d(TAG,"connHelper is null");
            connHelper = BluetoothConnectionHelper.getInstance();
        }
        Log.d(TAG,"sendImageData");
        if(!connHelper.isConnected()){
            return false;
        } else {
            Log.d(TAG,"BluetoothConnection is not null");
            BluetoothConnection connection = connHelper.getBluetoothConnection();
            connHelper.sendPictureToService(picture);

            return true;
        }
    }

    private void conectServer(){
        Set<BluetoothDevice> pairedDevices= BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        BluetoothDevice btDevice= null;
        if(pairedDevices.size() > 0){
            for(BluetoothDevice device : pairedDevices){
                btDevice = device;
                Log.d(TAG, "bonded device: "+ btDevice.getName());
            }
            Log.d(TAG, "connected device: "+ btDevice.getName());
        }
        connHelper.connectToServer(btDevice);
    }
}
