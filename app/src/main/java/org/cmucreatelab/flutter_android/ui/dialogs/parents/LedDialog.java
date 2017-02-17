package org.cmucreatelab.flutter_android.ui.dialogs.parents;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.cmucreatelab.android.melodysmart.models.MelodySmartMessage;
import org.cmucreatelab.flutter_android.R;
import org.cmucreatelab.flutter_android.classes.outputs.BlueLed;
import org.cmucreatelab.flutter_android.classes.outputs.GreenLed;
import org.cmucreatelab.flutter_android.classes.outputs.RedLed;
import org.cmucreatelab.flutter_android.classes.outputs.TriColorLed;
import org.cmucreatelab.flutter_android.classes.sensors.NoSensor;
import org.cmucreatelab.flutter_android.classes.sensors.Sensor;
import org.cmucreatelab.flutter_android.classes.settings.AdvancedSettings;
import org.cmucreatelab.flutter_android.classes.relationships.Relationship;
import org.cmucreatelab.flutter_android.helpers.GlobalHandler;
import org.cmucreatelab.flutter_android.helpers.static_classes.Constants;
import org.cmucreatelab.flutter_android.helpers.static_classes.FlutterProtocol;
import org.cmucreatelab.flutter_android.helpers.static_classes.MessageConstructor;
import org.cmucreatelab.flutter_android.ui.dialogs.BaseOutputDialog;
import org.cmucreatelab.flutter_android.ui.dialogs.children.AdvancedSettingsDialog;
import org.cmucreatelab.flutter_android.ui.dialogs.children.MaxColorDialog;
import org.cmucreatelab.flutter_android.ui.dialogs.children.MinColorDialog;
import org.cmucreatelab.flutter_android.ui.dialogs.children.RelationshipOutputDialog;
import org.cmucreatelab.flutter_android.ui.dialogs.children.SensorOutputDialog;

import java.io.Serializable;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Steve on 10/17/2016.
 *
 * LedDialog
 *
 * A Dialog that shows the options for creating a link between Led and a Sensor
 */
public class LedDialog extends BaseOutputDialog implements Serializable,
        AdvancedSettingsDialog.DialogAdvancedSettingsListener,
        SensorOutputDialog.DialogSensorListener,
        RelationshipOutputDialog.DialogRelationshipListener,
        MaxColorDialog.DialogHighColorListener,
        MinColorDialog.DialogLowColorListener {

    private View dialogView;
    private DialogLedListener dialogLedListener;
    private ImageView currentImageView;
    private TextView currentTextViewDescrp;
    private TextView currentTextViewItem;
    private Button saveButton;
    private ImageView maxColor;
    private ImageView minColor;
    private TriColorLed triColorLed;


    private void updateViews(View view) {
        updateViews(view, triColorLed.getRedLed());

        // max
        ImageView maxColorImg = (ImageView) view.findViewById(R.id.image_max_color);
        maxColorImg.setVisibility(View.GONE);
        maxColor.setImageResource(triColorLed.getMaxSwatch());
        maxColor.setVisibility(View.VISIBLE);
        TextView maxColorTxt = (TextView) view.findViewById(R.id.text_max_color);
        TextView maxColorValue = (TextView) view.findViewById(R.id.text_max_color_value);
        // TODO @tasota this should check for all (RGB) settings
        if (triColorLed.getRedLed().getSettings().getSensor().getClass() == NoSensor.class) {
            maxColorTxt.setText(R.string.maximum_color);
        } else {
            maxColorTxt.setText(getString(triColorLed.getRedLed().getSettings().getSensor().getHighTextId())+" Color");
        }
        maxColorValue.setText(triColorLed.getMaxColorText());

        // min
        ImageView minColorImg = (ImageView) view.findViewById(R.id.image_min_color);
        minColorImg.setVisibility(View.GONE);
        minColor.setImageResource(triColorLed.getMinSwatch());
        minColor.setVisibility(View.VISIBLE);
        TextView minColorTxt = (TextView) view.findViewById(R.id.text_min_color);
        TextView minColorValue = (TextView) view.findViewById(R.id.text_min_color_value);
        // TODO @tasota this should check for all (RGB) settings
        if (triColorLed.getRedLed().getSettings().getSensor().getClass() == NoSensor.class) {
            minColorTxt.setText(R.string.minimum_color);
        } else {
            minColorTxt.setText(getString(triColorLed.getRedLed().getSettings().getSensor().getLowTextId())+" Color");
        }
        minColorValue.setText(triColorLed.getMinColorText());
    }


    private int getProportionalValue(float value, float maxValue, float newMaxValue) {
        float ratio = value / maxValue;
        int result = (int) Math.ceil(ratio*newMaxValue);
        return result;
    }


    public static LedDialog newInstance(TriColorLed led, Serializable activity) {
        LedDialog ledDialog = new LedDialog();

        Bundle args = new Bundle();
        args.putSerializable(TriColorLed.LED_KEY, led);
        args.putSerializable(Constants.SerializableKeys.DIALOG_LED, activity);
        ledDialog.setArguments(args);

        return ledDialog;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(Constants.LOG_TAG, "onCreateDialog");
        super.onCreateDialog(savedInstanceState);

        // clone old object
        triColorLed = TriColorLed.newInstance((TriColorLed) getArguments().getSerializable(TriColorLed.LED_KEY));

        dialogLedListener = (DialogLedListener) getArguments().getSerializable(Constants.SerializableKeys.DIALOG_LED);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_leds, null);
        this.dialogView = view;
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppTheme));
        builder.setView(view);
        ((TextView) view.findViewById(R.id.text_output_title)).setText(getString(R.string.set_up_led) + " " +  String.valueOf(triColorLed.getPortNumber()));
        ButterKnife.bind(this, view);

        maxColor = (ImageView) view.findViewById(R.id.view_max_color);
        minColor = (ImageView) view.findViewById(R.id.view_min_color);

        updateViews(view);
        saveButton = (Button) view.findViewById(R.id.button_save_link);

        return builder.create();
    }


    // onClick listeners


    @OnClick(R.id.button_save_link)
    public void onClickSaveSettings() {
        Log.d(Constants.LOG_TAG, "onClickSaveSettings");
        ArrayList<MelodySmartMessage> msg = new ArrayList<>();
        msg.add(MessageConstructor.constructRemoveRelation(triColorLed.getRedLed()));
        msg.add(MessageConstructor.constructRemoveRelation(triColorLed.getGreenLed()));
        msg.add(MessageConstructor.constructRemoveRelation(triColorLed.getBlueLed()));

        msg.add(MessageConstructor.constructRelationshipMessage(triColorLed.getRedLed(), triColorLed.getRedLed().getSettings()));
        msg.add(MessageConstructor.constructRelationshipMessage(triColorLed.getGreenLed(), triColorLed.getGreenLed().getSettings()));
        msg.add(MessageConstructor.constructRelationshipMessage(triColorLed.getBlueLed(), triColorLed.getBlueLed().getSettings()));

        triColorLed.getRedLed().setIsLinked(true, triColorLed.getRedLed());
        triColorLed.getGreenLed().setIsLinked(true, triColorLed.getGreenLed());
        triColorLed.getBlueLed().setIsLinked(true, triColorLed.getBlueLed());

        // overwrite old object
        GlobalHandler.getInstance(getActivity()).sessionHandler.getSession().getFlutter().getTriColorLeds()[triColorLed.getPortNumber()-1] = triColorLed;

        dialogLedListener.onLedLinkListener(msg);
        this.dismiss();
    }


    @OnClick(R.id.button_remove_link)
    public void onClickRemoveLink() {
        ArrayList<MelodySmartMessage> msg = new ArrayList<>();
        Log.d(Constants.LOG_TAG, "onClickRemoveLink");
        RedLed redLed = triColorLed.getRedLed();
        GreenLed greenLed = triColorLed.getGreenLed();
        BlueLed blueLed = triColorLed.getBlueLed();

        msg.add(MessageConstructor.constructRemoveRelation(triColorLed.getRedLed()));
        msg.add(MessageConstructor.constructRemoveRelation(triColorLed.getGreenLed()));
        msg.add(MessageConstructor.constructRemoveRelation(triColorLed.getBlueLed()));

        redLed.setIsLinked(false, triColorLed.getRedLed());
        redLed.getSettings().setOutputMax(triColorLed.getRedLed().getMax());
        redLed.getSettings().setOutputMin(triColorLed.getRedLed().getMin());
        greenLed.setIsLinked(false, triColorLed.getGreenLed());
        greenLed.getSettings().setOutputMax(triColorLed.getGreenLed().getMax());
        greenLed.getSettings().setOutputMin(triColorLed.getGreenLed().getMin());
        blueLed.setIsLinked(false, triColorLed.getBlueLed());
        blueLed.getSettings().setOutputMax(triColorLed.getBlueLed().getMax());
        blueLed.getSettings().setOutputMin(triColorLed.getBlueLed().getMin());

        // overwrite old object
        GlobalHandler.getInstance(getActivity()).sessionHandler.getSession().getFlutter().getTriColorLeds()[triColorLed.getPortNumber()-1] = triColorLed;

        dialogLedListener.onLedLinkListener(msg);
        this.dismiss();
    }


    @OnClick(R.id.image_advanced_settings)
    public void onClickAdvancedSettings() {
        Log.d(Constants.LOG_TAG, "onClickAdvancedSettings");
        DialogFragment dialog = AdvancedSettingsDialog.newInstance(this, triColorLed);
        dialog.show(this.getFragmentManager(), "tag");
    }


    @OnClick(R.id.linear_set_linked_sensor)
    public void onClickSetLinkedSensor(View view) {
        Log.d(Constants.LOG_TAG, "onClickSetLinkedSensor");
        currentImageView = (ImageView) ((ViewGroup) view).getChildAt(0);
        View layout = ((ViewGroup) view).getChildAt(1);
        currentTextViewDescrp = (TextView) ((ViewGroup) layout).getChildAt(0);
        currentTextViewItem = (TextView) ((ViewGroup) layout).getChildAt(1);
        DialogFragment dialog = SensorOutputDialog.newInstance(this);
        dialog.show(this.getFragmentManager(), "tag");
    }


    @OnClick(R.id.linear_set_relationship)
    public void onClickSetRelationship(View view) {
        Log.d(Constants.LOG_TAG, "onClickSetRelationship");
        currentImageView = (ImageView) ((ViewGroup) view).getChildAt(0);
        View layout = ((ViewGroup) view).getChildAt(1);
        currentTextViewDescrp = (TextView) ((ViewGroup) layout).getChildAt(0);
        currentTextViewItem = (TextView) ((ViewGroup) layout).getChildAt(1);
        DialogFragment dialog = RelationshipOutputDialog.newInstance(this);
        dialog.show(this.getFragmentManager(), "tag");
    }


    @OnClick(R.id.linear_set_max_color)
    public void onClickSetMaximumColor(View view) {
        Log.d(Constants.LOG_TAG, "onClickSetMaximumColor");
        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) view).getChildAt(0);
        currentImageView = (ImageView) viewGroup.getChildAt(0);
        View layout = ((ViewGroup) view).getChildAt(1);
        currentTextViewDescrp = (TextView) ((ViewGroup) layout).getChildAt(0);
        currentTextViewItem = (TextView) ((ViewGroup) layout).getChildAt(1);
        DialogFragment dialog = MaxColorDialog.newInstance(triColorLed.getMaxColorText(),this);
        dialog.show(this.getFragmentManager(), "tag");
    }


    @OnClick(R.id.linear_set_min_color)
    public void onclickSetMinimumColor(View view) {
        Log.d(Constants.LOG_TAG, "onClickSetMinimumColor");
        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) view).getChildAt(0);
        currentImageView = (ImageView) viewGroup.getChildAt(0);
        View layout = ((ViewGroup) view).getChildAt(1);
        currentTextViewDescrp = (TextView) ((ViewGroup) layout).getChildAt(0);
        currentTextViewItem = (TextView) ((ViewGroup) layout).getChildAt(1);
        DialogFragment dialog = MinColorDialog.newInstance(triColorLed.getMinColorText(),this);
        dialog.show(this.getFragmentManager(), "tag");
    }


    // option listeners


    @Override
    public void onAdvancedSettingsSet(AdvancedSettings advancedSettings) {
        Log.d(Constants.LOG_TAG, "onAdvancedSettingsSet");

        triColorLed.getRedLed().getSettings().setAdvancedSettings(advancedSettings);
        triColorLed.getGreenLed().getSettings().setAdvancedSettings(advancedSettings);
        triColorLed.getBlueLed().getSettings().setAdvancedSettings(advancedSettings);
    }


    @Override
    public void onSensorChosen(Sensor sensor) {
        if (sensor.getSensorType() != FlutterProtocol.InputTypes.NOT_SET) {
            Log.d(Constants.LOG_TAG, "onSensorChosen");
            currentImageView.setImageResource(sensor.getGreenImageId());
            currentTextViewDescrp.setText(R.string.linked_sensor);
            currentTextViewItem.setText(sensor.getSensorTypeId());

            triColorLed.getRedLed().getSettings().setSensor(sensor);
            triColorLed.getGreenLed().getSettings().setSensor(sensor);
            triColorLed.getBlueLed().getSettings().setSensor(sensor);
            saveButton.setEnabled(true);
        }
        updateViews(dialogView);
    }


    @Override
    public void onRelationshipChosen(Relationship relationship) {
        Log.d(Constants.LOG_TAG, "onRelationshipChosen");
        currentImageView.setImageResource(relationship.getGreenImageIdMd());
        currentTextViewDescrp.setText(R.string.relationship);
        currentTextViewItem.setText(relationship.getRelationshipTypeId());
        triColorLed.getRedLed().getSettings().setRelationship(relationship);
        triColorLed.getGreenLed().getSettings().setRelationship(relationship);
        triColorLed.getBlueLed().getSettings().setRelationship(relationship);
    }


    @Override
    public void onHighColorChosen(int[] rgb, int swatch) {
        Log.d(Constants.LOG_TAG, "onHighColorChosen");
        currentImageView.setVisibility(View.GONE);
        currentTextViewDescrp.setText(R.string.maximum_color);
        int max = getProportionalValue(rgb[0], 255, triColorLed.getRedLed().getMax());
        triColorLed.getRedLed().getSettings().setOutputMax(max);
        max = getProportionalValue(rgb[1], 255, triColorLed.getGreenLed().getMax());
        triColorLed.getGreenLed().getSettings().setOutputMax(max);
        max = getProportionalValue(rgb[2], 255, triColorLed.getBlueLed().getMax());
        triColorLed.getBlueLed().getSettings().setOutputMax(max);
        maxColor.setImageResource(triColorLed.getMaxSwatch());
        maxColor.setVisibility(View.VISIBLE);
        currentTextViewItem.setText(triColorLed.getMaxColorText());
    }


    @Override
    public void onLowColorChosen(int[] rgb, int swatch) {
        Log.d(Constants.LOG_TAG, "onLowColorChosen");
        currentImageView.setVisibility(View.GONE);
        currentTextViewDescrp.setText(R.string.minimum_color);
        int min = getProportionalValue(rgb[0], 255, triColorLed.getRedLed().getMax());
        triColorLed.getRedLed().getSettings().setOutputMin(min);
        min = getProportionalValue(rgb[1], 255, triColorLed.getGreenLed().getMax());
        triColorLed.getGreenLed().getSettings().setOutputMin(min);
        min = getProportionalValue(rgb[2], 255, triColorLed.getBlueLed().getMax());
        triColorLed.getBlueLed().getSettings().setOutputMin(min);
        minColor.setImageResource(triColorLed.getMinSwatch());
        minColor.setVisibility(View.VISIBLE);
        currentTextViewItem.setText(triColorLed.getMinColorText());
    }


    public interface DialogLedListener {
        public void onLedLinkListener(ArrayList<MelodySmartMessage> msgs);
    }

}
