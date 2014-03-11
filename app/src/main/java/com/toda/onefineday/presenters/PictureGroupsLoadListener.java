package com.toda.onefineday.presenters;

import com.toda.onefineday.models.PictureGroup;

import java.util.List;

/**
 * Created by fpgeek on 2014. 3. 5..
 */
public interface PictureGroupsLoadListener {

    void onLoad(List<PictureGroup> loadedPictureGroups, boolean isLoadComplete);
}
