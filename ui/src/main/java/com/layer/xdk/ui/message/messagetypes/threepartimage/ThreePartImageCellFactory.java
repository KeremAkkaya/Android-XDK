package com.layer.xdk.ui.message.messagetypes.threepartimage;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.databinding.XdkUiMessageItemCellImageBinding;
import com.layer.xdk.ui.message.legacy.ThreePartImageConstants;
import com.layer.xdk.ui.message.messagetypes.CellFactory;
import com.layer.xdk.ui.util.Log;
import com.layer.xdk.ui.util.Util;
import com.layer.xdk.ui.util.imagecache.ImageCacheWrapper;
import com.layer.xdk.ui.util.imagecache.ImageRequestParameters;
import com.layer.xdk.ui.util.imagepopup.ImagePopupActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

/**
 * ThreePartImage handles image Messages with three parts: full image, preview image, and
 * image metadata.  The image metadata contains full image dimensions and rotation information used
 * for sizing and rotating images efficiently.
 */
public class ThreePartImageCellFactory extends
        CellFactory<ThreePartImageCellFactory.CellHolder, ThreePartImageCellFactory.Info> implements View.OnClickListener {
    private static final String IMAGE_CACHING_TAG = ThreePartImageCellFactory.class.getSimpleName();

    private static final int PLACEHOLDER = R.drawable.xdk_ui_message_item_cell_placeholder;
    private static final int CACHE_SIZE_BYTES = 256 * 1024;

    private final LayerClient mLayerClient;
    private final ImageCacheWrapper mImageCacheWrapper;

    public ThreePartImageCellFactory(LayerClient layerClient, ImageCacheWrapper imageCacheWrapper) {
        super(CACHE_SIZE_BYTES);
        mLayerClient = layerClient;
        mImageCacheWrapper = imageCacheWrapper;
    }

    @Override
    public boolean isBindable(Message message) {
        return isType(message);
    }

    @Override
    public CellHolder createCellHolder(ViewGroup cellView, boolean isMe, LayoutInflater layoutInflater) {

        return new CellHolder(XdkUiMessageItemCellImageBinding.inflate(layoutInflater, cellView, true));
    }

    @Override
    public Info parseContent(LayerClient layerClient, Message message) {
        return getInfo(message);
    }

    @Override
    public void bindCellHolder(final CellHolder cellHolder, final Info info, final Message message, CellHolderSpecs specs) {
        cellHolder.mImageView.setTag(info);
        cellHolder.mImageView.setOnClickListener(this);
        final ThreePartMessageParts parts = new ThreePartMessageParts(message);

        // Info width and height are the rotated width and height, though the content is not pre-rotated.
        int[] cellDims = Util.scaleDownInside(info.width, info.height, specs.maxWidth, specs.maxHeight);
        final ViewGroup.LayoutParams params = cellHolder.mImageView.getLayoutParams();
        params.width = cellDims[0];
        params.height = cellDims[1];
        cellHolder.mProgressBar.show();

        int width, height, rotate;


        switch (info.orientation) {
            case ThreePartImageConstants.ORIENTATION_0:
                width = cellDims[0];
                height = cellDims[1];
                rotate = 0;
                break;
            case ThreePartImageConstants.ORIENTATION_90:
                width = cellDims[1];
                height = cellDims[0];
                rotate = -90;
                break;
            case ThreePartImageConstants.ORIENTATION_180:
                width = cellDims[0];
                height = cellDims[1];
                rotate = 180;
                break;
            default:
                width = cellDims[1];
                height = cellDims[0];
                rotate = 90;
                break;
        }

        ImageCacheWrapper.Callback callback = new ImageCacheWrapper.Callback() {
            @Override
            public void onSuccess() {
                cellHolder.mProgressBar.hide();
            }

            @Override
            public void onFailure() {
                cellHolder.mProgressBar.hide();
            }
        };

        ImageRequestParameters imageRequestParameters = new ImageRequestParameters
                .Builder(parts.getPreviewPart().getId())
                .placeHolder(PLACEHOLDER)
                .resize(width, height)
                .tag(IMAGE_CACHING_TAG)
                .rotate(rotate)
                .centerCrop(false)
                .onlyScaleDown(false)
                .defaultCircularTransform(true)
                .callback(callback)
                .build();

        mImageCacheWrapper.loadImage(imageRequestParameters, cellHolder.mImageView);

        cellHolder.mImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

                BitmapFactory.decodeStream(parts.getFullPart().getDataStream(), null, options);
                Log.v("Full size: " + options.outWidth + "x" + options.outHeight);

                BitmapFactory.decodeStream(parts.getPreviewPart().getDataStream(), null, options);
                Log.v("Preview size: " + options.outWidth + "x" + options.outHeight);

                Log.v("Info: " + new String(parts.getInfoPart().getData()));

                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        ImagePopupActivity.init(mLayerClient);
        Context context = v.getContext();
        if (context == null) return;
        Info info = (Info) v.getTag();
        Intent intent = new Intent(context, ImagePopupActivity.class);
        intent.putExtra("previewId", info.previewPartId);
        intent.putExtra("fullId", info.fullPartId);
        intent.putExtra("info", info);

        if (Build.VERSION.SDK_INT >= 21 && context instanceof Activity) {
            context.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation((Activity) context, v, "image").toBundle());
        } else {
            context.startActivity(intent);
        }
    }

    @Override
    public void onScrollStateChanged(int newState) {
        switch (newState) {
            case RecyclerView.SCROLL_STATE_DRAGGING:
                mImageCacheWrapper.pauseTag(IMAGE_CACHING_TAG);
                break;
            case RecyclerView.SCROLL_STATE_IDLE:
            case RecyclerView.SCROLL_STATE_SETTLING:
                mImageCacheWrapper.resumeTag(IMAGE_CACHING_TAG);
                break;
        }
    }

    //==============================================================================================
    // Utilities
    //==============================================================================================

    public boolean isType(Message message) {
        Set<MessagePart> parts = message.getMessageParts();
        if (parts.size() != 3) {
            return false;
        }

        boolean hasInfoPart = false;
        boolean hasPreviewPart = false;
        boolean hasFullPart = false;
        for (MessagePart part : parts) {
            if (part.getMimeType().equals(ThreePartImageConstants.MIME_TYPE_INFO)) {
                hasInfoPart = true;
            } else if (part.getMimeType().equals(ThreePartImageConstants.MIME_TYPE_PREVIEW)) {
                hasPreviewPart = true;
            } else if (part.getMimeType().startsWith("image/")) {
                hasFullPart = true;
            }
        }
        return hasInfoPart && hasPreviewPart && hasFullPart;
    }

    @Override
    public String getPreviewText(Context context, Message message) {
        if (isType(message)) {
            return context.getString(R.string.xdk_ui_message_preview_image);
        }
        else {
            throw new IllegalArgumentException("Message is not of the correct type - ThreePartImage");
        }
    }

    public static Info getInfo(Message message) {
        ThreePartMessageParts parts = new ThreePartMessageParts(message);

        try {
            Info info = new Info();
            JSONObject infoObject = new JSONObject(new String(parts.getInfoPart().getData()));
            info.orientation = infoObject.getInt("orientation");
            info.width = infoObject.getInt("width");
            info.height = infoObject.getInt("height");
            info.previewPartId = parts.getPreviewPart().getId();
            info.fullPartId = parts.getFullPart().getId();
            return info;
        } catch (JSONException e) {
            if (Log.isLoggable(Log.ERROR)) {
                Log.e(e.getMessage(), e);
            }
        }
        return null;
    }


    //==============================================================================================
    // Inner classes
    //==============================================================================================

    public static class Info implements CellFactory.ParsedContent, Parcelable {
        public int orientation;
        public int width;
        public int height;
        public Uri fullPartId;
        public Uri previewPartId;

        @Override
        public int sizeOf() {
            return ((Integer.SIZE + Integer.SIZE + Integer.SIZE) / Byte.SIZE) + fullPartId.toString().getBytes().length + previewPartId.toString().getBytes().length;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(orientation);
            dest.writeInt(width);
            dest.writeInt(height);
        }

        public static final Parcelable.Creator<Info> CREATOR
                = new Parcelable.Creator<Info>() {
            public Info createFromParcel(Parcel in) {
                Info info = new Info();
                info.orientation = in.readInt();
                info.width = in.readInt();
                info.height = in.readInt();
                return info;
            }

            public Info[] newArray(int size) {
                return new Info[size];
            }
        };
    }

    static class CellHolder extends CellFactory.CellHolder {
        private ImageView mImageView;
        private ContentLoadingProgressBar mProgressBar;

        public CellHolder(XdkUiMessageItemCellImageBinding uiMessageItemCellImageBinding) {
                mImageView = uiMessageItemCellImageBinding.cellImage;
                mProgressBar = uiMessageItemCellImageBinding.cellProgress;
        }
    }

    private static class ThreePartMessageParts {
        private MessagePart mInfoPart;
        private MessagePart mPreviewPart;
        private MessagePart mFullPart;

        public ThreePartMessageParts(Message message) {
            Set<MessagePart> messageParts = message.getMessageParts();

            for (MessagePart part : messageParts) {
                if (part.getMimeType().equals(ThreePartImageConstants.MIME_TYPE_INFO)) {
                    mInfoPart = part;
                } else if (part.getMimeType().equals(ThreePartImageConstants.MIME_TYPE_PREVIEW)) {
                    mPreviewPart = part;
                } else if (part.getMimeType().startsWith("image/")) {
                    mFullPart = part;
                }
            }

            if (mInfoPart == null || mPreviewPart == null || mFullPart == null) {
                if (Log.isLoggable(Log.ERROR)) {
                    Log.e("Incorrect parts for a three part image: " + messageParts);
                }
                throw new IllegalArgumentException("Incorrect parts for a three part image: " + messageParts);
            }
        }

        @NonNull
        public MessagePart getInfoPart() {
            return mInfoPart;
        }

        @NonNull
        public MessagePart getPreviewPart() {
            return mPreviewPart;
        }

        @NonNull
        public MessagePart getFullPart() {
            return mFullPart;
        }
    }
}
