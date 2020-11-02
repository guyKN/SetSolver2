package com.guykn.setsolver.imageprocessing.classify.depracated;

import android.content.Context;
import android.graphics.Bitmap;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.classify.ClassificationResult;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.List;

public class OldFeatureClassifier {
    //todo: maybe use anodroid studio's built in tensorflow lite models
    //todo: make this implement an interface for better functionality, and then pass that interface into MachineLearningCardClassifier
    private static final int MOBILENET_IMAGE_SIZE =224;
    private static final DataType INPUT_DATA_TYPE = DataType.FLOAT32;
    private static final String MODEL_FILE_NAME = "Models/Old/Color/model.tflite";
    private static final String LABELS_FILE_NAME = "labels.txt";
    public static final int NUM_CATEGORIES =3;

    private Context context;
    private ImageProcessingConfig config;
    private String modelFilePath;
    private String labelsFilePath;
    private ImageProcessor imageProcessor;
    private Interpreter interpreter;
    TensorProcessor probabilityProcessor;
    List<String> labelValues;

    public OldFeatureClassifier(Context context, ImageProcessingConfig config, String modelDirectory) throws IOException {
        this.context = context;
        this.config = config;
        this.modelFilePath = modelDirectory + MODEL_FILE_NAME;
        this.labelsFilePath = modelDirectory + LABELS_FILE_NAME;
        initClassifier();
    }
    private void initClassifier() throws IOException{
        Interpreter.Options options = new Interpreter.Options();
        imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(MOBILENET_IMAGE_SIZE, MOBILENET_IMAGE_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                .build();
        MappedByteBuffer buffer = FileUtil.loadMappedFile(context, modelFilePath);
        interpreter = new Interpreter(buffer, options);
        probabilityProcessor = new TensorProcessor.Builder().add(new NormalizeOp(0, 1)).build();
    }

    //todo: convert to a tensorImage earlier, in order to be more efficent
    ClassificationResult classify(Bitmap image){
        TensorBuffer probabilityBuffer = TensorBuffer.createFixedSize(new int[]{1, NUM_CATEGORIES}, INPUT_DATA_TYPE);
        TensorImage tImage = new TensorImage(INPUT_DATA_TYPE);
        tImage.load(image);
        tImage = imageProcessor.process(tImage);
        interpreter.run(tImage.getBuffer(), probabilityBuffer.getBuffer());
        return ClassificationResult.fromProbabilityBuffer(probabilityBuffer, 1);
    }

}
