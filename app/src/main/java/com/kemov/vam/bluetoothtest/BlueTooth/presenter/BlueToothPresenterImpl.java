package com.kemov.vam.bluetoothtest.BlueTooth.presenter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.kemov.vam.bluetoothtest.BlueTooth.model.BlueToothModelImpl;
import com.kemov.vam.bluetoothtest.BlueTooth.model.IBlueToothModel;
import com.kemov.vam.bluetoothtest.BlueTooth.view.IBlueToothView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by RuanJian-GuoYong on 2017/10/27.
 */

public class BlueToothPresenterImpl implements IBlueToothPresenter {

    private IBlueToothView mIBlueToothView;
    private IBlueToothModel mIBlueToothModel;
    private Context mCtx;

    public BlueToothPresenterImpl(Context context, IBlueToothView iBlueToothView){
        this.mCtx = context;
        this.mIBlueToothView = iBlueToothView;
        mIBlueToothModel = new BlueToothModelImpl();
    }

    private BluetoothAdapter getBluetoothAdapter(){
        return BluetoothAdapter.getDefaultAdapter();
    }
    public boolean isBlueToothEnabled(){
        return getBluetoothAdapter().isEnabled();
    }

    @Override
    public boolean turnOnBlueTooth() {
        return getBluetoothAdapter().enable();
    }

    @Override
    public boolean turnOffBlueTooth() {
        return getBluetoothAdapter().disable();
    }

    @Override
    public void initBlueToothStatus() {
        if (this.isBlueToothEnabled()){
            mIBlueToothView.turnOnBlueTooth();
        } else {
            mIBlueToothView.turnOffBlueTooth();
        }
    }

    @Override
    public List<BluetoothDevice> getBlueToothDevices() {
        return new ArrayList<>(BluetoothAdapter.getDefaultAdapter().getBondedDevices());
    }
}
