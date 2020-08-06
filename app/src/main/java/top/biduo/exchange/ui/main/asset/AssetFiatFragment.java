package top.biduo.exchange.ui.main.asset;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import top.biduo.exchange.R;
import top.biduo.exchange.adapter.FiatAssetAdapter;
import top.biduo.exchange.adapter.TextWatcher;
import top.biduo.exchange.adapter.WalletAdapter;
import top.biduo.exchange.base.BaseFragment;
import top.biduo.exchange.customview.BottomSelectionFragment;
import top.biduo.exchange.data.DataSource;
import top.biduo.exchange.data.RemoteDataSource;
import top.biduo.exchange.entity.Coin;
import top.biduo.exchange.entity.FiatAssetBean;
import top.biduo.exchange.ui.wallet.WalletDialogFragment;
import top.biduo.exchange.utils.WonderfulToastUtils;

/**
 * @author 明哥
 * 法币资产
 * 参考火币命名
 */

public class AssetFiatFragment extends BaseFragment {
    @BindView(R.id.rc_asset)
    RecyclerView recyclerView;
    List<FiatAssetBean> coins = new ArrayList<>();
    private FiatAssetAdapter adapter;

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
        adapter = new FiatAssetAdapter(R.layout.adapter_wallet, coins);
        adapter.isFirstOnly(true);
        recyclerView.setAdapter(adapter);

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

    public void setData(List<FiatAssetBean> exchangeCoins) {
        coins.clear();
        coins.addAll(exchangeCoins);
        if(adapter!=null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void hideAmount(boolean isShowAmount){
        if(adapter!=null) {
            adapter.setHideAmount(isShowAmount);
            adapter.notifyDataSetChanged();
        }
    }

}
