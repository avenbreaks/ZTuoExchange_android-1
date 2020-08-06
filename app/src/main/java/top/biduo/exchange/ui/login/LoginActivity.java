package top.biduo.exchange.ui.login;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gyf.barlibrary.ImmersionBar;
import com.tencent.captchasdk.TCaptchaDialog;
import com.tencent.captchasdk.TCaptchaVerifyListener;

import top.biduo.exchange.R;
import top.biduo.exchange.config.AppConfig;
import top.biduo.exchange.ui.dialog.CommonDialog;
import top.biduo.exchange.ui.dialog.CommonDialogFragment;
import top.biduo.exchange.ui.forgot_pwd.ForgotPwdActivity;
import top.biduo.exchange.ui.main.MainActivity;
import top.biduo.exchange.ui.signup.SignUpActivity;
import top.biduo.exchange.app.MyApplication;
import top.biduo.exchange.base.BaseActivity;
import top.biduo.exchange.entity.Captcha;
import top.biduo.exchange.entity.User;
import top.biduo.exchange.utils.SharedPreferenceInstance;
import top.biduo.exchange.utils.EncryUtils;
import top.biduo.exchange.utils.WonderfulCodeUtils;
import top.biduo.exchange.utils.WonderfulCommonUtils;
import top.biduo.exchange.utils.WonderfulLogUtils;
import top.biduo.exchange.utils.WonderfulStringUtils;
import top.biduo.exchange.utils.WonderfulToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import butterknife.BindView;
import top.biduo.exchange.app.Injection;

public class LoginActivity extends BaseActivity implements LoginContract.View {
    public static final int RETURN_LOGIN = 0;
    @BindView(R.id.ibBack)
    TextView ibBack;
    @BindView(R.id.ibRegist)
    TextView ibRegist;
    @BindView(R.id.llTitle)
    LinearLayout llTitle;
    @BindView(R.id.etUsername)
    EditText etUsername;
    @BindView(R.id.etPassword)
    EditText etPassword;
    @BindView(R.id.tvLogin)
    TextView tvLogin;
    @BindView(R.id.tvForgetPas)
    TextView tvForgetPas;
    @BindView(R.id.tvToRegist)
    TextView tvToRegist;
    @BindView(R.id.yan)
    ImageView yan;
    @BindView(R.id.mCkRememberPass)
    CheckBox mCkRememberPass;
    private boolean isYan=false;
    private LoginContract.Presenter presenter;
    private Handler handler = new Handler();


    private TCaptchaDialog dialog;
    private String ticket;
    private String randstr;
    private String appId= AppConfig.AUTH_APP_ID;
    private TCaptchaVerifyListener listener = new TCaptchaVerifyListener() {
        @Override
        public void onVerifyCallback(JSONObject jsonObject) {
            int ret = 0;
            try {
                ret = jsonObject.getInt("ret");
                if (ret == 0) {
                    ticket = jsonObject.getString("ticket");
                    randstr = jsonObject.getString("randstr");
                    presenter.getLoginAuthType(username);
                } else if (ret == -1001) {
                    //验证码首个TCaptcha.js加载错误，业务可以根据需要重试
                    //jsonObject.getString("info")为错误信息
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                } else {
                    //验证失败回调，一般为用户关闭验证码弹框
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    private String username;
    private String password;
    private AlertDialog authDialog;

    private void startLogin() {
        dialog = new TCaptchaDialog(this, appId, listener, null);
        dialog.show();
    }

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }


    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        new LoginPresenter(Injection.provideTasksRepository(getApplicationContext()), this);

        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tvToRegist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignUpActivity.actionStart(LoginActivity.this);
            }
        });
        ibRegist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignUpActivity.actionStart(LoginActivity.this);
            }
        });
        tvForgetPas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ForgotPwdActivity.actionStart(LoginActivity.this);
            }
        });
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        yan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isYan=!isYan;
                Drawable no = getResources().getDrawable(R.drawable.yan_no);
                Drawable yes = getResources().getDrawable(R.drawable.yan_yes);
                if (isYan){
                    //显示
                    etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    yan.setImageDrawable(yes);

                }else {
                    etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    yan.setImageDrawable(no);
                }
            }
        });

        String strings[]=SharedPreferenceInstance.getInstance().getPhoneAndPass();
        etPassword.setText(strings[1]);
        etUsername.setText(strings[0]);
        if(!TextUtils.isEmpty(strings[0])&&!TextUtils.isEmpty(strings[1])){
            mCkRememberPass.setChecked(true);
        }else{
            mCkRememberPass.setChecked(false);
        }
    }

    private void login() {
        username = etUsername.getText().toString();
          password = etPassword.getText().toString();
        if (WonderfulStringUtils.isEmpty(username, password)) {
            WonderfulToastUtils.showToast(getResources().getString(R.string.Incomplete_information));
            return;}
        startLogin();
    }

    @Override
    protected void obtainData() {

    }

    @Override
    protected void fillWidget() {

    }

    @Override
    protected void loadData() {

    }

    @Override
    protected void initImmersionBar() {
        super.initImmersionBar();
        if (!isSetTitle) {
            ImmersionBar.setTitleBar(this, llTitle);
            isSetTitle = true;
        }
    }

    @Override
    public void setPresenter(LoginContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void loginFail(Integer code, String toastMessage) {
        WonderfulCodeUtils.checkedErrorCode(this, code, toastMessage);
    }

    @Override
    public void loginSuccess(User obj) {
        if(authDialog!=null) {
            authDialog.cancel();
        }
        if(mCkRememberPass.isChecked()) {
            SharedPreferenceInstance.getInstance().savePhoneAndPass(etUsername.getText().toString(), etPassword.getText().toString());
        }
        MyApplication.getApp().setCurrentUser(null);
        MainActivity.isAgain = true;
        String key = WonderfulCommonUtils.getSerialNumber() + etUsername.getText().toString() + etPassword.getText().toString();
        String md5Key = getMD5(key);
        SharedPreferenceInstance.getInstance().saveToken(EncryUtils.getInstance().encryptString(md5Key, MyApplication.getApp().getPackageName()));
        MyApplication.getApp().setLoginStatusChange(true);
        SharedPreferenceInstance.getInstance().saveLockPwd("");
        MyApplication.getApp().setCurrentUser(obj);
        SharedPreferenceInstance.getInstance().saveID(obj.getId());
        SharedPreferenceInstance.getInstance().saveTOKEN(obj.getToken());
        SharedPreferenceInstance.getInstance().saveaToken(EncryUtils.getInstance().decryptString(SharedPreferenceInstance.getInstance().getToken(), MyApplication.getApp().getPackageName()));
        setResult(RESULT_OK);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 600);
    }

    public String getMD5(String info) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(info.getBytes("UTF-8"));
            byte[] encryption = md5.digest();
            StringBuffer strBuf = new StringBuffer();
            for (int i = 0; i < encryption.length; i++) {
                if (Integer.toHexString(0xff & encryption[i]).length() == 1) {
                    strBuf.append("0").append(Integer.toHexString(0xff & encryption[i]));
                } else {
                    strBuf.append(Integer.toHexString(0xff & encryption[i]));
                }
            }
            return strBuf.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }


    String AUTH_GOOGLE_NO="0";
    String AUTH_GOOGLE_YES="1";

    @Override
    public void getLoginAuthTypeSuccess(String type) {
        if(AUTH_GOOGLE_NO.equals(type)){
            presenter.login(username,password,"",ticket,randstr);
        }else if(AUTH_GOOGLE_YES.equals(type)){
            CommonDialogFragment commonDialogFragment=CommonDialogFragment.getInstance(CommonDialogFragment.TYPE_GOOGLE_CODE,getString(R.string.text_input_google_code),getString(R.string.text_input_google_code),getString(R.string.confirm),getString(R.string.cancle),true);
            commonDialogFragment.show(getSupportFragmentManager(),"google");
            commonDialogFragment.setCommitClickListener(new CommonDialogFragment.OnCommitClickListener() {
                @Override
                public void onCommitClick(String pass) {
                    if(TextUtils.isEmpty(pass)){
                        WonderfulToastUtils.showToast(getResources().getString(R.string.text_input_google_code));
                    }else{
                        presenter.login(username,password,pass,ticket,randstr);
                        authDialog.cancel();
                    }
                }
            });
        }
    }

    @Override
    public void getLoginAuthTypeFail(Integer code, String toastMessage) {
        Log.i("getLoginAuthTypeFail",code+toastMessage);
        WonderfulToastUtils.showToast(toastMessage);
    }
}
