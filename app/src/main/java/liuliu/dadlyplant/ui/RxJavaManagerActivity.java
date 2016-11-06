package liuliu.dadlyplant.ui;


import liuliu.dadlyplant.R;
import liuliu.dadlyplant.base.BaseActivity;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * RxJava学习管理界面
 * Created by Administrator on 2016/11/6.
 */
public class RxJavaManagerActivity extends BaseActivity {
    @Override
    public void initViews() {
        setContentView(R.layout.activity_rx_java_manager);
        Observable observable = Observable.just("1", "2", "3", "4");
        String[] vals = {"11", "21", "31"};
        observable = Observable.from(vals);//存储在键值对中，依次抛出
        observable.create((Observable.OnSubscribe<String>) subscriber -> {
            subscriber.onNext("999");
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(val -> {

                        }
                );
        Observable.just("123")//耗时操作
                .subscribeOn(Schedulers.io())//存储，或者说获取123的操作在子线程
                .subscribeOn(AndroidSchedulers.mainThread())//下面的操作在主线程
                .subscribe(val -> {

                });
    }

    @Override
    public void initEvents() {

    }
}
