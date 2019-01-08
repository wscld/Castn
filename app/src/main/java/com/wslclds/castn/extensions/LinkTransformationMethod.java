package com.wslclds.castn.extensions;

import android.graphics.Rect;
import android.net.Uri;
import android.os.Parcel;
import android.support.customtabs.CustomTabsIntent;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.TransformationMethod;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;

public class LinkTransformationMethod implements TransformationMethod {

    private final int linkifyOptions;

    public LinkTransformationMethod(int linkifyOptions) {
        this.linkifyOptions = linkifyOptions;
    }

    @Override
    public CharSequence getTransformation(CharSequence source, View view) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            Linkify.addLinks(textView, linkifyOptions);
            if (textView.getText() == null || !(textView.getText() instanceof Spannable)) {
                return source;
            }
            Spannable text = (Spannable) textView.getText();
            URLSpan[] spans = text.getSpans(0, textView.length(), URLSpan.class);
            for (int i = spans.length - 1; i >= 0; i--) {
                URLSpan oldSpan = spans[i];
                int start = text.getSpanStart(oldSpan);
                int end = text.getSpanEnd(oldSpan);
                String url = oldSpan.getURL();
                if (!Patterns.WEB_URL.matcher(url).matches()) {
                    continue;
                }
                text.removeSpan(oldSpan);
                text.setSpan(new ChromeTabsUrlSpan(url), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return text;
        }
        return source;
    }

    @Override
    public void onFocusChanged(View view, CharSequence sourceText, boolean focused, int direction, Rect previouslyFocusedRect) {

    }

    public class ChromeTabsUrlSpan extends URLSpan {
        public ChromeTabsUrlSpan(String url) {
            super(url);
        }

        public ChromeTabsUrlSpan(Parcel src) {
            super(src);
        }

        @Override
        public void onClick(View widget) {
            String url = getURL();
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(widget.getContext(), Uri.parse(url));
        }
    }
}