package thealphalabs.defaultcamera.data;

import android.content.Context;

import thealphalabs.defaultcamera.data.bluetooth.BluetoothHelper;

/**
 * Created by yeol on 17. 5. 1.
 */

public interface DataManager extends BluetoothHelper {

    void bindToBluetoothService(Context context);

    boolean isServiceOn();

}
