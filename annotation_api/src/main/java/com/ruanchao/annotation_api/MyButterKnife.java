package com.ruanchao.annotation_api;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;

public class MyButterKnife {

    private static final Object SUFFIX = "$$Proxy";

    public static void bind(Activity activity){
        View view = activity.getWindow().getDecorView();
        createBinding(activity,view);
    }

    public static void bind(Fragment fragment, View view){
        createBinding(fragment,view);
    }

    public static void bind(View view){
        createBinding(view, view);
    }

    private static void createBinding(Object activity, View view) {
        try {
            String className = activity.getClass().getName() + SUFFIX;
            IProxy ProxyClass = (IProxy) Class.forName(className).newInstance();
            ProxyClass.inject(activity,view);
        }catch (Exception e){
        }
    }
}
