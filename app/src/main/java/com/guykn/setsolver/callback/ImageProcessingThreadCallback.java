package com.guykn.setsolver.callback;

import com.guykn.setsolver.ImageProcessingThreadManager;

public interface ImageProcessingThreadCallback {
    public void onImageProcessingSuccess(ImageProcessingThreadManager.ImageProcessingThreadMessage m);
    public void onImageProcessingFailure(ImageProcessingThreadManager.ImageProcessingThreadMessage m);
}
