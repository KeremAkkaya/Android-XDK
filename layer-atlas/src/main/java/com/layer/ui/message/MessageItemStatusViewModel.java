package com.layer.ui.message;


import android.content.Context;
import android.databinding.Bindable;
import android.databinding.Observable;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.ui.message.model.MessageModel;
import com.layer.ui.message.model.MessageModelManager;
import com.layer.ui.message.response.ResponseMessageModel;
import com.layer.ui.message.status.StatusMessageModel;
import com.layer.ui.viewmodel.ItemViewModel;

@SuppressWarnings("WeakerAccess")
public class MessageItemStatusViewModel extends ItemViewModel<Message> {
    private boolean mEnableReadReceipts;
    private boolean mVisible;
    private String mText;
    private MessageModelManager mModelRegistry;

    private MessageModel mMessageModel;

    public MessageItemStatusViewModel(Context context, LayerClient layerClient, MessageModelManager modelRegistry) {
        super(context, layerClient);
        mModelRegistry = modelRegistry;

    }

    public void update() {
        Message message = getItem();
        if (mMessageModel != null && mMessageModel.getMessage().equals(message)) {
            // Skip as this is an unnecessary update
            return;
        }

        String rootMimeType = MessagePartUtils.getRootMimeType(message);
        if (ResponseMessageModel.MIME_TYPE.equals(rootMimeType)) {
            mMessageModel = createResponseModel(message);
        } else {
            mMessageModel = createStatusModel(message);
        }

        boolean myMessage = getItem().getSender().equals(getLayerClient().getAuthenticatedUser());
        if (!myMessage && mEnableReadReceipts) {
            message.markAsRead();
        }
        notifyChange();
    }

    private ResponseMessageModel createResponseModel(Message message) {
        final ResponseMessageModel responseModel = new ResponseMessageModel(getContext(),
                getLayerClient());
        responseModel.setMessageModelManager(mModelRegistry);
        responseModel.setMessage(message);
        mText = responseModel.getText();
        mVisible = responseModel.getHasContent();
        responseModel.addOnPropertyChangedCallback(new OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                mText = responseModel.getText();
                mVisible = responseModel.getHasContent();
                notifyChange();
            }
        });
        return responseModel;
    }

    private StatusMessageModel createStatusModel(Message message) {
        final StatusMessageModel statusModel = new StatusMessageModel(getContext(),
                getLayerClient());
        statusModel.setMessageModelManager(mModelRegistry);
        statusModel.setMessage(message);
        mText = statusModel.getText();
        mVisible = statusModel.getHasContent();
        statusModel.addOnPropertyChangedCallback(new OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                mText = statusModel.getText();
                mVisible = statusModel.getHasContent();
                notifyChange();
            }
        });
        return statusModel;
    }

    public void setEnableReadReceipts(boolean enableReadReceipts) {
        mEnableReadReceipts = enableReadReceipts;
    }

    @Bindable
    public CharSequence getText() {
        return mText;
    }

    @Bindable
    public boolean isVisible() {
        return mVisible;
    }
}
