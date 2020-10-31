package com.guykn.setsolver.threading.deprecated;

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
import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.JavaImageProcessingManager;
import com.guykn.setsolver.imageprocessing.image.ByteArrayImage;
import com.guykn.setsolver.imageprocessing.image.MatImage;
import com.guykn.setsolver.threading.CameraProcessingThread;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.Objects;


public class ImageProcessingThreadManager {
    public static String TAG = "ImageProcessingThreadManager";
    //todo: make sure everything's thread safe
    //todo: make fields final
    //todo: reduce use of context from memory leaks
    //todo: use view model to communicate with activity, rather than callback
    private Thread mImageProcessingThread;
    private Context context;
    private ThreadSynchronized<Boolean> threadBusy = new ThreadSynchronized<>();

    private Handler workerThreadToUiHandler;
    private volatile Handler uiToWorkerThreadHandler;
    private final CameraProcessingThread.Callback callback;


    public ImageProcessingThreadManager(Context context, CameraProcessingThread.Callback callback){
        this.context = context;
        this.callback = callback;
        threadBusy.set(false);

        workerThreadToUiHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message handlerMessage){
                Log.i(MainActivity.TAG, "Message Received in UI thread. ");
                WorkerThreadToUiMessage message =
                        (WorkerThreadToUiMessage) handlerMessage.obj;
                if(message.isError()){
                    Exception exception = message.getException();
                    if(exception == null){
                        exception = new NullPointerException("The given exception was null.");
                    }
                    callback.onImageProcessingFailure(exception);
                }else{
                    DrawableOnCanvas drawable = message.getDrawable();
                    if(drawable == null){
                        callback.onImageProcessingFailure(
                                new NullPointerException("The given drawableOnCanvas was null"));
                        return;
                    }
                    callback.onImageProcessingSuccess(drawable);
                }
            }
        };
        mImageProcessingThread = new Thread(new ImageProcessingThread());
        mImageProcessingThread.start();
    }


    public boolean isThreadBusy(){
        boolean busy = threadBusy.get();
        Log.i(MainActivity.TAG, "Threadbusy: " + busy);
        return threadBusy.get();
    }
    public void terminateThread(){
        //todo: is this thread safe?
        uiToWorkerThreadHandler.getLooper().quit();
    }


    private class ImageProcessingThread implements Runnable{
        //todo: implement timeout
        private JavaImageProcessingManager imageProcessingManager;

        @Override
        public void run() {

            Looper.prepare();
            uiToWorkerThreadHandler = new Handler(Objects.requireNonNull(
                    Looper.myLooper(), "Looper has not yet been set up.")){
                @Override
                public void handleMessage(@NonNull Message handlerMessage){
                    try {
                        threadBusy.set(true);
                        Log.i(MainActivity.TAG, "Message Received in worker thread. ");
                        UiToWorkerThreadMessage message =
                                (UiToWorkerThreadMessage) handlerMessage.obj;

                        if(message.shouldTerminateThread()){
                            //todo: close all resources not needed
                            getLooper().quit();
                            return;
                        }


                        Mat originalImageMat = message.getMat();
                        ImageProcessingConfig config = message.getConfig();
                        ImageProcessingAction action = message.getAction();

                        imageProcessingManager = JavaImageProcessingManager.getDefaultManager(context, config);
                        imageProcessingManager.setImage(new MatImage(originalImageMat));
                        RotatedRectangleList result = imageProcessingManager.getCardPositions();


                        //result.trimToSize(1); //todo: remove
                        //save to the gallery if necessary
                        if(config.shouldSaveToGallery.shouldSaveToGallery(action)){
                            imageProcessingManager.saveCardImagesToGallery(
                                    new ImageFileManager(context));
                        }

                        //sends the result to the UI thread
                        new WorkerThreadToUiSuccessMessage(result).send();

                    }catch (Exception e){
                        //e.printStackTrace();
                        throw e;
                        //new WorkerThreadToUiErrorMessage(e).send();
                    }finally {
                        threadBusy.set(false);
                    }
                }
            };
            Log.i(MainActivity.TAG, "Handler Set Up");
            Looper.loop(); //continously listens for messages, and upon recieving a message, acts based on the handler
            imageProcessingManager.finish();
        }

    }

    private abstract class WorkerThreadToUiMessage {
        public void send(){
            send(workerThreadToUiHandler);
        }
        private void send(Handler handler){
            handler.obtainMessage(0, this).sendToTarget();
        }
        abstract boolean isError();
        @Nullable
        abstract DrawableOnCanvas getDrawable();
        @Nullable
        abstract Exception getException();

    }

    private class WorkerThreadToUiSuccessMessage extends WorkerThreadToUiMessage{
        private final DrawableOnCanvas drawable;
        public WorkerThreadToUiSuccessMessage(DrawableOnCanvas drawable){
            this.drawable = drawable;
        }
        @Override
        boolean isError(){
            return false;
        }

        @Nullable
        @Override
        DrawableOnCanvas getDrawable() {
            return drawable;
        }

        @Nullable
        @Override
        Exception getException() {
            return null;
        }
    }
    private class WorkerThreadToUiErrorMessage extends WorkerThreadToUiMessage{
        private final Exception exception;
        public WorkerThreadToUiErrorMessage (Exception exception){
            this.exception = exception;
        }
        @Override
        boolean isError() {
            return true;
        }

        @Nullable
        @Override
        DrawableOnCanvas getDrawable() {
            return null;
        }

        @Nullable
        @Override
        Exception getException() {
            return exception;
        }
    }

    public enum ImageProcessingAction {
        DETECT_CARDS,
        DETECT_AND_CLASSIFY_CARDS
    }

    private abstract class UiToWorkerThreadMessage {
        @NonNull
        private final ImageProcessingAction action;
        @NonNull
        private final ImageProcessingConfig config;

        protected UiToWorkerThreadMessage(@NonNull ImageProcessingAction action,
                                          @NonNull ImageProcessingConfig config){
            this.action = action;
            this.config = config;
        }

        @NonNull
        public ImageProcessingAction getAction(){
            return action;
        }
        @NonNull
        public ImageProcessingConfig getConfig(){
            return config;
        }

        public boolean shouldTerminateThread(){
            return false;
        }
        public void send(){
            send(uiToWorkerThreadHandler);
        }
        private void send(Handler handler){
            handler.obtainMessage(0, this).sendToTarget();
        }
        @NonNull
        public abstract Mat getMat();

    }

    protected final class UiToWorkerThreadBitmapMessage extends UiToWorkerThreadMessage{
        @NonNull
        private final Bitmap bitmap;
        public UiToWorkerThreadBitmapMessage(@NonNull ImageProcessingAction action, @NonNull ImageProcessingConfig config, @NonNull Bitmap bitmap){
            super(action, config);
            this.bitmap = bitmap;
        }
        @NonNull
        @Override
        public Mat getMat(){
            Mat mat = new Mat();
            Utils.bitmapToMat(bitmap, mat);
            return mat;
        }
    }

    protected class UiToWorkerThreadByteArrayMessage extends UiToWorkerThreadMessage{
        @NonNull
        private final byte[] binaryData;
        private final int width;
        private final int height;
        public UiToWorkerThreadByteArrayMessage(@NonNull ImageProcessingAction action, @NonNull ImageProcessingConfig config, @NonNull byte[] binaryData, int width, int height){
            super(action, config);
            this.binaryData = binaryData;
            this.width = width;
            this.height = height;
        }
        @Override
        @NonNull
        public Mat getMat(){
            ByteArrayImage image = new ByteArrayImage(binaryData, width, height);
            return image.toMat();
        }
    }

    protected final class UiToWorkerThreadFileMessage extends UiToWorkerThreadMessage{
        @NonNull
        private final String filePath;
        public UiToWorkerThreadFileMessage(@NonNull ImageProcessingAction action,
                                           @NonNull ImageProcessingConfig config, @NonNull String filePath){
            super(action, config);
            this.filePath = filePath;
        }
        @NonNull
        @Override
        public Mat getMat(){
            return Imgcodecs.imread(filePath);
        }
    }
    //It's ok for things to be null in this class, since they won't be accessed ever.
    @SuppressWarnings("NullableProblems")
    protected final class UiToWorkerThreadTerminationMessage extends UiToWorkerThreadMessage{
        public UiToWorkerThreadTerminationMessage(){
            //noinspection ConstantConditions
            super(null, null);
        }

        @Override
        public boolean shouldTerminateThread() {
            return true;
        }
        @Override
        public Mat getMat() {
            return null;
        }
    }


}