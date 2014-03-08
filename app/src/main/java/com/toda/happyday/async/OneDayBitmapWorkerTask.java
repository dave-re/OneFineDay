package com.toda.happyday.async;

import android.media.MediaMetadataRetriever;
import android.widget.ImageView;
import android.widget.ListView;
import android.graphics.Bitmap;

import com.google.android.gms.internal.ch;
import com.toda.happyday.models.Picture;
import com.toda.happyday.utils.BitmapUtils;

/**
 * Created by fpgeek on 2014. 2. 25..
 */
public class OneDayBitmapWorkerTask extends BitmapWorkerTask {

    public OneDayBitmapWorkerTask(Picture picture, ImageView imageView, int position, String cacheName) {
        super(picture, imageView, position, cacheName);
    }

    @Override
    public Bitmap createBitmap(ImageView imageView) {
        if (mPicture.getType() == Picture.TYPE_IMAGE) {
            return BitmapUtils.decodeSampledBitmapFromFile(imagePath, imageView.getLayoutParams().width / 2, imageView.getLayoutParams().height / 2);
        } else {
            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            try {
                metaRetriever.setDataSource(mPicture.getFilePath());
                return metaRetriever.getFrameAtTime(0);
            } catch(RuntimeException exception) {
                return null;
            } finally {
                metaRetriever.release();
            }
        }

    }
}
