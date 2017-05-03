package thealphalabs.defaultcamera.data;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import thealphalabs.defaultcamera.model.BluetoothPictureInfo;
import thealphalabs.defaultcamera.service.BluetoothClientService;

/**
 * Created by yeol on 17. 5. 2.
 */

public class AppDataManager implements DataManager {

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
    }

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
    }

    @Override
    public boolean sendImageData(BluetoothPictureInfo picture) {
        return mBluetoothService.sendImageData(picture);
    }

    @Override
    public boolean isServiceOn() {
        return isServiceOn;
    }
}
