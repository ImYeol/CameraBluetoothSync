package thealphalabs.defaultcamera.ui;

import android.app.Application;
import android.content.Context;

import thealphalabs.defaultcamera.data.AppDataManager;
import thealphalabs.defaultcamera.data.DataManager;

/**
 * Created by yeol on 17. 5. 1.
 */

/**
 *  http://webnautes.tistory.com/822
 *  https://github.com/ragnraok/RxCamera
 *  https://github.com/janishar/ParaCamera
 *  https://github.com/josnidhin/Android-Camera-Example/blob/master/src/com/example/cam/Preview.java
 *  https://github.com/nickaknudson/android-nickaknudson/blob/master/src/com/nickaknudson/android/bluetooth/BluetoothConnection.java
 */
public class CameraApp extends Application {

    private DataManager dataManager;

    @Override
    public void onCreate() {
        super.onCreate();
        dataManager = AppDataManager.getInstance();
        dataManager.bindToBluetoothService(this);
    }

    public static CameraApp get(Context context){
        return (CameraApp)context.getApplicationContext();
    }

    public DataManager getDataManager(){
        return dataManager;
    }

}
