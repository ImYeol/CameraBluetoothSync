package thealphalabs.defaultcamera.ui.main;

import android.content.Context;

import com.ragnarok.rxcamera.RxCamera;

import thealphalabs.defaultcamera.ui.base.MvpPresenter;

/**
 * Created by yeol on 17. 5. 1.
 */

public interface MainCameraMvpPresenter<V extends MainCameraMvpView> extends MvpPresenter<V> {

    void takePicture(RxCamera camera);

    boolean isBtServiceConnected();

    void registerConnectedReceiver(Context context);

    void unRegisterConnectedReceiver(Context context);

    void registerDisConnectedReceiver(Context context);

    void unRegisterDisConnectedReceiver(Context context);

    boolean checkServerConnected();

    boolean isBinded();
}
