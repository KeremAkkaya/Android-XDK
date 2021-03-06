package com.layer.xdk.ui.testactivity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.layer.sdk.messaging.Identity;
import com.layer.xdk.ui.DaggerXdkUiTestComponent;
import com.layer.xdk.ui.FakeXdkUiModule;
import com.layer.xdk.ui.XdkUiTestComponent;
import com.layer.xdk.ui.databinding.XdkUiFourPartItemBinding;
import com.layer.xdk.ui.fourpartitem.FourPartItemStyle;
import com.layer.xdk.ui.identity.adapter.IdentityItemModel;
import com.layer.xdk.ui.identity.adapter.viewholder.IdentityItemVHModel;
import com.layer.xdk.ui.mock.MockIdentity;

public class IdentityItemTestActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        XdkUiTestComponent component = DaggerXdkUiTestComponent.builder()
                .fakeXdkUiModule(new FakeXdkUiModule(this)).build();

        Identity identity = new MockIdentity();

        FrameLayout frameLayout = new FrameLayout(this);
        XdkUiFourPartItemBinding binding = XdkUiFourPartItemBinding.inflate(
                LayoutInflater.from(this), frameLayout, true);
        setContentView(frameLayout);

        FourPartItemStyle style = new FourPartItemStyle(this, null, 0);

        IdentityItemVHModel viewHolderModel = component.identityItemViewModel();
        IdentityItemModel itemModel = new IdentityItemModel(identity);
        viewHolderModel.setItem(itemModel);

        binding.avatar.setIdentityFormatter(viewHolderModel.getIdentityFormatter());
        binding.avatar.setImageCacheWrapper(viewHolderModel.getImageCacheWrapper());
        binding.avatar.setParticipants(identity);
        binding.setStyle(style);
        binding.setViewHolderModel(viewHolderModel);
    }
}
