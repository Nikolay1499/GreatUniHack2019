package com.example.carbonoffseter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OutputActivity extends AppCompatActivity {

    private static Context context;
    private static Bitmap image;
    public static ImageView bin_image;
    public static AssetManager assets;
    public static String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_output);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        filePath = extras.getString("Key");
        Button btn = (Button)findViewById(R.id.backButton);
        assets = getAssets();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(OutputActivity.this, MainActivity.class));
            }
        });
        OutputActivity.context = getApplicationContext();
        ImageView user_image = findViewById(R.id.imageView);
        user_image.setRotation(0);
        bin_image = findViewById(R.id.binImage);
        Bitmap rotateImage = BitmapFactory.decodeFile(filePath);
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        switch(orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                image = rotateImage(rotateImage, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                image = rotateImage(rotateImage, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                image = rotateImage(rotateImage, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                image = rotateImage;
        }

        user_image.setImageBitmap(image);
        try {
            readImage();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Context getAppContext() {
        return OutputActivity.context;
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    public void readImage() throws IOException
    {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer =
                new VisionRequestInitializer(SecureData.getKey()) {

                    @Override
                    protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                            throws IOException {
                        super.initializeVisionRequest(visionRequest);

                        String packageName = getPackageName();
                        visionRequest.getRequestHeaders().set("X-Android-Package", packageName);

                        String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                        visionRequest.getRequestHeaders().set("X-Android-Cert", sig);
                    }
                };

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();





        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();
        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();
            Image base64EncodedImage = new Image();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Bitmap bitmap = image;
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            base64EncodedImage.encodeContent(imageBytes);
            annotateImageRequest.setImage(base64EncodedImage);
            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                Feature labelDetection = new Feature();
                labelDetection.setType("LABEL_DETECTION");
                labelDetection.setMaxResults(20);
                add(labelDetection);
            }});

            // Add the list of one thing to the request
            add(annotateImageRequest);
        }});

        Vision.Images.Annotate annotateRequest =
                vision.images().annotate(batchAnnotateImagesRequest);
        AsyncTask<Object, Void, String> labelDetectionTask = new LableDetectionTask(this, annotateRequest);
        labelDetectionTask.execute();

    }

    private static class LableDetectionTask extends AsyncTask<Object, Void, String> {
        private final WeakReference<OutputActivity> mActivityWeakReference;
        private Vision.Images.Annotate mRequest;

        LableDetectionTask(OutputActivity activity, Vision.Images.Annotate annotate) {
            mActivityWeakReference = new WeakReference<>(activity);
            mRequest = annotate;
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                System.out.println("created Cloud Vision request object, sending request");
                BatchAnnotateImagesResponse response = mRequest.execute();
                return convertResponseToString(response);

            } catch (GoogleJsonResponseException e) {
                System.err.println(e);
            } catch (IOException e) {
                System.err.println(e);
            }
            return "Cloud Vision API request failed. Check logs for details.";
        }

        protected void onPostExecute(String result) {
            OutputActivity activity = mActivityWeakReference.get();
            if (activity != null && !activity.isFinishing()) {
                TextView imageDetail = activity.findViewById(R.id.textView);
                System.out.println(result);
                String output = CompareMaterials.getMatching(result);
                try
                {
                    if(output.contains("brown"))
                        bin_image.setImageBitmap(BitmapFactory.decodeStream(assets.open("brownBin.png")));
                    else if(output.contains("blue"))
                        bin_image.setImageBitmap(BitmapFactory.decodeStream(assets.open("blueBin.png")));
                    else if(output.contains("green"))
                        bin_image.setImageBitmap(BitmapFactory.decodeStream(assets.open("greenBin.png")));
                    else
                        bin_image.setImageBitmap(BitmapFactory.decodeStream(assets.open("greyBin.png")));
                }
                catch(IOException e)
                {
                    System.err.println(e);
                }
                if(output.length() > 0)
                    output = output.substring(0, 1).toUpperCase() + output.substring(1);
                imageDetail.setText(output);

            }
        }
    }

    private static String convertResponseToString(BatchAnnotateImagesResponse response) {
        StringBuilder message = new StringBuilder();

        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        System.out.println(labels.size());
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                if(label.getScore() > 0.50)
                {
                    message.append(String.format(Locale.US, "%s", label.getDescription()));
                    message.append("\n");
                }
            }
        } else {
            message.append("nothing");
        }
        return message.toString();
    }
}
