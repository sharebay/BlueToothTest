package com.kemov.vam.bluetoothtest;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.SwipeDismissBehavior;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import net.flyget.bluetoothchat.activity.bluetoothMainActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final int REQUEST_OPEN_BT_CODE = 1;

    Context mCtx;
    Button startBT = null;
    Button btnScanBlueToothDevices = null;
    Button btnShowBlueToothDevicesList = null;
    TextView tvHelloWord = null;
    CoordinatorLayout layoutRoot = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCtx = this;
        intView();

    }

    private void intView() {

        layoutRoot = (CoordinatorLayout) findViewById(R.id.layoutRoot);

        tvHelloWord = (TextView) findViewById(R.id.tvHelloWord);

        startBT = (Button) findViewById(R.id.btnStartBlueT);
        startBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //IntentToBlueTooth();
                startBTService();
            }
        });
        btnScanBlueToothDevices = (Button) findViewById(R.id.btnScanBlueToothDevices);
        btnScanBlueToothDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!getDefaultBlueToothAdapter().isDiscovering()){
                    getDefaultBlueToothAdapter().startDiscovery();
                    // 开始显示进度
                    /*ProgressBar progressBar = new ProgressBar(mCtx);
                    progressBar.setEnabled(true);
                    progressBar.setBackgroundColor(Color.TRANSPARENT);
                    AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
                    builder.setView(progressBar);
                    builder.show();*/


                    ProgressDialog progressDialog = new ProgressDialog(mCtx);// 创建进度对话框对象
                    progressDialog.setTitle("搜索蓝牙设备"); // 设置标题
                    progressDialog.setMessage("搜索中..."); // 设置消息
                    progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            getDefaultBlueToothAdapter().cancelDiscovery();
                            Set<BluetoothDevice> pairedDevices;
                            pairedDevices = getDefaultBlueToothAdapter().getBondedDevices();
                            Toast.makeText(mCtx, "搜索到的设备个数为："+pairedDevices.size(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    progressDialog.show(); // 显示进度条


                }


                //当扫描的蓝牙设备则做出相应
                //afterScanedSomeDevices(getIntent());
            }
        });

        btnShowBlueToothDevicesList = (Button) findViewById(R.id.btnShowBlueToothDevicesList);
        btnShowBlueToothDevicesList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取扫描的蓝牙设备
                Set<BluetoothDevice> pairedDevices = getDefaultBlueToothAdapter().getBondedDevices();

                List<String> deviceNames = new ArrayList<String>();

                Iterator<BluetoothDevice> iterator =  pairedDevices.iterator();
                while (iterator.hasNext()){
                    BluetoothDevice device = iterator.next();
                    if (device.getAddress() != null){
                        deviceNames.add(device.getName()+"("+device.getAddress()+")");
                    }else {
                        deviceNames.add(device.getName());
                    }
                }
                String Names[] = new String[deviceNames.size()];
                for (int i = 0;i<deviceNames.size();i++){
                    Names[i] = deviceNames.get(i);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
                builder.setTitle("扫描到的设备列表");
                builder.setItems(Names,null);
                builder.show();
            }
        });
    }

    private void afterScanedSomeDevices(Intent intent) {
        //扫描到了任一蓝牙设备
        if(BluetoothDevice.ACTION_FOUND.equals(intent.getAction()))
        {
            Log.v(TAG, "### BT BluetoothDevice.ACTION_FOUND ##");

            BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if(btDevice != null){
                Log.v(TAG , "Name : " + btDevice.getName() + " Address: " + btDevice.getAddress());

            }
            else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction()))
            {
                Log.v(TAG, "### BT ACTION_BOND_STATE_CHANGED ##");

                int cur_bond_state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
                int previous_bond_state = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_NONE);


                Log.v(TAG, "### cur_bond_state ##" + cur_bond_state + " ~~ previous_bond_state" + previous_bond_state);
            }
        }
    }

    public BluetoothAdapter getDefaultBlueToothAdapter(){
        return BluetoothAdapter.getDefaultAdapter();
    }
    /*开启蓝牙服务*/
    private void startBTService() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null){
            //Snackbar.make(View view, CharSequence text, int duration)
            Snackbar.make(layoutRoot,"您的设备不支持蓝牙",Snackbar.LENGTH_SHORT).show();
            return;
        }

        // 打开蓝牙设备
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_OPEN_BT_CODE);
            //mBluetoothAdapter.enable();//开启蓝牙有两种方法。
            Log.e(TAG, "startBTService: 蓝牙设备开启中。。");

        }else{
            // 默认设备作为服务端
            //startServiceAsServer();
            mBluetoothAdapter.disable();
            Log.e(TAG, "startBTService: 蓝牙设备已关闭。。");
            Snackbar.make(layoutRoot,"蓝牙已关闭",Snackbar.LENGTH_SHORT).show();
        }



        ///~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~



        Snackbar snackbar = Snackbar.make(layoutRoot,"test\n\t\r123\n123",10000).setAction("确定", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentToBlueTooth();
            }
        });
        //snackbar.show();

        ViewGroup.LayoutParams params = tvHelloWord.getLayoutParams();
        if(params instanceof CoordinatorLayout.LayoutParams){
            CoordinatorLayout.LayoutParams p= (CoordinatorLayout.LayoutParams) params;
            CoordinatorLayout.Behavior behavior = p.getBehavior();
            if(behavior instanceof SwipeDismissBehavior){
                SwipeDismissBehavior sb= (SwipeDismissBehavior) behavior;
                sb.setListener(new SwipeDismissBehavior.OnDismissListener() {
                    @Override
                    public void onDismiss(View view) {
                        Log.e("zgh","onDismiss");
                    }

                    @Override
                    public void onDragStateChanged(int state) {
                        Log.e("zgh","onDragStateChanged state="+state);
                    }
                });
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_OPEN_BT_CODE){
            if (resultCode == RESULT_OK){
                Snackbar.make(layoutRoot,"蓝牙已开启",Snackbar.LENGTH_SHORT).show();
            }else if (resultCode == RESULT_CANCELED){
                Snackbar.make(layoutRoot,"开启蓝牙已取消",Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    /*进入demo的主界面*/
    private void IntentToBlueTooth() {
        Intent openBT = new Intent();
        openBT.setClass(mCtx,bluetoothMainActivity.class);
        mCtx.startActivity(openBT);
    }


}
