package com.toda.happyday.async;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.path.android.jobqueue.JobManager;
import com.toda.happyday.job.LocationJob;
import com.toda.happyday.models.Picture;

import java.lang.ref.WeakReference;

/**
 * Created by fpgeek on 2014. 2. 25..
 */
abstract public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
    private static LruCache<String, Bitmap> mMemoryCache;
    static {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    private final WeakReference<ImageView> imageViewReference;
    protected Picture mPicture;
    protected String imagePath;
    protected int position;
    protected String cacheName;

    public BitmapWorkerTask(Picture picture, ImageView imageView, int position, String cacheName) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        this.mPicture = picture;
        this.imageViewReference = new WeakReference<ImageView>(imageView);
        this.position = position;
        this.cacheName = cacheName;
    }

    abstract public Bitmap createBitmap(ImageView imageView);

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(String... params) {
        imagePath = params[0];
        final ImageView imageView = imageViewReference.get();
        if (imageView != null) {
            Bitmap bitmap = createBitmap(imageView);
            if (mPicture.getDegrees() == 90 || mPicture.getDegrees() == 270) {
                Matrix mx = new Matrix();
                mx.postRotate(mPicture.getDegrees());
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mx ,true);
            } else if (mPicture.getDegrees() == 180) {
                Matrix mx = new Matrix();
                mx.postRotate(mPicture.getDegrees());
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mx ,true);
            }
            addBitmapToMemoryCache(cacheName + imagePath, bitmap);
            return bitmap;
        }
        return null;
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {

        if (isCancelled()) {
            bitmap = null;
        }

        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask =
                    getBitmapWorkerTask(imageView);
            if (this == bitmapWorkerTask && imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (key == null || bitmap == null) { return; }

        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    public static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                             BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    public String getImagePath() {
        return imagePath;
    }
}
