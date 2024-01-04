/*
package com.example.odexampleapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private Button InsertImage;
    private ImageView image;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InsertImage = (Button) findViewById(R.id.Insert);
        image = (ImageView) findViewById(R.id.imageView);

        InsertImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });

        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();

            // Load the selected image into a Bitmap object
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Run the object detector on the selected image
            ObjectDetector detector = null;
            try {
                detector = new ObjectDetector(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<DetectionResult> detectionResults = detector.detectObjects(bitmap);

            // Draw the detection results on the original image
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            paint.setColor(Color.RED);
            for (DetectionResult detectionResult : detectionResults) {
                RectF location = detectionResult.getLocation();
                canvas.drawRect(location, paint);
            }

            // Set the annotated image to the ImageView
            image.setImageBitmap(bitmap);
        }
    }

}
*/
package com.example.odexampleapp;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.odexampleapp.DetectionResult;
import com.example.odexampleapp.ObjectDetector;
import com.example.odexampleapp.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private Button InsertImage;
    private ImageView image;

    private static final int PICK_IMAGE_REQUEST = 1;

    private Handler handler;
    private Thread detectionThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InsertImage = (Button) findViewById(R.id.Insert);
        image = (ImageView) findViewById(R.id.imageView);

        InsertImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });
        // Initialize the Handler
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                // Update the UI with the detection results
                List<DetectionResult> detectionResults = (List<DetectionResult>) msg.obj;
                // ... update UI with detectionResults ...
            }
        };

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap bitmap = null;
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();

            // Load the selected image into a Bitmap object
            bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            final Bitmap finalBitmap = bitmap;
            detectionThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    // Run the object detector on the selected image
                    ObjectDetector detector = null;
                    try {
                        detector = new ObjectDetector(MainActivity.this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    List<DetectionResult> detectionResults = detector.detectObjects(finalBitmap);

                    // Draw the detection results on the original image
                    Canvas canvas = new Canvas(finalBitmap);
                    Paint paint = new Paint();
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(5);
                    paint.setColor(Color.RED);
                    for (DetectionResult detectionResult : detectionResults) {
                        RectF location = detectionResult.getLocation();
                        canvas.drawRect(location, paint);
                        // Send the detection results to the Handler
                        Message msg = handler.obtainMessage();
                        msg.obj = detectionResults;
                        handler.sendMessage(msg);
                    }
                }
            });

            // Start the detection Thread
            detectionThread.start();


            // Set the annotated image to the ImageView
            image.setImageBitmap(bitmap);
        }
    }
}
