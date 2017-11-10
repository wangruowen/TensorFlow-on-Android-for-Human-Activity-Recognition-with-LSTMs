package io.github.introml.activityrecognition;

import android.content.Context;
import android.util.Log;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.Arrays;


public class TensorFlowClassifier {
    private static final String TAG = "HumanActivityRecognition." + TensorFlowClassifier.class.getSimpleName();
    static {
        System.loadLibrary("tensorflow_inference");
    }

//    private static final String MODEL_FILE = "file:///android_asset/frozen_model.pb";
    private static final String MODEL_FILE = "file:///android_asset/frozen_har.pb";
    private static final String INPUT_NODE = "input";
    private static final String[] OUTPUT_NODES = {"y_"};
    private static final String OUTPUT_NODE = "y_";
    private static final long[] INPUT_SIZE = {1, 200, 3};
    private static final int OUTPUT_SIZE = 6;

    private static TensorFlowInferenceInterface sInferenceInterface = null;

    public TensorFlowClassifier(final Context context) {
        if (sInferenceInterface == null) {
            Log.d(TAG, "Init and load tensorflow model");
            sInferenceInterface = new TensorFlowInferenceInterface(context.getAssets(), MODEL_FILE);
        }
    }

    public float[] predictProbabilities(float[] data) {
        float[] result = new float[OUTPUT_SIZE];
        sInferenceInterface.feed(INPUT_NODE, data, INPUT_SIZE);
        sInferenceInterface.run(OUTPUT_NODES);
        sInferenceInterface.fetch(OUTPUT_NODE, result);

        //Downstairs	Jogging	  Sitting	Standing	Upstairs	Walking
        Log.d(TAG, String.format("Let's predict, result = %s", Arrays.toString(result)));
        return result;
    }
}
