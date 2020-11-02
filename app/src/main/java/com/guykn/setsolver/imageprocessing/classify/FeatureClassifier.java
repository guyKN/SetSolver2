package com.guykn.setsolver.imageprocessing.classify;

import android.content.Context;
import android.util.Log;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.Closeable;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.Arrays;

public abstract class FeatureClassifier implements Closeable {

    //todo: add post-processor

    private static final String TAG = MLCardClassifier.TAG;

    private static final int NUM_CATEGORIES = 3; //todo: make subclass provide this
    public static final float POST_PROCESSING_SCALE_FACTOR = 0.00392156862f;
    private final ImageProcessingConfig config;
    private final Interpreter tflite;
    private final TensorBuffer probabilityBuffer;


    public FeatureClassifier(Context context, ImageProcessingConfig config) throws IOException {
        this.config = config;

        Interpreter.Options options = new Interpreter.Options();
        MappedByteBuffer buffer = FileUtil.loadMappedFile(context, getModelPath());
        tflite = new Interpreter(buffer, options);

        probabilityBuffer = TensorBuffer.createFixedSize(
                new int[]{1, getNumCategories()},
                getDataType()
        );
    }

    public ClassificationResult classify(TensorImage image) {
        Log.d(MLCardClassifier.TAG, "w: " + image.getWidth() + ", h: " + image.getHeight());
        Log.d(TAG, "datatype: " + tflite.getInputTensor(0).dataType().name());
        tflite.run(image.getBuffer(), probabilityBuffer.getBuffer().rewind());
        Log.d(MLCardClassifier.TAG, Arrays.toString(probabilityBuffer.getFloatArray()));
        return ClassificationResult.fromProbabilityBuffer(probabilityBuffer, POST_PROCESSING_SCALE_FACTOR);
    }


    protected DataType getDataType(){
        return tflite.getInputTensor(0).dataType();
    }

    protected abstract int getNumCategories();
    protected abstract String getModelPath();


    @Override
    public void close() throws IOException {
        tflite.close();
        //todo: any more?
    }
}