package com.example.iamshweta.recipegenerator;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final int PIXEL_WIDTH = 32;
    static final int REQUEST_IMAGE_CAPTURE=1;
    private ImageView imageView;
    private TextView tvResult;
    private Uri file;
    private final static String SHARED_PREF_ID = "recipeGenerator";
    private Bitmap photo;
    //Bitmap photo = Bitmap.create(drawable.getBitmap());
    private NumberClassifier numberClassifier;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button captureButton= (Button) findViewById(R.id.captureButton);
        imageView = (ImageView) findViewById(R.id.imageView);

        //Disable the button if no camera
        if (!hasCamera()) {
            captureButton.setEnabled(false);
        }

        File path = MainActivity.this.getExternalFilesDir(null);
        numberClassifier = new NumberClassifier("retrained_graph.pb", this);


        Button classifyButton = (Button) findViewById(R.id.button_classify);
        classifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] classification = classifyNumber();

                if (classification==null) {
                    tvResult.setText("Please load model first.");
                } else {
                    tvResult.setText("Detected = " + classification);
                }

            }
        });

        Button clearButton = (Button) findViewById(R.id.button_clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClearClicked();
            }
        });

        tvResult = (TextView) findViewById(R.id.text_result);

    }

    private static File getOutputMediaFile(){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CameraDemo");

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                return null;
            }
        }

        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG.jpg");
    }
    //check if the user has a camera
    private boolean hasCamera(){
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    //launching the camera
    public void launchCamera(View view){
        //file = Uri.fromFile(getOutputMediaFile());
        Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //intent.putExtra(MediaStore.EXTRA_OUTPUT, file);
        //Take a picture and pass results along to on ActivityResult
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);

    }

    //if you want to return the image taken

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode==RESULT_OK){
            // get the photo
            Bundle extras= data.getExtras();
            photo=(Bitmap) extras.get("data");

            imageView.setImageBitmap(photo);
            //imageView.setImageURI(file);




        }
    }
    private int[] classifyNumber() {

        // Classify.
        int[] pixels = new int[2048];
        photo.getPixels(pixels,0,32,0,0,32,32);
        //bm.getPixels(pixels,0,32,1,1,32,32);
        int[] idx = new int[10];
        if (numberClassifier != null) {
            idx = numberClassifier.classify(pixels);
        }

        return idx;
    }
    private void onClearClicked() {

        imageView.clearFocus();

        tvResult.setText("");
    }

}
