package com.layer.xdk.ui.message.file;

import com.google.gson.annotations.SerializedName;
import com.layer.xdk.ui.message.MessageMetadata;

import java.util.Date;

@SuppressWarnings("WeakerAccess") // For Gson serialization/de-serialization
public class FileMessageMetadata extends MessageMetadata {

    @SerializedName("author")
    public String mAuthor;

    @SerializedName("created_at")
    public Date mCreatedAt;

    @SerializedName("comment")
    public String mComment;

    @SerializedName("size")
    public long mSize;

    @SerializedName("title")
    public String mTitle;

    @SerializedName("mime_type")
    public String mMimeType;

    @SerializedName("updated_at")
    public Date mUpdatedAt;

    @SerializedName("source_url")
    public String mSourceUrl;
}
