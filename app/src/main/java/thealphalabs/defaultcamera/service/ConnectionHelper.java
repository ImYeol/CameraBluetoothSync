package thealphalabs.defaultcamera.service;

import android.bluetooth.BluetoothDevice;

import com.github.ivbaranov.rxbluetooth.BluetoothConnection;

/**
 * Created by yeol on 17. 5. 2.
 */

public interface ConnectionHelper {

    void connectToServer(BluetoothDevice device);

    boolean isConnected();

    BluetoothConnection getBluetoothConnection();

    void clear();
}
