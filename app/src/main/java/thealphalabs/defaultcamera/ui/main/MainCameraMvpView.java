package thealphalabs.defaultcamera.ui.main;

import thealphalabs.defaultcamera.ui.base.MvpView;

/**
 * Created by yeol on 17. 5. 1.
 */

public interface MainCameraMvpView extends MvpView {

    void openCamera();

    void closeCamera();

    boolean checkCamera();

    void showLog(String s);

    void bindBluetoothService();
}
