package com.guykn.setsolver.imageprocessing.classify;

import android.content.Context;
import android.util.Log;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.Closeable;
import java.io.IOException;
import java.nio.MappedByteBuffer;

public class FeatureClassifier implements Closeable {

    private static final int NUM_CATEGORIES = 3; //todo: make subclass provide this
    private final ImageProcessingConfig config;
    private Interpreter interpreter;
    private TensorProcessor postProcessor;

    private TensorBuffer probabilityBuffer;


    public FeatureClassifier(Context context, String modelPath,
                             ImageProcessingConfig config) throws IOException {
        this.config = config;
        loadModel(context, modelPath);
    }

    private void loadModel(Context context, String modelPath) throws IOException {
        Interpreter.Options options = new Interpreter.Options();

        MappedByteBuffer buffer = FileUtil.loadMappedFile(context, modelPath);
        interpreter = new Interpreter(buffer, options);

        probabilityBuffer = TensorBuffer.createFixedSize(new int[]{1, NUM_CATEGORIES}, MLCardClassifier.INPUT_DATA_TYPE);

        postProcessor = new TensorProcessor.Builder().
                add(getPostProcessingNormalization())
                .build();
    }

    public ClassificationResult classify(TensorImage image) {
        probabilityBuffer.getBuffer().clear();
        Log.d(MLCardClassifier.TAG, "w: " + image.getWidth() + ", h: " + image.getHeight());
        interpreter.run(image.getBuffer(), probabilityBuffer.getBuffer());
        Log.d(MLCardClassifier.TAG, "all fine");
        return ClassificationResult.fromProbabilityBuffer(probabilityBuffer);
    }



    protected NormalizeOp getPostProcessingNormalization() {
        return new NormalizeOp(MLCardClassifier.PROBABILITY_MEAN, MLCardClassifier.PROBABILITY_STD);
    }


    @Override
    public void close() throws IOException {
        interpreter.close();
    }
}