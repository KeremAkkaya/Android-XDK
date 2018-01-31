package com.layer.xdk.ui.message;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.layer.sdk.LayerClient;
import com.layer.xdk.ui.identity.IdentityFormatter;
import com.layer.xdk.ui.message.messagetypes.CellFactory;
import com.layer.xdk.ui.util.DateFormatter;
import com.layer.xdk.ui.util.imagecache.ImageCacheWrapper;

import java.util.List;

public class MessageItemsListViewModel extends BaseObservable {
    protected MessageItemsAdapter mMessageItemsAdapter;
    protected List<CellFactory> mCellFactories;

    public MessageItemsListViewModel(Context context, LayerClient layerClient,
                                     ImageCacheWrapper imageCacheWrapper,
                                     DateFormatter dateFormatter,
                                     IdentityFormatter identityFormatter) {
        mMessageItemsAdapter = new MessageItemsAdapter(context, layerClient,
                imageCacheWrapper, dateFormatter, identityFormatter);
    }

    @Bindable
    public MessageItemsAdapter getAdapter() {
        return mMessageItemsAdapter;
    }

    @Bindable
    public List<CellFactory> getCellFactories() {
        return mCellFactories;
    }

    public void setCellFactories(List<CellFactory> cellFactories) {
        mCellFactories = cellFactories;
        notifyChange();
    }
}