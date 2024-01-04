package com.example.odexampleapp;/*
package com.example.odexampleapp;

import android.graphics.Bitmap;
import android.graphics.RectF;

import com.example.odexampleapp.ml.LiteModelSsdMobilenetV11Metadata2;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.TensorImage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ObjectDetector {
    private Interpreter tflite;

    try {
        LiteModelSsdMobilenetV11Metadata2 model = LiteModelSsdMobilenetV11Metadata2.newInstance(context);

        // Creates inputs for reference.
        TensorImage image = TensorImage.fromBitmap(bitmap);

        // Runs model inference and gets result.
        LiteModelSsdMobilenetV11Metadata2.Outputs outputs = model.process(image);
        LiteModelSsdMobilenetV11Metadata2.DetectionResult detectionResult = outputs.getDetectionResultList().get(0);

        // Gets result from DetectionResult.
        RectF location = detectionResult.getLocationAsRectF();
        String category = detectionResult.getCategoryAsString();
        float score = detectionResult.getScoreAsFloat();

        // Releases model resources if no longer used.
        model.close();
    } catch(
    IOException e) {
        // TODO Handle the exception
    }


    @Override
    public List<DetectionResult> detectObjects(Bitmap bitmap) {
        List<DetectionResult> results = new ArrayList<>();
        // Perform object detection on the input bitmap image using the TFLite model
        // and return a list of detection results
        return results;
    }
}*/

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.RectF;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.TensorImage;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class ObjectDetector {

    private Interpreter tflite;
    private final int IMAGE_SIZE = 300;

    public ObjectDetector(Context context) throws IOException {
        // Load the TFLite model from the assets folder
        tflite = new Interpreter(loadModelFile(context));
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd("ssd_mobilenet_v1_1_metadata_2.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public List<DetectionResult> detectObjects(Bitmap bitmap) {
        List<DetectionResult> results = new ArrayList<>();

        // Resize the input bitmap image to match the input size of the TFLite model
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true);

        // Convert the input bitmap image to a TensorImage object
        TensorImage inputImage = new TensorImage(DataType.UINT8);
        inputImage.load(resizedBitmap);

        // Run inference on the input TensorImage using the TFLite model
        // and get the detection results
        ByteBuffer outputBuffer = ByteBuffer.allocateDirect(4 * 100);
        tflite.run(inputImage.getBuffer(), outputBuffer);
        List<DetectionResult> detectionResults = getDetectionResults(outputBuffer);

        // Convert the detection results from the normalized coordinates (0 to 1)
        // to the pixel coordinates of the input image
        for (DetectionResult detectionResult : detectionResults) {
            RectF location = detectionResult.getLocation();
            location.left *= bitmap.getWidth();
            location.right *= bitmap.getWidth();
            location.top *= bitmap.getHeight();
            location.bottom *= bitmap.getHeight();
            detectionResult.setLocation(location);
            results.add(detectionResult);
        }

        return results;
    }

    private List<DetectionResult> getDetectionResults(ByteBuffer outputBuffer) {
        List<DetectionResult> detectionResults = new ArrayList<>();
        float[] output = new float[outputBuffer.limit() / 4];
        outputBuffer.asFloatBuffer().get(output);
        for (int i = 0; i < output.length; i += 6) {
            float score = output[i];
            RectF rect = new RectF(output[i + 1], output[i + 2], output[i + 3], output[i + 4]);
            int classId = (int) output[i + 5];
            DetectionResult detectionResult = new DetectionResult(rect, classId, score);
            detectionResults.add(detectionResult);
        }
        return detectionResults;
    }
}

