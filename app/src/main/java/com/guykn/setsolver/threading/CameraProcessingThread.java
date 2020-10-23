package com.guykn.setsolver.threading;

import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import com.guykn.setsolver.ImageFileManager;
import com.guykn.setsolver.drawing.DrawableOnCanvas;
import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.ImageProcessingManager;
import com.guykn.setsolver.imageprocessing.image.ByteArrayImage;
import com.guykn.setsolver.ui.main.CameraFragment;
@SuppressWarnings("deprecation")
public class CameraProcessingThread extends CameraPreviewThread implements Camera.PreviewCallback {

    //todo: use a byte buffer in the callback for better performance

    private final ImageProcessingManager processingManager;
    private Callback callback;
    private ImageFileManager fileManager;

    public CameraProcessingThread(ImageProcessingManager processingManager,
                                  Callback callback, ImageFileManager fileManager) {
        super();
        this.processingManager = processingManager;
        this.callback = callback;
        this.fileManager = fileManager;
    }


    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.d(CameraFragment.TAG, "inside of onPreviewFrame");
        Camera.Size imageSize = camera.getParameters().getPreviewSize();
        int width = imageSize.width;
        int height = imageSize.height;

        ByteArrayImage byteImage = new ByteArrayImage(data, width, height);
        processingManager.setImage(byteImage);
        RotatedRectangleList cardPositions = processingManager.getCardPositions();
        cardPositions.printStates();
        callback.onImageProcessingSuccess(cardPositions);

        processingManager.saveOriginalImageToGallery(fileManager);

        processingManager.finish();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int w, int h) {
        super.surfaceChanged(holder, format, w, h);
        getCamera().setPreviewCallback(this);
    }

    public void setConfig(ImageProcessingConfig config){
        processingManager.setConfig(config);
    }

    public interface Callback {
        public void onImageProcessingSuccess(DrawableOnCanvas drawable);
        public void onImageProcessingFailure(Exception exception);
    }
}
