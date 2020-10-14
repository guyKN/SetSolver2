package com.guykn.setsolver;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;

import com.guykn.setsolver.drawing.DrawableOnCanvas;
import com.guykn.setsolver.drawing.GenericRotatedRectangle;
import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.imageprocessing.detect.ContourBasedCardDetector;

import org.opencv.core.Mat;

import java.io.IOException;

public class ImageProcessingThreadManager {
    //todo: move the handler into this class, make it implement an interface, and make MainActivity Implement an interface that this calls.
    public static String TAG = "ImageProcessingThreadManager";
    public interface MessageConstants{
        int MESSAGE_HANDLER=0;
        int MESSAGE_SUCCESS=1;
        int MESSAGE_ERROR=2;
    }

    private Context context;
    private Thread mImageProcessingThread;
    private Handler handler;

    public ImageProcessingThreadManager(Context context, Handler handler){
        this.context = context;
        this.handler = handler;
    }

    public boolean runImageProcessingThread(Uri imageUri, ContourBasedCardDetector.Config config){
        String imagePath = imageUri.getPath();
        return runImageProcessingThread(imagePath, config);
    }
    public boolean runImageProcessingThread(String imagePath, ContourBasedCardDetector.Config config){ // returns true of the thread has been started sucsessfully, returns false if the thread is already running.
        if(!isImageProcessingThreadRunning()) {
            mImageProcessingThread = new Thread(new ImageProcessingThread(imagePath, config));
            mImageProcessingThread.start();
            return true;
        }else{
            return false;
        }
    }
    public boolean isImageProcessingThreadRunning(){
        return mImageProcessingThread != null && !mImageProcessingThread.isAlive();
    }

    private class ImageProcessingThread implements Runnable{
        //todo: implement some sort of timeout, and allow interuptions from the outside
        private String imagePath;
        private ContourBasedCardDetector.Config config;

        public ImageProcessingThread(String imagePath, ContourBasedCardDetector.Config config){
            this.imagePath = imagePath;
            this.config = config;
        }
        @Override
        public void run(){
            try {
                Bitmap originalImage = BitmapFactory.decodeFile(imagePath);
                if(originalImage == null){
                    throw new IOException("The specified file does not exist");
                }
                ContourBasedCardDetector cardDetector = new ContourBasedCardDetector(originalImage, config, context);
                RotatedRectangleList cardLocations = cardDetector.getAllCardRectangles();
                Bitmap bitmapCopy = copyBitmap(originalImage);

                // Saves every cropped image to the phone's media folder
                ImageFileManager fileManager = new ImageFileManager(context);
                for(GenericRotatedRectangle cardRect: cardLocations.getDrawables()){
                    try {
                        Bitmap cropped = cardRect.cropToRect(originalImage);
                        Log.d(TAG, "image cropped");
                        fileManager.saveToGallery(cropped);
                    }catch (IllegalArgumentException e){
                        cardRect.printState();
                        e.printStackTrace();
                    }
                }
                originalImage.recycle();

                DisplayImageMessage message = new DisplayImageMessage();
                message.drawable = cardLocations;
                message.bitmap = bitmapCopy;
                handler.obtainMessage(MessageConstants.MESSAGE_SUCCESS, message).sendToTarget(); //sends the location of the current image and its classification to the UI thread
            }catch (IOException e){
                e.printStackTrace();
                handler.obtainMessage(MessageConstants.MESSAGE_ERROR).sendToTarget(); // sends an error message to the current thread
            }
        }

        @Deprecated
        public String saveImage(Bitmap drawing) throws IOException {
            ImageFileManager imageFileManager = new ImageFileManager(context);
            imageFileManager.createTempImage();
            imageFileManager.saveImage(drawing);
            return imageFileManager.getCurrentImagePath();

        }

        private Bitmap copyBitmap(Bitmap src){
            return src.copy(src.getConfig(), true);
        }



    }
    public static class DisplayImageMessage{
        @Nullable
        public String stringToDisplay = null;
        @Nullable
        public DrawableOnCanvas drawable = null;
        @Nullable
        public Bitmap bitmap = null;
    }
}
