package com.toda.happyday.utils;

import android.view.View;
import android.widget.TextView;

/**
 * Created by fpgeek on 2014. 2. 24..
 */
public class TextViewUtil {

    public static void setText(TextView textView, String text) {
        textView.setText(text);
        if (text == null || text.equals("")) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
        }
    }
}
