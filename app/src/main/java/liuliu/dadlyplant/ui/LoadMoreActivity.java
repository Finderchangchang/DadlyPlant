package liuliu.dadlyplant.ui;

import android.widget.ListView;

import net.tsz.afinal.annotation.view.CodeNote;

import java.util.ArrayList;
import java.util.List;

import liuliu.dadlyplant.R;
import liuliu.dadlyplant.base.BaseActivity;
import liuliu.dadlyplant.model.CommonAdapter;
import liuliu.dadlyplant.model.CommonViewHolder;
import liuliu.dadlyplant.model.HttpUtil;
import liuliu.dadlyplant.model.MeiNvModel;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/11/6.
 */
public class LoadMoreActivity extends BaseActivity {
    @CodeNote(id = R.id.main_lv)
    ListView main_lv;
    RefreshLayout total_rl;
    CommonAdapter<MeiNvModel.NewslistBean> mAdapter;
    List<MeiNvModel.NewslistBean> mList;
    int now_index = 1;

    @Override
    public void initViews() {
        setContentView(R.layout.activity_load_more);
    }

    @Override
    public void initEvents() {
        total_rl = (RefreshLayout) findViewById(R.id.total_rl);
        mList = new ArrayList<>();
        mAdapter = new CommonAdapter<MeiNvModel.NewslistBean>(this, mList, R.layout.item_refresh) {
            @Override
            public void convert(CommonViewHolder holder, MeiNvModel.NewslistBean model, int position) {
                holder.setText(R.id.txt, model.getTitle());
                holder.setGlideImage(R.id.iv, model.getPicUrl());
            }
        };
        main_lv.setAdapter(mAdapter);
        loadMessage();
        total_rl.setOnLoadListener(() -> {
            now_index++;
            loadMessage();
        });
        total_rl.setOnRefreshListener(() -> {
            now_index = 1;
            loadMessage();
        });
    }

    private void loadMessage() {
        if (now_index == 1) {
            mList = new ArrayList<>();
        }
        HttpUtil.load().userInfo("10")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(userModel -> {//返回List
                    return Observable.from(userModel.getNewslist());
                })
                .toMap(model -> {//循环获得list中的model
                    mList.add(model);
                    return "";
                })
                .subscribe(val -> {
                    mAdapter.refresh(mList);
                    if (now_index > 1) {
                        total_rl.setLoading(false);
                    } else if (now_index == 1) {
                        total_rl.setRefreshing(false);
                    }
                });
    }
}
