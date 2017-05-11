package thealphalabs.defaultcamera.service.receiver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import thealphalabs.defaultcamera.service.BluetoothClientService;

import static android.content.Context.MODE_PRIVATE;
import static thealphalabs.defaultcamera.service.BluetoothClientService.RECENT_DEVICE;
import static thealphalabs.defaultcamera.service.BluetoothClientService.RECENT_DEVICE_NAME;

/**
 * Created by yeol on 17. 5. 11.
 */

public class BluetoothConnectedReceiver extends BroadcastReceiver {

    private static final String TAG= "BluetoothConnectedRecevier";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"BluetoothConnectedRecevier");
        if(intent.getAction().equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.d(TAG, "Action_ACL_CONNECTED - Name : " + device.getName());
            saveRecentDevice(context,device);
        }
    }

    public void saveRecentDevice(Context context,BluetoothDevice device){
        SharedPreferences pref = context.getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(RECENT_DEVICE, device.getAddress());
        editor.putString(RECENT_DEVICE_NAME, device.getName());
        editor.commit();
    }
}
