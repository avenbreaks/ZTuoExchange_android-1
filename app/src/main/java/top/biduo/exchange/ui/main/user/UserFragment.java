package top.biduo.exchange.ui.main.user;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gyf.barlibrary.ImmersionBar;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.OnClick;
import okhttp3.Request;
import top.biduo.exchange.R;
import top.biduo.exchange.app.UrlFactory;
import top.biduo.exchange.ui.bind_account.BindAccountActivity;
import top.biduo.exchange.ui.credit.CreditInfoActivity;
import top.biduo.exchange.ui.credit.VideoCreditActivity;
import top.biduo.exchange.ui.login.LoginActivity;
import top.biduo.exchange.ui.main.MainContract;
import top.biduo.exchange.ui.my_promotion.PromotionActivity;
import top.biduo.exchange.ui.myinfo.MyInfoActivity;
import top.biduo.exchange.ui.score_record.CandyRecordActivity;
import top.biduo.exchange.ui.seller.SellerApplyActivity;
import top.biduo.exchange.ui.servicefee.ServiceFeeActivity;
import top.biduo.exchange.ui.setting.NoticeActivity;
import top.biduo.exchange.ui.setting.SettingActivity;
import top.biduo.exchange.app.MyApplication;
import top.biduo.exchange.base.BaseTransFragment;
import top.biduo.exchange.entity.Coin;
import top.biduo.exchange.entity.SafeSetting;
import top.biduo.exchange.entity.User;
import top.biduo.exchange.utils.SharedPreferenceInstance;
import top.biduo.exchange.customview.intercept.WonderfulScrollView;
import top.biduo.exchange.utils.WonderfulStringUtils;
import top.biduo.exchange.utils.WonderfulToastUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import top.biduo.exchange.utils.okhttp.StringCallback;
import top.biduo.exchange.utils.okhttp.WonderfulOkhttpUtils;

/**
 * Created by Administrator on 2018/1/29.
 */

public class UserFragment extends BaseTransFragment implements MainContract.UserView {
    public static final String TAG = UserFragment.class.getSimpleName();
    @BindView(R.id.llTop)
    LinearLayout llTop;
    @BindView(R.id.line_top)
    LinearLayout line_top;
    @BindView(R.id.llSafe)
    LinearLayout llSafe;
    @BindView(R.id.llSettings)
    ImageView llSettings;
    @BindView(R.id.llMyinfo)
    RelativeLayout llMyinfo;
    @BindView(R.id.tv_user_name)
    TextView tvNickName;
    @BindView(R.id.tvAccount)
    TextView tvAccount;
    @BindView(R.id.rl_user_top_un_login)
    ViewGroup rl_user_top_un_login;
    @BindView(R.id.rl_user_top)
    ViewGroup rl_user_top;
    @BindView(R.id.ivHeader)
    ImageView ivHeader;
    @BindView(R.id.tv_user_id)
    TextView tv_user_id;

    @OnClick(R.id.llPromotion)
    public void startPromotion() {
        if (MyApplication.getApp().isLogin()) {
            PromotionActivity.actionStart(getActivity());
        } else {
            startActivityForResult(new Intent(getActivity(), LoginActivity.class), LoginActivity.RETURN_LOGIN);
        }
    }

    public static double sumUsd = 0;
    double sumCny = 0;
    @BindView(R.id.scrollView)
    WonderfulScrollView scrollView;
    Unbinder unbinder;
    private MainContract.UserPresenter presenter;
    private SafeSetting safeSetting;
    private PopupWindow loadingPopup;

    @OnClick(R.id.llFee)
    public void startServiceFee() {
        if (MyApplication.getApp().isLogin()) {
            startActivity(new Intent(getActivity(), ServiceFeeActivity.class));
        } else {
            startActivityForResult(new Intent(getActivity(), LoginActivity.class), LoginActivity.RETURN_LOGIN);
        }
    }

    @OnClick(R.id.tv_candy_box)
    public void startCandyActivity() {
        if (MyApplication.getApp().isLogin()) {
            startActivity(new Intent(getActivity(), CandyRecordActivity.class));
        } else {
            startActivityForResult(new Intent(getActivity(), LoginActivity.class), LoginActivity.RETURN_LOGIN);
        }
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_user;
    }

    /**
     * 初始化加载dialog
     */
    private void initLoadingPopup() {
        View loadingView = getLayoutInflater().inflate(R.layout.pop_loading, null);
        loadingPopup = new PopupWindow(loadingView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        loadingPopup.setFocusable(true);
        loadingPopup.setClippingEnabled(false);
        loadingPopup.setBackgroundDrawable(new ColorDrawable());
    }

    /**
     * 显示加载框
     */
    @Override
    public void displayLoadingPopup() {
        loadingPopup.showAtLocation(getActivity().getWindow().getDecorView(), Gravity.CENTER, 0, 0);
    }

    /**
     * 隐藏加载框
     */
    @Override
    public void hideLoadingPopup() {
        if (loadingPopup != null) {
            loadingPopup.dismiss();
        }

    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        initLoadingPopup();
        line_top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MyApplication.getApp().isLogin()) {
                    startActivityForResult(new Intent(getActivity(), LoginActivity.class), LoginActivity.RETURN_LOGIN);
                }
            }
        });

        llSafe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toLoginOrCenter();
            }
        });
        llSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MyApplication.getApp().isLogin()) {
                    displayLoadingPopup();
                    SettingActivity.actionStart(getActivity());
                } else {
                    startActivityForResult(new Intent(getActivity(), LoginActivity.class), LoginActivity.RETURN_LOGIN);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideLoadingPopup();

    }

    @Override
    public void onStart() {
        super.onStart();
        hideLoadingPopup();
    }


    private void toLoginOrCenter() {
        if (MyApplication.getApp().isLogin()) {
            MyInfoActivity.actionStart(getActivity());
        } else {
            startActivityForResult(new Intent(getActivity(), LoginActivity.class), LoginActivity.RETURN_LOGIN);
        }
    }

    @Override
    protected void obtainData() {
    }

    @Override
    protected void fillWidget() {
    }

    @Override
    protected void loadData() {
        if (MyApplication.getApp().isLogin()) {
            loginingViewText();
        } else {
            notLoginViewText();
        }
    }

    @Override
    protected void initImmersionBar() {
        super.initImmersionBar();
        if (!isSetTitle) {
            ImmersionBar.setTitleBar(getActivity(), llMyinfo);
            isSetTitle = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LoginActivity.RETURN_LOGIN:
                if (resultCode == Activity.RESULT_OK && getUserVisibleHint() && MyApplication.getApp().isLogin()) {
                    loginingViewText();
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    notLoginViewText();
                }
                break;
            default:
        }
    }

    private void notLoginViewText() {
        rl_user_top_un_login.setVisibility(View.VISIBLE);
        rl_user_top.setVisibility(View.GONE);
        try {
            sumCny = 0.00;
            sumUsd = 0.000000;
            tvNickName.setText(getResources().getString(R.string.not_logged_in));
            Glide.with(getActivity().getApplicationContext()).load(R.mipmap.icon_default_header).into(ivHeader);
        } catch (Exception e) {

        }

    }

    private void loginingViewText() {
        rl_user_top_un_login.setVisibility(View.GONE);
        rl_user_top.setVisibility(View.VISIBLE);
        try {
            presenter.myWallet(getmActivity().getToken());
            presenter.safeSetting(getmActivity().getToken());
            User user = MyApplication.getApp().getCurrentUser();
            tvNickName.setText(user.getUsername());
            tv_user_id.setText("UID:" + user.getId());
            if (!WonderfulStringUtils.isEmpty(user.getAvatar())) {
                Glide.with(getActivity().getApplicationContext()).load(user.getAvatar()).into(ivHeader);
            } else {
                Glide.with(getActivity().getApplicationContext()).load(R.mipmap.icon_default_header).into(ivHeader);
            }
        } catch (Exception e) {

        }

    }

    @Override
    public void setPresenter(MainContract.UserPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void myWalletSuccess(List<Coin> obj) {
        if (obj == null) {
            return;
        }
        calcuTotal(obj);
    }

    private void calcuTotal(List<Coin> coins) {
        sumUsd = 0;
        sumCny = 0;
        for (Coin coin : coins) {
            sumUsd += (coin.getBalance() * coin.getCoin().getUsdRate());
            sumCny += (coin.getBalance() * coin.getCoin().getCnyRate());
        }
    }

    @Override
    public void myWalletFail(Integer code, String toastMessage) {
        MyApplication.getApp().setCurrentUser(null);
        notLoginViewText();
        if (code == 4000) {
            SharedPreferenceInstance.getInstance().saveaToken("");
            SharedPreferenceInstance.getInstance().saveTOKEN("");
        }
    }

    private void accountClick() {
        if (safeSetting == null) {
            return;
        }
        hideLoadingPopup();
        if (safeSetting.getRealVerified() == 1 && safeSetting.getFundsVerified() == 1) {
            BindAccountActivity.actionStart(getmActivity());
        } else {
            WonderfulToastUtils.showToast(getResources().getString(R.string.password_realname));
        }
    }


    @Override
    public void safeSettingSuccess(SafeSetting obj) {
        if (obj == null) {
            return;
        }
        this.safeSetting = obj;
        MyApplication.number = safeSetting.getMobilePhone();
    }

    @Override
    public void safeSettingFail(Integer code, String toastMessage) {
        if (code == 4000) {
            MyApplication.getApp().setCurrentUser(null);
            SharedPreferenceInstance.getInstance().saveaToken("");
            SharedPreferenceInstance.getInstance().saveTOKEN("");
            notLoginViewText();
        }
    }

    @Override
    protected String getmTag() {
        return TAG;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.llCredit)
    public void startCredit() {
        if (MyApplication.getApp().isLogin()) {
            if (safeSetting == null) {
                return;
            }
            if (safeSetting.getRealVerified() == 0) {
                if (safeSetting.getRealAuditing() == 1) {//审核中
                    WonderfulToastUtils.showToast(getResources().getString(R.string.creditting));
                } else {
                    if (safeSetting.getRealNameRejectReason() != null) {//失败
                        CreditInfoActivity.actionStart(getActivity(), CreditInfoActivity.AUDITING_FILED, safeSetting.getRealNameRejectReason());
                    } else {//未认证
                        CreditInfoActivity.actionStart(getActivity(), CreditInfoActivity.UNAUDITING, safeSetting.getRealNameRejectReason());
                    }
                }
            } else {
                //身份证已通过
                int kycStatu = safeSetting.getKycStatus();
                switch (kycStatu) {
                    //0-未实名,5-待实名审核，2-实名审核失败、1-视频审核,6-待视频审核 ，3-视频审核失败,4-实名成功
                    case 1:
                        //实名通过，进行视频认证
                        VideoCreditActivity.actionStart(getActivity(), "");
                        break;
                    case 3:
                        //视频认证失败，进行视频认证，显示失败原因
                        VideoCreditActivity.actionStart(getActivity(), safeSetting.getRealNameRejectReason());
                        break;
                    case 4:
                        WonderfulToastUtils.showToast(getString(R.string.verification));
                        break;
                    case 6:
                        WonderfulToastUtils.showToast(getString(R.string.video_credit_auditing));
                        break;
                    default:
                }
            }
        } else {
            startActivityForResult(new Intent(getActivity(), LoginActivity.class), LoginActivity.RETURN_LOGIN);
        }
    }

    @OnClick(R.id.llNotice)
    public void startNotice() {
        NoticeActivity.actionStart(getmActivity());
    }

    @OnClick(R.id.tv_seller_credit)
    public void startSellerCredit() {
        if (MyApplication.getApp().isLogin()) {
            if (MyApplication.realVerified == 1) {
                WonderfulOkhttpUtils.get().url(UrlFactory.getShangjia())
                        .addHeader("x-auth-token", SharedPreferenceInstance.getInstance().getTOKEN())
                        .build().execute(new StringCallback() {
                    @Override
                    public void onError(Request request, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.optInt("code");
                            if (code == 0) {
                                JSONObject data = jsonObject.optJSONObject("data");
                                int certifiedBusinessStatus = data.optInt("certifiedBusinessStatus");
                                String reason = data.getString("detail");
                                SellerApplyActivity.actionStart(getActivity(), certifiedBusinessStatus + "", reason);
                            } else {
                                WonderfulToastUtils.showToast(jsonObject.getString("message"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                WonderfulToastUtils.showToast(getResources().getString(R.string.realname));
            }
        } else {
            startActivityForResult(new Intent(getActivity(), LoginActivity.class), LoginActivity.RETURN_LOGIN);
        }
    }

}
