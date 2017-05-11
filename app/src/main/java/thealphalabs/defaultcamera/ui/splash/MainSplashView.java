package thealphalabs.defaultcamera.ui.splash;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.ragnarok.rxcamera.RxCamera;

import thealphalabs.defaultcamera.R;
import thealphalabs.defaultcamera.data.AppDataManager;
import thealphalabs.defaultcamera.databinding.ActivityMainCameraViewBinding;
import thealphalabs.defaultcamera.ui.CameraApp;
import thealphalabs.defaultcamera.ui.base.BaseActivity;
import thealphalabs.defaultcamera.ui.main.MainCameraView;

public class MainSplashView extends BaseActivity implements MainSplashMvpView {

    public static final String TAG = MainSplashView.class.getSimpleName();
    private static final int CONNECTION_SUCCESS = 1;
    private static final int CONNECTION_FAILED = 2;
    private static final int RECEIBED_DATA=3;


    private RxCamera camera;
    private MainSplashMvpPresenter<MainSplashMvpView> mPresenter;

    private BroadcastReceiver BtConnectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"BtConnected onReceive");
            openCameraActivity();
        }
    };
    private static final String[] REQUEST_PERMISSIONS = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int REQUEST_PERMISSION_CODE = 233;

    private ActivityMainCameraViewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUp();
        bindAndAttach();
    }

    @Override
    protected void setUp() {
        mPresenter=new MainSplashPresenter<MainSplashMvpView>(AppDataManager.getInstance());
    }

    @Override
    protected void bindAndAttach() {
        setContentView(R.layout.activity_splash);
        mPresenter.onAttach(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mPresenter.isBinded() && mPresenter.checkServerConnected()){
            openCameraActivity();
        } else {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            registerReceiver(BtConnectedReceiver,filter);
        }
    }

    @Override
    protected void onPause() {
        unregisterReceiver(BtConnectedReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mPresenter.onDetach();
        super.onDestroy();
    }

    @Override
    public void openCameraActivity() {
        Intent i = new Intent(MainSplashView.this, MainCameraView.class);
        MainSplashView.this.startActivity(i);
    }

    @Override
    public void bindBluetoothService() {
        CameraApp.get(this).getDataManager().bindToBluetoothService(this);
    }


}
