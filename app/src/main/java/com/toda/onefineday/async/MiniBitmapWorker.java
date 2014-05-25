package com.toda.onefineday.async;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.toda.onefineday.models.Picture;

/**
 * Created by fpgeek on 2014. 5. 25..
 */
public class MiniBitmapWorker extends BitmapWorkerTask {

    private ContentResolver mContentResolver;

    public MiniBitmapWorker(Picture picture, ImageView imageView, int position, String cacheName, ContentResolver contentResolver) {
        super(picture, imageView, position, cacheName);

        mContentResolver = contentResolver;
    }

    @Override
    public Bitmap createBitmap(ImageView imageView) {
        if (mPicture.getType() == Picture.TYPE_IMAGE) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            Bitmap thumbnailBitmap = MediaStore.Images.Thumbnails.getThumbnail(mContentResolver, mPicture.getId(), MediaStore.Images.Thumbnails.MINI_KIND, options);

            if (mPicture.getDegrees() == 90) {
                Matrix mx = new Matrix();
                mx.postRotate(mPicture.getDegrees());
                return Bitmap.createBitmap(thumbnailBitmap, 0, 0, thumbnailBitmap.getWidth(), thumbnailBitmap.getHeight(), mx ,true);
            }
            return thumbnailBitmap;
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
