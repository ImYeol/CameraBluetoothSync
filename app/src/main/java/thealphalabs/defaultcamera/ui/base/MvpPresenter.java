package thealphalabs.defaultcamera.ui.base;

/**
 * Created by yeol on 17. 4. 19.
 */

public interface MvpPresenter<v extends MvpView> {

    void onAttach(v mvpView);

    void onDetach();

}
