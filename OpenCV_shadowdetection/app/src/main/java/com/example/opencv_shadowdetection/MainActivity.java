package com.example.opencv_shadowdetection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.Core;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    private static final int SELECT_IMAGE = 1;
    private ImageView imageView;
    private Bitmap selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        Button btnSelectImage = findViewById(R.id.btnSelectImage);
        Button btnRemoveShadow = findViewById(R.id.btnRemoveShadow);

        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, SELECT_IMAGE);
            }
        });

        btnRemoveShadow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedImage != null) {
                    removeShadow();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                InputStream imageStream = getContentResolver().openInputStream(selectedImageUri);
                selectedImage = BitmapFactory.decodeStream(imageStream);
                imageView.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void removeShadow() {
        // Convert bitmap to Mat
        Mat src = new Mat(selectedImage.getHeight(), selectedImage.getWidth(), CvType.CV_8UC4);
        org.opencv.android.Utils.bitmapToMat(selectedImage, src);

        // Convert to grayscale
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

        // Apply a basic threshold to create a binary image
        Mat binary = new Mat();
        Imgproc.threshold(gray, binary, 100, 255, Imgproc.THRESH_BINARY_INV);

        // Brighten the shadow regions
        Mat brightened = new Mat();
        src.copyTo(brightened);
        src.convertTo(brightened, -1, 1.1, 5); // Increase brightness overall

        // Create a mask from the binary image
        Mat mask = new Mat();
        Core.bitwise_not(binary, mask); // Invert the mask to get shadow regions

        // Apply the mask to brighten shadow areas
        brightened.copyTo(src, mask);

        // Convert Mat back to Bitmap
        Bitmap resultBitmap = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        org.opencv.android.Utils.matToBitmap(src, resultBitmap);

        // Draw "Done" text on the Bitmap
        resultBitmap = drawTextOnBitmap(resultBitmap, "Done", 50, 50, 60, Color.GREEN);

        imageView.setImageBitmap(resultBitmap);

        // Release resources
        src.release();
        gray.release();
        binary.release();
        brightened.release();
        mask.release();
    }

    private Bitmap drawTextOnBitmap(Bitmap bitmap, String text, int x, int y, int textSize, int textColor) {
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        paint.setColor(textColor);
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);
        canvas.drawText(text, x, y, paint);
        return mutableBitmap;
    }

}




