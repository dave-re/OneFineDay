package com.toda.happyday.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.toda.happyday.R;
import com.toda.happyday.async.BitmapWorkerTask;
import com.toda.happyday.async.OneDayBitmapWorkerTask;
import com.toda.happyday.models.PictureGroup;
import com.toda.happyday.models.Picture;
import com.toda.happyday.utils.TextViewUtil;


/**
 * Created by fpgeek on 2013. 12. 8..
 */
public class OneDayAdapter extends ArrayAdapter<Picture> {

    private static final String CACHE_NAME = "One";
    private Activity mActivity;
    private PictureGroup mPictureGroup;

    private int mWindowWidth = 0;
    private int mWindowHeight = 0;

    private static Bitmap mLoadingBitmap;
    private static ImageListLoader mImageListLoader;

    public OneDayAdapter(Activity activity, PictureGroup pictureGroup, ImageListLoader imageListLoader) {
        super(activity, R.layout.picture_group_item, pictureGroup);

        mActivity = activity;
        mPictureGroup = pictureGroup;

        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);

        mWindowWidth = metrics.widthPixels;
        mWindowHeight = metrics.heightPixels;

        mImageListLoader = imageListLoader;
        mLoadingBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.loading);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = mActivity.getLayoutInflater().inflate(R.layout.picture_item, null);

            viewHolder = new ViewHolder();
            viewHolder.pictureImageView = (ImageView)convertView.findViewById(R.id.picture);
            viewHolder.timeTextView = (TextView)convertView.findViewById(R.id.time_text);
            viewHolder.videoIconImageView = (ImageView)convertView.findViewById(R.id.video_icon);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        Picture picture = mPictureGroup.get(position);

        TextViewUtil.setText(viewHolder.timeTextView, picture.getTimeText());

        final int imageViewWidth = mWindowWidth;

        {
            double ratio = 1;
            if (picture.getDegrees() == 0 || picture.getDegrees() == 180) {
                ratio = (double)picture.getHeight() / (double)picture.getWidth();
            } else if (picture.getDegrees() == 90 || picture.getDegrees() == 270) {
                ratio = (double)picture.getWidth() / (double)picture.getHeight();
            }
            viewHolder.pictureImageView.getLayoutParams().width = imageViewWidth;
            viewHolder.pictureImageView.getLayoutParams().height = (int)(ratio * (double)imageViewWidth);
        }

        if (picture.getType() == Picture.TYPE_IMAGE) {
            viewHolder.videoIconImageView.setVisibility(View.GONE);
        } else {
            viewHolder.videoIconImageView.setVisibility(View.VISIBLE);
        }

        BitmapWorkerTask bitmapWorkerTask = new OneDayBitmapWorkerTask(picture, viewHolder.pictureImageView, position, CACHE_NAME);
        mImageListLoader.loadBitmap(picture, null, viewHolder.pictureImageView, bitmapWorkerTask, CACHE_NAME);

        return convertView;
    }

    private static class ViewHolder {
        public ImageView pictureImageView;
        public TextView timeTextView;
        public ImageView videoIconImageView;
    }
}
