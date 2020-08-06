package top.biduo.exchange.ui.main.asset;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.chad.library.adapter.base.BaseQuickAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Request;
import top.biduo.exchange.R;
import top.biduo.exchange.adapter.TextWatcher;
import top.biduo.exchange.adapter.WalletAdapter;
import top.biduo.exchange.app.MyApplication;
import top.biduo.exchange.app.UrlFactory;
import top.biduo.exchange.base.BaseFragment;
import top.biduo.exchange.customview.BottomSelectionFragment;
import top.biduo.exchange.data.DataSource;
import top.biduo.exchange.data.RemoteDataSource;
import top.biduo.exchange.entity.Coin;
import top.biduo.exchange.ui.extract.ExtractActivity;
import top.biduo.exchange.ui.recharge.RechargeActivity;
import top.biduo.exchange.ui.wallet.WalletDialogFragment;
import top.biduo.exchange.utils.SharedPreferenceInstance;
import top.biduo.exchange.utils.WonderfulLogUtils;
import top.biduo.exchange.utils.WonderfulMathUtils;
import top.biduo.exchange.utils.WonderfulStringUtils;
import top.biduo.exchange.utils.WonderfulToastUtils;
import top.biduo.exchange.utils.okhttp.StringCallback;
import top.biduo.exchange.utils.okhttp.WonderfulOkhttpUtils;

/**
 * @author 明哥
 * 币币资产
 * 参考火币命名
 */

public class AssetExchangeFragment extends BaseFragment {
    @BindView(R.id.rc_asset)
    RecyclerView recyclerView;
    List<Coin> coins = new ArrayList<>();
    private WalletAdapter adapter;

    @BindView(R.id.etSearch)
    EditText etSearch;
    @BindView(R.id.cbHide)
    CheckBox cbHide;


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_asset_list;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        initRecyclerView();
        etSearch.addTextChangedListener(localChangeWatcher);
        cbHide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                adapter.setHideZero(isChecked);
            }
        });
    }

    private void initRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);
        adapter = new WalletAdapter(R.layout.adapter_wallet, coins);
        adapter.isFirstOnly(true);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, final int itemPosition) {
                final Coin coin = coins.get(itemPosition);
                final ArrayList<String> list = new ArrayList<>();
                if (coins.get(itemPosition).getCoin().getCanRecharge() != 0) {
                    list.add(getResources().getString(R.string.chargeMoney));
                }
                if (coins.get(itemPosition).getCoin().getCanWithdraw() != 0) {
                    list.add(getResources().getString(R.string.withdraw));
                }

                BottomSelectionFragment bottomSelectionFragment = BottomSelectionFragment.getInstance(list);
                bottomSelectionFragment.show(getActivity().getSupportFragmentManager(), "BottomSelectionFragment");
                bottomSelectionFragment.setOnItemSelectListener(new BottomSelectionFragment.OnItemSelectListener() {
                    @Override
                    public void onItemSelected(int position) {
                        //"充币"
                        if(list.get(position).equals(getResources().getString(R.string.chargeMoney))){
//                            if("4".equals(MyApplication.getApp().getCurrentUser().getKycStatus())){//KycStatus=4，已通过视频认证，才可以提币
                                if (WonderfulStringUtils.isEmpty(coins.get(itemPosition).getAddress())) {
                                    getAddress(coin);
                                } else {
                                    RechargeActivity.actionStart(getActivity(), coins.get(itemPosition));
                                }
//                            }else{
//                                WonderfulToastUtils.showToast("请先完成实名认证");
//                            }
                        }
                        //"提币"
                        if(list.get(position).equals(getResources().getString(R.string.withdraw))){
                            ExtractActivity.actionStart(getActivity(), coin);
                        }

                    }
                });
            }
        });
        recyclerView.setAdapter(adapter);
    }

    public void getAddress(final Coin coin) {
        WonderfulOkhttpUtils.post().url(UrlFactory.getChongbi())
                .addHeader("x-auth-token", SharedPreferenceInstance.getInstance().getTOKEN())
                .addParams("unit", coin.getCoin().getUnit())
                .build().execute(new StringCallback() {
            @Override
            public void onError(Request request, Exception e) {
                super.onError(request, e);
            }

            @Override
            public void onResponse(String response) {
                // WonderfulToastUtils.showToast(WonderfulToastUtils.getString(R.string.noAddAddressTip));
                WonderfulLogUtils.logi("miao", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int code = jsonObject.optInt("code");
                    if (code == 0) {
                        RechargeActivity.actionStart(getActivity(), coin);
                    } else {
                        WonderfulToastUtils.showToast("" + jsonObject.optString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

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


    private TextWatcher localChangeWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            adapter.setSearchKey(s.toString());
        }
    };

    public void setData(List<Coin> exchangeCoins) {
        coins.clear();
        coins.addAll(exchangeCoins);
        adapter.notifyDataSetChanged();
    }

    public void hideAmount(boolean isShowAmount){
        if(adapter!=null) {
            adapter.setHideAmount(isShowAmount);
            adapter.notifyDataSetChanged();
        }
    }
}
