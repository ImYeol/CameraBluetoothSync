package thealphalabs.defaultcamera.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.github.ivbaranov.rxbluetooth.BluetoothConnection;

import java.io.IOException;
import java.util.UUID;

import static thealphalabs.defaultcamera.ui.main.MainCameraView.TAG;

/**
 * Created by yeol on 17. 5. 2.
 */

public class BluetoothConnectionHelper implements ConnectionHelper {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mSocket;
    private ConnectThread connectThread;
    private BluetoothConnection mBluetoothConnection;

    private static BluetoothConnectionHelper instance;
    private boolean isConnected;

    public static final UUID MY_UUID = UUID.fromString(
            "D04E3068-E15B-4482-8306-4CABFA1726E7");

    public static BluetoothConnectionHelper getInstance(){
        if(instance != null){
            instance = new BluetoothConnectionHelper();
        }
        return instance;
    }

    private BluetoothConnectionHelper(){
        mBluetoothConnection = null;
        isConnected = false;
        connectThread = null;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    @Override
    public void connectToServer(BluetoothDevice device) {
        connectThread = new ConnectThread(device);
        connectThread.start();
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public BluetoothConnection getBluetoothConnection() {
        return mBluetoothConnection;
    }

    @Override
    public void clear() {
        try{
            isConnected = false;
            if(mSocket.isConnected())
                mSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        isConnected = false;
        if(mSocket.isConnected())
            try {
                if(mSocket.isConnected())
                    mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        mSocket = null;
    }

    private class ConnectThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app’s UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Failed to get bluetooth socket.");
            }
            mmSocket = tmp;
            mmDevice = device;
        }
        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
                // Do work to manage the connection (in a separate thread)
                mSocket = mmSocket;
                isConnected = true;
                mBluetoothConnection = new BluetoothConnection(mSocket);
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                Log.e(TAG, "IOException : Unable to connect to device");
                cancel();
                return;
            } catch (Exception e){
                Log.e(TAG,"unable to get stream");
                Log.e(TAG,e.getMessage());
            }

            // Save connected device instance if succeed to connect.
            //setConnectedDevice(mmDevice);
        }
        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
                isConnected = false;
            } catch (IOException e) {
                Log.e(TAG, "[cancel()] Failed to close socket.");
            }
        }
    }
}
