package com.toda.happyday.presenters;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import com.toda.happyday.async.AsyncPostExecute;
import com.toda.happyday.R;
import com.toda.happyday.models.Picture;
import com.toda.happyday.models.PictureGroup;
import com.toda.happyday.models.db.DailyInfoDbHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fpgeek on 2014. 2. 13..
 */
public class PictureGroupPresenter {

    private Context mContext;
    private DailyInfoDbHelper mDbHelper;
    private List<Picture> mPictureList;
    private PictureGroupsLoadListener mPictureGroupsLoadListener = null;

    private long mLastLoadedDateValue = new Date().getTime();
    private boolean mIsDuplicateLoad = false;

    private final static long TAKEN_DATE_DIFF_MS = 1000 * 60 * 60; // 사진이 묶이는 시간 차이 - 1시간
    private final static int PICTURE_LOAD_COUNT = 200;

    public PictureGroupPresenter(Context context) {
        mContext = context;
        mDbHelper = new DailyInfoDbHelper(mContext);
    }

    public long getLastLoadedDateValue() {
        return mLastLoadedDateValue;
    }

    private AsyncPostExecute<List<Picture>> mOnPostGetPictureList = new AsyncPostExecute<List<Picture>>() {
        @Override
        public void onPostExecute(List<Picture> pictureList) {
            mPictureList = pictureList;
            Collections.sort(mPictureList, new PictureCompare());
            PictureGroup.all(mDbHelper, mOnPostGetPictureGroupList);
        }
    };

    private static class PictureCompare implements Comparator<Picture> {

        @Override
        public int compare(Picture picture, Picture picture2) {
            return picture2.getDate().compareTo(picture.getDate());
        }
    };

    private static class PictureGroupCompare implements Comparator<PictureGroup> {

        @Override
        public int compare(PictureGroup pictureGroup, PictureGroup pictureGroup2) {
            return pictureGroup2.getMainPicture().getDate().compareTo(pictureGroup.getMainPicture().getDate());
        }
    };

    private AsyncPostExecute<List<PictureGroup>> mOnPostGetPictureGroupList = new AsyncPostExecute<List<PictureGroup>>() {
        @Override
        public void onPostExecute(List<PictureGroup> allPictureGroupList) {

            List<PictureGroup> pictureGroupList = createGroupListByTime(allPictureGroupList);
            if (!pictureGroupList.isEmpty()) {

                if (pictureGroupList.size() == 1 && mPictureList.size() == PICTURE_LOAD_COUNT) { // 하나의 그룹이 존재하고 한번에 가져올 수 있는 이미지 개수와 동일한 특수한 경우 처리
                    if (!mIsDuplicateLoad) {
                        mIsDuplicateLoad = true;
                    } else {
                        if (mPictureGroupsLoadListener != null) {
                            mPictureGroupsLoadListener.onLoad(pictureGroupList, true);
                        }
                        return;
                    }
                }

                PictureGroup lastPictureGroup = pictureGroupList.get(pictureGroupList.size() - 1);
                mLastLoadedDateValue = lastPictureGroup.get(0).getDate().getTime();
                pictureGroupList.remove(lastPictureGroup);
            }

            if (mPictureGroupsLoadListener != null) {
                boolean isLoadComplete = !mIsDuplicateLoad && pictureGroupList.isEmpty();
                mPictureGroupsLoadListener.onLoad(pictureGroupList, isLoadComplete);
            }
        }

        private List<PictureGroup> createGroupListByTime(final List<PictureGroup> allPictureGroupList) {
            Map<Long, PictureGroup> allPictureGroupHashMap = pictureGroupListToMap(allPictureGroupList);
            List<List<Picture>> pictureGroupListGroupByTimes = pictureListToListGroupByTime(mPictureList);

            SharedPreferences sharedPreferences = mContext.getSharedPreferences(mContext.getString(R.string.preference_picture_info_key), Context.MODE_PRIVATE);
            SQLiteDatabase writableDb = mDbHelper.getWritableDatabase();

            List<PictureGroup> currentPictureGroupList = new ArrayList<PictureGroup>();
            for (List<Picture> picturesGroupByTime : pictureGroupListGroupByTimes) {
                PictureGroup pictureGroup = getOrCreatePictureGroup(allPictureGroupHashMap, sharedPreferences, writableDb, picturesGroupByTime);
                if (pictureGroup != null) {
                    pictureGroup.addAll(picturesGroupByTime);
                    currentPictureGroupList.add(pictureGroup);
                } else {
                    assert false;
                }
            }

            if (writableDb != null) {
                writableDb.close();
            }

            insertPictureGroupToPictureInfo(sharedPreferences, currentPictureGroupList);
            removeEmptyPictureGroups(currentPictureGroupList);

            Collections.sort(currentPictureGroupList, new PictureGroupCompare());

            return currentPictureGroupList;
        }

        private PictureGroup getOrCreatePictureGroup(Map<Long, PictureGroup> allPictureGroupHashMap, SharedPreferences sharedPreferences, SQLiteDatabase writableDb, List<Picture> picturesGroupByTime) {
            final long pictureGroupId = getPictureGroupId(sharedPreferences, picturesGroupByTime);
            if (pictureGroupId == -1) {
                return PictureGroup.create(writableDb);
            } else {
                return allPictureGroupHashMap.get(pictureGroupId);
            }
        }
    };

    private void insertPictureGroupToPictureInfo(SharedPreferences sharedPreferences, List<PictureGroup> newCreatedPictureGroups) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (PictureGroup pictureGroup : newCreatedPictureGroups) {
            for (Picture picture : pictureGroup) {
                editor.putLong(String.valueOf(picture.getId()), pictureGroup.getId());
            }
        }
        editor.commit();
    }

    private void removeEmptyPictureGroups(List<PictureGroup> pictureGroupList) {

        List<PictureGroup> emptyPictureGroup = new ArrayList<PictureGroup>();
        for (PictureGroup pictureGroup : pictureGroupList) {
            if (pictureGroup.isEmpty()) {
                emptyPictureGroup.add(pictureGroup);
            } else {
                pictureGroup.selectMainPicture(); // TODO - 밖으로 빼기
            }
        }

        for (PictureGroup pictureGroup : emptyPictureGroup) {
            pictureGroupList.remove(pictureGroup);
//            PictureGroup.remove(mDbHelper, pictureGroup.getId());
        }
    }


    public void loadPictureGroups(long lastLoadTime, PictureGroupsLoadListener pictureGroupsLoadListener) {
        mLastLoadedDateValue = lastLoadTime;
        mPictureGroupsLoadListener = pictureGroupsLoadListener;
        Picture.all(mContext, PICTURE_LOAD_COUNT, mLastLoadedDateValue, mOnPostGetPictureList);
    }

    private List<List<Picture>> pictureListToListGroupByTime(List<Picture> pictureList) {
        List<List<Picture>> pictureGroupList = new ArrayList<List<Picture>>();
        List<Picture> pictureGroup = new ArrayList<Picture>();

        long prevPictTakenTime = 0;
        for (Picture picture : pictureList) {
            if (prevPictTakenTime == 0) {
                pictureGroup.add(picture);
                prevPictTakenTime = picture.getDate().getTime();
                continue;
            }

            final long takenTime = picture.getDate().getTime();
            if ( (prevPictTakenTime - takenTime) <= TAKEN_DATE_DIFF_MS ) {
                pictureGroup.add(picture);
            } else {
                pictureGroupList.add(pictureGroup);
                pictureGroup = new ArrayList<Picture>();
                pictureGroup.add(picture);
            }

            prevPictTakenTime = takenTime;
        }

        if (!pictureGroupList.contains(pictureGroup)) {
            pictureGroupList.add(pictureGroup);
        }
        return pictureGroupList;
    }

    private Map<Long, PictureGroup> pictureGroupListToMap(List<PictureGroup> pictureGroupList) {
        Map<Long, PictureGroup> hashMap = new HashMap<Long, PictureGroup>(pictureGroupList.size());
        for (PictureGroup pictureGroup : pictureGroupList) {
            hashMap.put(pictureGroup.getId(), pictureGroup);
        }
        return hashMap;
    }

    private long getPictureGroupId(SharedPreferences sharedPreferences, List<Picture> pictureList) {
        for (Picture picture : pictureList) {
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//                editor.remove(String.valueOf(pictureInfo.getId()));
//                editor.commit();
            long value = sharedPreferences.getLong(String.valueOf(picture.getId()), -1);
            if (value > -1) {
                return value;
            }
        }
        return -1;
    }
}
