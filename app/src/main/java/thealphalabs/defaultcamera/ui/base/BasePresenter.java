package thealphalabs.defaultcamera.ui.base;


import thealphalabs.defaultcamera.data.DataManager;

/**
 * Created by yeol on 17. 4. 19.
 */

public class BasePresenter<V extends MvpView> implements MvpPresenter<V> {


    private final DataManager mDataManager;

    private V mMvpView;

    public BasePresenter(DataManager dataManager){
        mDataManager = dataManager;
    }

    @Override
    public void onAttach(V mvpView) {
        mMvpView = mvpView;
    }

    @Override
    public void onDetach() {
        mMvpView = null;
    }

    public boolean isViewAttached(){
        return mMvpView != null;
    }

    public V getMvpView(){
        return mMvpView;
    }

    public DataManager getDataManager(){
        return mDataManager;
    }

}
