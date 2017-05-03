package thealphalabs.defaultcamera.service;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
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
                connectSubscription = rxBluetooth.observeAclEvent() //
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.computation())
                        .subscribe(new Action1<AclEvent>() {
                            @Override public void call(AclEvent aclEvent) {
                                switch (aclEvent.getAction()) {
                                    case BluetoothDevice.ACTION_ACL_CONNECTED:
                                        //...
                                        connHelper.connectToServer();

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
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "BluetoothService stopped!");
        rxBluetooth.cancelDiscovery();
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
        if(connHelper.isConnected()){
            return false;
        } else {
            BluetoothConnection connection = connHelper.getBluetoothConnection();
            if(connection != null){
                GsonBuilder builder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation();
                final Gson gson = builder.create();

                String json= gson.toJson(picture);
                connection.send(json);
            }
        }
    }
}
