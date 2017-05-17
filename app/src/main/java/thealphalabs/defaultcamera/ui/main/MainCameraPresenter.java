package thealphalabs.defaultcamera.ui.main;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.ImageFormat;
import android.util.Log;

import com.ragnarok.rxcamera.RxCamera;
import com.ragnarok.rxcamera.RxCameraData;
import com.ragnarok.rxcamera.request.Func;

import java.text.SimpleDateFormat;
import java.util.Date;

import rx.functions.Action1;
import thealphalabs.defaultcamera.data.DataManager;
import thealphalabs.defaultcamera.model.BluetoothPictureInfo;
import thealphalabs.defaultcamera.service.BluetoothConnectionHelper;
import thealphalabs.defaultcamera.ui.base.BasePresenter;

import static android.R.attr.path;
import static thealphalabs.defaultcamera.ui.splash.MainSplashView.TAG;

/**
 * Created by yeol on 17. 5. 1.
 */

public class MainCameraPresenter<V extends MainCameraMvpView> extends BasePresenter<V>
                                                            implements MainCameraMvpPresenter<V> {


    private BroadcastReceiver BtConnectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"BtConnected onReceive");
            getMvpView().removeSplashView();
            if(!getMvpView().checkCamera())
                getMvpView().openCamera();
            context.unregisterReceiver(this);
        }
    };

    public MainCameraPresenter(DataManager dataManager){
        super(dataManager);
    }


    @Override
    public void onAttach(V mvpView) {
        super.onAttach(mvpView);

        // this job was done in Splash View before
        if( !getDataManager().isServiceOn() ){
            getMvpView().bindBluetoothService();
        }
        //getMvpView().openCamera();

    }

    @Override
    public void takePicture(RxCamera camera) {
        if (!getMvpView().checkCamera()) {
            return;
        }
        getMvpView().showLoading();
        camera.request().takePictureRequest(true, new Func() {
            @Override
            public void call() {
                //getMvpView().showLog("Captured!");
            }
        }, 480, 640, ImageFormat.JPEG, true).subscribe(new Action1<RxCameraData>() {
            @Override
            public void call(RxCameraData rxCameraData) {
               /* String path = Environment.getExternalStorageDirectory() + "/test.jpg";
                File file = new File(path);
                Bitmap bitmap = BitmapFactory.decodeByteArray(rxCameraData.cameraData, 0, rxCameraData.cameraData.length);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                        rxCameraData.rotateMatrix, false);
                try {
                    file.createNewFile();
                    FileOutputStream fos = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                BluetoothPictureInfo picture = new BluetoothPictureInfo();
                picture.setFileName(getTimeData());
                picture.setRawImageData(rxCameraData.cameraData);
                getDataManager().sendImageData(picture);
                //getMvpView().showLog("hide Loading");
                getMvpView().hideLoading();
                getMvpView().showLog("Save file on " + path);
            }
        });
    }

    private String getTimeData(){
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeData= sdfNow.format(date);
        return timeData;
    }
    @Override
    public boolean isBtServiceConnected() {
        return getDataManager().isServiceOn();
    }

    @Override
    public void registerConnectedReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothConnectionHelper.onSocketConnected);
        context.registerReceiver(BtConnectedReceiver,filter);
    }

    @Override
    public void unRegisterConnectedReceiver(Context context) {
        context.unregisterReceiver(BtConnectedReceiver);
    }

    @Override
    public boolean checkServerConnected() {
        return getDataManager().isConnected();
    }

    @Override
    public boolean isBinded() {
        return getDataManager().isServiceOn();
    }
}
