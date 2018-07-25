package org.cmucreatelab.flutter_android.ui.dialogs.wizards.robot_outputs_wizard.speaker;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.cmucreatelab.flutter_android.R;
import org.cmucreatelab.flutter_android.classes.relationships.Constant;
import org.cmucreatelab.flutter_android.classes.sensors.Sensor;
import org.cmucreatelab.flutter_android.helpers.GlobalHandler;
import org.cmucreatelab.flutter_android.helpers.static_classes.Constants;
import org.cmucreatelab.flutter_android.ui.dialogs.wizards.BaseResizableDialogWizard;
import org.cmucreatelab.flutter_android.ui.dialogs.wizards.robot_outputs_wizard.OutputWizard;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Parv on 6/28/18.
 */

public class ChooseVolumeSpeakerDialogWizard extends BaseResizableDialogWizard {

    SpeakerWizard.SpeakerWizardState wizardState;

    private int selectedValue = 0;

    private OUTPUT_TYPE outputType = OUTPUT_TYPE.MAX;

    private static final String DIALOG_TYPE = "dialog_type";


    public enum OUTPUT_TYPE {
        MIN, MAX
    }


    private TextView curentPosition;
    private SeekBar seekBarMaxMin;

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(convertDpToPx(390), ViewGroup.LayoutParams.WRAP_CONTENT);
    }


    public void updateWizardState() {
        wizardState = (SpeakerWizard.SpeakerWizardState) (wizard.getCurrentState());
    }


    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            Log.v(Constants.LOG_TAG, "onProgressChanged: selectedValue=" + selectedValue);
            selectedValue = i;
            curentPosition.setText(String.valueOf(selectedValue));
        }


        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }


        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };


    public static ChooseVolumeSpeakerDialogWizard newInstance(OutputWizard wizard, OUTPUT_TYPE type) {
        Bundle args = new Bundle();
        ChooseVolumeSpeakerDialogWizard dialogWizard = new ChooseVolumeSpeakerDialogWizard();
        args.putSerializable(BaseResizableDialogWizard.KEY_WIZARD, wizard);
        args.putSerializable(DIALOG_TYPE, type);
        dialogWizard.setArguments(args);

        return dialogWizard;
    }


    private void updateViewWithOptions() {
        //start off at 0 for constant relationships
        if (wizardState.volumeRelationshipType instanceof Constant) {
            wizardState.volumeMax = 0;
        } else if (wizardState.volumeMax == 0) {
            wizardState.volumeMax = 100;
        }

        if (this.outputType == OUTPUT_TYPE.MIN) {
            seekBarMaxMin.setProgress(wizardState.volumeMin);
        } else {
            seekBarMaxMin.setProgress(wizardState.volumeMax);
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_choose_position_wizard, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppTheme));
        builder.setView(view);
        ButterKnife.bind(this, view);
        this.outputType = (OUTPUT_TYPE) (getArguments().getSerializable(DIALOG_TYPE));

        Button nextButton = (Button) view.findViewById(R.id.button_next);
        nextButton.setBackgroundResource(R.drawable.round_green_button_bottom_right);

        updateWizardState();

        // grab info
        curentPosition = (TextView) view.findViewById(R.id.text_current_volume);
        seekBarMaxMin = (SeekBar) view.findViewById(R.id.seek_volume);
        seekBarMaxMin.setOnSeekBarChangeListener(seekBarChangeListener);

        updateViewWithOptions();
        updateTextViews(view);

        return builder.create();
    }


    private void updateTextViews(View view) {
        // views
        ((TextView) view.findViewById(R.id.text_output_title)).setText(getString(R.string.set_up_volume_speaker));
        ((ImageView) view.findViewById(R.id.text_output_title_icon)).setImageResource(R.drawable.link_icon_volume_high);
        ((TextView) view.findViewById(R.id.text_set_position)).setText(getPositionPrompt());
    }


    private String getPositionPrompt() {
        if (!(wizardState.volumeRelationshipType instanceof Constant)) {
            Sensor[] sensors = GlobalHandler.getInstance(this.getActivity()).sessionHandler.getSession().getFlutter().getSensors();

            switch (outputType) {
                case MIN:
                    return "Set the " + getString(sensors[wizardState.selectedSensorPortVolume - 1].getLowTextId()).toLowerCase() + " position";
                default:
                    return "Set the " + getString(sensors[wizardState.selectedSensorPortVolume - 1].getHighTextId()).toLowerCase() + " position";
            }
        } else {
            return "Set the constant position";
        }
    }


    @OnClick(R.id.button_back)
    public void onClickBack() {
        if (this.outputType == OUTPUT_TYPE.MIN) {
            wizardState.volumeMin = selectedValue;
        } else {
            wizardState.volumeMax = selectedValue;
            if (wizardState.volumeRelationshipType instanceof Constant) {

            } else {
                wizard.changeDialog(ChooseVolumeSpeakerDialogWizard.newInstance(wizard, ChooseVolumeSpeakerDialogWizard.OUTPUT_TYPE.MIN));
            }
        }
    }


    @OnClick(R.id.button_next)
    public void onClickNext() {
        if (this.outputType == OUTPUT_TYPE.MIN) {
            wizardState.volumeMin = selectedValue;
            wizard.changeDialog(ChooseVolumeSpeakerDialogWizard.newInstance(wizard, ChooseVolumeSpeakerDialogWizard.OUTPUT_TYPE.MAX));
        } else {
            wizardState.volumeMax = selectedValue;
            wizard.finish();
        }

    }


    @OnClick(R.id.image_advanced_settings)
    public void onClickAdvancedSettings() {
        Log.i(Constants.LOG_TAG, "onClickAdvancedSettings");
        wizard.finish();
    }


    @OnClick(R.id.button_close)
    public void onClickClose() {
        wizard.changeDialog(null);
    }
}
