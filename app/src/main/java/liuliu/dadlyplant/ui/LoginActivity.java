package liuliu.dadlyplant.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.tsz.afinal.annotation.view.CodeNote;

import java.util.ArrayList;
import java.util.List;

import liuliu.dadlyplant.R;
import liuliu.dadlyplant.base.BaseActivity;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity {
    private static final String TAG = "TAG_LoginActivity";
    @CodeNote(id = R.id.email)
    AutoCompleteTextView mEmailView;
    @CodeNote(id = R.id.password)
    EditText mPasswordView;
    @CodeNote(id = R.id.login_progress)
    View mProgressView;
    @CodeNote(id = R.id.login_user_btn)
    Button login_user_btn;//登录
    @CodeNote(id = R.id.reg_user_btn)
    Button reg_user_btn;//注册
    @CodeNote(id = R.id.toolbar)
    Toolbar toolbar;

    @Override
    public void initViews() {
        setContentView(R.layout.activity_login);
    }

    @Override
    public void initEvents() {
        login_user_btn.setOnClickListener(v -> attemptLogin());
        reg_user_btn.setOnClickListener(v -> Log.i(TAG, "注册点击："));
        mPasswordView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == R.id.login || actionId == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }
            return false;
        });
        toolbar.setNavigationOnClickListener(v -> ToastShort("123123"));
    }

    private void attemptLogin() {
        mEmailView.setError(null);
        mPasswordView.setError(null);
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        View focusView;

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError("账号不能为空");
            focusView = mEmailView;
            focusView.requestFocus();
        } else if (TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError("密码太短，请重新输入");
            focusView = mPasswordView;
            focusView.requestFocus();
        } else {
            mProgressView.setVisibility(View.VISIBLE);
            Intent intent = new Intent(LoginActivity.this, ScrollingActivity.class);
            startActivity(intent);
            this.finish();
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }
}

