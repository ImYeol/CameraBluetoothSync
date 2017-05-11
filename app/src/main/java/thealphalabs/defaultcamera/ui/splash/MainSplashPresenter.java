package thealphalabs.defaultcamera.ui.splash;

import thealphalabs.defaultcamera.data.DataManager;
import thealphalabs.defaultcamera.ui.base.BasePresenter;

/**
 * Created by yeol on 17. 5. 1.
 */

public class MainSplashPresenter<V extends MainSplashMvpView> extends BasePresenter<V>
                                                            implements MainSplashMvpPresenter<V> {

    public MainSplashPresenter(DataManager dataManager){
        super(dataManager);
    }


    @Override
    public void onAttach(V mvpView) {
        super.onAttach(mvpView);

        if( ! isBinded() ){
            getMvpView().bindBluetoothService();
        }

    }


    @Override
    public boolean checkServerConnected() {
        return getDataManager().isConnected();
    }

    @Override
    public boolean isBinded() {
        return getDataManager().isServiceOn();
    }
}
