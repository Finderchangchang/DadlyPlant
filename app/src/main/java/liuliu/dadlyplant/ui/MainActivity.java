package liuliu.dadlyplant.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.cache.ACache;

import java.util.List;

import liuliu.dadlyplant.R;
import liuliu.dadlyplant.listener.LoginListener;
import liuliu.dadlyplant.model.GitHubAPI;
import liuliu.dadlyplant.model.UserModel;
import liuliu.dadlyplant.view.ILoginView;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends FinalActivity implements ILoginView {
    ImageView iv;
    LoginListener listener;
    private ACache mCache;
    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                //设置baseUrl,注意，baseUrl必须后缀"/"
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        retrofit.create(GitHubAPI.class).userInfo("baiiu")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userModel -> {
                    Log.i("TAG", userModel.getBlog());
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void clearText() {

    }

    @Override
    public void loginResult(boolean result) {
        Toast.makeText(this, mCache.getAsString("name") + mCache.getAsString("pwd"), Toast.LENGTH_SHORT).show();
    }
}
