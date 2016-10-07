package liuliu.dadlyplant.base;

import android.app.Application;
import android.content.Context;
import android.os.Vibrator;
import com.tencent.bugly.crashreport.CrashReport;

import cn.bmob.v3.Bmob;
import liuliu.dadlyplant.method.Utils;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class BaseApplication extends Application {
    private static Context context;
    public Vibrator mVibrator;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        //Bugly异常检测
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
        strategy.setAppVersion(Utils.getVersion());
        Bmob.initialize(this,"9703d5b739d4bba0669f6214d9068159");//初始化Bmob
//        CrashReport.initCrashReport(getApplicationContext(), "1105548720", false, strategy);
    }

    public static Context getContext() {
        return context;
    }

    //系统处于资源匮乏的状态
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}