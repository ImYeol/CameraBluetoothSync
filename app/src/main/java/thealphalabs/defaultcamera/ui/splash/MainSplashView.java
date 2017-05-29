package thealphalabs.defaultcamera.ui.splash;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.ragnarok.rxcamera.RxCamera;

import thealphalabs.defaultcamera.R;
import thealphalabs.defaultcamera.data.AppDataManager;
import thealphalabs.defaultcamera.service.BluetoothConnectionHelper;
import thealphalabs.defaultcamera.ui.CameraApp;
import thealphalabs.defaultcamera.ui.base.BaseActivity;
import thealphalabs.defaultcamera.ui.main.MainCameraView;

public class MainSplashView extends BaseActivity implements MainSplashMvpView {

    public static final String TAG = MainSplashView.class.getSimpleName();
    private RxCamera camera;
    private MainSplashMvpPresenter<MainSplashMvpView> mPresenter;

    private BroadcastReceiver BtConnectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"BtConnected onReceive");
            openCameraActivity();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
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
    protected void onStart() {
        Log.d(TAG,"onStart");
        bindBluetoothService();
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mPresenter.isBinded() && mPresenter.checkServerConnected()){
            Log.d(TAG,"onResume - openCameraAcitivity");
            openCameraActivity();
        } else {
            Log.d(TAG,"onResume - register receiver : "+ BtConnectedReceiver);
            IntentFilter filter = new IntentFilter();
            //filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            filter.addAction(BluetoothConnectionHelper.onSocketConnected);
            registerReceiver(BtConnectedReceiver,filter);
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG,"onPause");
        unregisterReceiver(BtConnectedReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG,"onStop");
        super.onStop();
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
        CameraApp.get(this).getDataManager().bindToBluetoothService(getApplicationContext());
    }

}
