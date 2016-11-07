package liuliu.dadlyplant.ui;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.widget.Button;

import net.tsz.afinal.annotation.view.CodeNote;

import liuliu.dadlyplant.R;
import liuliu.dadlyplant.base.BaseActivity;

/**
 * 蓝牙打印页面
 * Created by Administrator on 2016/10/8.
 */

public class BlueprintActivity extends BaseActivity {
    @CodeNote(id = R.id.btn)
    Button btn;
    private BluetoothAdapter mBluetoothAdapter = null;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    @Override
    public void initViews() {
        setContentView(R.layout.activity_blue_print);
    }

    @Override
    public void initEvents() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            ToastShort("您的设备不支持蓝牙");
            finish();//关闭当前页面
            return;
        }
        btn.setOnClickListener(v -> {

        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            //打开蓝牙
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
//        if (mService == null) {
//            mService = new BluetoothService(this, mHandler);
//        }
    }
}
