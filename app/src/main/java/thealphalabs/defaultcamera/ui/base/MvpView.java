package thealphalabs.defaultcamera.ui.base;

/**
 * Created by yeol on 17. 4. 19.
 */

public interface MvpView {

    void showLoading();

    void hideLoading();

    void onError(String message);

    void hideKeyboard();

    boolean isNetworkConnected();

}
