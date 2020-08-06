package top.biduo.exchange.ui.main;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import top.biduo.exchange.R;
import top.biduo.exchange.ui.main.asset.AssetFragment;
import top.biduo.exchange.ui.main.drawer.BTCMarket2Fragment;
import top.biduo.exchange.ui.main.drawer.ETHMarket2Fragment;
import top.biduo.exchange.ui.main.drawer.Favorite2Fragment;
import top.biduo.exchange.ui.main.drawer.USDTMarket2Fragment;
import top.biduo.exchange.ui.main.home.OneFragment;
import top.biduo.exchange.ui.main.market.BTCMarketFragment;
import top.biduo.exchange.ui.main.market.ETHMarketFragment;
import top.biduo.exchange.ui.main.market.FavoriteFragment;
import top.biduo.exchange.ui.main.market.TwoFragment;
import top.biduo.exchange.ui.main.market.USDTMarketFragment;
import top.biduo.exchange.ui.main.user.UserPresenter;
import top.biduo.exchange.ui.main.presenter.MainPresenter;
import top.biduo.exchange.ui.main.home.OnePresenter;
import top.biduo.exchange.ui.main.market.TwoPresenter;
import top.biduo.exchange.ui.kline.KlineActivity;
import top.biduo.exchange.adapter.DrawerListener;
import top.biduo.exchange.adapter.PagerAdapter;
import top.biduo.exchange.app.GlobalConstant;
import top.biduo.exchange.app.MyApplication;
import top.biduo.exchange.base.BaseFragment;
import top.biduo.exchange.base.BaseTransFragmentActivity;
import top.biduo.exchange.entity.ChatTable;
import top.biduo.exchange.entity.ChatTipEvent;
import top.biduo.exchange.entity.Currency;
import top.biduo.exchange.entity.Favorite;
import top.biduo.exchange.entity.SafeSetting;
import top.biduo.exchange.entity.Vision;
import top.biduo.exchange.app.UrlFactory;
import top.biduo.exchange.socket.ISocket;
import top.biduo.exchange.ui.login.LoginActivity;
import top.biduo.exchange.ui.main.trade.ThreeFragment;
import top.biduo.exchange.ui.main.user.UserFragment;
import top.biduo.exchange.utils.SharedPreferenceInstance;
import top.biduo.exchange.serivce.MyService;
import top.biduo.exchange.serivce.MyTextService;
import top.biduo.exchange.serivce.SocketMessage;
import top.biduo.exchange.serivce.SocketResponse;
import top.biduo.exchange.utils.LoadDialog;
import top.biduo.exchange.utils.WonderfulCodeUtils;
import top.biduo.exchange.utils.WonderfulDatabaseUtils;
import top.biduo.exchange.utils.WonderfulLogUtils;
import top.biduo.exchange.utils.WonderfulPermissionUtils;
import top.biduo.exchange.utils.WonderfulToastUtils;
import top.biduo.exchange.utils.okhttp.StringCallback;
import top.biduo.exchange.utils.okhttp.WonderfulOkhttpUtils;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.PermissionListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import top.biduo.exchange.app.Injection;
import okhttp3.Request;
import top.biduo.exchange.utils.sysinfo.QMUIStatusBarHelper;

public class MainActivity extends BaseTransFragmentActivity implements MainContract.View, MarketBaseFragment.MarketOperateCallback {

    @BindView(R.id.flContainer)
    FrameLayout flContainer;
    @BindView(R.id.llOne)
    LinearLayout llOne;
    @BindView(R.id.llTwo)
    LinearLayout llTwo;
    @BindView(R.id.llThree)
    LinearLayout llThree;
    @BindView(R.id.llFour)
    LinearLayout llFour;
    @BindView(R.id.llFive)
    LinearLayout llFive;
    @BindView(R.id.llTab)
    LinearLayout llTab;
    @BindView(R.id.ibClose)
    ImageButton ibClose;
    @BindView(R.id.tab)
    TabLayout tab;
    @BindView(R.id.vpMenu)
    ViewPager vpMenu;
    @BindView(R.id.dlRoot)
    DrawerLayout dlRoot;
    @BindArray(R.array.home_two_top_tab)
    String[] titles;
    private int currentPage;
    private List<Currency> currencies = new ArrayList<>();
    private List<Currency> currenciesTwo = new ArrayList<>();

    private List<Currency> currencyListAllExchange = new ArrayList<>();
    private List<Currency> currencyListAllMargin = new ArrayList<>();//杠杆全部交易对，用于杠杆交易页面左边侧滑选择交易对
    private List<Currency> baseUsdt = new ArrayList<>();
    private List<Currency> baseBtc = new ArrayList<>();
    private List<Currency> baseEth = new ArrayList<>();
    private List<BaseFragment> menusFragments2 = new ArrayList<>();

    private OneFragment oneFragment;
    private TwoFragment twoFragment;
    private ThreeFragment threeFragment;
    private AssetFragment fourFragment;
    private UserFragment fiveFragment;

    private USDTMarket2Fragment usdtMarketFragment2;
    private BTCMarket2Fragment btcMarketFragment2;
    private ETHMarket2Fragment ethMarketFragment2;
    private Favorite2Fragment favoriteFragment2;

    private MainContract.Presenter presenter;
    private long lastPressTime = 0;
    private int type;// 1 去买币  2 去卖币
    private LinearLayout[] lls;

    private Gson gson = new Gson();
    private boolean hasNew = false;
    private byte[] groupBody;
    //private byte[] groupBody;
    private WonderfulDatabaseUtils databaseUtils;
    private List<ChatTable> list;
    private List<ChatTable> findByOrderList;
    private Vision vision;
    private ProgressDialog progressDialog;
    private Intent intentTcp;

    private void tcpNotify() {
        oneFragment.tcpNotify();
        twoFragment.tcpNotify();
        threeFragment.tcpNotify();
        usdtMarketFragment2.tcpNotify();
        btcMarketFragment2.tcpNotify();
        ethMarketFragment2.tcpNotify();
        favoriteFragment2.tcpNotify();
    }

    /**
     * socket 推送过来的信息
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSocketMessage(SocketResponse response) {
        if (response.getCmd() == ISocket.CMD.PUSH_SYMBOL_THUMB) {
            // 如果是盘口返回的信息
            try {
                Currency temp = gson.fromJson(response.getResponse(), Currency.class);
                if (temp == null) {
                    return;
                }
                for (Currency currency : currencies) {
                    if (temp.getSymbol().equals(currency.getSymbol())) {
                        Currency.shallowClone(currency, temp);
                        break;
                    }
                }
                for (Currency currency : currenciesTwo) {
                    if (temp.getSymbol().equals(currency.getSymbol())) {
                        Currency.shallowClone(currency, temp);
                        break;
                    }
                }
                for (Currency currency : currencyListAllExchange) {
                    if (temp.getSymbol().equals(currency.getSymbol())) {
                        Currency.shallowClone(currency, temp);
                        break;
                    }
                }
                tcpNotify();
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }

    /*private void storageData(String response) {
            ChatEntity chatEntity = gson.fromJson(response, ChatEntity.class);
            if (chatEntity == null) return;
            hasNew = true;
            oneFragment.setChatTip(hasNew);
            databaseUtils = new WonderfulDatabaseUtils();
            list = databaseUtils.findAll();
            if (list == null || list.size() == 0) {
                ChatTable table = new ChatTable();
                table.setContent(chatEntity.getContent());
                table.setFromAvatar(chatEntity.getFromAvatar());
                table.setNameFrom(chatEntity.getNameFrom());
                table.setNameTo(chatEntity.getNameTo());
                table.setUidFrom(chatEntity.getUidFrom());
                table.setUidTo(chatEntity.getUidTo());
                table.setOrderId(chatEntity.getOrderId());
                table.setRead(false);
                databaseUtils.saveChat(table);
                return;
            }
            ChatTable table = new ChatTable();
            for (int i = 0; i < list.size(); i++) {
                if (chatEntity.getOrderId().equals(list.get(i).getOrderId())) {
                    findByOrderList = databaseUtils.findByOrder(chatEntity.getOrderId());
                    table = findByOrderList.get(findByOrderList.size()-1);
                    table.setContent(chatEntity.getContent());
                    databaseUtils.update(table);
                    return;
                }
            }
            table.setContent(chatEntity.getContent());
            table.setFromAvatar(chatEntity.getFromAvatar());
            table.setNameFrom(chatEntity.getNameFrom());
            table.setNameTo(chatEntity.getNameTo());
            table.setUidFrom(chatEntity.getUidFrom());
            table.setUidTo(chatEntity.getUidTo());
            table.setOrderId(chatEntity.getOrderId());
            table.setRead(false);
            databaseUtils.saveChat(table);

    }*/

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getGroupChatEvent(ChatTipEvent tipEvent) {
        oneFragment.setChatTip(tipEvent.isHasNew());
    }

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    //重载启动方法 在K线 页面用到
    public static void actionStart(Context context, int type, String symbol) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("symbol", symbol);
        context.startActivity(intent);
    }

    public static void actionStart(Context context, int type, Currency currency) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("currency", currency);
        context.startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        long now = System.currentTimeMillis();
        if (lastPressTime == 0 || now - lastPressTime > 2 * 1000) {
            WonderfulToastUtils.showToast(getResources().getString(R.string.exit_again));
            lastPressTime = now;
        } else if (now - lastPressTime < 2 * 1000) {
            super.onBackPressed();
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private LoadDialog mDialog = null;

    private void show() {
        if (mDialog == null) {
            mDialog = new LoadDialog(this);
        }
        mDialog.show();
    }

    private void hideDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    private void getAllCurrency() {
        //获取币币交易全部交易对
        WonderfulOkhttpUtils.post().url(UrlFactory.getAllCurrency()).build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Request request, Exception e) {
                        super.onError(request, e);
                        hideDialog();
                    }

                    @Override
                    public void onResponse(String response) {
                        WonderfulLogUtils.logi("miao", "所有币种-币币交易" + response.toString());
                        // 获取所有币种
                        hideDialog();
                        try {
                            List<Currency> obj = new Gson().fromJson(response, new TypeToken<List<Currency>>() {
                            }.getType());
                            setCurrency(obj);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        //获取杠杆交易全部交易对,侧滑菜单用
        WonderfulOkhttpUtils.post().url(UrlFactory.getAllCurrency()).addParams("isLever", "true").build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Request request, Exception e) {
                        super.onError(request, e);
                        hideDialog();
                    }

                    @Override
                    public void onResponse(String response) {
                        WonderfulLogUtils.logi("miao", "所有币种-杠杆交易" + response.toString());
                        // 获取所有币种
                        hideDialog();
                        try {
                            List<Currency> obj = new Gson().fromJson(response, new TypeToken<List<Currency>>() {
                            }.getType());
                            currencyListAllMargin.clear();
                            currencyListAllMargin.addAll(obj);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        // 获取当前版本号
        versionName = getAppVersionName(this);
        // 请求版本更新
        initProgressDialog();
        getNewVision();
        //startService(new Intent(MainActivity.this, MyTextService.class)); // 开启服务
        //groupBody = buildGetBodyJson("").toString().getBytes();
        lls = new LinearLayout[]{llOne, llTwo, llThree, llFour, llFive};
        new MainPresenter(Injection.provideTasksRepository(getApplicationContext()), this);
        llOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selecte(v, 0);
            }
        });
        llTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selecte(v, 1);
            }
        });
        llThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selecte(v, 2);
            }
        });
        llFour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MyApplication.getApp().isLogin()) {
                    selecte(v, 3);
                } else {
                    LoginActivity.actionStart(MainActivity.this);
                }
            }
        });
        llFive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selecte(v, 4);
            }
        });
        dlRoot.addDrawerListener(new DrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                usdtMarketFragment2.notifyData();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                oneFragment.notifyData();
            }
        });
        ibClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlRoot.closeDrawers();
            }
        });
        if (fragments.size() == 0) {
            recoverFragment();
        }
        if (savedInstanceState == null) {
            hideFragment(oneFragment);
            selecte(llThree, 2);
            selecte(llOne, 0);
            addFragments();
        } else {
            recoverMenuFragment();
        }
        vpMenu.setOffscreenPageLimit(1);
        List<String> titles = Arrays.asList(this.titles);
        vpMenu.setAdapter(new PagerAdapter(getSupportFragmentManager(), menusFragments2, titles));
        tab.setupWithViewPager(vpMenu);
        new OnePresenter(Injection.provideTasksRepository(getApplicationContext()), oneFragment);
        new TwoPresenter(Injection.provideTasksRepository(getApplicationContext()), twoFragment);
        new UserPresenter(Injection.provideTasksRepository(getApplicationContext()), fiveFragment);
    }

    private void initProgressDialog() {
        //创建进度条对话框
        progressDialog = new ProgressDialog(this);
        //设置标题
        progressDialog.setTitle(getResources().getString(R.string.versionUpdateTip4));
        //设置信息
        progressDialog.setMessage(getResources().getString(R.string.versionUpdateTip5));
        progressDialog.setProgress(0);
        //设置显示的格式
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    }

    private void recoverMenuFragment() {
        usdtMarketFragment2 = (USDTMarket2Fragment) getSupportFragmentManager().findFragmentByTag(BaseFragment.makeFragmentName(vpMenu.getId(), 0));
        btcMarketFragment2 = (BTCMarket2Fragment) getSupportFragmentManager().findFragmentByTag(BaseFragment.makeFragmentName(vpMenu.getId(), 1));
        ethMarketFragment2 = (ETHMarket2Fragment) getSupportFragmentManager().findFragmentByTag(BaseFragment.makeFragmentName(vpMenu.getId(), 2));
        favoriteFragment2 = (Favorite2Fragment) getSupportFragmentManager().findFragmentByTag(BaseFragment.makeFragmentName(vpMenu.getId(), 3));
        menusFragments2.add(usdtMarketFragment2);
        menusFragments2.add(btcMarketFragment2);
        menusFragments2.add(ethMarketFragment2);
        if (currentMenuType == MENU_TYPE_EXCHANGE) {
            menusFragments2.add(favoriteFragment2);
        }
    }

    private void addFragments() {
        if (usdtMarketFragment2 == null) {
            menusFragments2.add(usdtMarketFragment2 = USDTMarket2Fragment.getInstance());
        }
        if (btcMarketFragment2 == null) {
            menusFragments2.add(btcMarketFragment2 = BTCMarket2Fragment.getInstance());
        }
        if (ethMarketFragment2 == null) {
            menusFragments2.add(ethMarketFragment2 = ETHMarket2Fragment.getInstance());
        }
        if (favoriteFragment2 == null) {
            menusFragments2.add(favoriteFragment2 = Favorite2Fragment.getInstance());
        }
    }

    public void selecte(View v, int page) {
        if (page != 2) {
            QMUIStatusBarHelper.setStatusBarDarkMode(this);
        } else {
            QMUIStatusBarHelper.setStatusBarLightMode(this);
        }
        currentPage = page;
        llOne.setSelected(false);
        llTwo.setSelected(false);
        llThree.setSelected(false);
        llFour.setSelected(false);
        llFive.setSelected(false);
        v.setSelected(true);
        showFragment(fragments.get(page));
        if (currentFragment == fragments.get(2)) {
            dlRoot.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        } else {
            dlRoot.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    public void showCoin2Coin(int position) {
        QMUIStatusBarHelper.setStatusBarLightMode(this);
        currentPage = 2;
        llOne.setSelected(false);
        llTwo.setSelected(false);
        llThree.setSelected(true);
        llFour.setSelected(false);
        llFive.setSelected(false);
        ThreeFragment fragment = (ThreeFragment) fragments.get(2);
        showFragment(fragment);
        dlRoot.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        fragment.showTargetFragment(position);
    }

    @Override
    protected void obtainData() {
        getRate();
    }

    @Override
    protected void fillWidget() {

    }

    @Override
    protected void loadData() {
        presenter.allCurrency();
        getAllCurrency();
    }

    @Override
    protected void initFragments() {
        if (oneFragment == null) {
            fragments.add(oneFragment = new OneFragment());
        }
        if (twoFragment == null) {
            fragments.add(twoFragment = new TwoFragment());
        }
        if (threeFragment == null) {
            fragments.add(threeFragment = new ThreeFragment());
        }
        if (fourFragment == null) {
            fragments.add(fourFragment = new AssetFragment());
        }
        if (fiveFragment == null) {
            fragments.add(fiveFragment = new UserFragment());
        }
    }

    @Override
    protected void recoverFragment() {
        oneFragment = (OneFragment) getSupportFragmentManager().findFragmentByTag(OneFragment.TAG);
        twoFragment = (TwoFragment) getSupportFragmentManager().findFragmentByTag(TwoFragment.TAG);
        threeFragment = (ThreeFragment) getSupportFragmentManager().findFragmentByTag(ThreeFragment.TAG);
        fourFragment = (AssetFragment) getSupportFragmentManager().findFragmentByTag(AssetFragment.TAG);
        fiveFragment = (UserFragment) getSupportFragmentManager().findFragmentByTag(UserFragment.TAG);

        if (oneFragment == null) {
            fragments.add(oneFragment = new OneFragment());
        } else {
            fragments.add(oneFragment);
        }
        if (twoFragment == null) {
            fragments.add(twoFragment = new TwoFragment());
        } else {
            fragments.add(twoFragment);
        }
        if (threeFragment == null) {
            fragments.add(threeFragment = new ThreeFragment());
        } else {
            fragments.add(threeFragment);
        }
        if (fourFragment == null) {
            fragments.add(fourFragment = new AssetFragment());
        } else {
            fragments.add(fourFragment);
        }
        if (fiveFragment == null) {
            fragments.add(fiveFragment = new UserFragment());
        } else {
            fragments.add(fiveFragment);
        }
    }

    @Override
    public int getContainerId() {
        return R.id.flContainer;
    }

    public DrawerLayout getDlRoot(int menuType) {
        //如果menuType与currentMenuType不同，则需要切换数据（币币交易对和杠杆交易对）
        if (currentMenuType != menuType) {
            switch (menuType) {
                case MENU_TYPE_EXCHANGE:
                    baseUsdt = Currency.baseCurrencies(currencyListAllExchange, "USDT");
                    baseBtc = Currency.baseCurrencies(currencyListAllExchange, "BTC");
                    baseEth = Currency.baseCurrencies(currencyListAllExchange, "ETH");
                    menusFragments2.add(favoriteFragment2);
                    break;
                case MENU_TYPE_MARGIN:
                    baseUsdt = Currency.baseCurrencies(currencyListAllMargin, "USDT");
                    baseBtc = Currency.baseCurrencies(currencyListAllMargin, "BTC");
                    baseEth = Currency.baseCurrencies(currencyListAllMargin, "ETH");
                    menusFragments2.remove(favoriteFragment2);
                    break;
                default:
            }
            vpMenu.getAdapter().notifyDataSetChanged();
            usdtMarketFragment2.dataLoaded(baseUsdt);
            btcMarketFragment2.dataLoaded(baseBtc);
            ethMarketFragment2.dataLoaded(baseEth);
            currentMenuType = menuType;
        }
        return dlRoot;
    }

    public static boolean isAgain = false;

    public void subscribeThumb() {
        EventBus.getDefault().post(new SocketMessage(0, ISocket.CMD.SUBSCRIBE_SYMBOL_THUMB, null));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 开始订阅
        subscribeThumb();
        if (isAgain) {
            isAgain = false;
            if (!TextUtils.isEmpty(getToken())) {
                presenter.find(getToken());
            }
        }
        if (MyApplication.app.isLogin()) {
            isRenZheng();
            Intent intent = new Intent(MainActivity.this, MyService.class);
            intent.putExtra("uid", buildGetBodyJson("").toString().getBytes());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(this, MyService.class));
            } else {
                startService(new Intent(this, MyService.class));
            }
        }
       /* intentTcp = new Intent(MainActivity.this, MyBindService.class);
        intentTcp.putExtra("type",MyBindService.MARKET);
        startService(intentTcp);*/
        hasNew = SharedPreferenceInstance.getInstance().getHasNew();
        oneFragment.setChatTip(hasNew);
        SharedPreferenceInstance.getInstance().saveHasNew(false);
        //startTCP();
        //退出登录后 刷新展示未登录状态的数据
        if (!MyApplication.getApp().isLogin() && currencyListAllExchange != null && currencyListAllExchange.size() != 0) {
            notLoginCurrencies();
        }
        //登录回来，刷新展示登录状态的数据
        if (MyApplication.getApp().isLoginStatusChange()) {
            presenter.allCurrency();
            MyApplication.getApp().setLoginStatusChange(false);
        }
        //以下是点击了 K线页面的买入 卖出 后的页面跳转逻辑
        type = getIntent().getIntExtra("type", 0);
//        String symbol = getIntent().getStringExtra("symbol");
        Currency currency = (Currency) getIntent().getSerializableExtra("currency");
        if (type == 0) {
            return;//默认值 或是 不需要跳转 就返回
        }
        showCoin2Coin(0);
        //当type=1 就显示交易fragment就显示买入，对应的page就是0 （即type -1），当type=2，同理！
        if (threeFragment != null) {
            threeFragment.showPageFragment(currency, type - 1);
        }
        getIntent().putExtra("type", 0);
    }

    public void isRenZheng() {
        WonderfulOkhttpUtils.post().url(UrlFactory.getSafeSettingUrl()).addHeader("x-auth-token", getToken()).build().execute(new StringCallback() {
            @Override
            public void onError(Request request, Exception e) {
                super.onError(request, e);
                WonderfulLogUtils.logi("账户设置出错", e.getMessage());
            }

            @Override
            public void onResponse(String response) {
                //WonderfulLogUtils.logi("账户设置回执：",  response.toString());
                try {
                    JSONObject object = new JSONObject(response);
                    if (object.optInt("code") == 0) {
                        SafeSetting obj = gson.fromJson(object.getJSONObject("data").toString(), SafeSetting.class);
                        MyApplication.realVerified = obj.getRealVerified();
                    } else {
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                }
            }
        });
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        hasNew = SharedPreferenceInstance.getInstance().getHasNew();
        oneFragment.setChatTip(hasNew);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        hideDialog();
        // MainActivity销毁则停止
        stopService(new Intent(MainActivity.this, MyTextService.class));
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 取消订阅
        EventBus.getDefault().post(new SocketMessage(0, ISocket.CMD.UNSUBSCRIBE_SYMBOL_THUMB, null));
        EventBus.getDefault().unregister(this);
    }

    private JSONObject buildGetBodyJson(String content) {
        JSONObject obj = new JSONObject();
        try {
            //obj.put("orderId", orderDetial.getOrderSn());
            //obj.put("uid", orderDetial.getMyId());
            obj.put("uid", MyApplication.getApp().getCurrentUser().getId());
            return obj;
        } catch (Exception ex) {
            return null;
        }
    }

    private void notLoginCurrencies() {
        for (Currency currency : currencyListAllExchange) {
            currency.setCollect(false);
        }
    }

    @Override
    public void setPresenter(MainContract.Presenter presenter) {
        this.presenter = presenter;
    }

    private void setCurrency(List<Currency> currencies) {
        if (currencies == null || currencies.size() == 0) {
            return;
        }
        this.currencyListAllExchange.clear();
        this.currencyListAllExchange.addAll(currencies);
        MyApplication.list.clear();
        MyApplication.list.addAll(currencies);
        baseUsdt = Currency.baseCurrencies(currencyListAllExchange, "USDT");
        baseBtc = Currency.baseCurrencies(currencyListAllExchange, "BTC");
        baseEth = Currency.baseCurrencies(currencyListAllExchange, "ETH");
        if (MyApplication.getApp().isLogin()) {
            presenter.find(getToken());
        }
        setData();
        // 当请求成功 为交易fragment设置初始交易对 即symbol值
        if (threeFragment != null) {
            threeFragment.setViewContent(currencyListAllExchange);
        }
    }

    @Override
    public void allCurrencySuccess(Object obj) {
        try {
            JsonObject object = new JsonParser().parse((String) obj).getAsJsonObject();
            JsonArray array = object.getAsJsonArray("changeRank").getAsJsonArray();
            List<Currency> objs = gson.fromJson(array, new TypeToken<List<Currency>>() {
            }.getType());

            JsonArray array1 = object.getAsJsonArray("recommend").getAsJsonArray();
            List<Currency> currency1 = gson.fromJson(array1, new TypeToken<List<Currency>>() {
            }.getType());
            this.currenciesTwo.clear();
            this.currenciesTwo.addAll(currency1);
            this.currencies.clear();
            this.currencies.addAll(objs);
            if (oneFragment != null) {
                oneFragment.dataLoaded(currencies, currenciesTwo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //setCurrency( objs);
        /*if (obj == null && obj.size() == 0) return;
        this.currencies.clear();
        this.currencies.addAll(obj);
        baseUsdt = Currency.baseCurrencies(currencies, "USDT");
        baseBtc = Currency.baseCurrencies(currencies, "BTC");
        baseEth = Currency.baseCurrencies(currencies, "ETH");
        if (MyApplication.getApp().isLogin()) {
            setData();
            presenter.find(getToken());
        } else {
            *//* 请求成功为前三个模块设置数据*//*
         *//* 重要提示： 这里所有数据源都是这里的currencies，几个fragment共用，这段内存数据 一改全改*//*
            setData();
        }
        *//*当请求成功 为交易fragment设置初始交易对 即symbol值*//*
        threeFragment.setViewContent(currencies);*/
    }

    private void setData() {
        oneFragment.dataLoaded(currencies, currenciesTwo);
        twoFragment.dataLoaded(baseUsdt, baseBtc, baseEth, currencyListAllExchange);
        usdtMarketFragment2.dataLoaded(baseUsdt);
        btcMarketFragment2.dataLoaded(baseBtc);
        ethMarketFragment2.dataLoaded(baseEth);
        favoriteFragment2.dataLoaded(currencyListAllExchange);
    }

    @Override
    public void allCurrencyFail(Integer code, String toastMessage) {
        WonderfulCodeUtils.checkedErrorCode(this, code, toastMessage);
    }

    public static List<Favorite> mFavorte = new ArrayList<>();

    public void Find() {
        presenter.find(getToken());
    }

    @Override
    public void findSuccess(List<Favorite> obj) {
        //登录
        if (obj == null) {
            return;
        }
        mFavorte.clear();
        mFavorte.addAll(obj);
//        Log.d("jiejie", "我的收藏" + obj.size());
        for (Currency currency : currencyListAllExchange) {
            currency.setCollect(false);
        }
        for (Currency currency : currencyListAllExchange) {
            for (Favorite favorite : mFavorte) {
                if (favorite.getSymbol().equals(currency.getSymbol())) {
                    currency.setCollect(true);
//                    Log.d("jiejie", "我的收藏collect  " + currency.getSymbol());
                }
            }
        }
        if (twoFragment != null) {
            twoFragment.dataLoaded(baseUsdt, baseBtc, baseEth, currencyListAllExchange);
        }
        if (favoriteFragment2 != null) {
            favoriteFragment2.dataLoaded(currencyListAllExchange);
        }
    }

    @Override
    public void findFail(Integer code, String toastMessage) {

    }

    //侧滑菜单选择交易对
    public static final int MENU_TYPE_EXCHANGE = 0;//侧滑菜单-币币交易
    public static final int MENU_TYPE_MARGIN = 1;//侧滑菜单-法币交易
    public int currentMenuType = MENU_TYPE_EXCHANGE;

    @Override
    public void itemClick(Currency currency) {
        dlRoot.closeDrawers();
        threeFragment.resetSymbol(currency, currentMenuType);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        int page = savedInstanceState.getInt("page");
        selecte(lls[page], page);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("page", currentPage);
        super.onSaveInstanceState(outState);
    }

    /**
     * 获取汇率的接口
     */
    public static double rate = 1.0;

    private void getRate() {
        WonderfulOkhttpUtils.post().url(UrlFactory.getRateUrl()).build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Request request, Exception e) {
                        super.onError(request, e);
                        rate = 0;
                    }

                    @Override
                    public void onResponse(String response) {
                        JsonObject object = new JsonParser().parse(response).getAsJsonObject();
                        rate = object.getAsJsonPrimitive("data").getAsDouble();
                        WonderfulLogUtils.logi("miao", rate + "汇率");
                    }
                });
    }

    // 获取版本信息
    private void getNewVision() {
        WonderfulOkhttpUtils.post().url(UrlFactory.getNewVision()).build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Request request, Exception e) {
                        super.onError(request, e);

                    }

                    @Override
                    public void onResponse(String response) {
                        Log.d("jiejie", versionName + "  " + response);
                        try {
                            JSONObject object = new JSONObject(response);
                            if (object.optInt("code") == 0) {
                                vision = gson.fromJson(object.toString(), new TypeToken<Vision>() {
                                }.getType());
                                if (!WonderfulPermissionUtils.isCanUseStorage(MainActivity.this)) {
                                    checkPermission(GlobalConstant.PERMISSION_STORAGE, Permission.STORAGE);
                                } else {
                                    showUpDialog(vision);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void checkPermission(int requestCode, String[] permissions) {
        AndPermission.with(MainActivity.this).requestCode(requestCode).permission(permissions).callback(permissionListener).start();
    }

    private PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
            switch (requestCode) {
                case GlobalConstant.PERMISSION_STORAGE:
                    showUpDialog(vision);
                    break;
                default:
            }
        }

        @Override
        public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
            switch (requestCode) {
                case GlobalConstant.PERMISSION_STORAGE:
                    WonderfulToastUtils.showToast(getResources().getString(R.string.storage_permission));
                    break;
                default:
            }
        }
    };

    //判断文件是否存在
    public boolean fileIsExists(String strFile) {
        try {
            File f = new File(strFile);
            if (!f.exists()) {
                return false;
            }

        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private void showUpDialog(final Vision obj) {
        if (obj.getData() == null) {
            return;
        }
        if (!versionName.equals(obj.getData().getVersion())) {
            new AlertDialog.Builder(MainActivity.this)
                    .setIcon(null)
                    .setMessage(getResources().getString(R.string.versionUpdateTip2) + "\n不升级将会影响您的使用。")
                    .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (obj.getData().getDownloadUrl() == null || "".equals(obj.getData().getDownloadUrl())) {
                                WonderfulToastUtils.showToast(getResources().getString(R.string.versionUpdateTip3));
                            } else {
                                Intent intent = new Intent();
                                intent.setData(Uri.parse(obj.getData().getDownloadUrl()));//Url 就是你要打开的网址
                                intent.setAction(Intent.ACTION_VIEW);
                                startActivity(intent); //启动浏览器
                            }
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    })
                    .show().setCancelable(false);
            ;
        }
    }
//    private void download(String url) {
//            String name=getString(R.string.app_name)+versionCode+".apk";
//            WonderfulOkhttpUtils.get().url(url).build().execute(new FileCallback(WonderfulFileUtils.getCacheSaveFile(this, name).getAbsolutePath()) {
//            @Override
//            public void inProgress(float progress) {
//                progressDialog.show();
//                progressDialog.setProgress((int) (progress * 100));
//            }
//
//            @Override
//            public void onError(Request request, Exception e) {
//                WonderfulLogUtils.logi("下载失败：", e.getMessage());
//                progressDialog.dismiss();
//                WonderfulCodeUtils.checkedErrorCode(MainActivity.this, GlobalConstant.OKHTTP_ERROR, "null");
//            }
//
//            @TargetApi(26)
//            @Override
//            public void onResponse(File response) {
//                progressDialog.dismiss();
//                respons2e=response;
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//                    if (getPackageManager().canRequestPackageInstalls()){
//                        installAPk(response);
//                    }else {
//                        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
//                        startActivityForResult(intent, 10086);
//                    }
//                }else {
//                    installAPk(response);
//                }
//
//
//            }
//        });
//    }
//    private File respons2e;
//    @TargetApi(26)
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode ==10086){
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//                if (getPackageManager().canRequestPackageInstalls()){
//                    installAPk(respons2e);
//                }else {
//                    WonderfulToastUtils.showToast("安装应用需要打开未知来源权限，请去设置中开启权限");
//                }
//            }else {
//                installAPk(respons2e);
//            }
//
//        }
//
//    }

//    //普通安装
//    private void installAPk(File apkPath) {
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        Uri apkUri = WonderfulFileUtils.getUriForFile(this, apkPath);
//        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
//        startActivity(intent);
//    }

    /**
     * 返回当前程序版本名
     */
    private String versionName;
    private int versionCode;

    public String getAppVersionName(Context context) {
        versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            versionCode = pi.versionCode;
            //versioncode = pi.versionCode;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }
}
