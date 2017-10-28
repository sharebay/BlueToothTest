package com.kemov.vam.bluetoothtest.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;

/**
 * Created by RuanJian-GuoYong on 2017/10/28.
 */

public class BlueToothManager {

    private static BlueToothManager instance;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBluetoothSocket;
    private BluetoothManager bluetoothManager;

    private BlueToothManager(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static BlueToothManager getInstance() {
        if (instance == null){
            instance = new BlueToothManager();
        }
        return instance;
    }

    public void initBltManager(Context context) {
        if (bluetoothManager != null) return;
        //首先获取BluetoothManager
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        //获取BluetoothAdapter
        if (bluetoothManager != null)
            mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    public void blueToothOperationEvent(Context context,int eventId){
        switch (eventId){
            case BlueToothConstants.BLUE_TOOTH_SEARTH:
                startSearthBltDevice(context);
                break;
        }
    }

    /**
     * 扫描附近的蓝牙
    * */
    private boolean startSearthBltDevice(Context context) {
        //开始搜索设备，当搜索到一个设备的时候就应该将它添加到设备集合中，保存起来
        checkBleDevice(context);
        //如果当前发现了新的设备，则停止继续扫描，当前扫描到的新设备会通过广播推向新的逻辑
        if (mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.cancelDiscovery();
        Log.i("bluetooth", "本机蓝牙地址：" + mBluetoothAdapter.getAddress());
        //开始搜索
        mBluetoothAdapter.startDiscovery();
        return true;
    }

    public void checkBleDevice(Context context) {
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(enableBtIntent);
            }
        } else {
            Log.i("blueTooth", "该手机不支持蓝牙");
        }
    }

    public void createBond(BluetoothDevice btDev, Handler handler) {
        if (btDev.getBondState() == BluetoothDevice.BOND_NONE) {
            //如果这个设备取消了配对，则尝试配对
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                btDev.createBond();
            }
        } else if (btDev.getBondState() == BluetoothDevice.BOND_BONDED) {
            //如果这个设备已经配对完成，则尝试连接
            connect(btDev, handler);
        }

    }

    private void connect(BluetoothDevice btDev, Handler handler) {
        try {
            //通过和服务器协商的uuid来进行连接
            mBluetoothSocket = btDev.createRfcommSocketToServiceRecord(BlueToothConstants.SPP_UUID);
            if (mBluetoothSocket != null)
                //全局只有一个bluetooth，所以我们可以将这个socket对象保存在appliaction中
                //gy  BltAppliaction.bluetoothSocket = mBluetoothSocket;
            //通过反射得到bltSocket对象，与uuid进行连接得到的结果一样，但这里不提倡用反射的方法
            //mBluetoothSocket = (BluetoothSocket) btDev.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(btDev, 1);
            Log.d("blueTooth", "开始连接...");
            //在建立之前调用
            if (mBluetoothAdapter.isDiscovering())
                //停止搜索
                mBluetoothAdapter.cancelDiscovery();
            //如果当前socket处于非连接状态则调用连接
            if (!mBluetoothSocket.isConnected()) {
                //你应当确保在调用connect()时设备没有执行搜索设备的操作。
                // 如果搜索设备也在同时进行，那么将会显著地降低连接速率，并很大程度上会连接失败。
                mBluetoothSocket.connect();
            }
            Log.d("blueTooth", "已经链接");
            if (handler == null) return;
            //结果回调
            Message message = new Message();
            message.what = 4;
            message.obj = btDev;
            handler.sendMessage(message);
        } catch (Exception e) {
            Log.e("blueTooth", "...链接失败");
            try {
                mBluetoothSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    public void registerBlueToothReceiver(Context context, OnRegisterBltReceiver onRegisterBltReceiver){
        this.onRegisterBltReceiver = onRegisterBltReceiver;
        // 用BroadcastReceiver来取得搜索结果
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_FOUND);//搜索发现设备
        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//状态改变
        intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);//行动扫描模式改变了
        intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//动作状态发生了变化
        context.registerReceiver(searchDevices, intent);
        Log.e("gy", "gy.registerBlueToothReceiver:  广播接受者注册成功！");
    }

    /**
     * 蓝牙接收广播
     */
    private BroadcastReceiver searchDevices = new BroadcastReceiver() {
        //接收
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle b = intent.getExtras();
            Object[] lstName = b.keySet().toArray();

            Log.e("gy", "gy.onReceive: Action="+action );

            // 显示所有收到的消息及其细节
            for (int i = 0; i < lstName.length; i++) {
                String keyName = lstName[i].toString();
                Log.e("gy", "gy."+keyName + ">>>" + String.valueOf(b.get(keyName)));
            }
            BluetoothDevice device;
            // 搜索发现设备时，取得设备的信息；注意，这里有可能重复搜索同一设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.e("gy", "gy.onReceive: "+"扫描到一个蓝牙设备" );
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                onRegisterBltReceiver.onBluetoothDevice(device);
            }
            //状态改变时
            else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING://正在配对
                        Log.d("BlueToothTestActivity", "正在配对......");
                        onRegisterBltReceiver.onConnecting(device);
                        break;
                    case BluetoothDevice.BOND_BONDED://配对结束
                        Log.d("BlueToothTestActivity", "完成配对");
                        onRegisterBltReceiver.onConnected(device);
                        break;
                    case BluetoothDevice.BOND_NONE://取消配对/未配对
                        Log.d("BlueToothTestActivity", "取消配对");
                        onRegisterBltReceiver.onUnConnected(device);
                    default:
                        break;
                }
            }
        }
    };

    /**
     * 蓝牙状态接口
     */
    private OnRegisterBltReceiver onRegisterBltReceiver;

    public interface OnRegisterBltReceiver {
        void onBluetoothDevice(BluetoothDevice device);//搜索到新设备

        void onConnecting(BluetoothDevice device);//连接中

        void onConnected(BluetoothDevice device);//连接完成

        void onUnConnected(BluetoothDevice device);//未连接
    }

}
