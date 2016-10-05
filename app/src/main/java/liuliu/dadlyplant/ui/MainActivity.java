package liuliu.dadlyplant.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.cache.ACache;

import java.io.IOException;
import java.util.List;

import liuliu.dadlyplant.R;
import liuliu.dadlyplant.listener.LoginListener;
import liuliu.dadlyplant.method.Utils;
import liuliu.dadlyplant.model.GitHubAPI;
import liuliu.dadlyplant.model.UserModel;
import liuliu.dadlyplant.view.ILoginView;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
    OkHttpClient client;

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
        client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request().newBuilder()
                        .addHeader("apikey", "706da19045e60c089cd457bd10e5e733").build();
                return chain.proceed(request);
            }
        }).build();
        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                //设置baseUrl,注意，baseUrl必须后缀"/"
                .baseUrl("http://apis.baidu.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        retrofit.create(GitHubAPI.class)
                .userInfo("10")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userModel -> {
                    Toast.makeText(this, userModel.getMsg(), Toast.LENGTH_SHORT).show();
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
