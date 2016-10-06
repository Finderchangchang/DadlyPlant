package liuliu.dadlyplant.ui;

import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import net.tsz.afinal.annotation.view.CodeNote;
import net.tsz.afinal.cache.ACache;

import liuliu.dadlyplant.R;
import liuliu.dadlyplant.base.BaseActivity;
import liuliu.dadlyplant.listener.LoginListener;
import liuliu.dadlyplant.model.HttpUtil;
import liuliu.dadlyplant.view.ILoginView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends BaseActivity implements ILoginView {
    ImageView iv;
    @CodeNote(id = R.id.total_slv)
    ScrollView total_slv;
    private LoginListener listener;
    private ACache mCache;

    @Override
    public void initViews() {
        setContentView(R.layout.activity_main);
        listener = new LoginListener(this);
        mCache = ACache.get(this);
        mCache.put("name", "lwj");
        mCache.put("pwd", "pwd");
        iv = (ImageView) findViewById(R.id.iv);
        iv.setOnClickListener(v -> listener.doLogin("123", ""));
        Glide.with(MainActivity.this)
                .load("http://jcodecraeer.com/uploads/20150327/1427445293711143.png")
                .into(iv);

        HttpUtil.load().userInfo("10")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userModel -> {
                    Toast.makeText(this, userModel.getMsg(), Toast.LENGTH_SHORT).show();
                });

    }

    @Override
    public void initEvents() {

    }

    @Override
    public void clearText() {

    }

    @Override
    public void loginResult(boolean result) {
        Toast.makeText(this, mCache.getAsString("name") + mCache.getAsString("pwd"), Toast.LENGTH_SHORT).show();
    }
}
