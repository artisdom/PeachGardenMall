package me.zoro.peachgardenmall.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.zoro.peachgardenmall.R;
import me.zoro.peachgardenmall.model.Goods;

/**
 * Created by dengfengdecao on 16/12/5.
 */

public class MyShoppingCartRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_EMPTY = -1;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;


    private Context mContext;
    private List<Goods> mGoodses;

    public MyShoppingCartRecyclerViewAdapter(Context context, List<Goods> goodses) {
        mContext = context;
        mGoodses = goodses;
    }

    public int getItemViewType(int position) {
        // item 第一个位置position为0，之后递增
        if (isEmpty()) {
            return TYPE_EMPTY;
        }

        return TYPE_ITEM;
    }

    private boolean isEmpty() {
        return mGoodses.size() == 0;
    }

    public void replaceData(List<Goods> goods) {
        mGoodses = goods;
        // 调用以下方法更新后，会依次调用getItemViewType和onBindViewHolder方法
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_EMPTY) {
            View viewItem = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1,
                    parent, false);
            return new RecyclerEmptyViewHolder(viewItem);
        }
        View viewItem = LayoutInflater.from(mContext).inflate(R.layout.my_shopping_cart_rvi,
                parent, false);

        return RecyclerItemViewHolder.newInstance(viewItem);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // item 第一个位置position为0，之后递增
        if (holder instanceof RecyclerEmptyViewHolder) {
            RecyclerEmptyViewHolder viewHolder = (RecyclerEmptyViewHolder) holder;
            viewHolder.mTvEmptyHint.setText(R.string.empty_data_hint);
        } else {
            RecyclerItemViewHolder viewHolder = (RecyclerItemViewHolder) holder;
            Goods goods = getItem(position);
            viewHolder.mGoodsNameTv.setText(goods.getName());
        }
    }

    private Goods getItem(int position) {
        return mGoodses.get(position);
    }

    @Override
    public int getItemCount() {
        return mGoodses.size() > 0 ? mGoodses.size() : 1;
    }


    static class RecyclerItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.goods_img_iv)
        ImageView mGoodsesImgIv;
        @BindView(R.id.goods_info)
        LinearLayout mGoodsesInfo;
        @BindView(R.id.goods_name_tv)
        TextView mGoodsNameTv;
        @BindView(R.id.subtract_iv)
        ImageView mSubtractIv;
        @BindView(R.id.count_tv)
        TextView mCountTv;
        @BindView(R.id.add_iv)
        ImageView mAddIv;

        private int count = 1;

        public RecyclerItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public static RecyclerView.ViewHolder newInstance(View viewItem) {
            return new RecyclerItemViewHolder(viewItem);
        }

        @OnClick({R.id.goods_info, R.id.subtract_iv, R.id.add_iv})
        public void onViewClicked(View view) {
            switch (view.getId()) {
                case R.id.goods_info:
                    Toast.makeText(view.getContext(), "goods info", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.subtract_iv:
                    if (count <= 1) {
                        count = 1;
                    } else {
                        count--;
                    }
                    mCountTv.setText(String.valueOf(count));
                    break;
                case R.id.add_iv:
                    count++;
                    mCountTv.setText(String.valueOf(count));
                    break;
            }
        }
    }

    private class RecyclerEmptyViewHolder extends RecyclerView.ViewHolder {
        TextView mTvEmptyHint;

        public RecyclerEmptyViewHolder(View viewItem) {
            super(viewItem);
            mTvEmptyHint = (TextView) viewItem.findViewById(android.R.id.text1);
        }
    }
}
