package thealphalabs.defaultcamera.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.ragnarok.rxcamera.RxCamera;
import com.ragnarok.rxcamera.config.RxCameraConfig;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import thealphalabs.defaultcamera.R;
import thealphalabs.defaultcamera.data.AppDataManager;
import thealphalabs.defaultcamera.databinding.ActivityMainCameraViewBinding;
import thealphalabs.defaultcamera.ui.CameraApp;
import thealphalabs.defaultcamera.ui.base.BaseActivity;

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

    private View splashView = null;



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
        //addSplashView();
        //mPresenter.registerConnectedReceiver(this);
    }

    @Override
    public void showLoading() {
        super.showLoading();
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
        if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){

        }
        else if(keyCode == KeyEvent.KEYCODE_DPAD_UP){
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
    protected void onResume() {
        super.onResume();
        addSplashView();
        /*Log.d(TAG,"onResume : "+ checkCamera());
        if( !mPresenter.isBinded() ){
            mPresenter.registerConnectedReceiver(this);
            //bindBluetoothService();
        }*/
        startPreview();
       /* if(!checkCamera()){
            //closeCamera();
            openCamera();

        }*/
    }

    private void startPreview(){
        if(mPresenter.isBinded() && mPresenter.checkServerConnected()){
            Log.d(TAG,"onResume - openCameraAcitivity");
            removeSplashView();
            if(!checkCamera()){
                //closeCamera();
                openCamera();
            }
        } else {
            Log.d(TAG,"onResume - register receiver : "+mPresenter.isBinded() + " " + mPresenter.checkServerConnected());
            mPresenter.registerConnectedReceiver(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG,"onNewIntent");
        super.onNewIntent(intent);
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
               // Toast.makeText(MainSplashView.this, "Now you can tap to focus", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void closeCamera() {
        camera.closeCameraWithResult().subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                showLog("close camera finished, success: " + aBoolean);
            }
        });
        //camera.closeCamera();
        camera = null;
    }

    @Override
    public boolean checkCamera() {
        if (camera == null || !camera.isOpenCamera()) {
            return false;
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"onPause : " +checkCamera());
        if(checkCamera()) {
            closeCamera();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
        CameraApp.get(this).getDataManager().unBindBluetoothService(this);
        /*if (camera != null) {
            camera.closeCamera();
        }*/
    }

    @Override
    public void showLog(String s) {
        Log.d(TAG, s);
    }

    @Override
    public void bindBluetoothService() {
        CameraApp.get(this).getDataManager().bindToBluetoothService(this);
    }

    @Override
    public void removeSplashView() {
        if(splashView != null)
            binding.rootView.removeView(splashView);
    }

    @Override
    public void addSplashView() {
        splashView = getLayoutInflater().inflate(R.layout.activity_splash,null);
        binding.rootView.addView(splashView);
    }


}
