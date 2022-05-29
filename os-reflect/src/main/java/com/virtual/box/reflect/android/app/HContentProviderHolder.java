package com.virtual.box.reflect.android.app;

import android.content.pm.ProviderInfo;
import android.os.IInterface;

import com.virtual.box.reflect.MirrorReflection;

public class HContentProviderHolder {
    public static final MirrorReflection REF = MirrorReflection.on("android.app.ContentProviderHolder");

    public static MirrorReflection.FieldWrapper<ProviderInfo> info = REF.field("info");
    public static MirrorReflection.FieldWrapper<IInterface> provider = REF.field("provider");
}
