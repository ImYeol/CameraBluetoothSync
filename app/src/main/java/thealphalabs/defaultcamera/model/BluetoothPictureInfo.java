package thealphalabs.defaultcamera.model;

import android.util.Base64;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by yeol on 17. 5. 1.
 */

public class BluetoothPictureInfo {

    @Expose
    @SerializedName("fileName")
    private String fileName;

    @Expose
    @SerializedName("rawImageData")
    private String rawImageData;

    public BluetoothPictureInfo(){

    }

    @Override
    public boolean equals(Object obj) {
        return fileName.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public void setFileName(String fileName){
        this.fileName = fileName;
    }
    public String getFileName(){
        return this.fileName;
    }

    public void setRawImageData(byte[] imageData){
        this.rawImageData = Base64.encodeToString(imageData, Base64.DEFAULT);
    }

    public String getRawImageData(){
        return this.rawImageData;
    }

    public void setRawImageData(String imageData){
        this.rawImageData = imageData;
    }

    public void clear(){
        this.rawImageData = null;
        this.fileName = null;
    }


}
