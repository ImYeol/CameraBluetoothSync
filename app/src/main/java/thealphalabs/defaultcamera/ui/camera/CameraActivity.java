package thealphalabs.defaultcamera.ui.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.TextureView;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.IOException;

import thealphalabs.defaultcamera.R;

/**
 * Created by yeol on 17. 5. 25.
 */

public class CameraActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener{

    private static final String TAG="CameraAcitivy";
    private int CAMERA_ID = 0;
    private int cameraNum;
    private TextureView mTextureView;
    private Camera mCamera;

    private final int resWidth = 640;
    private final int resHeight = 480;

    SurfaceTexture mSurfaceTexture = null;
    private FrameLayout rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cameraNum = Camera.getNumberOfCameras();
        Log.d(TAG,"camera NUM: " + cameraNum);
        if(cameraNum == 0){
            Toast.makeText(CameraActivity.this, "there is no camera", Toast.LENGTH_LONG).show();
            finish();
        }else if(cameraNum >= 2){
            CAMERA_ID = 0;
        }

        init();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height){
        Log.d(TAG, " onSurfaceTextureAvailable");
        /*if(mCamera !=null){
            mCamera.release();
        }
        mCamera = Camera.open(CAMERA_ID);
        mSurfaceTexture = surfaceTexture;
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(resWidth, resHeight);
        parameters.setPictureSize(resWidth, resHeight);
        mCamera.setParameters(parameters);

        try{
            mCamera.setPreviewTexture(surfaceTexture);
            mCamera.startPreview();
        }catch(IOException ioe){
            Log.e("camera-reverse", ioe.getMessage());
        }*/
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        Log.d(TAG, " onSurfaceTextureDestroyed");
        if (null != mCamera) {
            mCamera.stopPreview();
            mCamera.release();
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    public void onPause() {
        super.onPause();
        Log.d(TAG, " onPause");
        releaseCamera();
    }

    @Override
    protected void onDestroy() {

        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, " onResume");
        if(mCamera == null){
            Log.d(TAG, " onResume - mCamera is null : TextureView " + mTextureView.isAvailable());
            mCamera = Camera.open(CAMERA_ID);
            mTextureView.setSurfaceTextureListener(this);

            //rootView.addView(mTextureView);
 /*       Matrix matrix = new Matrix();
        matrix.setScale(-1, 1);
        matrix.postTranslate(width, 0);
        mTextureView.setTransform(matrix);
*/

            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(resWidth, resHeight);
            parameters.setPictureSize(resWidth, resHeight);
            mCamera.setParameters(parameters);

            try{
                mCamera.setPreviewTexture(mTextureView.getSurfaceTexture());
                mCamera.startPreview();
                Log.d(TAG,"startPreview : TextureView " + mTextureView.isAvailable());
            }catch(IOException ioe){
                Log.e("camera-reverse", ioe.getMessage());
            }
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            try {
                mCamera.setPreviewTexture(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){

        }
        else if(keyCode == KeyEvent.KEYCODE_DPAD_UP){
            mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
            return true;
        }
        return false;
    }

    private void init(){
        setContentView(R.layout.camera_view);
        rootView = (FrameLayout)findViewById(R.id.rootview);
      //  setContentView(R.layout.activity_main_camera_view);
        mTextureView = (TextureView)findViewById(R.id.preview2);
      // mTextureView = new TextureView(this);

       // mTextureView.setSurfaceTextureListener(this);
       /* FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mTextureView.setLayoutParams(params);
        rootView.addView(mTextureView);*/

    }

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback(){
        public void onShutter(){
            Log.e(TAG, "shutterCallback : " + Thread.currentThread());
        }
    };

    Camera.PictureCallback rawCallback = new Camera.PictureCallback(){
        public void onPictureTaken(byte[] data, Camera camera){
            Log.e(TAG, "rawCallback : " + Thread.currentThread());
        }
    };

    Camera.PictureCallback jpegCallback = new Camera.PictureCallback(){
        public void onPictureTaken(byte[] data, Camera camera){
            Log.e(TAG, "jpegCallback : " + Thread.currentThread());

         /*   Bitmap picture = BitmapFactory.decodeByteArray(data, 0, data.length);

            try {

                File dir = new File("/path");
                dir.mkdirs();

                File outFile = new File(dir, "test.jpg");

                FileOutputStream outStream = new FileOutputStream(outFile);
                picture.compress(Bitmap.CompressFormat.JPEG, 100, outStream);

                outStream.flush();
                outStream.close();

                picture.recycle();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }*/
        }
    };
}
