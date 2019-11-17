package com.example.carbonoffseter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

public class OutputActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output);
        ImageView user_image = findViewById(R.id.imageView);
        user_image.setImageBitmap(MainActivity.getImageToAnalyse());
        System.out.println(MainActivity.getImageToAnalyse());

    }
}
