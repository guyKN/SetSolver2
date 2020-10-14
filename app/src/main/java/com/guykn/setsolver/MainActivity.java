package com.guykn.setsolver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.guykn.setsolver.ImageProcessingThreadManager.DisplayImageMessage;
import com.guykn.setsolver.drawing.DrawableOnCanvas;
import com.guykn.setsolver.imageprocessing.detect.ContourBasedCardDetector;
import com.guykn.setsolver.unittest.GenericRotatedRectangleTest;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.IOException;

import static com.guykn.setsolver.imageprocessing.detect.ContourBasedCardDetector.Config.getDefaultConfig;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_CROP_PHOTO = 2;
    public static final String TAG = "MainActivity1";

    private File currentImageFile;
    private File currentCroppedImageFile;

    private ImageView originalImageDisplay;
    private EditText minThresholdPicker;
    private EditText ratioPicker;
    private EditText blurRadiusPicker;
    private ProgressBar imageLoadingProgressBar;
    private Button recalculate;
    private TextView messageDisplay;

    private ImageProcessingThreadManager imThreadManager;
    private Handler imageProcessingHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg){
            int messageCode = msg.what;
            switch (messageCode){
                case ImageProcessingThreadManager.MessageConstants.MESSAGE_SUCCESS:
                    Log.i(TAG, "msg has been recieved.");
                    DisplayImageMessage message = (DisplayImageMessage) msg.obj;
                    DrawableOnCanvas drawable = message.drawable;
                    Bitmap bitmapToDisplay = message.bitmap;
                    String stringToDisplay = message.stringToDisplay;
                    try {
                        recalculate.setVisibility(View.INVISIBLE);
                        imageLoadingProgressBar.setVisibility(View.GONE);
                        originalImageDisplay.setAlpha(1.0f);
                        if(drawable != null && bitmapToDisplay != null) {
                            Canvas canvas = new Canvas(bitmapToDisplay);

                            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                            paint.setColor(Color.RED);
                            paint.setStyle(Paint.Style.FILL);
                            paint.setStrokeWidth(100f);

                            drawable.drawOnCanvas(canvas, paint);
                            originalImageDisplay.setImageBitmap(bitmapToDisplay);
                        }else{
                            originalImageDisplay.setImageDrawable(null);
                        }

                        if(stringToDisplay !=null){
                            messageDisplay.setText(stringToDisplay);
                        }


                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        showImageErrorMessage();
                        Log.i(TAG,"0");
                    }
                    break;
                case ImageProcessingThreadManager.MessageConstants.MESSAGE_ERROR:
                    showImageErrorMessage();
                    Log.i(TAG,"1");
                    break;

            }
        }
    };

    private TextWatcher showButtonOnEdit = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }
        @Override
        public void afterTextChanged(Editable s) {
            if(currentCroppedImageFile != null && !imThreadManager.isImageProcessingThreadRunning()){
                recalculate.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Hello world");
        runTest();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        originalImageDisplay = findViewById(R.id.original_image_display);
        imageLoadingProgressBar = findViewById(R.id.image_loading_progress_bar);
        messageDisplay = findViewById(R.id.message_display);
        recalculate = findViewById(R.id.recalculate);
        minThresholdPicker = findViewById(R.id.min_threshold);
        ratioPicker = findViewById(R.id.ratio);
        blurRadiusPicker = findViewById(R.id.blur_radius);
        //set the textEdits to allow only numbers.
        minThresholdPicker.setTransformationMethod(new NumericKeyBoardTransformationMethod());
        ratioPicker.setTransformationMethod(new NumericKeyBoardTransformationMethod());
        blurRadiusPicker.setTransformationMethod(new NumericKeyBoardTransformationMethod());
        //show the update button if the user changes any textbox //todo: make this actually work
        minThresholdPicker.addTextChangedListener(showButtonOnEdit);
        ratioPicker.addTextChangedListener(showButtonOnEdit);
        ratioPicker.addTextChangedListener(showButtonOnEdit);

        imageLoadingProgressBar.setVisibility(View.GONE);//make the progress bar disapear.
        imThreadManager = new ImageProcessingThreadManager(getApplicationContext(),imageProcessingHandler);//initialize the image processing thread

        initOpenCV();
        //todo: save important variables in a bundle
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_TAKE_PHOTO:
                try {
                    Uri inputUri = Uri.fromFile(currentImageFile);
                    ImageFileManager imageFileManager = new ImageFileManager(this);
                    File outputImageFile = imageFileManager.createTempImage();
                    Uri outputUri = Uri.fromFile(outputImageFile);
                    dispatchCropPictureIntent(inputUri, outputUri);
                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                    showImageErrorMessage();
                    Log.i(TAG,"2");

                }
                break;
            case UCrop.REQUEST_CROP:
                if(resultCode == RESULT_OK) {
                    try {
                        Uri resultUri = UCrop.getOutput(data);
                        currentCroppedImageFile = new File(resultUri.getPath());
                        runImageProceedingThread();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        showImageErrorMessage();
                        Log.i(TAG,"3");

                    }
                }
                break;

        }
    }

    @Override
    protected void onDestroy(){
        destroyTempFiles();
        super.onDestroy();
    }

    public void takePhoto(View view){ //called by the UI.
        dispatchTakePictureIntent();
    }

    private void dispatchTakePictureIntent(){ //calls the intent that takes a picture.
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // create the file where the photo will go.
            File photoFile = null;
            try {
                ImageFileManager imageFileManager = new ImageFileManager(this);
                photoFile = imageFileManager.createTempImage();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            if (photoFile != null) {
                currentImageFile = photoFile;
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.guykn.setsolver.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }else{
                showImageErrorMessage();
                Log.i(TAG,"4");

            }

        }else{
            showImageErrorMessage();
            Log.i(TAG,"5");

        }
    }

    private void dispatchCropPictureIntent(Uri inputUri, Uri outputUri){
        UCrop.Options options = new UCrop.Options();
        options.setFreeStyleCropEnabled(true);//allows user to crop without fixed aspect ratio
        options.setAllowedGestures(UCropActivity.ALL,UCropActivity.ALL,UCropActivity.ALL);
        options.setToolbarTitle("Crop down to the cards.");
        UCrop.of(inputUri, outputUri).withOptions(options)
                .start(this);
    }

    private void destroyTempFiles(){
        File storageDir = new File(getCacheDir(), "images");
        if (storageDir.exists()) {
            for (File child : storageDir.listFiles()) {
                child.delete();
            }
        }
    }

    public void runImageProceedingThread(View view){
        runImageProceedingThread();
    }
    private void runImageProceedingThread(){
        ContourBasedCardDetector.Config config =getDefaultConfig();
        //Log.i(TAG, "Min threshold: " + config.threshold1 + "\nMax threshold: " + config.threshold2+ "\nBlur Radius: " + config.gaussianBlurRadius);
        originalImageDisplay.setAlpha(0.5f);
        try {
            displayImage(currentCroppedImageFile.getAbsolutePath(), originalImageDisplay);
        }catch (IOException e){
            e.printStackTrace();
            showImageErrorMessage();
            return;
        }
        imageLoadingProgressBar.setVisibility(View.VISIBLE);
        imThreadManager.runImageProcessingThread(Uri.fromFile(currentCroppedImageFile), config);


    }


    private void initOpenCV(){

        if (OpenCVLoader.initDebug()) {
            Log.i(TAG, "OpenCV initialize success");
        } else {
            Log.i(TAG, "OpenCV initialize failed");
        }

    }

    /* UI Stuff -----------------------------------------------------------------------------------*/

    private void displayImage(String filePath, ImageView display) throws IOException{
        File im = new File(filePath);
        if(im.exists()){
            Glide.with(this).load(im).
            into(display);
        }else{
            throw new IOException("The specified image does not exist.");
        }
    }

    private void showImageErrorMessage(){ // shows a message that says that the creation of the image was unsecsessfull.
        CharSequence text = "Could not take picture!";
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(this, text, duration);
        toast.show();
    }

    //class lets you have a textview with only numbers.
    private class NumericKeyBoardTransformationMethod extends PasswordTransformationMethod {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return source;
        }
    }

    private void runTest(){
        GenericRotatedRectangleTest.test();
    }


}