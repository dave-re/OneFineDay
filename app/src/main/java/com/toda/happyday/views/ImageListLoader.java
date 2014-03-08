package com.toda.happyday.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.toda.happyday.async.BitmapWorkerTask;
import com.toda.happyday.models.Picture;

/**
 * Created by fpgeek on 2014. 2. 15..
 */
public class ImageListLoader {

    private Context mContext;

    public ImageListLoader(Context context) {
        mContext = context;
    }

    public void loadBitmap(Picture picture, Bitmap loadingBitmap, ImageView imageView, BitmapWorkerTask bitmapWorkerTask, String cacheName) {
        boolean isCancelWork = cancelPotentialWork(picture.getFilePath(), imageView);

        final Bitmap bitmap = bitmapWorkerTask.getBitmapFromMemCache(cacheName + picture.getFilePath());
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            if (isCancelWork) {
                final BitmapWorkerTask task = bitmapWorkerTask;
                final BitmapWorkerTask.AsyncDrawable asyncDrawable =
                        new BitmapWorkerTask.AsyncDrawable(mContext.getResources(), loadingBitmap, task);
                imageView.setImageDrawable(asyncDrawable);
                task.execute(picture.getFilePath());
            }
        }
    }

    private static boolean cancelPotentialWork(String bitmapImagePath, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String imagePath = bitmapWorkerTask.getImagePath();

            if (!bitmapImagePath.equals(imagePath)) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof BitmapWorkerTask.AsyncDrawable) {
                final BitmapWorkerTask.AsyncDrawable asyncDrawable = (BitmapWorkerTask.AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }
}
