/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

/////////////Copy pasted From Tensorflow
// https://github.com/tensorflow/examples/tree/master/lite/examples/image_classification/android

package com.guykn.setsolver.imageprocessing.classify.tflite.copypasta;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Log;

import com.guykn.setsolver.imageprocessing.classify.ClassificationResult;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.nnapi.NnApiDelegate;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeOp.ResizeMethod;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.Closeable;
import java.io.IOException;
import java.nio.MappedByteBuffer;

/**
 * A classifier specialized to label images using TensorFlow Lite.
 */
public abstract class Classifier implements Closeable {
    public static final String TAG = "ClassifierWithSupport";
    private final DataType imageDataType;

    public enum ModelType {
        FLOAT {

            /** Float MobileNet requires additional normalization of the used input. */
            private static final float IMAGE_MEAN = 127.5f;
            private static final float IMAGE_STD = 127.5f;

            /**
             * Float model does not need dequantization in the post-processing. Setting mean and std as 0.0f
             * and 1.0f, repectively, to bypass the normalization.
             */
            private static final float PROBABILITY_MEAN = 0.0f;
            private static final float PROBABILITY_STD = 1.0f;

            @Override
            public TensorOperator getPreprocessNormalizeOp() {
                return new NormalizeOp(IMAGE_MEAN, IMAGE_STD);
            }

            @Override
            public TensorOperator getPostprocessNormalizeOp() {
                return new NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD);
            }
        },

        QUANTIZED {
            /**
             * The quantized model does not require normalization, thus set mean as 0.0f, and std as 1.0f to
             * bypass the normalization.
             */
            private static final float IMAGE_MEAN = 0.0f;
            private static final float IMAGE_STD = 1.0f;

            /** Quantized MobileNet requires additional dequantization to the output probability. */
            private static final float PROBABILITY_MEAN = 0.0f;
            private static final float PROBABILITY_STD = 255.0f;

            @Override
            public TensorOperator getPreprocessNormalizeOp() {
                return new NormalizeOp(IMAGE_MEAN, IMAGE_STD);
            }

            @Override
            public TensorOperator getPostprocessNormalizeOp() {
                return new NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD);
            }
        };

        abstract TensorOperator getPreprocessNormalizeOp();

        abstract TensorOperator getPostprocessNormalizeOp();
    }

    /**
     * The runtime device type used for executing classification.
     */
    public enum Device {
        CPU,
        NNAPI
    }

    /**
     * Image size along the x axis.
     */
    private final int imageSizeX;

    /**
     * Image size along the y axis.
     */
    private final int imageSizeY;

    /**
     * Optional NNAPI delegate for accleration.
     */
    private NnApiDelegate nnApiDelegate = null;

    /**
     * An instance of the driver class to run model inference with Tensorflow Lite.
     */
    protected Interpreter tflite;

    /**
     * Options for configuring the Interpreter.
     */
    private final Interpreter.Options tfliteOptions = new Interpreter.Options();



    /**
     * Output probability TensorBuffer.
     */
    private final TensorBuffer outputProbabilityBuffer;

    /**
     * Processer to apply post processing of the output probability.
     */
    private final TensorProcessor probabilityProcessor;


    /**
     * Initializes a {@code Classifier}.
     */
    protected Classifier(Context context, Device device, int numThreads) throws IOException {
        MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(context, getModelPath());
        switch (device) {
            case NNAPI:
                nnApiDelegate = new NnApiDelegate();
                tfliteOptions.addDelegate(nnApiDelegate);
                break;
            case CPU:
                break;
        }
        tfliteOptions.setNumThreads(numThreads);
        tflite = new Interpreter(tfliteModel, tfliteOptions);

        // Reads type and shape of input and output tensors, respectively.
        int imageTensorIndex = 0;
        int[] imageShape = tflite.getInputTensor(imageTensorIndex).shape(); // {1, height, width, 3}
        imageSizeY = imageShape[1];
        imageSizeX = imageShape[2];
        imageDataType = tflite.getInputTensor(imageTensorIndex).dataType();
        int probabilityTensorIndex = 0;
        int[] probabilityShape =
                tflite.getOutputTensor(probabilityTensorIndex).shape(); // {1, NUM_CLASSES}
        DataType probabilityDataType = tflite.getOutputTensor(probabilityTensorIndex).dataType();

        // Creates the input tensor.

        // Creates the output tensor and its processor.
        outputProbabilityBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType);

        // Creates the post processor for the output probability.
        probabilityProcessor = new TensorProcessor.Builder().add(getPostprocessNormalizeOp()).build();

    }

    /**
     * Runs inference and returns the classification results.
     */
    public ClassificationResult classify(final Bitmap bitmap) {
        // Logs this method so that it can be analyzed with systrace.
        Trace.beginSection("recognizeImage");

        TensorImage inputImageBuffer = loadImage(bitmap);
        return classify(inputImageBuffer);
    }
    //todo: add overload to classify a tImage

    public ClassificationResult classify(final TensorImage inputImageBuffer){
        Trace.beginSection("runInference");
        long startTimeForReference = SystemClock.uptimeMillis();

        tflite.run(inputImageBuffer.getBuffer().rewind(),
                outputProbabilityBuffer.getBuffer().rewind());
        probabilityProcessor.process(outputProbabilityBuffer);

        long endTimeForReference = SystemClock.uptimeMillis();
        Trace.endSection();

        Log.v(TAG, "Timecost to run model inference: " +
                (endTimeForReference - startTimeForReference));

        return ClassificationResult.fromProbabilityBuffer(outputProbabilityBuffer);
    }

    /**
     * Closes the interpreter and model to release resources.
     */
    @Override
    public void close()  {
        if (tflite != null) {
            tflite.close();
            tflite = null;
        }
        if (nnApiDelegate != null) {
            nnApiDelegate.close();
            nnApiDelegate = null;
        }
    }

    /**
     * Get the image size along the x axis.
     */
    public int getImageSizeX() {
        return imageSizeX;
    }

    /**
     * Get the image size along the y axis.
     */
    public int getImageSizeY() {
        return imageSizeY;
    }

    /**
     * Loads input image, and applies preprocessing.
     */
    public TensorImage loadImage(final Bitmap bitmap) {

        Trace.beginSection("loadImage");
        long startTimeForLoadImage = SystemClock.uptimeMillis();

        // Loads bitmap into a TensorImage.
        TensorImage inputImageBuffer = new TensorImage(imageDataType);
        inputImageBuffer.load(bitmap);

        // Creates processor for the TensorImage.
        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        //todo: is nearestNeighbor better a better resize method
                        //todo: maybe create the imageProcessor only once
                        .add(new ResizeOp(imageSizeY, imageSizeX, ResizeMethod.BILINEAR))
                        .add(getPreprocessNormalizeOp())
                        .build();
        long endTimeForLoadImage = SystemClock.uptimeMillis();
        Trace.endSection();
        Log.v(TAG, "Timecost to load the image: " + (endTimeForLoadImage - startTimeForLoadImage));

        return imageProcessor.process(inputImageBuffer);
    }

    protected abstract ModelType getModelType();

    /**
     * Gets the name of the model file stored in Assets.
     */
    protected abstract String getModelPath();

    /**
     * Gets the TensorOperator to nomalize the input image in preprocessing.
     */
    protected TensorOperator getPreprocessNormalizeOp() {
        return getModelType().getPreprocessNormalizeOp();
    }

    /**
     * Gets the TensorOperator to dequantize the output probability in post processing.
     *
     * <p>For quantized model, we need de-quantize the prediction with NormalizeOp (as they are all
     * essentially linear transformation). For float model, de-quantize is not required. But to
     * uniform the API, de-quantize is added to float model too. Mean and std are set to 0.0f and
     * 1.0f, respectively.
     */
    protected TensorOperator getPostprocessNormalizeOp() {
        return getModelType().getPostprocessNormalizeOp();
    }
}