package com.toda.happyday.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.network.NetworkUtil;
import com.path.android.jobqueue.network.NetworkUtilImpl;
import com.toda.happyday.R;
import com.toda.happyday.async.AsyncPostExecute;
import com.toda.happyday.async.BitmapWorkerTask;
import com.toda.happyday.async.PictureGroupBitmapWorkerTask;
import com.toda.happyday.job.LocationJob;
import com.toda.happyday.models.PictureGroup;
import com.toda.happyday.models.Picture;
import com.toda.happyday.utils.TextViewUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by fpgeek on 2014. 1. 25..
 */
public class PictureGroupAdapter extends ArrayAdapter<PictureGroup> {

    private static final String CACHE_NAME = "All";

    private Activity mActivity;
    private List<PictureGroup> mPictureGroups;
    private AsyncPostExecute<Integer> mLastViewCallback;

    private int windowWidth = 0;
    private int windowHeight = 0;

    private static ImageListLoader mImageListLoader;
    private static Bitmap mLoadingBitmap;

    private JobManager mJobManager;
    private Set<Long> mLocationJobPictureIdSet;

    private static NetworkUtil mNetworkUtil;

    private int mPrevLastPosition = 0;

    public PictureGroupAdapter(Activity activity, List<PictureGroup> pictureGroups, ImageListLoader imageListLoader, AsyncPostExecute<Integer> lastViewCallback) {
        super(activity, R.layout.picture_group_item, pictureGroups);
        this.mActivity = activity;

        this.mPictureGroups = pictureGroups;
        this.mLastViewCallback = lastViewCallback;

        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);

        windowWidth = metrics.widthPixels;
        windowHeight = metrics.heightPixels;

        mImageListLoader = imageListLoader;
        mLoadingBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.loading);

        mNetworkUtil = new NetworkUtilImpl(getContext());

        mJobManager = new JobManager(getContext());
        mLocationJobPictureIdSet = new HashSet<Long>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = mActivity.getLayoutInflater().inflate(R.layout.picture_group_item, null);

            viewHolder = new ViewHolder();
            viewHolder.dayTextView = (TextView)convertView.findViewById(R.id.day_text);
            viewHolder.monthTextView = (TextView)convertView.findViewById(R.id.month_text);
            viewHolder.pictureImageView = (ImageView)convertView.findViewById(R.id.picture);
            viewHolder.stickerImageView = (ImageView)convertView.findViewById(R.id.sticker_thumb);
            viewHolder.dairyTextView = (TextView)convertView.findViewById(R.id.dairy_text);
            viewHolder.dateTextView = (TextView)convertView.findViewById(R.id.date_text);
            viewHolder.locationTextView = (TextView)convertView.findViewById(R.id.location_text);
            viewHolder.locationAreaLayout = (ViewGroup)convertView.findViewById(R.id.location_area);
            viewHolder.position = position;
            viewHolder.videoIconImageView = (ImageView)convertView.findViewById(R.id.video_icon);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        PictureGroup pictureGroup = mPictureGroups.get(position);
        Picture picture = pictureGroup.getMainPicture();

        TextViewUtil.setText(viewHolder.dayTextView, picture.getDayText());
        TextViewUtil.setText(viewHolder.monthTextView, picture.getMonthText());
        viewHolder.stickerImageView.setImageResource(pictureGroup.getSticker());
        TextViewUtil.setText(viewHolder.dairyTextView, pictureGroup.getDairyText());
        TextViewUtil.setText(viewHolder.dateTextView, picture.getDateText());
        TextViewUtil.setText(viewHolder.locationTextView, picture.getLocation());
        LocationTextClickListener locationTextClickListener = new LocationTextClickListener(mPictureGroups.get(position).getMainPicture());
        viewHolder.locationTextView.setOnClickListener(locationTextClickListener);
        viewHolder.locationAreaLayout.setOnClickListener(locationTextClickListener);

        if (picture.getType() == Picture.TYPE_IMAGE) {
            viewHolder.videoIconImageView.setVisibility(View.GONE);
        } else {
            viewHolder.videoIconImageView.setVisibility(View.VISIBLE);
        }

        final int imageViewWidth = windowWidth / 2;

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

        BitmapWorkerTask bitmapWorkerTask = new PictureGroupBitmapWorkerTask(mActivity.getContentResolver(), picture, viewHolder.pictureImageView, position, CACHE_NAME);
        mImageListLoader.loadBitmap(picture, null, viewHolder.pictureImageView, bitmapWorkerTask, CACHE_NAME);

        if (mNetworkUtil.isConnected(getContext()) && picture.getLocation() == null && picture.hasValidLocationInfo()) {
            mJobManager.addJobInBackground(new LocationJob(getContext(), picture, mLocationJobPictureIdSet, new LocationPostListener()));
        }

        if (isLastView(position)) {
            mLastViewCallback.onPostExecute(position);
        }

        return convertView;
    }

    private boolean isLastView(int position) {
        final boolean isLastPosition = (position + 1) == mPictureGroups.size();
        boolean isLastView = isLastPosition && (mPrevLastPosition != position);
        if (isLastPosition) {
            mPrevLastPosition = position;
        }
        return isLastView;
    }

    private class LocationTextClickListener implements View.OnClickListener {

        private Picture mPicture;

        public LocationTextClickListener(Picture picture) {
            mPicture = picture;
        }

        @Override
        public void onClick(View view) {
            if (mPicture.hasValidLocationInfo() && mPicture.getLocation() != null) {
                final String geo =  "geo:" + mPicture.getLatitude() + "," + mPicture.getLongitude();
                Log.i("MAP", "geo : " + geo);
                Intent intent =new Intent(Intent.ACTION_VIEW, Uri.parse(geo));
                getContext().startActivity(intent);
            }
        }
    };

    private class LocationPostListener implements AsyncPostExecute<String> {

        public LocationPostListener() {
        }

        @Override
        public void onPostExecute(String location) {
            if (location != null) {
//                notifyDataSetChanged();
            }
        }
    }

    private static class ViewHolder {
        public TextView dayTextView;
        public TextView monthTextView;
        public ImageView pictureImageView;
        public ImageView stickerImageView;
        public TextView dairyTextView;
        public int position;
        public TextView dateTextView;
        public TextView locationTextView;
        public ViewGroup locationAreaLayout;
        public ImageView videoIconImageView;
    }
}
