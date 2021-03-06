package org.cmucreatelab.flutter_android.ui.dialogs.robots_tab.outputs.speaker.tab_pitch;

import android.util.Log;

import org.cmucreatelab.flutter_android.classes.outputs.Speaker;
import org.cmucreatelab.flutter_android.classes.settings.SettingsAmplitude;
import org.cmucreatelab.flutter_android.classes.settings.SettingsChange;
import org.cmucreatelab.flutter_android.classes.settings.SettingsConstant;
import org.cmucreatelab.flutter_android.classes.settings.SettingsCumulative;
import org.cmucreatelab.flutter_android.classes.settings.SettingsFrequency;
import org.cmucreatelab.flutter_android.classes.settings.SettingsProportional;
import org.cmucreatelab.flutter_android.helpers.static_classes.Constants;
import org.cmucreatelab.flutter_android.ui.dialogs.robots_tab.outputs.speaker.SpeakerStateHelper;

/**
 * Created by mike on 3/27/17.
 *
 * SpeakerStateHelper when the Pitch tab is selected
 *
 */
public abstract class SpeakerPitchStateHelper extends SpeakerStateHelper {


    SpeakerPitchStateHelper(Speaker speaker) {
        super(TabType.PITCH, speaker);
    }


    public static SpeakerPitchStateHelper newInstance(Speaker speaker) {
        SpeakerPitchStateHelper result;

        if (speaker.getPitch().getSettings().getClass() == SettingsProportional.class) {
            result = SpeakerPitchProportional.newInstance(speaker);
        } else if (speaker.getPitch().getSettings().getClass() == SettingsConstant.class) {
            result = SpeakerPitchConstant.newInstance(speaker);
        } else if (speaker.getPitch().getSettings().getClass() == SettingsAmplitude.class) {
            result = SpeakerPitchAmplitude.newInstance(speaker);
        } else if (speaker.getPitch().getSettings().getClass() == SettingsFrequency.class) {
            result = SpeakerPitchFrequency.newInstance(speaker);
        } else if (speaker.getPitch().getSettings().getClass() == SettingsChange.class) {
            result = SpeakerPitchChange.newInstance(speaker);
        } else if (speaker.getPitch().getSettings().getClass() == SettingsCumulative.class) {
            result = SpeakerPitchCumulative.newInstance(speaker);
        } else {
            Log.w(Constants.LOG_TAG, "SpeakerPitchStateHelper.newInstance: settings/relationship not implemented, returning null.");
            result = null;
        }

        return result;
    }

}
