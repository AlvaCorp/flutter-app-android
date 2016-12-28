package org.cmucreatelab.flutter_android.helpers;

import android.bluetooth.BluetoothAdapter;

import com.bluecreation.melodysmart.BondingListener;
import com.bluecreation.melodysmart.DataService;
import com.bluecreation.melodysmart.MelodySmartDevice;
import com.bluecreation.melodysmart.MelodySmartListener;
import org.cmucreatelab.flutter_android.classes.flutters.FlutterOG;

/**
 * Created by mike on 12/27/16.
 */

public class MelodySmartDeviceHandler {

    private GlobalHandler globalHandler;
    private MelodySmartDevice mMelodySmartDevice; // used for connecting/disconnecting to a device and sending messages to the bluetooth device and back


    public MelodySmartDeviceHandler(GlobalHandler globalHandler) {
        this.globalHandler = globalHandler;
        mMelodySmartDevice = MelodySmartDevice.getInstance();
        mMelodySmartDevice.init(globalHandler.appContext);
    }


    public void connectFlutter(FlutterOG flutter) {
        if (!globalHandler.sessionHandler.isBluetoothConnected)
            mMelodySmartDevice.connect(flutter.getDevice().getAddress());
    }


    public void disconnectFlutter() {
        mMelodySmartDevice.disconnect();
    }


    public void startLeScan(final BluetoothAdapter.LeScanCallback leScanCallback) {
        mMelodySmartDevice.startLeScan(leScanCallback);
    }


    public void stopLeScan(final BluetoothAdapter.LeScanCallback leScanCallback) {
        mMelodySmartDevice.stopLeScan(leScanCallback);
    }


    public DataService getDataService() {
        return mMelodySmartDevice.getDataService();
    }


    public void registerListeners(BondingListener bondingListener, MelodySmartListener melodySmartListener, DataService.Listener dataServiceListener) {
        mMelodySmartDevice.registerListener(bondingListener);
        mMelodySmartDevice.registerListener(melodySmartListener);
        mMelodySmartDevice.getDataService().registerListener(dataServiceListener);
    }


    public void unregisterListeners(BondingListener bondingListener, MelodySmartListener melodySmartListener, DataService.Listener dataServiceListener) {
        mMelodySmartDevice.unregisterListener(bondingListener);
        mMelodySmartDevice.unregisterListener(melodySmartListener);
        mMelodySmartDevice.getDataService().unregisterListener(dataServiceListener);
    }

}
