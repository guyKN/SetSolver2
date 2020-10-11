package com.guykn.setsolver;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;

import com.guykn.setsolver.imageprocessing.SetCardFinder;
import com.guykn.setsolver.set.SetCard;

import org.opencv.core.Mat;

import java.io.IOException;

public class ImageProcessingThreadManager {
    public static String TAG = "ImageProcessingThreadManager";
    public interface MessageConstants{
        int MESSAGE_SUCCESS=0;
        int MESSAGE_ERROR=1;
    }

    private Context context;
    private Thread mImageProcessingThread;
    private Handler handler;

    public ImageProcessingThreadManager(Context context, Handler handler){
        this.context = context;
        this.handler = handler;
    }

    public boolean runImageProcessingThread(Uri imageUri, SetCardFinder.Config config){
        String imagePath = imageUri.getPath();
        return runImageProcessingThread(imagePath, config);
    }
    public boolean runImageProcessingThread(String imagePath, SetCardFinder.Config config){ // returns true of the thread has been started sucsessfully, returns false if the thread is already running.
        if(!isImageProcessingThreadRunning()) {
            Log.i(TAG, "inside of if statement");
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
        private SetCardFinder.Config config;

        public ImageProcessingThread(String imagePath, SetCardFinder.Config config){
            this.imagePath = imagePath;
            this.config = config;
        }
        @Override
        public void run(){
            try {
                SetCardFinder setCardFinder = new SetCardFinder(imagePath, config, context);
                Mat drawing = setCardFinder.getContourBoxDrawing(SetCardFinder.BackgroundType.ORIGINAL_IMAGE);
                SetCard card = setCardFinder.getCardClassifications();

                //saves the image
                ImageFileManager imageFileManager = new ImageFileManager(context);
                imageFileManager.createTempImage();
                imageFileManager.saveImage(drawing);

                DisplayImageMessage message = new DisplayImageMessage();
                message.imagePath = imageFileManager.getCurrentImagePath();
                message.stringToDisplay = card.toString();


                handler.obtainMessage(MessageConstants.MESSAGE_SUCCESS, message).sendToTarget(); //sends the location of the current image and its classification to the UI thread
            }catch (IOException e){
                e.printStackTrace();
                handler.obtainMessage(MessageConstants.MESSAGE_ERROR).sendToTarget(); // sends an error message to the current thread
            }
        }


    }
    public static class DisplayImageMessage{
        @Nullable
        public String imagePath = null;
        @Nullable
        public String stringToDisplay = null;
    }
}
