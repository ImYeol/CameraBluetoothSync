package thealphalabs.defaultcamera.ui.main;

import com.ragnarok.rxcamera.RxCamera;

/**
 * Created by yeol on 17. 5. 1.
 */

public class MainCameraPresenter<V extends MainCameraPresenter> extends MainCameraMvpPresenter<V>
                                                            implements MainCameraMvpPresenter<V> {



    @Override
    public void onAttach(V mvpView) {

    }

    @Override
    public void onDetach() {

    }

    @Override
    public void takePicture(RxCamera camera) {

    }

    @Override
    public boolean isBtServiceConnected() {
        return false;
    }
}
