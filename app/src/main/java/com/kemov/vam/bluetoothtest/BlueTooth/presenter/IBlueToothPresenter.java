package com.kemov.vam.bluetoothtest.BlueTooth.presenter;

import android.bluetooth.BluetoothDevice;

import java.util.List;

/**
 * Created by RuanJian-GuoYong on 2017/10/27.
 */

public interface IBlueToothPresenter {
    boolean turnOnBlueTooth();
    boolean turnOffBlueTooth();

    void initBlueToothStatus();

    List<BluetoothDevice> getBlueToothDevices();

}
