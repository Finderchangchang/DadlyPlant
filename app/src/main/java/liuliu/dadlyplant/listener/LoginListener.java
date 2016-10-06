package liuliu.dadlyplant.listener;

import liuliu.dadlyplant.mview.LoginMView;
import liuliu.dadlyplant.model.MeiNvModel;
import liuliu.dadlyplant.view.ILoginView;

/**
 * Created by Administrator on 2016/9/19.
 */

public class LoginListener implements LoginMView {
    private ILoginView view;
    private MeiNvModel model;

    public LoginListener(ILoginView view) {
        this.view = view;
    }

    @Override
    public void clear() {
        view.clearText();
    }

    @Override
    public void doLogin(String name, String pwd) {
        view.loginResult(true);
    }
}
