package com.example.carbonoffseter;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Parcel;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.w3c.dom.Text;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@TargetApi(29)
public class CamTestActivity extends AppCompatActivity {

    private final static String TAG = "CAMERA_IMAGE_READY: ";
    private ImageReader imageReader;
    private String cameraId;
    private CameraDevice camera;
    private HandlerThread handlerThread;
    private Handler handler;
    private Surface imageReaderSurface;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_test);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String filePath = extras.getString("Key");
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        imageView = findViewById(R.id.texture);
        imageView.setImageBitmap(bitmap);
        //setupCamera(640, 480);
        //connectCamera();
    }

}