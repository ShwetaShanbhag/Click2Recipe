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

//For reading file
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


import java.io.File;

import io.interactionlab.tutorial_mobile_example.RnnGenerator;

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

    private RnnGenerator rnnGenerator;


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
                //passing the 1 hot encoding to decoder
                String outLabel = decodelabels(classification);

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
    private String decodelabels(int[] indx) {
        int pos = 0;
        String label = "";
        for (int i = 0 ; i < indx.length; i++)
        {
            if (indx[i] == 1)
            {
                pos = i;
            }
        }

        try {

            File f = new File("src/main/assets/labels.txt");

            BufferedReader b = new BufferedReader(new FileReader(f));
            for ( int x = 0; x < pos ; x++)
            {
                b.readLine();
            }
            label = b.readLine();


        } catch (IOException e) {
            e.printStackTrace();
        }
        return label;

    }
    //private  int[] genRecipe() {

        //String output = rnnGenerator.generate();


    //}
    private void onClearClicked() {

        imageView.clearFocus();

        tvResult.setText("");
    }

}
