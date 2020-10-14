package com.guykn.setsolver.callback;

public interface ImageProcessingCallback <T> {
    public void onImageProcessingSuccess(T t);
    public void onImageProcessingFailure(T t);
}
