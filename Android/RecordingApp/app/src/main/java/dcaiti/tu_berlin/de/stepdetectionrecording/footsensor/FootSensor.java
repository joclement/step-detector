package dcaiti.tu_berlin.de.stepdetectionrecording.footsensor;

import android.bluetooth.BluetoothDevice;

import java.io.File;
import java.io.IOException;

import dcaiti.tu_berlin.de.stepdetectionrecording.Logger;
import dcaiti.tu_berlin.de.stepdetectionrecording.Utils;

/**
 * Class to hold all the necessary fields, information related to 1 of the 2 footSensors.
 *
 * Created by Joris Clement on 08.01.18.
 */
class FootSensor {

    @SuppressWarnings("unused")
    private static final String TAG = FootSensor.class.getSimpleName();

    // has the mark Z on bottom
    private static final String FOOT_SENSOR_LEFT  = "CE:BD:39:BB:A0:D0";
    // has the mark L1 on top
    private static final String FOOT_SENSOR_RIGHT = "ED:CF:D1:CD:9B:AD";

    private static final java.lang.String LEFT_FOOT_SENSOR_FILENAME = "LeftFootSensor";
    private static final java.lang.String RIGHT_FOOT_SENSOR_FILENAME = "RightFootSensor";

    private FootSensorStatus intToEnumState() {
        switch (this.state) {
            case STATE_BLUETOOTH_OFF:
                return FootSensorStatus.OFF;
            case STATE_DISCONNECTED:
                return FootSensorStatus.DISCONNECTED;
            case STATE_CONNECTING:
                return FootSensorStatus.CONNECTING;
            case STATE_CONNECTED:
                return FootSensorStatus.CONNECTED;
        }
        throw new Error("Implementation error");
    }

    FootSensorStatus getState() {
        return intToEnumState();
    }

    // Bluetooth State machine
    @SuppressWarnings("unused")
    final static int STATE_BLUETOOTH_OFF = 1;
    final static int STATE_DISCONNECTED = 2;
    final static int STATE_CONNECTING = 3;
    final static int STATE_CONNECTED = 4;

    boolean boundRSCSensor;

    int state;

    BluetoothDevice footSensorBluetoothDevice;

    final String mac;

    Logger logger;

    Logger extraInfoLogger;

    FootSensor(boolean isLeft, File logFolder) throws IOException {
        this.boundRSCSensor = false;
        this.state = STATE_DISCONNECTED;
        this.mac = isLeft ? FOOT_SENSOR_LEFT : FOOT_SENSOR_RIGHT;

        String filename = this.isLeft() ? LEFT_FOOT_SENSOR_FILENAME : RIGHT_FOOT_SENSOR_FILENAME;
        this.logger = new Logger(Utils.createCsvFile(logFolder, filename));
        // Note: Dropped code here due to removal of Copyright protected file

        this.extraInfoLogger = new Logger(Utils.createCsvFile(logFolder, filename + "-extra"));
    }

    static boolean isLeft(String mac) {
        return FOOT_SENSOR_LEFT.equals(mac);
    }

    boolean isLeft() {
        return isLeft(this.mac);
    }

    static boolean isRight(String mac) {
        return FOOT_SENSOR_RIGHT.equals(mac);
    }

    boolean isRight() {
        return isRight(this.mac);
    }

    String side() {
        return side(this.mac);
    }

    static String side(String mac) {
        if (isLeft(mac)) {
            return "left";
        } else if (isRight(mac)) {
            return "right";
        } else {
            return "unknown";
        }
    }
}
