package thealphalabs.defaultcamera.ui.main;

import com.ragnarok.rxcamera.RxCamera;

import thealphalabs.defaultcamera.ui.base.MvpPresenter;

/**
 * Created by yeol on 17. 5. 1.
 */

public interface MainCameraMvpPresenter<V extends MainCameraMvpView> extends MvpPresenter<V> {

    void takePicture(RxCamera camera);

    boolean isBtServiceConnected();
}
