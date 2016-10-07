package liuliu.dadlyplant.base;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.FinalDb;

/**
 * BaseActivity声明相关通用方法
 * <p/>
 * Created by LiuWeiJie on 2015/7/22 0022.
 * Email:1031066280@qq.com
 */
public abstract class BaseActivity extends FinalActivity {
    public FinalDb finalDb;
    public Toolbar toolbar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        finalDb = FinalDb.create(this, false);
        initViews();
        initEvents();
    }


    public abstract void initViews();

    public abstract void initEvents();

    private Toast toast = null;

    public void ToastShort(String msg) {
        if (toast == null) {
            toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }
}
