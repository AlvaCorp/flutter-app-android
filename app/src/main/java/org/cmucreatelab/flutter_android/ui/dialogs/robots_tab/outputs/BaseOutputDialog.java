package org.cmucreatelab.flutter_android.ui.dialogs.robots_tab.outputs;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.cmucreatelab.flutter_android.R;
import org.cmucreatelab.flutter_android.classes.outputs.Output;
import org.cmucreatelab.flutter_android.classes.sensors.NoSensor;
import org.cmucreatelab.flutter_android.classes.sensors.Sensor;
import org.cmucreatelab.flutter_android.classes.settings.Settings;
import org.cmucreatelab.flutter_android.helpers.static_classes.Constants;
import org.cmucreatelab.flutter_android.ui.dialogs.BaseResizableDialog;

/**
 * Created by Steve on 2/1/2017.
 */
public abstract class BaseOutputDialog extends BaseResizableDialog {

    protected void updateViews(View view, Output output) {
        if (output.getSettings() != null) {
            Log.v(Constants.LOG_TAG, "BaseOutputDialog.updateViews");
            Settings settings = output.getSettings();
            Button saveButton = (Button) view.findViewById(R.id.button_save_link);
            Button removeButton = (Button) view.findViewById(R.id.button_remove_link);

            // sensor
            Sensor sensor = settings.getSensor();
            if (sensor.getClass() != NoSensor.class) {
                ImageView sensorImage = (ImageView) view.findViewById(R.id.image_sensor);
                sensorImage.setImageResource(sensor.getGreenImageId());
                TextView sensorText = (TextView) view.findViewById(R.id.text_sensor_link);
                sensorText.setText(R.string.linked_sensor);
                TextView sensorType = (TextView) view.findViewById(R.id.text_sensor_type);
                sensorType.setText(getString(sensor.getSensorTypeId()));
            }

            // buttons (save, remove)
            if (settings.isSettable()) {
                saveButton.setEnabled(true);
                removeButton.setEnabled(true);
            }

            ImageView relationshipImage = (ImageView) view.findViewById(R.id.image_relationship);
            relationshipImage.setImageResource(settings.getRelationship().getGreenImageIdMd());
            TextView relationshipText = (TextView) view.findViewById(R.id.text_relationship);
            relationshipText.setText(R.string.relationship);
            TextView relationshipType = (TextView) view.findViewById(R.id.text_relationship_type);
            relationshipType.setText(settings.getRelationship().getRelationshipTypeId());
        }
    }

}
