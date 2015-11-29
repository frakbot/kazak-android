package io.kazak.session.view.speaker;

import android.content.Context;

public class ImageLoaderFactory {

    public static ImageLoader getInstance(Context context) {
        return new GlideImageLoader(context);
    }

}
