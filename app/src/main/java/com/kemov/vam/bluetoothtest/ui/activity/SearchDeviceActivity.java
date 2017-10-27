package com.kemov.vam.bluetoothtest.ui.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.kemov.vam.bluetoothtest.R;
import com.kemov.vam.bluetoothtest.commons.listview.CommonAdapterHelper.CommonAdapter;
import com.kemov.vam.bluetoothtest.commons.listview.CommonAdapterHelper.ViewHolder;
import com.kemov.vam.bluetoothtest.utils.ClsUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by RuanJian-GuoYong on 2017/10/26.
 */

public class SearchDeviceActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "SearchDeviceActivity";
    static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    ListView lv_bltDevices = null;
    CommonAdapter<BluetoothDevice> adpter;
    List<BluetoothDevice> devices;


    public static BluetoothSocket btSocket;
    BluetoothAdapter bltAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_search_ble_device);

        initData();
        initView();

        initBltReceiver();
    }

    private void initData() {
        bltAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    // 注册Receiver来获取蓝牙设备相关的结果
    private void initBltReceiver() {
        if (bltAdapter.cancelDiscovery())
            bltAdapter.startDiscovery();

        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_FOUND);// 用BroadcastReceiver来取得搜索结果
        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(searchDevices, intent);
    }

    private void initView() {
        lv_bltDevices = (ListView) findViewById(R.id.lv_bltDevices);
        Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        devices = new ArrayList<>(pairedDevices);
        Log.e(TAG, "initView:  size= "+devices.size());
        adpter = new CommonAdapter<BluetoothDevice>(this,devices,R.layout.device_item) {
            @Override
            public void convert(ViewHolder viewHolder, BluetoothDevice itemBean) {
                viewHolder.setText(R.id.tv_bltName,itemBean.getName());
                viewHolder.setText(R.id.tv_bltAddr,itemBean.getAddress());
            }
        };
        lv_bltDevices.setAdapter(adpter);
        lv_bltDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (bltAdapter.isDiscovering())
                    bltAdapter.cancelDiscovery();
                //~~~~~connetToDevice(devices.get(position));

                //ClsUtils.createBond(devices.get(position).getClass(),devices.get(position));
                try {
                    boolean connected = devices.get(position).createBond();//ClsUtils.createBond(BluetoothDevice.class,devices.get(position));
                    if(connected){
                        Toast.makeText(SearchDeviceActivity.this, "配对成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SearchDeviceActivity.this, "配对未成功", Toast.LENGTH_SHORT).show();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        this.unregisterReceiver(searchDevices);
        super.onDestroy();
        //android.os.Process.killProcess(android.os.Process.myPid());
    }

    /*
    * 配对/接触配对 参考：
    * http://blog.csdn.net/love_xsq/article/details/50379330
    * */
    public void connetToDevice(BluetoothDevice bluetoothDevice){
        try {
            Boolean returnValue = false;
            if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                //利用反射方法调用BluetoothDevice.createBond(BluetoothDevice remoteDevice);
                Method createBondMethod = BluetoothDevice.class
                        .getMethod("createBond");
                Log.e(TAG, "开始配对");
                Toast.makeText(this, "开始配对", Toast.LENGTH_SHORT).show();
                returnValue = (Boolean) createBondMethod.invoke(bluetoothDevice);
                Log.e(TAG, "connetToDevice: returnValue="+returnValue);

            }else if(bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                //没有配对过的将会走这里。
                connect(bluetoothDevice);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View v) {

    }


    private BroadcastReceiver searchDevices = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle b = intent.getExtras();
            Object[] lstName = b.keySet().toArray();

            // 显示所有收到的消息及其细节
            for (int i = 0; i < lstName.length; i++) {
                String keyName = lstName[i].toString();
                Log.e(keyName, String.valueOf(b.get(keyName)));
            }
            BluetoothDevice device = null;
            // 搜索设备时，取得设备的MAC地址
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    /*String str = "未配对|" + device.getName() + "|"
                            + device.getAddress();*/
                    if (devices.indexOf(device) == -1)// 防止重复添加
                        devices.add(device);
                    adpter.notifyDataSetChanged();
                }
            }else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING:
                        Log.d("BlueToothTestActivity", "正在配对......");
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Log.d("BlueToothTestActivity", "完成配对");
                        connect(device);//连接设备
                        break;
                    case BluetoothDevice.BOND_NONE:
                        Log.d("BlueToothTestActivity", "取消配对");
                    default:
                        break;
                }
            }

        }
    };
    private void connect(BluetoothDevice btDev) {
        UUID uuid = UUID.fromString(SPP_UUID);
        try {
            btSocket = btDev.createRfcommSocketToServiceRecord(uuid);
            Log.d("BlueToothTestActivity", "开始连接...");
            btSocket.connect();
        } catch (IOException e) {
            Log.d("BlueToothTestActivity", "连接失败...");
            try {
                btSocket.close();
            } catch (IOException closeException) {
            }
            e.printStackTrace();
        }
    }
}
