package com.toda.happyday.presenters;

import com.toda.happyday.models.PictureGroup;

import java.util.List;

/**
 * Created by fpgeek on 2014. 3. 5..
 */
public interface PictureGroupsLoadListener {

    void onLoad(List<PictureGroup> loadedPictureGroups, boolean isLoadComplete);
}
