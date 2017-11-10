package io.github.introml.activityrecognition;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.util.Log;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener, TextToSpeech.OnInitListener {
    private static final String TAG = "HumanActivityRecognition";

    private static final int N_SAMPLES = 200;
    private static List<Float> acce_x_list, gyro_x_list;
    private static List<Float> acce_y_list, gyro_y_list;
    private static List<Float> acce_z_list, gyro_z_list;
    private TextView downstairsTextView;

    private TextView joggingTextView;
    private TextView sittingTextView;
    private TextView standingTextView;
    private TextView upstairsTextView;
    private TextView walkingTextView;
    private TextToSpeech textToSpeech;
    private float[] results;
    private TensorFlowClassifier classifier;

    private Sensor mAcceSensor;
    private Sensor mGyroSensor;

    private String[] labels = {"Downstairs", "Jogging", "Sitting", "Standing", "Upstairs", "Walking"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        acce_x_list = new ArrayList<>();
        acce_y_list = new ArrayList<>();
        acce_z_list = new ArrayList<>();
        gyro_x_list = new ArrayList<>();
        gyro_y_list = new ArrayList<>();
        gyro_z_list = new ArrayList<>();

        downstairsTextView = (TextView) findViewById(R.id.downstairs_prob);
        joggingTextView = (TextView) findViewById(R.id.jogging_prob);
        sittingTextView = (TextView) findViewById(R.id.sitting_prob);
        standingTextView = (TextView) findViewById(R.id.standing_prob);
        upstairsTextView = (TextView) findViewById(R.id.upstairs_prob);
        walkingTextView = (TextView) findViewById(R.id.walking_prob);

        classifier = new TensorFlowClassifier(getApplicationContext());

        textToSpeech = new TextToSpeech(this, this);
        textToSpeech.setLanguage(Locale.US);

        mAcceSensor = getSensorManager().getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroSensor = getSensorManager().getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    @Override
    public void onInit(int status) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (results == null || results.length == 0) {
                    return;
                }
                float max = -1;
                int idx = -1;
                for (int i = 0; i < results.length; i++) {
                    if (results[i] > max) {
                        idx = i;
                        max = results[i];
                    }
                }

                textToSpeech.speak(labels[idx], TextToSpeech.QUEUE_ADD, null, Integer.toString(new Random().nextInt()));
            }
        }, 2000, 5000);
    }

    protected void onPause() {
        getSensorManager().unregisterListener(this);
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
        getSensorManager().registerListener(this, mAcceSensor, SensorManager.SENSOR_DELAY_GAME);
        getSensorManager().registerListener(this, mGyroSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float cur_x, cur_y, cur_z;
        cur_x = event.values[0];
        cur_y = event.values[1];
        cur_z = event.values[2];

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                Log.d(TAG, String.format("Accelerometer event: x = %f, y = %f, z = %f", cur_x, cur_y, cur_z));
//        activityPrediction();
                acce_x_list.add(cur_x);
                acce_y_list.add(cur_y);
                acce_z_list.add(cur_z);

                break;
            case Sensor.TYPE_GYROSCOPE:
                Log.d(TAG, String.format("Gyroscope event: x = %f, y = %f, z = %f", cur_x, cur_y, cur_z));
//        activityPrediction();
                gyro_x_list.add(cur_x);
                gyro_y_list.add(cur_y);
                gyro_z_list.add(cur_z);

                break;
            default:
                Log.e(TAG, "Unknown sensor event: " + event.sensor.getStringType());
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void activityPrediction() {
        if (acce_x_list.size() == N_SAMPLES && acce_y_list.size() == N_SAMPLES && acce_z_list.size() == N_SAMPLES) {
            List<Float> data = new ArrayList<>();
            for (int i = 0; i < acce_x_list.size(); i++) {
                data.add(acce_x_list.get(i));
                data.add(acce_y_list.get(i));
                data.add(acce_z_list.get(i));
            }
//            data.addAll(acce_x_list);
//            data.addAll(acce_y_list);
//            data.addAll(acce_z_list);

            results = classifier.predictProbabilities(toFloatArray(data));

            downstairsTextView.setText(Float.toString(round(results[0], 2)));
            joggingTextView.setText(Float.toString(round(results[1], 2)));
            sittingTextView.setText(Float.toString(round(results[2], 2)));
            standingTextView.setText(Float.toString(round(results[3], 2)));
            upstairsTextView.setText(Float.toString(round(results[4], 2)));
            walkingTextView.setText(Float.toString(round(results[5], 2)));

            acce_x_list.clear();
            acce_y_list.clear();
            acce_z_list.clear();
        }
    }

    private float[] toFloatArray(List<Float> list) {
        int i = 0;
        float[] array = new float[list.size()];

        for (Float f : list) {
            array[i++] = (f != null ? f : Float.NaN);
        }
        return array;
    }

    private static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    private SensorManager getSensorManager() {
        return (SensorManager) getSystemService(SENSOR_SERVICE);
    }

}
