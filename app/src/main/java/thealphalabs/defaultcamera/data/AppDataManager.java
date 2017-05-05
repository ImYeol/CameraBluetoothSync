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

    private ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothClientService.btBinder binder = (BluetoothClientService.btBinder)service;
            mBluetoothService = binder.getService();
            isServiceOn = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceOn = false;
        }
    };

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
        Intent intent = new Intent(context, BluetoothClientService.class);
        context.bindService(intent,mConn,Context.BIND_AUTO_CREATE);
        Log.d(TAG,"bindToBluetoothService");
    }

    @Override
    public void unBindBluetoothService(Context context) {
        Intent intent = new Intent(context, BluetoothClientService.class);
        context.unbindService(mConn);
        context.stopService(intent);
    }

    @Override
    public boolean sendImageData(BluetoothPictureInfo picture) {
        if(mBluetoothService == null)
            Log.d(TAG,"mBluetoothService is null");
        return mBluetoothService.sendImageData(picture);
    }

    @Override
    public boolean isServiceOn() {
        return isServiceOn;
    }
}
