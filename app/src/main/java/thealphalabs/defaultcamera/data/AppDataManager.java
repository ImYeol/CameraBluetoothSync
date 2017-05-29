package thealphalabs.defaultcamera.data;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import thealphalabs.defaultcamera.model.BluetoothPictureInfo;
import thealphalabs.defaultcamera.service.BluetoothClientService;

/**
 * Created by yeol on 17. 5. 2.
 */

public class AppDataManager implements DataManager {

    public static final String TAG= "AppDataManager";
    private static AppDataManager instance;
    private BluetoothClientService mBluetoothService;
    private boolean isServiceOn = false;
    private BluetoothClientService.btBinder binder;
    private ServiceConnection mConn ;

/*    private ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothClientService.btBinder binder = (BluetoothClientService.btBinder)service;
            mBluetoothService = binder.getService();
            isServiceOn = true;
            Log.d(TAG,"onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG,"onServiceDisconnected");
            isServiceOn = false;
            mBluetoothService = null;
        }
    };*/

    public static AppDataManager getInstance(){
        if(instance == null){
            instance = new AppDataManager();
        }
        return instance;
    }

    private AppDataManager(){

    }

    @Override
    public void bindToBluetoothService(Context context){
        if(!isServiceOn) {
            if(mConn == null){
                mConn = new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        BluetoothClientService.btBinder binder = (BluetoothClientService.btBinder)service;
                        mBluetoothService = binder.getService();
                        isServiceOn = true;
                        Log.d(TAG,"onServiceConnected");
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        Log.d(TAG,"onServiceDisconnected");
                        isServiceOn = false;
                        mBluetoothService = null;
                    }
                };
            }
            Log.d(TAG, "bindToBluetoothService conn: "+mConn);
            Intent intent = new Intent(context, BluetoothClientService.class);
            context.bindService(intent, mConn, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void unBindBluetoothService(Context context) {
        if(isServiceOn) {
            //Intent intent = new Intent(context, BluetoothClientService.class);
            context.unbindService(mConn);
            //context.stopService(intent);
            mConn = null;
            isServiceOn = false;
            Log.d(TAG, "unBindToBluetoothService");
        }
    }

    @Override
    public boolean sendImageData(BluetoothPictureInfo picture) {
        if(mBluetoothService == null) {
            Log.d(TAG, "mBluetoothService is null");
            return false;
        }
        return mBluetoothService.sendImageData(picture);
    }

    @Override
    public boolean isServiceOn() {
        return isServiceOn;
    }

    @Override
    public boolean isConnected() {
        if(mBluetoothService == null) {
            Log.d(TAG, "isConnected - binder is null");
            return false;
        }
        else
            return mBluetoothService.isConnected();
    }

}
