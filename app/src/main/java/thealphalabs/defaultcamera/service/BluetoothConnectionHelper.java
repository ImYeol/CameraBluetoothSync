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
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonWriter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import thealphalabs.defaultcamera.model.BluetoothPictureInfo;
import thealphalabs.defaultcamera.model.BtPictureInfo;

import static thealphalabs.defaultcamera.service.BluetoothClientService.TAG;

/**
 * Created by yeol on 17. 5. 2.
 */

public class BluetoothConnectionHelper implements ConnectionHelper {

    public static final String onSocketConnected = "BluetoothConnectionHelper.connected";
    public static final String onSocketDisConnected = "BluetoothConnectionHelper.disconnected";

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


    public class ConnectedReceiver extends BroadcastReceiver {
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
                    notifySocketDisConnected();
                }
            }
        }
    }
    private ConnectedReceiver BtConnectedReceiver = new ConnectedReceiver();
    /*private BroadcastReceiver BtConnectedReceiver = new BroadcastReceiver() {

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
                    notifySocketDisConnected();
                }
            }
        }
    };*/

    public void registerAclConnectedReceiver(Context context){
        if(BtConnectedReceiver == null){
            Log.d(TAG,"registerAclConnectedReceiver - BtConnectedReceiver is null");
            BtConnectedReceiver = new ConnectedReceiver();
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
            Log.d(TAG,"connectToServer == null");
            connectThread = new ConnectThread(device);
            connectThread.start();
        } else {
            if(!connectThread.isAlive()){
                Log.d(TAG,"connectToServer not null");
                connectThread.start();
            }
        }
    }

    @Override
    public void sendPictureToService(BluetoothPictureInfo data) {
        //writer.setIndent("  ");
        try {

           /* GsonBuilder builder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation();
            final Gson gson = builder.create();
            String json = gson.toJson(data,BluetoothPictureInfo.class);
            Log.d(TAG,"origin fileName: "+data.getFileName() + " Thread: " + Thread.currentThread());
            Log.d(TAG,"json fileName : "+ json.substring(0,100));
            gson.toJson(data, BluetoothPictureInfo.class, writer);
            writer.flush();*/
           //sendData(data);
            sendFlatbuffer(data);

        }  catch (JsonIOException e){
            Log.d(TAG," json IO exception");
            e.printStackTrace();
            //writer.close();
        } catch (IOException e){
            Log.d(TAG," IO Exception on sendPictureToServer");
            e.printStackTrace();
        }

    }

    private void sendFlatbuffer(BluetoothPictureInfo data) throws IOException {
        FlatBufferBuilder fbb = new FlatBufferBuilder(1024);

        int name = fbb.createString(data.getFileName());
        int image = BtPictureInfo.createRawImageDataVector(fbb,data.getRawImageData());

        BtPictureInfo.startBtPictureInfo(fbb);
        BtPictureInfo.addFileName(fbb,name);
        BtPictureInfo.addRawImageData(fbb,image);
        int end = BtPictureInfo.endBtPictureInfo(fbb);
        fbb.finish(end);

        // Do not write in text mode but binary mode

        byte[] flatData = fbb.sizedByteArray();

        DataOutputStream out = new DataOutputStream(outputStream);
        out.writeInt(flatData.length);
        out.write(flatData);
        //out.flush();
        Log.d(TAG,"sendFlatBuffer : byte size: " + data.getRawImageData().length + "flat size: "+ flatData.length);

    }

    private void sendData(BluetoothPictureInfo data) throws IOException{
        DataOutputStream out = new DataOutputStream(outputStream);

            out.writeUTF(data.getFileName());
            out.writeInt(data.getRawImageData().length);
            out.write(data.getRawImageData(),0,data.getRawImageData().length);
            out.flush();
    }

    @Override
    public boolean isConnected() {
        if(mSocket == null){
            return false;
        }
        isConnected = mSocket.isConnected();
        return isConnected;
    }

    @Override
    public BluetoothConnection getBluetoothConnection() {
        return mBluetoothConnection;
    }

    @Override
    public void clear() {
        isConnected = false;
       /* try {
            writer.close();
            connectThread.interrupt();
            connectThread.cancel();
            connectThread = null;
        } catch (IOException e) {
            e.printStackTrace();
        }*/
       if(connectThread != null) {
           connectThread.interrupt();
           connectThread.cancel();
           connectThread = null;
       }

        mSocket = null;
    }
    public void notifySocketConnected(){
        Intent intent = new Intent();
        intent.setAction(onSocketConnected);
        context.sendBroadcast(intent);
    }

    public void notifySocketDisConnected(){
        if(context == null){
            Log.d(TAG,"notifySocketDisConnected context : "+context);
        }
        Intent intent = new Intent();
        intent.setAction(onSocketDisConnected);
        context.sendBroadcast(intent);
    }

    private class ConnectThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private int retryCount=0;

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
            mSocket=tmp;
            mmDevice = device;
        }
        public void run() {
            // Cancel discovery because it will slow down the connection
            //mBluetoothAdapter.cancelDiscovery();
            //retryCount = 3;
            while(!isInterrupted()) {
                try {
                    Log.d(TAG, "run connectThread :"+mmSocket);
                    if (mmSocket.isConnected()) {
                        Log.d(TAG, "mmSocket is already connected");
                        return;
                    }
                    // Connect the device through the socket. This will block
                    // until it succeeds or throws an exception
                    mmSocket.connect();
                    isConnected = true;
                    // Do work to manage the connection (in a separate thread)
                    //mBluetoothConnection = new BluetoothConnection(mSocket);
                    outputStream = mSocket.getOutputStream();
                    inputStream = mSocket.getInputStream();
                    /*try {
                        writer = new JsonWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        Log.d(TAG, "unSupported Encoding Exception");
                        e.printStackTrace();
                    }*/
                    Log.d(TAG, "BluetoothConnection success to get stream " + mSocket.isConnected() );
                    notifySocketConnected();
                    return ;

                } catch (IOException connectException) {
                    // Unable to connect; close the socket and get out
                    Log.e(TAG, "IOException : Unable to connect to device");
                    connectException.printStackTrace();
                    //retryCount--;
                    cancel();
                    return;
                } catch (Exception e) {
                    //retryCount--;
                    Log.e(TAG, "unable to get stream");
                    Log.e(TAG, e.getMessage());
                    return ;
                }
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
                connectThread = null;
            } catch (IOException e) {
                Log.e(TAG, "[cancel()] Failed to close socket.");
            }
        }
    }
}
