package com.guykn.setsolver.threading;

import com.guykn.setsolver.threading.ImageProcessingThreadManager;

public interface ImageProcessingThreadCallback {
    public void onImageProcessingSuccess(ImageProcessingThreadManager.ImageProcessingThreadMessage message);
    public void onImageProcessingFailure(ImageProcessingThreadManager.ImageProcessingThreadMessage message);
}
