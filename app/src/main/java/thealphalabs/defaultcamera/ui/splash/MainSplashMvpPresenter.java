package thealphalabs.defaultcamera.ui.splash;

import thealphalabs.defaultcamera.ui.base.MvpPresenter;

/**
 * Created by yeol on 17. 5. 1.
 */

public interface MainSplashMvpPresenter<V extends MainSplashMvpView> extends MvpPresenter<V> {
    boolean checkServerConnected();

    boolean isBinded();
}
