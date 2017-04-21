package me.zoro.peachgardenmall.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.zoro.peachgardenmall.R;
import me.zoro.peachgardenmall.adapter.GoodsFilterAdapter;
import me.zoro.peachgardenmall.adapter.GoodsGridAdapter;
import me.zoro.peachgardenmall.datasource.GoodsDatasource;
import me.zoro.peachgardenmall.datasource.GoodsRepository;
import me.zoro.peachgardenmall.datasource.domain.Goods;
import me.zoro.peachgardenmall.datasource.remote.GoodsRemoteDatasource;
import me.zoro.peachgardenmall.fragment.MallFragment;

public class GoodsListActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, AbsListView.OnScrollListener {

    private static final String TAG = "GoodsListActivity";
    @BindView(R.id.searchView)
    SearchView mSearchView;
    @BindView(R.id.tb_comprehensive_sorting)
    Button mTbComprehensiveSorting;
    @BindView(R.id.tb_sales_sorting)
    Button mTbSalesSorting;
    @BindView(R.id.btn_filter)
    Button mBtnFilter;
    @BindView(R.id.grid_view)
    GridView mGridView;

    Button mBtnPromotion;
    Button mBtnSelfSupport;
    RecyclerView mLeftRecyclerView;
    RecyclerView mRightRecyclerView;

    TextView mTvGoodsCount;
    TextView mTvReset;
    TextView mTvConfirm;

    private GoodsRepository mGoodsRepository;

    private GoodsGridAdapter mGoodsGridAdapter;
    private List<Goods> mGoodses;
    /**
     * 是否正在加载更多，true，表示正在加载，false，则不是
     */
    private boolean isLoadingMore;
    /**
     * 默认获取第一页
     */
    private int mPageNum = 1;
    /**
     * 默认获取10条
     */
    private int mPageSize = 10;

    private int mCategoryId;

    private PopupWindow mPopupWindow;
    private String[] mCategory = new String[]{"直辖市", "特别行政区", "黑龙江"};
    private String[][] mRightCategory = new String[][]{{"北京", "上海", "天津", "重庆"}, {"香港", "澳门"},
            {"哈尔滨", "齐齐哈尔", "牡丹江", "大庆", "伊春", "双鸭山", "鹤岗", "鸡西", "佳木斯", "七台河", "黑河", "绥化", "大兴安岭"}};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goods_list);
        ButterKnife.bind(this);

        initPopupWindow();

        if (getIntent() != null) {
            mGoodses = (ArrayList<Goods>) getIntent().getSerializableExtra(MallFragment.GOODSES_EXTRA);
            mCategoryId = getIntent().getIntExtra(MallFragment.GOODS_CATEGORY_EXTRA, -1);
        }

        mGoodsRepository = GoodsRepository.getInstance(GoodsRemoteDatasource.getInstance(
                getApplicationContext()
        ));

        mGoodsGridAdapter = new GoodsGridAdapter(this, mGoodses);
        mGridView.setAdapter(mGoodsGridAdapter);
        mGridView.setOnItemClickListener(this);
        mGridView.setOnScrollListener(this);
    }

    private void initPopupWindow() {
        View contentView = LayoutInflater.from(this).inflate(R.layout.filter_content, null);
        mBtnPromotion = (Button) contentView.findViewById(R.id.btn_promotion);
        mBtnSelfSupport = (Button) contentView.findViewById(R.id.btn_self_support);
        mLeftRecyclerView = (RecyclerView) contentView.findViewById(R.id.left_recycler_view);
        mRightRecyclerView = (RecyclerView) contentView.findViewById(R.id.right_recycler_view);
        mTvGoodsCount = (TextView) contentView.findViewById(R.id.tv_goods_count);
        mTvReset = (TextView) contentView.findViewById(R.id.tv_reset);
        mTvConfirm = (TextView) contentView.findViewById(R.id.tv_confirm);


        mBtnPromotion.setOnClickListener(this);
        mBtnSelfSupport.setOnClickListener(this);
        mTvReset.setOnClickListener(this);
        mTvConfirm.setOnClickListener(this);


        mPopupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.GRAY));
        GoodsFilterAdapter leftAdapter = new GoodsFilterAdapter(this, mCategory);
        mLeftRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mLeftRecyclerView.setAdapter(leftAdapter);

        mRightRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRightRecyclerView.setVisibility(View.GONE);
        leftAdapter.setOnItemClickListener(new GoodsFilterAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                showMessage(mCategory[position]);
                mRightRecyclerView.setVisibility(View.VISIBLE);
                final String[] secondLevel = mRightCategory[position];
                GoodsFilterAdapter rightAdapter = new GoodsFilterAdapter(GoodsListActivity.this, secondLevel);
                mRightRecyclerView.setAdapter(rightAdapter);
                rightAdapter.setOnItemClickListener(new GoodsFilterAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        showMessage(secondLevel[position]);
                    }
                });
            }
        });

    }

    private void showMessage(String msg) {
        if (!isFinishing()) {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick({R.id.tb_comprehensive_sorting, R.id.tb_sales_sorting, R.id.btn_filter})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            // TODO: 17/4/13 商品排序
            case R.id.tb_comprehensive_sorting:
                break;
            case R.id.tb_sales_sorting:
                break;
            case R.id.btn_filter:
                mPopupWindow.showAsDropDown(mBtnFilter);
                break;

        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_promotion:
                Log.d(TAG, "onClick: 促销");
                break;
            case R.id.btn_self_support:
                Log.d(TAG, "onClick: 自营");
                break;
            case R.id.tv_reset:
                Log.d(TAG, "onClick: 重置");
                break;
            case R.id.tv_confirm:
                Log.d(TAG, "onClick: 确定");
                break;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Goods goods = mGoodses.get(position);
        // TODO: 17/4/9 商品点击事件
        Toast.makeText(this, goods.getGoodsName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (SCROLL_STATE_IDLE == scrollState && view.getAdapter().getCount() == mPageSize) {
            isLoadingMore = false;
            mPageNum = 1;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // 上拉加载
        if (view.getLastVisiblePosition() == totalItemCount - 1 && !isLoadingMore && mCategoryId != -1) {
            isLoadingMore = true;
            mPageNum++;
            new FetchGoodsesTask().execute(mCategoryId);
        }
    }

    private class FetchGoodsesTask extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... params) {
            Map<String, Object> map = new HashMap<>();
            map.put("categoryId", params[0]);
            map.put("pn", mPageNum);
            map.put("ps", mPageSize);
            mGoodsRepository.getGoodses(map, new GoodsDatasource.GetGoodsesCallback() {
                @Override
                public void onGoodsesLoaded(ArrayList<Goods> goodses) {
                    if (goodses.size() > 0) {
                        mGoodses = goodses;
                        if (mPageNum > 1) {
                            mGoodsGridAdapter.appendData(goodses);
                        } else {
                            mGoodsGridAdapter.replaceData(goodses);
                        }
                        isLoadingMore = false;
                    }
                }

                @Override
                public void onDataNotAvailable(String errorMsg) {
                    showMessage(errorMsg);
                    isLoadingMore = false;
                }
            });
            return null;
        }
    }
}
