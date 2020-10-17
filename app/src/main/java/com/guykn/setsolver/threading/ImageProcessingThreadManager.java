    package com.guykn.setsolver.threading;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.guykn.setsolver.ImageFileManager;
import com.guykn.setsolver.MainActivity;
import com.guykn.setsolver.drawing.DrawableOnCanvas;
import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.imageprocessing.ImageProcessingManger;
import com.guykn.setsolver.imageprocessing.detect.CardDetector;
import com.guykn.setsolver.imageprocessing.Config;
import com.guykn.setsolver.imageprocessing.detect.ContourBasedCardDetector;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.Objects;

public class ImageProcessingThreadManager {
    public static String TAG = "ImageProcessingThreadManager";

    //todo: make sure everything's thread safe
    //todo: send config through the Handler rather than the constrocutor
    private Thread mImageProcessingThread;
    private Context context;
    private Callback callback;
    private ThreadSynchronized<Boolean> threadBusy = new ThreadSynchronized<>();

    private Handler workerThreadToUiHandler;
    private volatile Handler uiToWorkerThreadHandler;


    public ImageProcessingThreadManager(Context context, Callback callback, Config config){
        this.context = context;
        this.callback = callback;
        threadBusy.set(false);

        workerThreadToUiHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg){ //todo: seperate message and msg
                WorkerThreadToUiMessage message = (WorkerThreadToUiMessage) msg.obj;
                switch (msg.what){
                    case(WorkerThreadToUiMessage.MessageCodes.SUCCESS):
                        callback.onImageProcessingSuccess(message);
                        break;
                    case(WorkerThreadToUiMessage.MessageCodes.ERROR):
                        callback.onImageProcessingFailure(message);
                        break;
                }
            }
        };
        mImageProcessingThread = new Thread(new ImageProcessingThread(config));
        mImageProcessingThread.start();

    }

    public void processImage(byte[] data, int width, int height){
        UiToWorkerThreadMessage message = new UiToWorkerThreadMessage();
        message.imageWidth = width;
        message.imageHeight = height;
        message.imageByteArray = data;
        uiToWorkerThreadHandler.obtainMessage(UiToWorkerThreadMessage.MessageCodes.PROCESS_BYTE_ARRAY, message).sendToTarget();
    }
    public void processImage(Bitmap bitmap){
        UiToWorkerThreadMessage message = new UiToWorkerThreadMessage();
        message.imageBitmap = bitmap;
        uiToWorkerThreadHandler.obtainMessage(UiToWorkerThreadMessage.MessageCodes.PROCESS_BITMAP, message).sendToTarget();
    }
    public void processImage(String imagePath){
        UiToWorkerThreadMessage message = new UiToWorkerThreadMessage();
        message.imagePath = imagePath;
        uiToWorkerThreadHandler.obtainMessage(UiToWorkerThreadMessage.MessageCodes.PROCESS_IMAGE_fILE, message).sendToTarget();
    }

    public boolean isThreadBusy(){
        return threadBusy.get();
    }

    //todo: add utilities, like checking if a thread is running, and terminating the thread


    private class ImageProcessingThread implements Runnable{
        //todo: implement some sort of timeout, and allow interuptions from the outside
        private Config config;
        public ImageProcessingThread(Config config){ //todo: allow config changes from outside
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
                    try {
                        threadBusy.set(true);
                        Log.i(MainActivity.TAG, "Message Received");
                        Mat originalImageMat;
                        UiToWorkerThreadMessage message = (UiToWorkerThreadMessage) msg.obj;
                        switch (msg.what) {
                            case UiToWorkerThreadMessage.MessageCodes.PROCESS_BYTE_ARRAY:
                                //Bitmap originalImageConvertedToBitmap = Nv21Image.nv21ToBitmap(rs,originalImageByteArray, 1000,1000);
                                originalImageMat = ImageProcessingManger.nv21ToRgbMat(message.imageByteArray, message.imageWidth, message.imageHeight);
                                break;
                            case UiToWorkerThreadMessage.MessageCodes.PROCESS_BITMAP:
                                originalImageMat = new Mat();
                                Utils.bitmapToMat(message.imageBitmap, originalImageMat);
                                break;
                            case (UiToWorkerThreadMessage.MessageCodes.PROCESS_IMAGE_fILE):
                                originalImageMat = Imgcodecs.imread(message.imagePath);
                                break;
                            default:
                                Log.w(MainActivity.TAG, "The specified message value was not found. "); //todo: better warning?
                                return;
                        }
                        processAndSendFromMat(originalImageMat);
                    }finally {
                        threadBusy.set(false);
                    }
                }
            };
            Log.i(MainActivity.TAG, "Handler Set Up");
            Looper.loop();
        }

        private void processAndSendFromMat(Mat originalImage){
            CardDetector detector = new ContourBasedCardDetector(originalImage, config, context);
            ImageProcessingManger manger = new ImageProcessingManger(detector, null);
            RotatedRectangleList result = manger.getCardPositions();
            result.saveToGallery(new ImageFileManager(context), originalImage);
            //result.saveToGallery(new ImageFileManager(context), originalImage);
            //Bitmap originalImageMutable = ImageProcessingManger.copyBitmapAsMutable(originalImage);
            //originalImage.recycle();

            WorkerThreadToUiMessage message = new WorkerThreadToUiMessage();
            message.drawable = result;
            //message.bitmap = originalImageMutable;
            workerThreadToUiHandler.obtainMessage(WorkerThreadToUiMessage.MessageCodes.SUCCESS, message).sendToTarget(); //sends the location of the current image and its classification to the UI thread
        }

    }




    public static class WorkerThreadToUiMessage {
        public interface MessageCodes {
            int MESSAGE_HANDLER=0;
            int SUCCESS =1;
            int ERROR =2;
        }

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

    public static class UiToWorkerThreadMessage {
        public UiToWorkerThreadMessage(@NonNull Config config) {
            this.config = config;
        }

        public interface MessageCodes{
            int PROCESS_BYTE_ARRAY =0;
            int PROCESS_BITMAP = 1;
            int PROCESS_IMAGE_fILE =2;
            int TERMINATE_THREAD =3;
        }
        @NonNull
        public Config config;
        @Nullable
        public byte[] imageByteArray;
        @Nullable
        public Bitmap imageBitmap;
        @Nullable
        public String imagePath;
        @Nullable
        public Integer imageHeight;
        @Nullable
        public Integer imageWidth;


    }

    public static interface Callback {
        public void onImageProcessingSuccess(WorkerThreadToUiMessage message);
        public void onImageProcessingFailure(WorkerThreadToUiMessage message);
    }

}
