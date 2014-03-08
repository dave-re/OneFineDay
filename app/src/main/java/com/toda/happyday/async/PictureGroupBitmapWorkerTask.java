package com.toda.happyday.async;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.android.gms.internal.bi;
import com.toda.happyday.models.Picture;

/**
 * Created by fpgeek on 2014. 2. 25..
 */
public class PictureGroupBitmapWorkerTask extends BitmapWorkerTask {

    private ContentResolver mContentResolver;

    public PictureGroupBitmapWorkerTask(ContentResolver contentResolver, Picture picture, ImageView imageView, int position, String cacheName) {
        super(picture, imageView, position, cacheName);
        mContentResolver = contentResolver;
    }

    @Override
    public Bitmap createBitmap(ImageView imageView) {
        if (mPicture.getType() == Picture.TYPE_IMAGE) {
            return MediaStore.Images.Thumbnails.getThumbnail(mContentResolver, mPicture.getId(), MediaStore.Images.Thumbnails.MINI_KIND, null);
        } else {
            return MediaStore.Video.Thumbnails.getThumbnail(mContentResolver, mPicture.getId(), MediaStore.Video.Thumbnails.MINI_KIND, null);
        }
    }
}
