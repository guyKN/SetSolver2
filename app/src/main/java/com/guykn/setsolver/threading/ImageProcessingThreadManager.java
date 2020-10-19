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
import com.guykn.setsolver.imageprocessing.ImageTypeConverter;
import com.guykn.setsolver.imageprocessing.detect.CardDetector;
import com.guykn.setsolver.imageprocessing.Config;
import com.guykn.setsolver.imageprocessing.detect.ContourCardDetectorWrapper;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.Objects;


public class ImageProcessingThreadManager {
    public static String TAG = "ImageProcessingThreadManager";
    //todo: make sure everything's thread safe
    //todo: send config through the Handler rather than the constrocutor
    //todo: make fields final
    private Thread mImageProcessingThread;
    private Context context;
    private ThreadSynchronized<Boolean> threadBusy = new ThreadSynchronized<>();

    private Handler workerThreadToUiHandler;
    private volatile Handler uiToWorkerThreadHandler;
    private final Callback callback;


    public ImageProcessingThreadManager(Context context, Callback callback){
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
                            getLooper().quit();
                            return;
                        }

                        Mat originalImageMat = message.getMat();
                        Config config = message.getConfig();
                        ImageProcessingAction action = message.getAction();

<<<<<<< HEAD

                        RotatedRectangleList result = imageProcessingManager.getCardPositions(
                                originalImageMat, config, context);

                        //result.trimToSize(1); //todo: remove
=======
                        CardDetector detector = new ContourCardDetectorWrapper.ContourBasedCardDetector(
                                originalImageMat, config, context);
                        ImageProcessingManger manger = new ImageProcessingManger(detector, null);
                        RotatedRectangleList result = manger.getCardPositions();
                        //result.trimToSize(1);
>>>>>>> parent of 6c8959f... Scale down images with a consistent aspect ratio
                        //save to the gallery if necessary
                        if(config.shouldSaveToGallery.shouldSaveToGallery(action)){
                            result.saveToGallery(new ImageFileManager(context), originalImageMat);
                        }

                        //sends the result to the UI thread
                        new WorkerThreadToUiSuccessMessage(result).send();

                    }catch (Exception e){
                        e.printStackTrace();
                        new WorkerThreadToUiErrorMessage(e).send();
                    }finally {
                        threadBusy.set(false);
                    }
                }
            };
            Log.i(MainActivity.TAG, "Handler Set Up");
            Looper.loop();
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
        private final Config config;

        protected UiToWorkerThreadMessage(@NonNull ImageProcessingAction action,
                                          @NonNull Config config){
            this.action = action;
            this.config = config;
        }

        @NonNull
        public ImageProcessingAction getAction(){
            return action;
        }
        @NonNull
        public Config getConfig(){
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
        public UiToWorkerThreadBitmapMessage(@NonNull ImageProcessingAction action, @NonNull Config config, @NonNull Bitmap bitmap){
            super(action, config);
            this.bitmap = bitmap;
        }
        @NonNull
        @Override
        public Mat getMat(){
            return ImageTypeConverter.bitmapToMat(bitmap);
        }
    }

    protected class UiToWorkerThreadByteArrayMessage extends UiToWorkerThreadMessage{
        @NonNull
        private final byte[] binaryData;
        private final int width;
        private final int height;
        public UiToWorkerThreadByteArrayMessage(@NonNull ImageProcessingAction action, @NonNull Config config, @NonNull byte[] binaryData, int width, int height){
            super(action, config);
            this.binaryData = binaryData;
            this.width = width;
            this.height = height;
        }
        @Override
        @NonNull
        public Mat getMat(){
            return ImageTypeConverter.nv21ToRgbMat(binaryData, width, height);
        }
    }

    protected final class UiToWorkerThreadFileMessage extends UiToWorkerThreadMessage{
        @NonNull
        private final String filePath;
        public UiToWorkerThreadFileMessage(@NonNull ImageProcessingAction action,
                                           @NonNull Config config, @NonNull String filePath){
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


    public interface Callback {
        public void onImageProcessingSuccess(DrawableOnCanvas drawable);
        public void onImageProcessingFailure(Exception exception);
    }

}
