package com.ruanchao.annotation_api;

import android.view.View;

public interface IProxy<T> {

    public void inject(T target, View root);
}
