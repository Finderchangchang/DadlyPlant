package liuliu.dadlyplant.ui;

import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import net.tsz.afinal.annotation.view.CodeNote;
import net.tsz.afinal.cache.ACache;

import java.util.ArrayList;
import java.util.List;

import liuliu.dadlyplant.R;
import liuliu.dadlyplant.base.BaseActivity;
import liuliu.dadlyplant.listener.LoginListener;
import liuliu.dadlyplant.method.BottomItemModel;
import liuliu.dadlyplant.method.BottomTabView;
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
    BottomTabView bottom_btv;
    private List<BottomItemModel> mBottom = new ArrayList<>();

    @Override
    public void initViews() {
        setContentView(R.layout.activity_main);
        listener = new LoginListener(this);
        mCache = ACache.get(this);
        mCache.put("name", "lwj");
        mCache.put("pwd", "pwd");
        mBottom.add(new BottomItemModel("首页", R.mipmap.loag));
        mBottom.add(new BottomItemModel("订单", R.mipmap.loag));
        mBottom.add(new BottomItemModel("我的", R.mipmap.loag));
        iv = (ImageView) findViewById(R.id.iv);
        bottom_btv = (BottomTabView) findViewById(R.id.bottssom_btv);
        iv.setOnClickListener(v -> listener.doLogin("123", ""));
        Glide.with(MainActivity.this)
                .load("http://jcodecraeer.com/uploads/20150327/1427445293711143.png")
                .into(iv);
        bottom_btv.setGridAdapter(mBottom, new BottomTabView.GridAdatper() {
            @Override
            public View getView(int index) {
                View view = getLayoutInflater().inflate(R.layout.view_bottom_tab,
                        null);
                ImageView iv = (ImageView) view.findViewById(R.id.iv);
                TextView tv = (TextView) view.findViewById(R.id.tv);
//                iv.setImageResource(srcs[index]);
                tv.setText("" + index);
                return view;
            }

            @Override
            public int getCount() {
                return 3;
            }
        });
        bottom_btv.setOnItemClickListener((v, index) -> {

        });
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
