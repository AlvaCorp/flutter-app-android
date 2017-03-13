package org.cmucreatelab.flutter_android.ui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import org.cmucreatelab.flutter_android.R;
import org.cmucreatelab.flutter_android.activities.DataLogsActivity;
import org.cmucreatelab.flutter_android.helpers.GlobalHandler;
import org.cmucreatelab.flutter_android.helpers.static_classes.Constants;

import java.io.Serializable;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Steve on 1/9/2017.
 */

public class RecordDataLoggingDialog extends BaseDataLoggingDialog implements Serializable {

    private DialogRecordDataLoggingListener dialogRecordDataLoggingListener;
    private DataLogsActivity dataLogsActivity;

    private EditText dataSetNameText;
    private EditText intervalsText;
    private Spinner intervalSpinner;
    private EditText timePeriodText;
    private Spinner timePeriodSpinner;


    public static RecordDataLoggingDialog newInstance(Serializable serializable) {
        RecordDataLoggingDialog recordDataLoggingDialog = new RecordDataLoggingDialog();

        Bundle args = new Bundle();
        args.putSerializable(DataLogsActivity.DATA_LOGS_ACTIVITY_KEY, serializable);
        recordDataLoggingDialog.setArguments(args);

        return recordDataLoggingDialog;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        dialogRecordDataLoggingListener = (DialogRecordDataLoggingListener) getArguments().getSerializable(DataLogsActivity.DATA_LOGS_ACTIVITY_KEY);
        dataLogsActivity = (DataLogsActivity) getArguments().getSerializable(DataLogsActivity.DATA_LOGS_ACTIVITY_KEY);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_datalogging_record_data, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppTheme));
        builder.setView(view);
        ButterKnife.bind(this, view);

        dataSetNameText = (EditText) view.findViewById(R.id.edit_data_set_name);
        intervalsText = (EditText) view.findViewById(R.id.edit_number_of_intervals);
        intervalSpinner = (Spinner) view.findViewById(R.id.spinner_dropdown_interval);
        timePeriodText = (EditText) view.findViewById(R.id.edit_time_period);
        timePeriodSpinner = (Spinner) view.findViewById(R.id.spinner_dropdown_time);

        intervalSpinner = (Spinner) view.findViewById(R.id.spinner_dropdown_interval);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.time_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        intervalSpinner.setAdapter(adapter);

        timePeriodSpinner = (Spinner) view.findViewById(R.id.spinner_dropdown_time);
        adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.times_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timePeriodSpinner.setAdapter(adapter);

        return builder.create();
    }


    @OnClick(R.id.button_start_recording)
    public void onClickButtonStartRecording() {
        Log.d(Constants.LOG_TAG, "onClickButtonStartRecording");
        String name = dataSetNameText.getText().toString();
        if (!name.matches("")) {
            String intervalString = intervalsText.getText().toString();
            if (!intervalString.matches("")) {
                String timerPeriodString = timePeriodText.getText().toString();
                if (!timerPeriodString.matches("")) {
                    if (!getIsWaitingForResponse()) {
                        if (!getIsLogging()) {
                            GlobalHandler globalHandler = GlobalHandler.getInstance(getActivity());
                            int iInt = Integer.parseInt(intervalsText.getText().toString());
                            String iString = intervalSpinner.getSelectedItem().toString();
                            int tInt = Integer.parseInt(timePeriodText.getText().toString());
                            String tString = timePeriodSpinner.getSelectedItem().toString();
                            globalHandler.dataLoggingHandler.saveDataLogDetails(getActivity(), iInt, iString, tInt, tString);

                            int intervalsT = Integer.valueOf(intervalString);
                            // in seconds
                            int interval = 0;

                            String temp = intervalSpinner.getSelectedItem().toString();
                            interval = timeToSeconds(temp);
                            interval = interval / intervalsT;

                            int timePeriodT = Integer.valueOf(timerPeriodString);
                            // in seconds
                            int timePeriod = 0;
                            temp = timePeriodSpinner.getSelectedItem().toString();
                            timePeriod = timeToSeconds(temp);
                            timePeriod = timePeriodT * timePeriod;
                            int sample = timePeriod / interval;

                            Log.d(Constants.LOG_TAG, "RecordDataLoggingDialog - " + name);
                            Log.d(Constants.LOG_TAG, "RecordDataLoggingDialog - " + interval);
                            Log.d(Constants.LOG_TAG, "RecordDataLoggingDialog - " + sample);

                            dialogRecordDataLoggingListener.onRecordData(name, interval, sample);

                            DataLoggingConfirmationDataLog dataLoggingConfirmationDataLog = DataLoggingConfirmationDataLog.newInstance(dataLogsActivity);
                            dataLoggingConfirmationDataLog.show(getFragmentManager(), "tag");

                            this.dismiss();
                        } else {
                            IsRecordingDialog isRecordingDialog = IsRecordingDialog.newInstance(this);
                            isRecordingDialog.show(getFragmentManager(), "tag");
                            this.dismiss();
                        }
                    }
                } else {
                    timePeriodText.setError(getString(R.string.this_field_cannot_be_blank));
                }
            } else {
                intervalsText.setError(getString(R.string.this_field_cannot_be_blank));
            }
        } else {
            dataSetNameText.setError(getString(R.string.this_field_cannot_be_blank));
        }
    }


    public interface DialogRecordDataLoggingListener {
        public void onRecordData(String name, int interval, int sample);
    }

}
