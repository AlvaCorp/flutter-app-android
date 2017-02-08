package org.cmucreatelab.flutter_android.helpers.static_classes;

import android.graphics.Color;

/**
 * Created by Steve on 5/26/2016.
 *
 * Constants
 *
 * A class that handles global constants that is used throughout the app.
 *
 */
public final class Constants {

    public static final String LOG_TAG = "FlutterAndroid";

    public static final String APP_VERSION = "1.0.0";

    public static final String FLUTTER_MAC_ADDRESS = "20:FA:BB";

    public static final String NAMES_TABLE_FILE = "names_table.txt";

    public static final String DATA_SET_PREFIX = "DATA_SET_";

    public static final String EMAIL_SUBJECT = "Flutter Data Log";

    public final class MusicNoteFrequencies {
        public static final int C_4 = 262;
        public static final int D_4 = 294;
        public static final int E_4 = 330;
        public static final int F_4 = 349;
        public static final int G_4 = 392;
        public static final int A_4 = 440;
        public static final int B_4 = 494;
        public static final int C_5 = 523;
        public static final int D_5 = 587;
        public static final int E_5 = 659;
        public static final int F_5 = 698;
        public static final int G_5 = 784;
        public static final int A_5 = 880;
        public static final int B_5 = 988;
        public static final int C_6 = 1047;
    }

    public static final class ColorSwatches {
        public static final int RED = Color.parseColor("#ff0000");
        public static final int ORANGE = Color.parseColor("#ff7f00");
        public static final int YELLOW = Color.parseColor("#ffff00");
        public static final int CHARTREUSE_GREEN = Color.parseColor("#7fff00");
        public static final int GREEN = Color.parseColor("#00ff00");
        public static final int SPRING_GREEN = Color.parseColor("#00ff7f");
        public static final int CYAN = Color.parseColor("#00ffff");
        public static final int AZURE = Color.parseColor("#007fff");
        public static final int BLUE = Color.parseColor("#0000ff");
        public static final int VIOLET = Color.parseColor("#7f00ff");
        public static final int MAGENTA = Color.parseColor("#ff00ff");
        public static final int ROSE = Color.parseColor("#ff007f");
        public static final int WHITE = Color.parseColor("#ffffff");
        public static final int BLACK = Color.parseColor("#000000");
    }

    // forces MessageQueue to throw away all "r" requests
    public static final boolean IGNORE_READ_SENSORS = false;

    // for activities

    public static final class SerializableKeys {
        public static final String DIALOG_LED = "dialog_led";
        public static final String DIALOG_SERVO = "dialog_servo";
        public static final String DIALOG_SPEAKER = "dialog_speaker";
        public static String SENSOR_KEY = "sensor_key";
        public static String SENSOR_PORT_KEY = "sensor_text_key";
        public static String RELATIONSHIP_KEY = "relationship_key";
    }

}
