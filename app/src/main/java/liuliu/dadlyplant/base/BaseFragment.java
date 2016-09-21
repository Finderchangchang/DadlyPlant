package liuliu.dadlyplant.base;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.tsz.afinal.FinalActivity;

/**
 * Fragment的基类
 * <p/>
 * Created by LiuWeiJie on 2015/7/29 0029.
 * Email:1031066280@qq.com
 */
public abstract class BaseFragment extends Fragment {
    int mLayId;
    private View viewRoot = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initViews();
        if (viewRoot == null) {
            viewRoot = inflater.inflate(mLayId, container, false);
        }
        ViewGroup parent = (ViewGroup) viewRoot.getParent();
        if (parent != null) {
            parent.removeView(viewRoot);
        }
        FinalActivity.initInjectedView(this, viewRoot);
        initEvents();
        return viewRoot;
    }

    public abstract void initViews();

    public abstract void initEvents();

    public void setContentView(int layoutId) {
        mLayId = layoutId;
    }

}
