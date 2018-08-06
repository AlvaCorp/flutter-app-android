package org.cmucreatelab.flutter_android.classes.settings;

import org.cmucreatelab.flutter_android.classes.flutters.Flutter;
import org.cmucreatelab.flutter_android.classes.relationships.Amplitude;
import org.cmucreatelab.flutter_android.classes.relationships.Relationship;
import org.cmucreatelab.flutter_android.classes.sensors.NoSensor;
import org.cmucreatelab.flutter_android.classes.sensors.Sensor;

/**
 * Created by mike on 4/6/17.
 *
 * SettingsAmplitude
 *
 * A class that represents the generic settings made with Amplitude relationship
 */
public class SettingsAmplitude extends Settings {

    // getters
    public int getOutputMax() { return outputMax; }
    public int getOutputMin() { return outputMin; }
    public int getSensorPortNumber() { return sensorPortNumber; }
    public AdvancedSettings getAdvancedSettings() { return advancedSettings; }
    // setters
    public void setOutputMax(int max) { outputMax = max; }
    public void setOutputMin(int min) { outputMin = min; }
    public void setAdvancedSettings (AdvancedSettings advancedSettings) { this.advancedSettings = advancedSettings; }


    protected SettingsAmplitude(Settings settings) {
        super(settings);
    }


    protected SettingsAmplitude(int min, int max, Flutter flutter) {
        super(min, max, flutter);
    }


    public void setSensorPortNumber(int portNumber) {
        Sensor oldSensor, newSensor;
        oldSensor = getSensor();
        this.sensorPortNumber = portNumber;
        newSensor = getSensor();
        updateWithNewSensorType(oldSensor, newSensor);
    }


    @Override
    public boolean hasAdvancedSettings() {
        return true;
    }


    @Override
    public boolean hasSpeed() {
        return true;
    }


    @Override
    public Relationship getRelationship() {
        return Amplitude.getInstance();
    }


    @Override
    public Sensor getSensor() {
        if (getSensorPortNumber() == 0)
            return new NoSensor(0);
        return flutter.getSensors()[getSensorPortNumber()-1];
    }


    @Override
    public boolean isSettable() {
        if (getSensor().getClass() == NoSensor.class) {
            return false;
        }
        return true;
    }


    /**
     * When opening a dialog on RobotsActivity, we want to create a new instance of its respective
     * SettingsAmplitude. That way we can display changes the user makes and, if the settings are not saved,
     * then the real SettingsAmplitude will not be overwritten.
     *
     * @param oldInstance The object that is to be copied.
     * @return A new instance of SettingsAmplitude.
     */
    public static SettingsAmplitude newInstance(Settings oldInstance) {
        SettingsAmplitude newInstance = new SettingsAmplitude(oldInstance.outputMin, oldInstance.outputMax, oldInstance.flutter);
        newInstance.sensorPortNumber = oldInstance.sensorPortNumber;
        newInstance.advancedSettings = AdvancedSettings.newInstance(oldInstance.advancedSettings, newInstance);
        return newInstance;
    }


    public static SettingsAmplitude newInstance(Flutter flutter) {
        SettingsAmplitude newInstance = new SettingsAmplitude(0,100,flutter);
        // TODO create new instance for Adv Settings?
        return newInstance;
    }

}
