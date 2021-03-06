package me.zoro.peachgardenmall.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.zoro.peachgardenmall.R;
import me.zoro.peachgardenmall.datasource.UserDatasource;
import me.zoro.peachgardenmall.datasource.UserRepository;
import me.zoro.peachgardenmall.datasource.remote.UserRemoteDatasource;
import me.zoro.peachgardenmall.utils.DensityUtil;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    public static final String USERNAME_EXTRA = "username";
    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.et_tel)
    TextInputEditText mEtTel;
    @BindView(R.id.et_password)
    TextInputEditText mEtPassword;
    @BindView(R.id.et_captcha)
    TextInputEditText mEtCaptcha;
    @BindView(R.id.btn_fetch_captcha)
    Button mBtnFetchCaptcha;
    @BindView(R.id.btn_register)
    Button mBtnRegister;
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;
    @BindView(R.id.progress_bar_title)
    TextView mProgressBarTitle;
    @BindView(R.id.progress_bar_container)
    LinearLayout mProgressBarContainer;

    private UserRepository mUserRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            final int statusBarHeight = DensityUtil.getStatusBarHeight(this);
            mToolbar.post(new Runnable() {
                @Override
                public void run() {
                    mToolbar.getLayoutParams().height += statusBarHeight;
                    mToolbar.setPadding(mToolbar.getPaddingLeft(),
                            statusBarHeight + mToolbar.getPaddingTop(),
                            mToolbar.getPaddingRight(),
                            mToolbar.getPaddingBottom());

                }
            });
        }

        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);

        mUserRepository = UserRepository.getInstance(UserRemoteDatasource.getInstance(getApplicationContext()));
    }

    @OnClick({R.id.btn_fetch_captcha, R.id.btn_register})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_fetch_captcha:
                // 获取验证码
                fetchCaptcha();
                break;
            case R.id.btn_register:
                register();
                break;
        }
    }

    private void fetchCaptcha() {
        String phone = mEtTel.getText().toString();
        if (TextUtils.isEmpty(phone)) {
            mEtTel.setError(getString(R.string.empty_phone_msg));
            return;
        }
        mUserRepository.fetchCaptcha(phone, new UserDatasource.GetCaptchaCallback() {
            @Override
            public void onFetchSuccess(String msg) {
                showMessage(msg);
                final int[] countDown = {60};
                final Handler handler = new Handler();
                if (!isFinishing()) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            countDown[0]--;
                            if (countDown[0] > 0) {
                                if (mBtnFetchCaptcha != null) {
                                    mBtnFetchCaptcha.setEnabled(false);
                                    mBtnFetchCaptcha.setText("倒计时（".concat(String.valueOf(countDown[0])).concat("s)"));
                                }
                                handler.postDelayed(this, 1000);
                            } else {
                                if (mBtnFetchCaptcha != null) {
                                    mBtnFetchCaptcha.setEnabled(true);
                                    mBtnFetchCaptcha.setText(R.string.fetch_captcha);
                                }
                                handler.removeCallbacks(this);
                            }
                        }
                    }, 1000);
                }
            }

            @Override
            public void onFetchFailure(String msg) {
                showMessage(msg);
            }
        });
    }

    private void register() {
        Map<String, Object> params = new HashMap<>();
        String phone = mEtTel.getText().toString();
        String password = mEtPassword.getText().toString();
        String captcha = mEtCaptcha.getText().toString();
        if (TextUtils.isEmpty(phone)) {
            mEtTel.setError(getString(R.string.empty_phone_msg));
            return;
        }
        if (TextUtils.isEmpty(password)) {
            mEtPassword.setError(getString(R.string.empty_password_msg));
            return;
        }
        if (TextUtils.isEmpty(captcha)) {
            mEtCaptcha.setError(getString(R.string.empty_captcha_msg));
            return;
        }
        params.put("phone", phone);
        params.put("password", password);
        params.put("captcha", captcha);
        setLoadingIndicator(true);
        mUserRepository.registerNewUser(params, new UserDatasource.RegisterUserCallback() {
            @Override
            public void onRegisterSuccess(String username) {
                setLoadingIndicator(false);
                showMessage(getString(R.string.register_success_msg));

                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.putExtra(USERNAME_EXTRA, username);
                startActivity(intent);
                finish();
            }

            @Override
            public void onRegisterFailure(String errorMsg) {
                setLoadingIndicator(false);

                showMessage(errorMsg);
            }
        });
    }

    private void setLoadingIndicator(boolean active) {
        if (mProgressBarContainer != null) {
            if (active) {
                //设置滚动条可见
                mProgressBarContainer.setVisibility(View.VISIBLE);
                mProgressBarTitle.setText(R.string.register_progress_bar_title);
            } else {
                if (mProgressBarContainer.getVisibility() == View.VISIBLE) {
                    mProgressBarContainer.setVisibility(View.GONE);
                }
            }
        }
    }

    private void showMessage(String msg) {
        if (!isFinishing()) {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }
    }
}
