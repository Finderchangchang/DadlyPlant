package net.tsz.afinal.http;

/**
 * Created by LiuWeiJie on 2015/7/25 0025.
 * Email:1031066280@qq.com
 */
public abstract class AjaxCallBack<T> {
    private boolean progress = true;
    private int rate = 1000;

    public AjaxCallBack() {
    }

    public boolean isProgress() {
        return this.progress;
    }

    public int getRate() {
        return this.rate;
    }

    public AjaxCallBack<T> progress(boolean progress, int rate) {
        this.progress = progress;
        this.rate = rate;
        return this;
    }

    public void onStart() {
    }

    public void onLoading(long count, long current) {
    }

    public void onSuccess(T t) {
    }

    public void onFailure(Throwable t, int errorNo, String strMsg) {
    }
}
