package com.guykn.setsolver.threading;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.guykn.setsolver.ImageFileManager;
import com.guykn.setsolver.MainActivity;
import com.guykn.setsolver.callback.ImageProcessingThreadCallback;
import com.guykn.setsolver.drawing.DrawableOnCanvas;
import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.imageprocessing.ImageProcessingManger;
import com.guykn.setsolver.imageprocessing.detect.CardDetector;
import com.guykn.setsolver.imageprocessing.detect.ContourBasedCardDetector;

import java.util.Objects;

public class ImageProcessingThreadManager {
    public static String TAG = "ImageProcessingThreadManager";
    public interface WorkerThreadToUiMessageConstants { //todo: eliminate, and instead use a class for all data sent between thread
        int MESSAGE_HANDLER=0;
        int MESSAGE_SUCCESS=1;
        int MESSAGE_ERROR=2;
    }
    public interface UiToWorkerThreadMessageConstants{ //todo: eliminate, and instead use a class for all data sent between thread
        int MESSAGE_PROCESS_BYTE_ARRAY =0;
        int MESSAGE_PROCESS_BITMAP = 1;
        int MESSAGE_PROCESS_IMAGE_FILE=2;
        int MESSAGE_TERMINATE_THREAD =3;
    }

    private Thread mImageProcessingThread;
    private Context context;
    private ImageProcessingThreadCallback callback;

    private Handler workerThreadToUiHandler; //todo: make this thread-safe
    private volatile Handler uiToWorkerThreadHandler; //todo: is the volitile keyword correct here


    public ImageProcessingThreadManager(Context context, ImageProcessingThreadCallback callback, ContourBasedCardDetector.Config config){
        this.context = context;
        this.callback = callback;

        workerThreadToUiHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg){ //todo: seperate message and msg
                ImageProcessingThreadMessage message;
                switch (msg.what){
                    case(WorkerThreadToUiMessageConstants.MESSAGE_SUCCESS):
                        message = (ImageProcessingThreadMessage) msg.obj;
                        callback.onImageProcessingSuccess(message);
                        break;
                    case(WorkerThreadToUiMessageConstants.MESSAGE_ERROR):
                        message = (ImageProcessingThreadMessage) msg.obj;
                        callback.onImageProcessingFailure(message);
                        break;
                }
            }
        };
        mImageProcessingThread = new Thread(new ImageProcessingThread(config));
        mImageProcessingThread.start();

    }

    public void processImage(byte[] originalImageByteArray){
        uiToWorkerThreadHandler.obtainMessage(UiToWorkerThreadMessageConstants.MESSAGE_PROCESS_BYTE_ARRAY, originalImageByteArray).sendToTarget();
    }
    public void processImage(Bitmap originalImageBitmap){
        uiToWorkerThreadHandler.obtainMessage(UiToWorkerThreadMessageConstants.MESSAGE_PROCESS_BITMAP, originalImageBitmap).sendToTarget();
    }
    public void processImage(String imagePath){
        uiToWorkerThreadHandler.obtainMessage(UiToWorkerThreadMessageConstants.MESSAGE_PROCESS_IMAGE_FILE, imagePath).sendToTarget();
    }
    //todo: add utilities, like checking if a thread is running, and terminating the thread


    private class ImageProcessingThread implements Runnable{
        //todo: implement some sort of timeout, and allow interuptions from the outside
        private ContourBasedCardDetector.Config config;
        public ImageProcessingThread(ContourBasedCardDetector.Config config){ //todo: allow config changes from outside
            this.config = config;
        }

        @Override
        public void run() {
            Log.i(MainActivity.TAG, "Handler Set Up");
            //todo: implement looper and handler
            Looper.prepare();
            uiToWorkerThreadHandler = new Handler(Objects.requireNonNull(
                    Looper.myLooper(), "Looper has not yet been set up.")){
                @Override
                public void handleMessage(@NonNull Message msg){
                    Log.i(MainActivity.TAG, "Message Received");
                    Bitmap originalImage;
                    switch(msg.what){
                        case UiToWorkerThreadMessageConstants.MESSAGE_PROCESS_BYTE_ARRAY:
                            byte[] originalImageByteArray = (byte[]) msg.obj;
                            originalImage = ImageProcessingManger.byteArrayToBitmap(originalImageByteArray);
                            break;
                        case UiToWorkerThreadMessageConstants.MESSAGE_PROCESS_BITMAP:
                            originalImage = (Bitmap) msg.obj;
                        case(UiToWorkerThreadMessageConstants.MESSAGE_PROCESS_IMAGE_FILE):
                            String imagePath = (String) msg.obj;
                            originalImage = BitmapFactory.decodeFile(imagePath);
                            break;
                        default:
                            Log.w(MainActivity.TAG, "The specified message value was not found. "); //todo: better warning?
                            return;
                    }
                    processAndSendFromBitmap(originalImage);
                }
            };
            Log.i(MainActivity.TAG, "Handler Set Up");
            Looper.loop();
        }

        private void processAndSendFromBitmap(Bitmap originalImage){
            CardDetector detector = new ContourBasedCardDetector(originalImage, config, context);
            ImageProcessingManger manger = new ImageProcessingManger(detector, null);
            RotatedRectangleList result = manger.getCardPositions(originalImage);
            result.saveToGallery(new ImageFileManager(context), originalImage);
            Bitmap originalImageMutable = ImageProcessingManger.copyBitmapAsMutable(originalImage);
            originalImage.recycle();

            ImageProcessingThreadMessage message = new ImageProcessingThreadMessage();
            message.drawable = result;
            message.bitmap = originalImageMutable;
            Log.d(MainActivity.TAG, "size of result: " + String.valueOf(result.getDrawables().size()));
            Log.d(MainActivity.TAG, "amount of bytes: " + String.valueOf(originalImageMutable.getByteCount()));

            workerThreadToUiHandler.obtainMessage(WorkerThreadToUiMessageConstants.MESSAGE_SUCCESS, message).sendToTarget(); //sends the location of the current image and its classification to the UI thread
        }

    }
    public static class ImageProcessingThreadMessage { //todo: implement enum instead of relying on msg.what
        @Nullable
        public DrawableOnCanvas drawable = null;
        @Nullable
        public Bitmap bitmap = null;
        @Nullable
        public String messageToDisplay = null;
        @Nullable
        public String errorMessage = null;
        @Nullable
        public Throwable error = null;
    }
}
