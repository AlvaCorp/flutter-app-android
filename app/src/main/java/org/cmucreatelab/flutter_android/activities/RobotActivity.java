package org.cmucreatelab.flutter_android.activities;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RotateDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.cmucreatelab.android.melodysmart.models.MelodySmartMessage;
import org.cmucreatelab.flutter_android.R;
import org.cmucreatelab.flutter_android.activities.abstract_activities.BaseSensorReadingActivity;
import org.cmucreatelab.flutter_android.classes.Session;
import org.cmucreatelab.flutter_android.classes.outputs.Output;
import org.cmucreatelab.flutter_android.classes.outputs.Servo;
import org.cmucreatelab.flutter_android.classes.outputs.Speaker;
import org.cmucreatelab.flutter_android.classes.outputs.TriColorLed;
import org.cmucreatelab.flutter_android.classes.relationships.Constant;
import org.cmucreatelab.flutter_android.classes.sensors.NoSensor;
import org.cmucreatelab.flutter_android.classes.sensors.Sensor;
import org.cmucreatelab.flutter_android.classes.settings.Settings;
import org.cmucreatelab.flutter_android.classes.settings.SettingsAmplitude;
import org.cmucreatelab.flutter_android.classes.settings.SettingsChange;
import org.cmucreatelab.flutter_android.classes.settings.SettingsConstant;
import org.cmucreatelab.flutter_android.classes.settings.SettingsCumulative;
import org.cmucreatelab.flutter_android.classes.settings.SettingsFrequency;
import org.cmucreatelab.flutter_android.classes.settings.SettingsProportional;
import org.cmucreatelab.flutter_android.helpers.GlobalHandler;
import org.cmucreatelab.flutter_android.helpers.static_classes.Constants;
import org.cmucreatelab.flutter_android.helpers.static_classes.FlutterProtocol;
import org.cmucreatelab.flutter_android.helpers.static_classes.MessageConstructor;
import org.cmucreatelab.flutter_android.ui.ServoAngleDrawable;
import org.cmucreatelab.flutter_android.ui.dialogs.SensorTypeDialog;
import org.cmucreatelab.flutter_android.ui.dialogs.error_dialogs.ConnectFlutterDialog;
import org.cmucreatelab.flutter_android.ui.dialogs.robots_tab.ControlOutputsDialog;
import org.cmucreatelab.flutter_android.ui.dialogs.robots_tab.GreenSensorTypeDialog;
import org.cmucreatelab.flutter_android.ui.dialogs.robots_tab.SimulateSensorsDialog;
import org.cmucreatelab.flutter_android.ui.dialogs.robots_tab.outputs.led.LedDialog;
import org.cmucreatelab.flutter_android.ui.dialogs.robots_tab.outputs.servo.ServoDialog;
import org.cmucreatelab.flutter_android.ui.dialogs.robots_tab.outputs.speaker.SpeakerDialog;
import org.cmucreatelab.flutter_android.ui.dialogs.wizards.robot_outputs_wizard.led.LedWizard;
import org.cmucreatelab.flutter_android.ui.dialogs.wizards.robot_outputs_wizard.servo.ServoWizard;
import org.cmucreatelab.flutter_android.ui.dialogs.wizards.robot_outputs_wizard.speaker.SpeakerWizard;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Steve.
 *
 * RobotActivity
 *
 * An activity where the user can interact with the flutter board.
 */
public class RobotActivity extends BaseSensorReadingActivity implements ServoDialog.DialogServoListener, LedDialog.DialogLedListener, SpeakerDialog.DialogSpeakerListener, SensorTypeDialog.DialogSensorTypeListener, SimulateSensorsDialog.SimulateSensorsDismissed {

    private RobotActivity instance;
    private Session session;
    private boolean speakerMuted = false;
    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        private int seekBarValue = 0;


        @Override
        public void onProgressChanged(SeekBar seekBar, final int i, boolean b) {
            Log.d(Constants.LOG_TAG, "onProgressChanged");
            seekBarValue = i;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Sensor[] sensors = session.getFlutter().getSensors();
                    TextView sensorReadingText;

                    if (sensors[0].getSensorType() != FlutterProtocol.InputTypes.NOT_SET) {
                        sensorReadingText = (TextView) findViewById(R.id.text_sensor_1_reading);
                        sensorReadingText.setText(String.valueOf(i));
                    }
                    if (sensors[1].getSensorType() != FlutterProtocol.InputTypes.NOT_SET) {
                        sensorReadingText = (TextView) findViewById(R.id.text_sensor_2_reading);
                        sensorReadingText.setText(String.valueOf(i));
                    }
                    if (sensors[2].getSensorType() != FlutterProtocol.InputTypes.NOT_SET) {
                        sensorReadingText = (TextView) findViewById(R.id.text_sensor_3_reading);
                        sensorReadingText.setText(String.valueOf(i));
                    }
                }
            });
        }


        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }


        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (session.isSimulatingData()) {
                session.getFlutter().setSensorValues(seekBarValue, seekBarValue, seekBarValue);
                GlobalHandler.getInstance(getApplicationContext()).melodySmartDeviceHandler.addMessage(MessageConstructor.constructSimulateData(seekBarValue, seekBarValue, seekBarValue));
            }
        }
    };


    private void updateLedCircleColors(final int ledNumber, final TriColorLed triColorLed) {
        Log.v(Constants.LOG_TAG, "updateLedCircleColors");
        final View[] circle_views = new View[]{findViewById(R.id.view_color_1), findViewById(R.id.view_color_2), findViewById(R.id.view_color_3)};
        if (ledNumber > circle_views.length || ledNumber <= 0) {
            Log.e(Constants.LOG_TAG, "updateLedCircleColors: received bad ledNumber=" + ledNumber);
            return;
        }
        if (!triColorLed.getRedLed().isLinked() && !triColorLed.getGreenLed().isLinked() && !triColorLed.getBlueLed().isLinked()) {
            Log.e(Constants.LOG_TAG, "updateLedCircleColors: one of LEDs is currently not linked.");
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View circleView = circle_views[ledNumber - 1];

                Integer maxHex = Color.parseColor(triColorLed.getMaxColorHex());
                if (TriColorLed.isSwatchInExistingSelection(triColorLed.getMaxColorHex())) {
                    maxHex = Constants.TRUE_HEX_TO_SWATCH_HEX.get(Color.parseColor(triColorLed.getMaxColorHex()));
                }

                Integer minHex = Color.parseColor(triColorLed.getMinColorHex());
                if (TriColorLed.isSwatchInExistingSelection(triColorLed.getMinColorHex())) {
                    minHex = Constants.TRUE_HEX_TO_SWATCH_HEX.get(Color.parseColor(triColorLed.getMinColorHex()));
                }

                RotateDrawable rotateDrawable;

                switch (ledNumber) {
                    case 1:
                        rotateDrawable = (RotateDrawable) getResources().getDrawable(R.drawable.two_color_relationship_circle_led_one);
                        break;
                    case 2:
                        rotateDrawable = (RotateDrawable) getResources().getDrawable(R.drawable.two_color_relationship_circle_led_two);
                        break;
                    default:
                        rotateDrawable = (RotateDrawable) getResources().getDrawable(R.drawable.two_color_relationship_circle_led_three);
                        break;
                }

                GradientDrawable gradientDrawable = (GradientDrawable) rotateDrawable.getDrawable();

                // if the link uses Constant relationship, do not display a minimum color
                if (triColorLed.getRedLed().getSettings().getRelationship() == Constant.getInstance() || triColorLed.getGreenLed().getSettings().getRelationship() == Constant.getInstance() || triColorLed.getBlueLed().getSettings().getRelationship() == Constant.getInstance()) {
                    gradientDrawable.setColors(new int[]{maxHex, maxHex});
                } else {
                    gradientDrawable.setColors(new int[]{minHex, maxHex});
                }

                circleView.setBackground(rotateDrawable);
            }
        });
    }


    public LayerDrawable getCustomSwatchWithBorder(String hexColor) {
        LayerDrawable layerDrawable = (LayerDrawable) getResources().getDrawable(R.drawable.universal_swatch);

        ((GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.color_main_swatch)).setColor(Color.parseColor(hexColor));

        return layerDrawable;
    }


    private void updateDynamicViews() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Sensor[] sensors = session.getFlutter().getSensors();
                TextView sensorReadingText;

                sensorReadingText = (TextView) findViewById(R.id.text_sensor_1_reading);
                if (sensors[0].getSensorType() != FlutterProtocol.InputTypes.NOT_SET) {
                    sensorReadingText.setText(String.valueOf(sensors[0].getSensorReading()));
                } else {
                    sensorReadingText.setText("");
                }

                sensorReadingText = (TextView) findViewById(R.id.text_sensor_2_reading);
                if (sensors[1].getSensorType() != FlutterProtocol.InputTypes.NOT_SET) {
                    sensorReadingText.setText(String.valueOf(sensors[1].getSensorReading()));
                } else {
                    sensorReadingText.setText("");
                }

                sensorReadingText = (TextView) findViewById(R.id.text_sensor_3_reading);
                if (sensors[2].getSensorType() != FlutterProtocol.InputTypes.NOT_SET) {
                    sensorReadingText.setText(String.valueOf(sensors[2].getSensorReading()));
                } else {
                    sensorReadingText.setText("");
                }
            }
        });
    }


    private void updateLinkedViews() {
        Log.d(Constants.LOG_TAG, "updateLinkedViews");
        Servo[] servos = session.getFlutter().getServos();
        TriColorLed[] triColorLeds = session.getFlutter().getTriColorLeds();
        Speaker speaker = session.getFlutter().getSpeaker();

        // servos link check
        Output[] outputs = new Output[8];
        for (int i = 0; i < servos.length + triColorLeds.length + 2; i++) {
            // TODO @tasota I moved the array declaration of outputs outside of the for loop (Mohit)
            RelativeLayout currentLayout = null;
            ViewGroup linkAndSensor;
            ImageView questionMark = null;
            ImageView link;
            ImageView sensor;
            int ledNumber = 0;

            switch (i) {
                case 0:
                    currentLayout = (RelativeLayout) findViewById(R.id.relative_servo_1);
                    questionMark = (ImageView) findViewById(R.id.image_servo_1);
                    outputs[0] = servos[0];
                    break;
                case 1:
                    currentLayout = (RelativeLayout) findViewById(R.id.relative_servo_2);
                    questionMark = (ImageView) findViewById(R.id.image_servo_2);
                    outputs[1] = servos[1];
                    break;
                case 2:
                    currentLayout = (RelativeLayout) findViewById(R.id.relative_servo_3);
                    questionMark = (ImageView) findViewById(R.id.image_servo_3);
                    outputs[2] = servos[2];
                    break;
                case 3:
                    currentLayout = (RelativeLayout) findViewById(R.id.relative_led_1);
                    questionMark = (ImageView) findViewById(R.id.image_led_1);
                    outputs[3] = triColorLeds[0].getRedLed();
                    ledNumber = 1;
                    break;
                case 4:
                    currentLayout = (RelativeLayout) findViewById(R.id.relative_led_2);
                    questionMark = (ImageView) findViewById(R.id.image_led_2);
                    outputs[4] = triColorLeds[1].getRedLed();
                    ledNumber = 2;
                    break;
                case 5:
                    currentLayout = (RelativeLayout) findViewById(R.id.relative_led_3);
                    questionMark = (ImageView) findViewById(R.id.image_led_3);
                    outputs[5] = triColorLeds[2].getRedLed();
                    ledNumber = 3;
                    break;
                case 6:
                    currentLayout = (RelativeLayout) findViewById(R.id.relative_speaker);
                    questionMark = (ImageView) findViewById(R.id.image_speaker);
                    outputs[6] = speaker.getVolume();
                    break;
                case 7:
                    currentLayout = (RelativeLayout) findViewById(R.id.relative_speaker2);
                    questionMark = (ImageView) findViewById(R.id.image_speaker);
                    outputs[7] = speaker.getPitch();
                    break;
            }
            if (outputs[i].isLinked()) {
                if (currentLayout != null && questionMark != null) {
                    currentLayout.setVisibility(View.VISIBLE);
                    questionMark.setVisibility(View.INVISIBLE);
                    linkAndSensor = ((ViewGroup) currentLayout.getChildAt(0));
                    link = (ImageView) linkAndSensor.getChildAt(0);
                    sensor = (ImageView) linkAndSensor.getChildAt(1);
                    link.setImageResource(outputs[i].getSettings().getRelationship().getGreyImageIdSm());

                    Settings settings = outputs[i].getSettings();

                    if (ledNumber > 0) {
                        TriColorLed led = triColorLeds[ledNumber - 1];
                        updateLedCircleColors(ledNumber, led);
                    }

                    // TODO @tasota handle finding the sensor more cleanly?
                    int imageRes = new NoSensor(0).getGreyImageIdSm();
                    if (settings.getClass() == SettingsProportional.class && ((SettingsProportional) settings).getSensorPortNumber() != 0) {
                        imageRes = session.getFlutter().getSensors()[((SettingsProportional) settings).getSensorPortNumber() - 1].getGreyImageIdSm();
                    } else if (settings.getClass() == SettingsAmplitude.class && ((SettingsAmplitude) settings).getSensorPortNumber() != 0) {
                        imageRes = session.getFlutter().getSensors()[((SettingsAmplitude) settings).getSensorPortNumber() - 1].getGreyImageIdSm();
                    } else if (settings.getClass() == SettingsFrequency.class && ((SettingsFrequency) settings).getSensorPortNumber() != 0) {
                        imageRes = session.getFlutter().getSensors()[((SettingsFrequency) settings).getSensorPortNumber() - 1].getGreyImageIdSm();
                    } else if (settings.getClass() == SettingsChange.class && ((SettingsChange) settings).getSensorPortNumber() != 0) {
                        imageRes = session.getFlutter().getSensors()[((SettingsChange) settings).getSensorPortNumber() - 1].getGreyImageIdSm();
                    } else if (settings.getClass() == SettingsCumulative.class && ((SettingsCumulative) settings).getSensorPortNumber() != 0) {
                        imageRes = session.getFlutter().getSensors()[((SettingsCumulative) settings).getSensorPortNumber() - 1].getGreyImageIdSm();
                    }
                    sensor.setImageResource(imageRes);
                }
            } else {
                if (currentLayout != null && questionMark != null) {
                    currentLayout.setVisibility(View.INVISIBLE);
                    questionMark.setVisibility(View.VISIBLE);
                }
            }
        }
    }


    private void updateSpeakerToggleVisibility() {
        Speaker speaker = session.getFlutter().getSpeaker();
        ImageView speakerMuteToggle = (ImageView) findViewById(R.id.image_speaker_mute_toggle);

        //set visibility based on if volume or pitch is linked
        if (speaker.getVolume().isLinked() || speaker.getPitch().isLinked()) {
            speakerMuteToggle.setVisibility(View.VISIBLE);
        } else {
            speakerMuteToggle.setVisibility(View.INVISIBLE);
        }
    }


    private void updatePointer(ImageView servoPointer, int selectedValue) {
        RotateAnimation rotateAnimation = new RotateAnimation(selectedValue - 1, selectedValue, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setFillEnabled(true);
        rotateAnimation.setFillAfter(true);
        servoPointer.startAnimation(rotateAnimation);
    }


    private void updateServoIndicators() {
        Servo[] servos = session.getFlutter().getServos();
        int minPos[] = new int[servos.length];
        int maxPos[] = new int[servos.length];
        boolean[] constant = {false, false, false};

        ImageView servoOnePointer = (ImageView) findViewById(R.id.image_servo_1_pointer);
        ImageView servoTwoPointer = (ImageView) findViewById(R.id.image_servo_2_pointer);
        ImageView servoThreePointer = (ImageView) findViewById(R.id.image_servo_3_pointer);

        servoOnePointer.setVisibility(View.GONE);
        servoTwoPointer.setVisibility(View.GONE);
        servoThreePointer.setVisibility(View.GONE);

        for (int i = 0; i < servos.length; i++) {
            if (servos[i].isLinked()) {
                Settings settings = servos[i].getSettings();
                if (settings.getClass() == SettingsProportional.class && ((SettingsProportional) settings).getSensorPortNumber() != 0) {
                    minPos[i] = ((SettingsProportional) settings).getOutputMin();
                    maxPos[i] = ((SettingsProportional) settings).getOutputMax();
                } else if (settings.getClass() == SettingsAmplitude.class && ((SettingsAmplitude) settings).getSensorPortNumber() != 0) {
                    minPos[i] = ((SettingsAmplitude) settings).getOutputMin();
                    maxPos[i] = ((SettingsAmplitude) settings).getOutputMax();
                } else if (settings.getClass() == SettingsFrequency.class && ((SettingsFrequency) settings).getSensorPortNumber() != 0) {
                    minPos[i] = ((SettingsFrequency) settings).getOutputMin();
                    maxPos[i] = ((SettingsFrequency) settings).getOutputMax();
                } else if (settings.getClass() == SettingsChange.class && ((SettingsChange) settings).getSensorPortNumber() != 0) {
                    minPos[i] = ((SettingsChange) settings).getOutputMin();
                    maxPos[i] = ((SettingsChange) settings).getOutputMax();
                } else if (settings.getClass() == SettingsCumulative.class && ((SettingsCumulative) settings).getSensorPortNumber() != 0) {
                    minPos[i] = ((SettingsCumulative) settings).getOutputMin();
                    maxPos[i] = ((SettingsCumulative) settings).getOutputMax();
                } else if (settings.getClass() == SettingsConstant.class) {
                    constant[i] = true;
                    maxPos[i] = ((SettingsConstant) settings).getValue();
                }
            }
        }

        if (servos[0].isLinked()) {
            TextView servo1MinPosText = (TextView) findViewById(R.id.text_servo_1_min_pos);
            TextView servo1MaxPosText = (TextView) findViewById(R.id.text_servo_1_max_pos);
            ImageView servo1GreenIndicator = (ImageView) findViewById(R.id.servo_1_foreground_green_indicator);

            if (constant[0]) {
                servo1MaxPosText.setText(maxPos[0] + "°");
                servo1MinPosText.setVisibility(View.INVISIBLE);

                servo1GreenIndicator.setImageDrawable(null);

                servoOnePointer.setVisibility(View.VISIBLE);
                updatePointer(servoOnePointer, maxPos[0]);
            } else {
                servo1MinPosText.setVisibility(View.VISIBLE);
                ServoAngleDrawable servoAngleDrawable = new ServoAngleDrawable(R.color.fluttergreen, minPos[0], maxPos[0], this);
                servo1GreenIndicator.setImageDrawable(servoAngleDrawable);
                servo1MinPosText.setText(minPos[0] + "°");
                servo1MaxPosText.setText(maxPos[0] + "°");
            }
        }
        if (servos[1].isLinked()) {
            TextView servo2MinPosText = (TextView) findViewById(R.id.text_servo_2_min_pos);
            TextView servo2MaxPosText = (TextView) findViewById(R.id.text_servo_2_max_pos);
            ImageView servo2GreenIndicator = (ImageView) findViewById(R.id.servo_2_foreground_green_indicator);

            if (constant[1]) {
                servo2MaxPosText.setText(maxPos[1] + "°");
                servo2MinPosText.setVisibility(View.INVISIBLE);

                servo2GreenIndicator.setImageDrawable(null);

                servoTwoPointer.setVisibility(View.VISIBLE);
                updatePointer(servoTwoPointer, maxPos[1]);
            } else {
                servo2MinPosText.setVisibility(View.VISIBLE);
                ServoAngleDrawable servoAngleDrawable = new ServoAngleDrawable(R.color.fluttergreen, minPos[1], maxPos[1], this);
                servo2GreenIndicator.setImageDrawable(servoAngleDrawable);
                servo2MinPosText.setText(minPos[1] + "°");
                servo2MaxPosText.setText(maxPos[1] + "°");
            }
        }
        if (servos[2].isLinked()) {
            TextView servo3MinPosText = (TextView) findViewById(R.id.text_servo_3_min_pos);
            TextView servo3MaxPosText = (TextView) findViewById(R.id.text_servo_3_max_pos);
            ImageView servo3GreenIndicator = (ImageView) findViewById(R.id.servo_3_foreground_green_indicator);

            if (constant[2]) {
                servo3MaxPosText.setText(maxPos[2] + "°");
                servo3MinPosText.setVisibility(View.INVISIBLE);

                servo3GreenIndicator.setImageDrawable(null);

                servoThreePointer.setVisibility(View.VISIBLE);
                updatePointer(servoThreePointer, maxPos[2]);
            } else {
                servo3MinPosText.setVisibility(View.VISIBLE);
                ServoAngleDrawable servoAngleDrawable = new ServoAngleDrawable(R.color.fluttergreen, minPos[2], maxPos[2], this);
                servo3GreenIndicator.setImageDrawable(servoAngleDrawable);
                servo3MinPosText.setText(minPos[2] + "°");
                servo3MaxPosText.setText(maxPos[2] + "°");
            }
        }
    }


    // OnClickListeners


    private void onClickSensor(int portNumber) {
        SensorTypeDialog sensorTypeDialog = GreenSensorTypeDialog.newInstance(portNumber, instance);
        sensorTypeDialog.show(getSupportFragmentManager(), "tag");
    }


    private View.OnClickListener sensor1OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onClickSensor(1);
        }
    };
    private View.OnClickListener sensor2OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onClickSensor(2);
        }
    };
    private View.OnClickListener sensor3OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onClickSensor(3);
        }
    };


    private void onClickServo(int portNumber) {
        Log.d(Constants.LOG_TAG, "RobotActivity.onClickServo " + portNumber);
        Log.d(Constants.LOG_TAG, "onClickServo1");
        Servo[] servos = session.getFlutter().getServos();
        //Log.i("SesnorType", "IsThis: " + sensors[portNumber-1].getSensorType());

        if (!servos[portNumber - 1].isLinked()) {
            new ServoWizard(this, servos[portNumber - 1]).start();
        } else {
            ServoDialog dialog = ServoDialog.newInstance(servos[portNumber - 1], this);
            dialog.show(getSupportFragmentManager(), "tag");
        }
    }


    private View.OnClickListener servo3FrameClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onClickServo(3);
        }
    };
    private View.OnClickListener servo2FrameClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onClickServo(2);
        }
    };
    private View.OnClickListener servo1FrameClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onClickServo(1);
        }
    };


    private void onClickLed(int portNumber) {
        Log.d(Constants.LOG_TAG, "RobotActivity.onClickLed " + portNumber);
        TriColorLed[] triColorLeds = session.getFlutter().getTriColorLeds();

        if (!triColorLeds[portNumber - 1].getRedLed().isLinked() || !triColorLeds[portNumber - 1].getGreenLed().isLinked() || !triColorLeds[portNumber - 1].getBlueLed().isLinked()) {
            new LedWizard(this, triColorLeds[portNumber - 1]).start();
        } else {
            LedDialog dialog = LedDialog.newInstance(triColorLeds[portNumber - 1], this);
            dialog.show(getSupportFragmentManager(), "tag");
        }
    }


    private View.OnClickListener led1FrameClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onClickLed(1);
        }
    };
    private View.OnClickListener led2FrameClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onClickLed(2);
        }
    };
    private View.OnClickListener led3FrameClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onClickLed(3);
        }
    };


    private void onClickSpeaker() {
        Log.d(Constants.LOG_TAG, "onClickSpeaker");
        Speaker speaker = session.getFlutter().getSpeaker();

        if (!speaker.getVolume().isLinked() || !speaker.getPitch().isLinked()) {
            new SpeakerWizard(this, speaker).start();
        } else {
            SpeakerDialog dialog = SpeakerDialog.newInstance(speaker, this);
            dialog.show(getSupportFragmentManager(), "tag");
        }
    }


    private View.OnClickListener speakerFrameClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onClickSpeaker();
        }
    };


    // Class methods


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot);
        ButterKnife.bind(this);
        GlobalHandler globalHandler = GlobalHandler.getInstance(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        toolbar.setBackground(ContextCompat.getDrawable(this, R.drawable.tab_b_g_robot));
        toolbar.setContentInsetsAbsolute(0, 0);
        setSupportActionBar(toolbar);

        // Menu icon and text
        TextView robotMenuEntry = (TextView) findViewById(R.id.text_menu_robot);
        robotMenuEntry.setTextColor(getResources().getColor(R.color.white));
        robotMenuEntry.setCompoundDrawablesWithIntrinsicBounds(R.drawable.menu_icon_robot, 0, 0, 0);

        if (!globalHandler.melodySmartDeviceHandler.isConnected()) {
            ConnectFlutterDialog connectFlutterDialog = ConnectFlutterDialog.newInstance(ConnectFlutterDialog.ConnectFlutterPreviousScreen.ROBOT);
            connectFlutterDialog.show(getSupportFragmentManager(), "tag");
        } else {
            instance = this;
            this.session = globalHandler.sessionHandler.getSession();

            TextView sensor1Text = (TextView) findViewById(R.id.text_sensor_1);
            TextView sensor2Text = (TextView) findViewById(R.id.text_sensor_2);
            TextView sensor3Text = (TextView) findViewById(R.id.text_sensor_3);
            sensor1Text.setOnClickListener(sensor1OnClickListener);
            sensor2Text.setOnClickListener(sensor2OnClickListener);
            sensor3Text.setOnClickListener(sensor3OnClickListener);

            FrameLayout frameServo1, frameServo2, frameServo3, frameled1, frameled2, frameled3, frameSpeaker;
            frameServo1 = (FrameLayout) findViewById(R.id.frame_servo_1);
            frameServo2 = (FrameLayout) findViewById(R.id.frame_servo_2);
            frameServo3 = (FrameLayout) findViewById(R.id.frame_servo_3);
            frameled1 = (FrameLayout) findViewById(R.id.frame_led_1);
            frameled2 = (FrameLayout) findViewById(R.id.frame_led_2);
            frameled3 = (FrameLayout) findViewById(R.id.frame_led_3);
            frameSpeaker = (FrameLayout) findViewById(R.id.frame_speaker);
            frameServo1.setOnClickListener(servo1FrameClickListener);
            frameServo2.setOnClickListener(servo2FrameClickListener);
            frameServo3.setOnClickListener(servo3FrameClickListener);
            frameled1.setOnClickListener(led1FrameClickListener);
            frameled2.setOnClickListener(led2FrameClickListener);
            frameled3.setOnClickListener(led3FrameClickListener);
            frameSpeaker.setOnClickListener(speakerFrameClickListener);

            updateSensorViews();
            updateDynamicViews();
            updateServoIndicators();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        GlobalHandler globalHandler = GlobalHandler.getInstance(getApplicationContext());
        TextView flutterStatusText = (TextView) findViewById(R.id.text_flutter_connection_status);
        ImageView flutterStatusIcon = (ImageView) findViewById(R.id.image_flutter_status_icon);

        if (!globalHandler.melodySmartDeviceHandler.isConnected()) {
            ConnectFlutterDialog connectFlutterDialog = ConnectFlutterDialog.newInstance(ConnectFlutterDialog.ConnectFlutterPreviousScreen.ROBOT);
            connectFlutterDialog.show(getSupportFragmentManager(), "tag");

            // Flutter status icon (upper right)
            flutterStatusText.setText(R.string.connection_disconnected);
            flutterStatusText.setTextColor(Color.GRAY);
            flutterStatusIcon.setImageResource(R.drawable.flutterdisconnectgraphic);
        } else {
            String flutterName = session.getFlutter().getName();

            // Flutter status icon (upper right)
            TextView flutterStatusButtonName = (TextView) findViewById(R.id.text_connected_flutter_name);
            flutterStatusButtonName.setText(flutterName);
            flutterStatusText.setText(R.string.connection_connected);
            flutterStatusText.setTextColor(getResources().getColor(R.color.fluttergreen));
            flutterStatusIcon.setImageResource(R.drawable.flutterconnectgraphic);

            updateSpeakerToggleVisibility();
            updateLinkedViews();
            if (!session.isSimulatingData()) {
                startSensorReading();
            }
        }
    }


    // Dialog Listeners


    @Override
    public void onServoLinkListener(MelodySmartMessage message) {
        GlobalHandler globalHandler = GlobalHandler.getInstance(getApplicationContext());

        Log.d(Constants.LOG_TAG, "onServoLinkListener");
        globalHandler.melodySmartDeviceHandler.addMessage(message);
        updateLinkedViews();
        updateServoIndicators();
    }


    @Override
    public void onLedLinkListener(ArrayList<MelodySmartMessage> msgs) {
        GlobalHandler globalHandler = GlobalHandler.getInstance(getApplicationContext());

        Log.d(Constants.LOG_TAG, "onLedLinkCreated");
        for (MelodySmartMessage message : msgs) {
            globalHandler.melodySmartDeviceHandler.addMessage(message);
        }
        updateLinkedViews();
    }


    @Override
    public void onSpeakerLinkListener(ArrayList<MelodySmartMessage> msgs) {
        GlobalHandler globalHandler = GlobalHandler.getInstance(getApplicationContext());

        Log.d(Constants.LOG_TAG, "onSpeakerLinkCreated");
        for (MelodySmartMessage message : msgs) {
            globalHandler.melodySmartDeviceHandler.addMessage(message);
        }
        updateSpeakerToggleVisibility();

        //flip the muted state of speaker
        speakerMuted = false;

        ImageView speakerMuteToggle = (ImageView) findViewById(R.id.image_speaker_mute_toggle);
        speakerMuteToggle.setImageResource(R.drawable.speaker_unmute);

        updateLinkedViews();
    }


    // onClick listeners


    @OnClick(R.id.image_servo_1)
    public void onClickServo1Image() {
        onClickServo(1);
    }


    @OnClick(R.id.relative_servo_1)
    public void onClickServo1Relative() {
        onClickServo(1);
    }


    @OnClick(R.id.image_servo_2)
    public void onClickServo2Image() {
        onClickServo(2);
    }


    @OnClick(R.id.relative_servo_2)
    public void onClickServo2Relative() {
        onClickServo(2);
    }


    @OnClick(R.id.image_servo_3)
    public void onClickServo3Image() {
        onClickServo(3);
    }


    @OnClick(R.id.relative_servo_3)
    public void onclickServo3Relative() {
        onClickServo(3);
    }


    @OnClick(R.id.image_led_1)
    public void onClickLed1Image() {
        onClickLed(1);
    }


    @OnClick(R.id.relative_led_1)
    public void onClickLed1Relative() {
        onClickLed(1);
    }


    @OnClick(R.id.image_led_2)
    public void onClickLed2Image() {
        onClickLed(2);
    }


    @OnClick(R.id.relative_led_2)
    public void onClickLed2Relative() {
        onClickLed(2);
    }


    @OnClick(R.id.image_led_3)
    public void onClickLed3Image() {
        onClickLed(3);
    }


    @OnClick(R.id.relative_led_3)
    public void onClickLed3Relative() {
        onClickLed(3);
    }


    @OnClick(R.id.image_speaker)
    public void onClickSpeakerImage() {
        onClickSpeaker();
    }


    @OnClick(R.id.relative_speaker)
    public void onClickSpeakerRelative() {
        onClickSpeaker();
    }


    @OnClick(R.id.relative_speaker2)
    public void onClickSpeakerRelative2() {
        onClickSpeaker();
    }


    @OnClick(R.id.image_speaker_mute_toggle)
    public void onClickSpeakerMuteToggle() {
        //flip the muted state of speaker
        speakerMuted = !speakerMuted;

        ImageView speakerMuteToggle = (ImageView) findViewById(R.id.image_speaker_mute_toggle);
        Speaker speaker = session.getFlutter().getSpeaker();

        MelodySmartMessage message;

        //setting volume to 0
        if (speakerMuted) {
            speakerMuteToggle.setImageResource(R.drawable.speaker_mute);
            message = MessageConstructor.constructSetOutput(speaker.getVolume(), 0);
        }
        //adding previously set speaker relation when unmuted
        else {
            speakerMuteToggle.setImageResource(R.drawable.speaker_unmute);
            message = MessageConstructor.constructRelationshipMessage(speaker.getVolume(), speaker.getVolume().getSettings());
        }

        GlobalHandler globalHandler = GlobalHandler.getInstance(getApplicationContext());
        globalHandler.melodySmartDeviceHandler.addMessage(message);
    }


    @OnClick(R.id.button_simulate_sensors)
    public void onClickSensorData() {
        Log.d(Constants.LOG_TAG, "onClickSensorData");
        if (!session.isSimulatingData()) {
            session.getFlutter().setSensorValues(0, 0, 0);
            session.setSimulatingData(true);
            stopSensorReading();
            updateSensorViews();
            SimulateSensorsDialog simulateSensorsDialog = SimulateSensorsDialog.newInstance(session.getFlutter().getSensors(), this);
            simulateSensorsDialog.show(getSupportFragmentManager(), "tag");
        }
    }


    @OnClick(R.id.button_control_outputs)
    public void onClickControlOutputs() {
        Log.d(Constants.LOG_TAG, "onClickControlOutputs");

        ControlOutputsDialog controlOutputsDialog = ControlOutputsDialog.newInstance(speakerMuted);
        controlOutputsDialog.show(getSupportFragmentManager(), "tag");
    }


    @Override
    public void updateSensorViews() {
        updateDynamicViews();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Sensor[] sensors = session.getFlutter().getSensors();
                TextView sensorText;

                sensorText = (TextView) findViewById(R.id.text_sensor_1);
                sensorText.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(instance, sensors[0].getWhiteImageIdSm()), null, null);
                sensorText.setText(sensors[0].getTypeTextId());

                sensorText = (TextView) findViewById(R.id.text_sensor_2);
                sensorText.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(instance, sensors[1].getWhiteImageIdSm()), null, null);
                sensorText.setText(sensors[1].getTypeTextId());

                sensorText = (TextView) findViewById(R.id.text_sensor_3);
                sensorText.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(instance, sensors[2].getWhiteImageIdSm()), null, null);
                sensorText.setText(sensors[2].getTypeTextId());
            }
        });
    }


    @Override
    public void onSensorTypeChosen(Sensor sensor) {
        int portNumber = sensor.getPortNumber();
        Log.d(Constants.LOG_TAG, "onSensorTypeChosen; PORT #" + portNumber);

        // update sensor
        GlobalHandler.getInstance(this).sessionHandler.getSession().getFlutter().updateSensorAtPort(GlobalHandler.getInstance(this).melodySmartDeviceHandler, portNumber, sensor);

        updateSensorViews();
        updateLinkedViews();
    }


    @Override
    public void onSimulateSensorsDismissed() {
        if (session.isSimulatingData()) {
            session.setSimulatingData(false);
            startSensorReading();
        }
    }

}