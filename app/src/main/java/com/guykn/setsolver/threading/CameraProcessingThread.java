package com.guykn.setsolver.threading;

import android.content.Context;
import android.hardware.Camera;

import androidx.annotation.Nullable;

import com.guykn.setsolver.FpsCounter;
import com.guykn.setsolver.ImageFileManager;
import com.guykn.setsolver.drawing.DrawableOnCanvas;
import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.ImageProcessingManager;
import com.guykn.setsolver.imageprocessing.image.ByteArrayImage;
import com.guykn.setsolver.imageprocessing.image.MatImage;

import org.opencv.core.Mat;

@SuppressWarnings("deprecation")
public class CameraProcessingThread extends CameraPreviewThread {

    //todo: use a byte buffer in the callback for better performance

    private final ImageProcessingManager processingManager;
    private final Callback callback;

    public CameraProcessingThread(@Nullable ImageFileManager fileManager,
            @Nullable FpsCounter fpsCounter,
            ImageProcessingManager processingManager,
            Callback callback) {
        super(fileManager, fpsCounter);
        this.processingManager = processingManager;
        this.callback = callback;
    }

    public CameraProcessingThread(Context context,
                                  @Nullable FpsCounter fpsCounter,
                                  ImageProcessingManager processingManager,
                                  Callback callback) {
        super(context, fpsCounter);
        this.processingManager = processingManager;
        this.callback = callback;
    }



    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        super.onPreviewFrame(data, camera);
        Camera.Size imageSize = camera.getParameters().getPreviewSize();
        int width = imageSize.width;
        int height = imageSize.height;

        ByteArrayImage byteImage = new ByteArrayImage(data, width, height);
        processingManager.setImage(byteImage);
        RotatedRectangleList cardPositions = processingManager.getCardPositions();
        callback.onImageProcessingSuccess(cardPositions);
        processingManager.finish();
    }

    @Override
    protected void startCamera(){
        super.startCamera();
        getCamera().setPreviewCallback(this);
    }

    @Override
    protected void onTerminateThread(){
        processingManager.finish();
        super.onTerminateThread();
    }

    public void setConfig(ImageProcessingConfig config){
        processingManager.setConfig(config);
    }

    @Override
    public void pictureTakenMatAction(Mat mat){
        super.pictureTakenMatAction(mat);
        MatImage matImage = new MatImage(mat);

        processingManager.setImage(matImage);
        processingManager.getCardPositions();
        processingManager.saveCardImagesToGallery(getFileManager());
        processingManager.finish();
    }

    public interface Callback {
        public void onImageProcessingSuccess(DrawableOnCanvas drawable);
        public void onImageProcessingFailure(Exception exception);
    }
}
