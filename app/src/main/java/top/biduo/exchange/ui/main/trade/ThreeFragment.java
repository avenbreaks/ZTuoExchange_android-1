package top.biduo.exchange.ui.main.trade;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import top.biduo.exchange.R;
import top.biduo.exchange.app.Injection;
import top.biduo.exchange.base.BaseTransFragment;
import top.biduo.exchange.entity.Currency;
import top.biduo.exchange.utils.sysinfo.QMUIStatusBarHelper;
import top.biduo.exchange.ui.main.MainActivity;

public class ThreeFragment extends BaseTransFragment {
    public static final String TAG = ThreeFragment.class.getSimpleName();
    Coin2CoinFragment coin2CoinFragment = new Coin2CoinFragment();
    BaseCoinFragment baseCoinFragment = new BaseCoinFragment();

    @BindView(R.id.tb_transaction)
    TabLayout mTabLayout;
    @BindView(R.id.vp_transaction)
    ViewPager mViewPager;
    @BindView(R.id.ll_title)
    LinearLayout ll_title;

    @Override
    protected String getmTag() {
        return TAG;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_three;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        initImmersionBar();
        ImmersionBar.setTitleBar(getActivity(), ll_title);
        initViewPager();
        new Coin2CoinPresenter(Injection.provideTasksRepository(getActivity()),coin2CoinFragment);
        new BaseCoinPresenter(Injection.provideTasksRepository(getActivity()),baseCoinFragment);

    }


    private void initViewPager() {
        List<Fragment> fragmentList = new ArrayList<>();
        final List<String> listTitle = new ArrayList<>();
        fragmentList.add(coin2CoinFragment);
        fragmentList.add(baseCoinFragment);
        listTitle.add(getResources().getString(R.string.exchange));
        listTitle.add(getResources().getString(R.string.fiat));
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(new MyPagerAdapter(getActivity().getSupportFragmentManager(), fragmentList, listTitle));
        mTabLayout.setupWithViewPager(mViewPager, true);
    }

    public void showPageFragment(String symbol, int i) {
        if (coin2CoinFragment != null) coin2CoinFragment.showPageFragment(symbol, i);
    }

    public void showPageFragment(Currency currency,int position){
        if(coin2CoinFragment!=null){
            coin2CoinFragment.resetSymbol(currency,position);
        }
    }

    public void setViewContent(List<Currency> currencyListAll) {
        if (coin2CoinFragment != null) coin2CoinFragment.setViewContent(currencyListAll);
    }

    public void resetSymbol(Currency currency, int currentMenuType) {
        switch (currentMenuType){
            case MainActivity.MENU_TYPE_EXCHANGE:
                if (coin2CoinFragment != null) {
                    coin2CoinFragment.resetSymbol(currency);
                }
                break;
            case MainActivity.MENU_TYPE_MARGIN:
                break;
            default:
        }
    }

    public void resetSybol(Currency currency,int position){

    }

    public void tcpNotify() {
        if (coin2CoinFragment != null) {
            coin2CoinFragment.tcpNotify();
        }
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragmentList;
        private List<String> listTitle;

        public MyPagerAdapter(FragmentManager fm, List<Fragment> fragmentList, List<String> listTitle) {
            super(fm);
            this.fragmentList = fragmentList;
            this.listTitle = listTitle;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return listTitle.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return listTitle.get(position);
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

    }

    public void showTargetFragment(int position){
        mViewPager.setCurrentItem(position);
    }

}
