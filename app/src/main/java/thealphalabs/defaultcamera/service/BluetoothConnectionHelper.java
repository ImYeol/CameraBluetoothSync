package thealphalabs.defaultcamera.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelUuid;
import android.util.Log;

import com.github.ivbaranov.rxbluetooth.BluetoothConnection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import thealphalabs.defaultcamera.model.BluetoothPictureInfo;

import static thealphalabs.defaultcamera.ui.main.MainCameraView.TAG;

/**
 * Created by yeol on 17. 5. 2.
 */

public class BluetoothConnectionHelper implements ConnectionHelper {

    public static final String onSocketConnected = "BluetoothConnectionHelper.connected";

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mSocket;
    private ConnectThread connectThread;
    private BluetoothConnection mBluetoothConnection;

    private JsonWriter writer;
    private OutputStream outputStream;
    private InputStream inputStream;

    private static BluetoothConnectionHelper instance;
    private boolean isConnected;
    private Context context;

    public final ParcelUuid MY_UUID = ParcelUuid.fromString("00001101-0000-1000-8000-00805F9B34FB");


    private BroadcastReceiver BtConnectedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals(BluetoothDevice.ACTION_ACL_CONNECTED)){
                if(isConnected()){
                    Log.d(TAG,"BtConnectedReceiver - isConnected True");
                } else {
                    Log.d(TAG,"BtConnectedReceiver - isConnected false");
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    connectToServer(device);
                }
            } else if(intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)){
                Log.d(TAG,"BtConnectedReceiver - ACL_DisConnected");
                if(isConnected()){
                    connectThread.interrupt();
                    connectThread.cancel();
                    connectThread = null;
                }
            }
        }
    };

    public void registerAclConnectedReceiver(Context context){
        if(BtConnectedReceiver == null){
            Log.d(TAG,"registerAclConnectedReceiver - BtConnectedReceiver is null");
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        context.registerReceiver(BtConnectedReceiver,filter);
    }

    public void unRegisterAclConnectedReceiver(Context context){
        context.unregisterReceiver(BtConnectedReceiver);
        BtConnectedReceiver = null;
    }

    public static BluetoothConnectionHelper getInstance(Context context){
        if(instance == null){
            instance = new BluetoothConnectionHelper(context);
        }
        return instance;
    }

    private BluetoothConnectionHelper(Context context){
        this.context = context;
        mBluetoothConnection = null;
        isConnected = false;
        connectThread = null;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    @Override
    public void connectToServer(BluetoothDevice device) {
        if(connectThread == null ) {
            connectThread = new ConnectThread(device);
            connectThread.start();
        } else {
            if(!connectThread.isAlive()){
                connectThread.start();
            }
        }
    }

    @Override
    public void sendPictureToService(BluetoothPictureInfo data) {
        //writer.setIndent("  ");
        try {
           // writer.beginObject();
            //data.setRawImageData("nice");
            GsonBuilder builder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation();
            final Gson gson = builder.create();
            String json = gson.toJson(data,BluetoothPictureInfo.class);
            Log.d(TAG,"origin fileName: "+data.getFileName());
            Log.d(TAG,"json fileName : "+ json.substring(0,100));
            gson.toJson(data, BluetoothPictureInfo.class, writer);
            writer.flush();
           // writer.endObject();
           // writer.close();
        }  catch (JsonIOException e){
            Log.d(TAG," json IO exception");
            e.printStackTrace();
            //writer.close();
        } catch (IOException e){
            Log.d(TAG," IO Exception on sendPictureToServer");
            e.printStackTrace();
        }

    }

    @Override
    public boolean isConnected() {
        if(mSocket == null){
            return false;
        }
        isConnected = mSocket.isConnected() && isConnected;
        return isConnected;
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

            Log.d(TAG,"ConnectThread ");
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app’s UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID.getUuid());
            } catch (IOException e) {
                Log.e(TAG, "Failed to get bluetooth socket.");
            }
            mmSocket = tmp;
            mmDevice = device;
        }
        public void run() {
            // Cancel discovery because it will slow down the connection
            //mBluetoothAdapter.cancelDiscovery();

            try {
                Log.d(TAG,"run connectThread ");
                if(mmSocket.isConnected()){
                    Log.d(TAG,"mmSocket is already connected");
                    return ;
                }
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
                isConnected = true;
                // Do work to manage the connection (in a separate thread)
                mSocket = mmSocket;
                //mBluetoothConnection = new BluetoothConnection(mSocket);
                outputStream = mSocket.getOutputStream();
                inputStream = mSocket.getInputStream();
                try {
                    writer = new JsonWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    Log.d(TAG,"unSupported Encoding Exception");
                    e.printStackTrace();
                }
                Log.d(TAG,"BluetoothConnection success to get stream");
                notifySocketConnected();

            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                Log.e(TAG, "IOException : Unable to connect to device");
                connectException.printStackTrace();
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
                isConnected = false;
                if(mmSocket.isConnected())
                    mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "[cancel()] Failed to close socket.");
            }
        }

        public void notifySocketConnected(){
            Intent intent = new Intent();
            intent.setAction(onSocketConnected);
            context.sendBroadcast(intent);
        }
    }
}
