package com.example.carbonoffseter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private static Bitmap imageToAnalyse;
    private static String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        Button btn_choose_photo = findViewById(R.id.uploadBtn);
        Button btn_camera = findViewById(R.id.cameraBtn);
        btn_choose_photo.setOnClickListener(btnChoosePhotoPressed);
        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MainActivity.this, CamActivity.class));
            }
        });
        int currentPoints = getCurrentPoints();
        TextView currentPointsText = (TextView) findViewById(R.id.currentPointsView);
        String message = "Your current points are " + currentPoints;
        currentPointsText.setText(message);
    }

    private int getCurrentPoints() {
        SharedPreferences sharedPref = getSharedPreferences("MyFile", Context.MODE_PRIVATE);
        if (sharedPref.contains("currentPoints")){
            return sharedPref.getInt("currentPoints", 0);
        }
        else {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("currentPoints", 0);
            editor.commit();
            return 0;
        }
    }

    public static String getFilePath()
    {
        return filePath;
    }

    public View.OnClickListener btnChoosePhotoPressed = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            grantPermission();
            Intent i = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            final int ACTIVITY_SELECT_IMAGE = 1234;
            startActivityForResult(i, ACTIVITY_SELECT_IMAGE);
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1234:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    filePath = cursor.getString(columnIndex);
                    cursor.close();

                    imageToAnalyse = BitmapFactory.decodeFile(filePath);
                    Intent startIntent =  new Intent(getApplicationContext(), OutputActivity.class);
                    startIntent.putExtra("Key", filePath);
                    startActivity(startIntent);
                }
        }
    }

    public static Bitmap getImageToAnalyse(){
        if(imageToAnalyse != null)
            return imageToAnalyse;
        else
            return null;
    }


    public void grantPermission(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            }
            else {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    /*imageToAnalyse = BitmapFactory.decodeFile(filePath);
                    Intent startIntent =  new Intent(getApplicationContext(), OutputActivity.class);
                    startActivity(startIntent);*/
                }
                else {
                    System.out.println("Permission not granted");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            default: System.out.println("Error in request permission result");
        }
    }

}
