/*
 * Copyright 2016 "Henry Tao <hi@henrytao.me>"
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package common.lib.adapter;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by henrytao on 2/6/16.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    private static String makeTagName(int position) {
        return ViewPagerAdapter.class.getName() + ":" + position;
    }

    private final FragmentManager mFragmentManager;

    private final List<Fragment> mFragments = new ArrayList<>();

    private final Map<Integer, String> mTags = new HashMap<>();

    private final List<CharSequence> mTitles = new ArrayList<>();

    private Bundle mSavedInstanceState = new Bundle();

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
        mFragmentManager = fm;
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    public int getFragmentPosition(Object object) {
        return mFragments.indexOf(object);
    }

    @Override
    public Fragment getItem(int position) {
        String tagName = mSavedInstanceState.getString(makeTagName(position));
        if (!TextUtils.isEmpty(tagName)) {
            Fragment fragment = mFragmentManager.findFragmentByTag(tagName);
            return fragment != null ? fragment : mFragments.get(position);
        }
        return mFragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }

    public void setPageTitle(int position, String title) {
        mTitles.set(position,title);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Object object = super.instantiateItem(container, position);
        mTags.put(position, ((Fragment) object).getTag());
        return object;
    }

    public void addFragment(Fragment fragment, CharSequence title) {
        mFragments.add(fragment);
        mTitles.add(title);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mSavedInstanceState = savedInstanceState != null ? savedInstanceState : new Bundle();
    }

    public void onSaveInstanceState(Bundle outState) {
        Iterator<Map.Entry<Integer, String>> iterator = mTags.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, String> entry = iterator.next();
            outState.putString(makeTagName(entry.getKey()), entry.getValue());
        }
    }
}
