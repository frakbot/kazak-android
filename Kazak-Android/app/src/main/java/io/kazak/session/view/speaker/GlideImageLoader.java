package io.kazak.session.view.speaker;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class GlideImageLoader implements ImageLoader {
    private Context context;

    protected GlideImageLoader(Context context) {
        this.context = context;
    }

    @Override
    public void load(String url, ImageView imageView) {
        Glide.with(context).load(url).into(imageView);
    }
}
