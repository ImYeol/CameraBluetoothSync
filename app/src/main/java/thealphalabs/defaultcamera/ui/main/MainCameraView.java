package thealphalabs.defaultcamera.ui.main;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.ragnarok.rxcamera.RxCamera;
import com.ragnarok.rxcamera.config.RxCameraConfig;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import java.util.UUID;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import thealphalabs.defaultcamera.R;
import thealphalabs.defaultcamera.data.AppDataManager;
import thealphalabs.defaultcamera.databinding.ActivityMainCameraViewBinding;
import thealphalabs.defaultcamera.model.BluetoothPictureInfo;
import thealphalabs.defaultcamera.ui.CameraApp;
import thealphalabs.defaultcamera.ui.base.BaseActivity;

import static android.R.attr.keycode;

public class MainCameraView extends BaseActivity implements MainCameraMvpView {

    public static final String TAG = MainCameraView.class.getSimpleName();
    private static final int CONNECTION_SUCCESS = 1;
    private static final int CONNECTION_FAILED = 2;
    private static final int RECEIBED_DATA=3;


    private RxCamera camera;
    private MainCameraMvpPresenter<MainCameraMvpView> mPresenter;
    private static final String[] REQUEST_PERMISSIONS = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int REQUEST_PERMISSION_CODE = 233;

    private ActivityMainCameraViewBinding binding;

    //////////////////////////////////
    public static final UUID MY_UUID =
            UUID.fromString("D04E3068-E15B-4482-8306-4CABFA1726E7");
    private BluetoothSocket clientSocket;
    private AccecptThread accecptThread;
    private InputStream inputStream;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUp();
        bindAndAttach();
    }

    @Override
    protected void setUp() {
        mPresenter=new MainCameraPresenter<MainCameraMvpView>(AppDataManager.getInstance());

    }

    @Override
    protected void bindAndAttach() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_camera_view);
        mPresenter.onAttach(this);
    }


    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        for (String permission : REQUEST_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keycode == KeyEvent.KEYCODE_DPAD_DOWN){

        }
        else if(keycode == KeyEvent.KEYCODE_DPAD_UP){
            mPresenter.takePicture(camera);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            mPresenter.takePicture(camera);
            return true;
        }
        return false;
    }

    @Override
    public void openCamera() {
        RxCameraConfig config = new RxCameraConfig.Builder()
                .useBackCamera()
                .setAutoFocus(true)
                .setPreferPreviewFrameRate(15, 30)
                .setPreferPreviewSize(new Point(640, 480), false)
                .setHandleSurfaceEvent(true)
                .build();
        Log.d(TAG, "config: " + config);
        RxCamera.open(this, config).flatMap(new Func1<RxCamera, Observable<RxCamera>>() {
            @Override
            public Observable<RxCamera> call(RxCamera rxCamera) {
                showLog("isopen: " + rxCamera.isOpenCamera() + ", thread: " + Thread.currentThread());
                camera = rxCamera;
                return rxCamera.bindTexture(binding.preview);
            }
        }).flatMap(new Func1<RxCamera, Observable<RxCamera>>() {
            @Override
            public Observable<RxCamera> call(RxCamera rxCamera) {
                showLog("isbindsurface: " + rxCamera.isBindSurface() + ", thread: " + Thread.currentThread());
                return rxCamera.startPreview();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<RxCamera>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                showLog("open camera error: " + e.getMessage());
            }

            @Override
            public void onNext(final RxCamera rxCamera) {
                camera = rxCamera;
                showLog("open camera success: " + camera);
               // Toast.makeText(MainCameraView.this, "Now you can tap to focus", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean checkCamera() {
        if (camera == null || !camera.isOpenCamera()) {
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CameraApp.get(this).getDataManager().unBindBluetoothService(this);
        if (camera != null) {
            camera.closeCamera();
        }
    }

    @Override
    public void showLog(String s) {
        Log.d(TAG, s);
    }

    @Override
    public void bindBluetoothService() {
        CameraApp.get(this).getDataManager().bindToBluetoothService(this);
    }


    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch ( msg.what){
                case CONNECTION_SUCCESS :
                    Log.d(TAG,"connection success");
                    break;
                case CONNECTION_FAILED :
                    Log.d(TAG,"connection failed");
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case RECEIBED_DATA :
                    Log.d(TAG,"Received_data");
                    BluetoothPictureInfo picture = (BluetoothPictureInfo) msg.obj;
                    Toast.makeText(MainCameraView.this,picture.getFileName(),Toast.LENGTH_SHORT);

                    break;
            }
        }
    };

}
