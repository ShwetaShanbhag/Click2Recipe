package io.interactionlab.tutorial_mobile_example;

import android.content.Context;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.InputStream;


/**
 * This class demonstrates the use of the inference interface of TensorFlow.
 * The model (protobuf file) can either be loaded from the assets folder of the APK, or using an InputStream.
 */
public class RnnGenerator {
    private TensorFlowInferenceInterface inferenceInterface;

    public RnnGenerator(String modelPath, Context context) {
        // Loading model from assets folder.
        inferenceInterface = new TensorFlowInferenceInterface(context.getAssets(), modelPath);
    }

    public RnnGenerator(InputStream inputStream) {
        // Loading the model from an input stream.
        inferenceInterface = new TensorFlowInferenceInterface(inputStream);
    }

    public int[] generate(int[] pixels) {
        // Node Names
        String inputName = "DecodeJpeg/contents:0";
        String outputName = "final_result:0";

        // Define output nodes
        String[] outputNodes = new String[]{outputName};
        int[] outputs = new int[8150];

        // Feed image into the model and fetch the results.
        inferenceInterface.feed(inputName , pixels,1,2048);
        inferenceInterface.run(outputNodes);
        inferenceInterface.fetch(outputName, outputs);

        // Convert one-hot encoded result to an int (= detected class)
        float max = Float.MIN_VALUE;
        int[] idx=new int[10];
        int out=0;
        for (int i = 0; i < 10/*outputs.length*/; i++) {
            if (outputs[i] > max) {
                idx[out] = outputs[i];
                out=out+1;
            }
        }

        return idx;
    }

}
