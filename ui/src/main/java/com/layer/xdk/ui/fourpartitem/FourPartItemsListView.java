package com.layer.xdk.ui.fourpartitem;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.layer.sdk.query.Queryable;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.adapters.ItemRecyclerViewAdapter;
import com.layer.xdk.ui.recyclerview.ItemsRecyclerView;
import com.layer.xdk.ui.style.FourPartItemStyle;

import java.util.List;

public abstract class FourPartItemsListView<ITEM extends Queryable, ADAPTER extends ItemRecyclerViewAdapter> extends ConstraintLayout {

    protected FourPartItemStyle mFourPartItemStyle;
    protected ItemsRecyclerView<ITEM> mItemsRecyclerView;

    public FourPartItemsListView(Context context) {
        super(context);
        init();
    }

    public FourPartItemsListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        mFourPartItemStyle = new FourPartItemStyle(context, attrs, 0);
    }

    protected void init() {
        inflate(getContext(), R.layout.ui_four_part_items_list, this);
        mItemsRecyclerView = (ItemsRecyclerView<ITEM>) findViewById(R.id.ui_items_recycler);

        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        manager.setStackFromEnd(false);
        mItemsRecyclerView.setLayoutManager(manager);

        DefaultItemAnimator noChangeAnimator = new DefaultItemAnimator() {
            @Override
            public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull List<Object> payloads) {
                return true;
            }
        };
        noChangeAnimator.setSupportsChangeAnimations(false);
        mItemsRecyclerView.setItemAnimator(noChangeAnimator);
    }

    public void setAdapter(ADAPTER adapter) {
        adapter.setStyle(mFourPartItemStyle);
        mItemsRecyclerView.setAdapter(adapter);
        adapter.refresh();
    }

    public void onDestroy() {
        mItemsRecyclerView.onDestroy();
    }
}