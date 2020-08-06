package top.biduo.exchange.ui.main.asset;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.gyf.barlibrary.ImmersionBar;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import top.biduo.exchange.R;
import top.biduo.exchange.adapter.PagerAdapter;
import top.biduo.exchange.adapter.TextWatcher;
import top.biduo.exchange.adapter.WalletAdapter;
import top.biduo.exchange.app.MyApplication;
import top.biduo.exchange.base.BaseFragment;
import top.biduo.exchange.base.BaseTransFragment;
import top.biduo.exchange.data.DataSource;
import top.biduo.exchange.data.RemoteDataSource;
import top.biduo.exchange.entity.Coin;
import top.biduo.exchange.entity.FiatAssetBean;
import top.biduo.exchange.entity.MarginAssetBean;
import top.biduo.exchange.ui.login.LoginActivity;
import top.biduo.exchange.ui.main.trade.ThreeFragment;
import top.biduo.exchange.ui.score_record.ScoreRecordActivity;
import top.biduo.exchange.ui.wallet.ChongBiJLActivity;
import top.biduo.exchange.ui.wallet.TiBiJLActivity;
import top.biduo.exchange.ui.wallet.WalletDialogFragment;
import top.biduo.exchange.ui.wallet_detail.WalletDetailActivity;
import top.biduo.exchange.utils.SharedPreferenceInstance;
import top.biduo.exchange.utils.WonderfulCodeUtils;
import top.biduo.exchange.utils.WonderfulDpPxUtils;
import top.biduo.exchange.utils.WonderfulMathUtils;
import top.biduo.exchange.utils.WonderfulToastUtils;

public class AssetFragment extends BaseTransFragment {
    public static final String TAG = AssetFragment.class.getSimpleName();
    private List<Coin> exchangeCoins = new ArrayList<>();
    private List<FiatAssetBean> fiatCoins = new ArrayList<>();

    @Override
    protected String getmTag() {
        return TAG;
    }

    @BindView(R.id.ibDetail)
    TextView ibDetail;
    @BindView(R.id.llTitle)
    LinearLayout llTitle;
    @BindView(R.id.tvAmount)
    TextView tvAmount;
    @BindView(R.id.tvCnyAmount)
    TextView tvCnyAmount;
    @BindView(R.id.llAccount)
    LinearLayout llAccount;
    @BindView(R.id.ivSee)
    ImageView ivSee;
    double sumUsd = 0;
    double sumCny = 0;
    private List<BaseFragment> fragments;
    @BindView(R.id.vp_asset)
    ViewPager mViewPager;
    @BindView(R.id.tb_asset)
    TabLayout mTabLayout;
    AssetExchangeFragment exchangeFragment = new AssetExchangeFragment();
    AssetFiatFragment fiatFragment = new AssetFiatFragment();
    private List<String> lists_chong=new ArrayList<>();
    private List<String> lists_ti=new ArrayList<>();
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_asset;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        initViewPager();
        ivSee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchSee();
            }
        });
        ibDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View popupView = getLayoutInflater().inflate(R.layout.popupwindow_ctb, null);
                LinearLayout my_chong = popupView.findViewById(R.id.my_chong);
                my_chong.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ChongBiJLActivity.actionStart(getActivity(), lists_chong);
                    }
                });
                LinearLayout my_ti = popupView.findViewById(R.id.my_ti);
                my_ti.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TiBiJLActivity.actionStart(getActivity(), lists_ti);
                    }
                });
                LinearLayout tv_wallet_detail = popupView.findViewById(R.id.tv_wallet_detail);
                tv_wallet_detail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        WalletDetailActivity.actionStart(getActivity());
                    }
                });
                LinearLayout tv_record_score = popupView.findViewById(R.id.tv_record_score);
                tv_record_score.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getActivity(), ScoreRecordActivity.class));
                    }
                });
                PopupWindow window = new PopupWindow(popupView, WonderfulDpPxUtils.dip2px(getActivity(), 120), WonderfulDpPxUtils.dip2px(getActivity(), 163));
                window.setOutsideTouchable(true);
                window.setTouchable(true);
                window.setFocusable(true);
                window.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.my_bg));
                window.update();
                window.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                int i = window.getWidth();
                int width = ibDetail.getWidth();
                window.showAsDropDown(ibDetail, -(i - width), 0);

            }
        });
    }

    private void initViewPager() {
        fragments = new ArrayList<BaseFragment>() {{
            add(exchangeFragment);
            add(fiatFragment);
        }};
        MyPagerAdapter pagerAdapter = new MyPagerAdapter(getFragmentManager(), fragments);
        mViewPager.setAdapter(pagerAdapter);
        final List<String> listTitle = new ArrayList<>();
        listTitle.add(getResources().getString(R.string.exchange));
        listTitle.add(getResources().getString(R.string.fiat));
        for (String title : listTitle) {
            TabLayout.Tab tab = mTabLayout.newTab();
            View inflate = View.inflate(getActivity(), R.layout.custom_tablayout, null);
            TextView textView = inflate.findViewById(R.id.tv_tab_title);
            textView.setText(title);
            tab.setCustomView(inflate);
            mTabLayout.addTab(tab);
        }
        setTitleSelectedByPosition(0, listTitle);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mTabLayout.setScrollPosition(position, 0, true);
                setTitleSelectedByPosition(position, listTitle);
                getData(position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setTitleSelectedByPosition(int position, List<String> listTitle) {
        for (int i = 0; i < listTitle.size(); i++) {
            mTabLayout.getTabAt(i).getCustomView().findViewById(R.id.tv_tab_title).setEnabled(i == position);
        }
    }


    private void switchSee() {
        if (!"*****".equals(tvCnyAmount.getText())) {
            tvCnyAmount.setText("*****");
            tvAmount.setText("********");
            Drawable drawable = getResources().getDrawable(R.drawable.icon_eye_guan);
            ivSee.setImageDrawable(drawable);
            SharedPreferenceInstance.getInstance().saveMoneyShowtype(2);
            setHideAmount(true);
        } else {
            tvAmount.setText(WonderfulMathUtils.getRundNumber(sumUsd, 6, null));
            tvCnyAmount.setText(WonderfulMathUtils.getRundNumber(sumCny, 2, null) + "CNY");
            Drawable drawable = getResources().getDrawable(R.drawable.icon_eye_open);
            ivSee.setImageDrawable(drawable);
            SharedPreferenceInstance.getInstance().saveMoneyShowtype(1);
            setHideAmount(false);
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
        getData(0);
    }

    private void getData(int pagePosition) {
        switch (pagePosition) {
            case 0:
                getExchangeAsset();
                break;
            case 1:
                break;
            case 2:
                getFiatAsset();
                break;
            default:
        }
    }

    private void getFiatAsset() {
        RemoteDataSource.getInstance().getFiatAsset(getmActivity().getToken(), new DataSource.DataCallback() {
            @Override
            public void onDataLoaded(Object obj) {
                fiatCoins.clear();
                fiatCoins.addAll((List<FiatAssetBean>) obj);
                fiatFragment.setData(fiatCoins);
                if(mViewPager.getCurrentItem()==2){
                    getFiatTotalAsset();
                }
            }

            @Override
            public void onDataNotAvailable(Integer code, String toastMessage) {
                WonderfulToastUtils.showToast(toastMessage);
            }
        });
    }

    private void getFiatTotalAsset() {
        sumCny=0;
        sumUsd=0;
        if(fiatCoins!=null&&fiatCoins.size()!=0){
            for (FiatAssetBean coin : fiatCoins) {
                sumUsd += (coin.getBalance() * coin.getCoin().getUsdRate());
                sumCny += (coin.getBalance() * coin.getCoin().getCnyRate());
            }
        }
        if (SharedPreferenceInstance.getInstance().getMoneyShowType() == 1) {
            tvAmount.setText(WonderfulMathUtils.getRundNumber(sumUsd, 6, null));
            tvCnyAmount.setText(WonderfulMathUtils.getRundNumber(sumCny, 2, null) + " CNY");
            setHideAmount(false);
        } else if (SharedPreferenceInstance.getInstance().getMoneyShowType() == 2) {
            tvAmount.setText("********");
            tvCnyAmount.setText("*****");
            setHideAmount(true);
        }

    }

    private void getExchangeAsset() {
        RemoteDataSource.getInstance().myWallet(getmActivity().getToken(), new DataSource.DataCallback() {
            @Override
            public void onDataLoaded(Object obj) {
                exchangeCoins.clear();
                exchangeCoins.addAll((List<Coin>) obj);
                lists_chong.clear();
                lists_ti.clear();
                for (int i=0;i<exchangeCoins.size();i++){
                    if (exchangeCoins.get(i).getCoin().getCanRecharge() == 1){
                        lists_chong.add(exchangeCoins.get(i).getCoin().getUnit());
                    }else if(exchangeCoins.get(i).getCoin().getCanWithdraw() == 1){
                        lists_ti.add(exchangeCoins.get(i).getCoin().getUnit());
                    }
                }
                exchangeFragment.setData(exchangeCoins);
                if(mViewPager.getCurrentItem()==0){
                    getExchangeTotalAsset();
                }
            }

            @Override
            public void onDataNotAvailable(Integer code, String toastMessage) {
                WonderfulToastUtils.showToast(toastMessage);
            }
        });
    }

    private void getExchangeTotalAsset() {
        sumCny=0;
        sumUsd=0;
        if(exchangeCoins!=null&&exchangeCoins.size()!=0){
            for (Coin coin : exchangeCoins) {
                sumUsd += (coin.getBalance() * coin.getCoin().getUsdRate());
                sumCny += (coin.getBalance() * coin.getCoin().getCnyRate());
            }
        }
        if (SharedPreferenceInstance.getInstance().getMoneyShowType() == 1) {
            tvAmount.setText(WonderfulMathUtils.getRundNumber(sumUsd, 6, null));
            tvCnyAmount.setText(WonderfulMathUtils.getRundNumber(sumCny, 2, null) + " CNY");
            setHideAmount(false);
        } else if (SharedPreferenceInstance.getInstance().getMoneyShowType() == 2) {
            tvAmount.setText("********");
            tvCnyAmount.setText("*****");
            setHideAmount(true);
        }



    }

    @Override
    protected void initImmersionBar() {
        super.initImmersionBar();
        if (!isSetTitle) {
            ImmersionBar.setTitleBar(getActivity(), llTitle);
            isSetTitle = true;
        }
    }


    class MyPagerAdapter extends FragmentPagerAdapter {
        private List<BaseFragment> baseFragments;

        public MyPagerAdapter(FragmentManager fm, List<BaseFragment> baseFragments) {
            super(fm);
            this.baseFragments = baseFragments;
        }

        @Override
        public int getCount() {
            return baseFragments.size();
        }

        @Override
        public Fragment getItem(int position) {
            return baseFragments.get(position);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getData(mViewPager.getCurrentItem());
    }

    private void setHideAmount(boolean isShow){
        exchangeFragment.hideAmount(isShow);
        fiatFragment.hideAmount(isShow);
    }
}
