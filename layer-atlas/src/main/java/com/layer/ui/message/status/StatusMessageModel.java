package com.layer.ui.message.status;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.MessagePart;
import com.layer.ui.R;
import com.layer.ui.message.model.MessageModel;
import com.layer.ui.message.view.MessageView;
import com.layer.ui.util.json.AndroidFieldNamingStrategy;

import java.io.InputStreamReader;

public class StatusMessageModel extends MessageModel {

    public static final String MIME_TYPE = "application/vnd.layer.status+json";
    private static final int PREVIEW_MAX_LENGTH = 100;

    private StatusMessageMetadata mMetadata;
    private final Gson mGson;

    public StatusMessageModel(Context context, LayerClient layerClient) {
        super(context, layerClient);
        mGson = new GsonBuilder().setFieldNamingStrategy(new AndroidFieldNamingStrategy()).create();
    }


    @Override
    public Class<? extends MessageView> getRendererType() {
        // No renderer type since this is rendered inside a MessageItemStatusViewModel
        return null;
    }

    @Override
    protected void parse(@NonNull MessagePart messagePart) {
        JsonReader reader = new JsonReader(new InputStreamReader(messagePart.getDataStream()));
        mMetadata = mGson.fromJson(reader, StatusMessageMetadata.class);
    }

    @Override
    protected boolean shouldDownloadContentIfNotReady(@NonNull MessagePart messagePart) {
        return true;
    }

    @Nullable
    @Override
    public String getTitle() {
        return null;
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @Nullable
    @Override
    public String getFooter() {
        return null;
    }

    @Override
    public boolean getHasContent() {
        return mMetadata != null;
    }

    @Nullable
    @Override
    public String getPreviewText() {
        if (mMetadata != null && mMetadata.getText() != null) {
            String text = mMetadata.getText();
            return text.length() > PREVIEW_MAX_LENGTH ? text.substring(0, PREVIEW_MAX_LENGTH) : text;
        }
        return getContext().getString(R.string.ui_status_message_preview_text);
    }

    @Override
    public String getActionEvent() {
        if (super.getActionEvent() != null) {
            return super.getActionEvent();
        }

        if (mMetadata.getAction() != null) {
            return mMetadata.getAction().getEvent();
        }
        return null;
    }


    @NonNull
    @Override
    public JsonObject getActionData() {
        if (super.getActionData().size() > 0) {
            return super.getActionData();
        }

        if (mMetadata.getAction() != null) {
            return mMetadata.getAction().getData();
        }

        return new JsonObject();
    }

    @Nullable
    public String getText() {
        if (mMetadata != null && mMetadata.getText() != null) {
            return mMetadata.getText().trim();
        }
        return null;
    }
}
