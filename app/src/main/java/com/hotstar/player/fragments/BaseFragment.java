package com.hotstar.player.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hotstar.player.HomeActivity;
import com.hotstar.player.R;
import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.custom.CustomButtonTouchListener;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.todddavies.components.progressbar.ProgressWheel;

public class BaseFragment extends Fragment {
    private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + BaseFragment.class.getSimpleName();

    public final static int BASE_FRAGMENT = 0;
    /*
    public final static int HOT_NOW_FRAGMENT = 1;
    public final static int MY_PLAYLIST_FRAGMENT = 2;
    public final static int DOWNLOAD_FRAGMENT = 3;
    public final static int SHOWS_FRAGMENT = 4;
    public final static int MOVIES_FRAGMENT = 5;
    public final static int SPORTS_FRAGMENT = 6;
    public final static int COLLECTION_FRAGMENT = 7;
    public final static int INTERNATIONAL_FRAGMENT = 8;

    protected int currentFragmentStatus = HOT_NOW_FRAGMENT;
    */

    public final static int FEATURED_FRAGMENT = 1;
    public final static int TVSHOWS_FRAGMENT = 2;
    public final static int MOVIES_FRAGMENT = 3;

    protected int currentFragmentStatus = FEATURED_FRAGMENT;

    public static final int LEFT_BTN_BACK = 1;
    public static final int LEFT_BTN_MENU = 2;
    static final int LEFT_BTN_NONE = 0;

    protected TextView actionBarTitleTV = null;
    protected ProgressWheel actionbar_pw_spinner;
    protected TextView actionbar_pw_mins;
    protected ImageView btnBack;
    protected ImageView btnMenu;

    protected HomeActivity mActivity = null;

    protected ViewPager viewPager = null;
    protected SmartTabLayout viewPagerTab = null;

    @Override
    public void onAttach (Activity activity) {
        AdVideoApplication.logger.i(LOG_TAG + "#onAttach", "onAttach is called");
        super.onAttach (activity);
        mActivity = (HomeActivity)activity;
    }

    @Override
    public void onDetach () {
        AdVideoApplication.logger.i(LOG_TAG + "#onDetach", "onDetach is called");
        super.onDetach ();
        mActivity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        AdVideoApplication.logger.i(LOG_TAG + "#onCreateView", "onCreateView is called");

        View v = inflater.inflate(R.layout.fragment_base, container, false);
        final View actionBarView = v.findViewById(R.id.custom_actionbar);
        actionBarTitleTV = (TextView) actionBarView.findViewById(R.id.custom_actionbar_title);

        btnBack = (ImageView) actionBarView.findViewById(R.id.button_back);
        btnBack.setOnTouchListener(CustomButtonTouchListener.getInstance());
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickButtonBack();
            }
        });
        btnBack.setVisibility(View.GONE);

        btnMenu = (ImageView) actionBarView.findViewById(R.id.button_menu);
        btnMenu.setOnTouchListener(CustomButtonTouchListener.getInstance());
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickButtonMenu();
            }
        });
        btnMenu.setVisibility(View.GONE);

        showMenuButton(LEFT_BTN_MENU);
        setTitle("hotstar");

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                mActivity.getSupportFragmentManager(), FragmentPagerItems.with(mActivity.getBaseContext())
                /*
                .add(R.string.hotnow_title, HotNowFragment.class)
                .add(R.string.shows_title, ShowsFragment.class)
                .add(R.string.movies_title, MoviesFragment.class)
                .add(R.string.sports_title, SportsFragment.class)
                .add(R.string.collections_title, CollectionsFragment.class)
                .add(R.string.international_title, InternationalFragment.class)
                */
                /*
                .add(R.string.featured_title, FeaturedFragment.class)
                 */
                .add(R.string.tvshows_title, ShowsFragment.class)
                .add(R.string.movies_title, NewMoviesFragment.class)
                .create());

        viewPager = (ViewPager) v.findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        viewPagerTab = (SmartTabLayout) v.findViewById(R.id.viewpagertab);
        viewPagerTab.setViewPager(viewPager);
        return v;
    }

    public void onClickButtonMenu ()
    {
        if (mActivity != null)
            mActivity.showSlidingMenu();
    }

    public void onClickButtonBack()
    {
    }

    public void setTitle (String title) {
        actionBarTitleTV.setText(title);
    }

    public void setTimeDelay (String delay) {
        actionbar_pw_spinner.resetCount();
        actionbar_pw_spinner.setText(delay);
    }

    public void setTimeDelayVisibility (Boolean visible) {
        if (visible) {
            actionbar_pw_spinner.setVisibility (View.VISIBLE);
            actionbar_pw_mins.setVisibility(View.VISIBLE);
        } else {
            actionbar_pw_spinner.setVisibility (View.GONE);
            actionbar_pw_mins.setVisibility(View.GONE);
        }
    }

    public void showMenuButton (int opt) {
        if (opt == LEFT_BTN_MENU) {
            btnMenu.setVisibility (View.VISIBLE);
            btnBack.setVisibility (View.GONE);
        } else if (opt == LEFT_BTN_BACK) {
            btnMenu.setVisibility (View.GONE);
            btnBack.setVisibility (View.VISIBLE);
        } else {
            btnMenu.setVisibility (View.GONE);
            btnBack.setVisibility (View.GONE);
        }
    }

    public void setFragmentStatus(int number)
    {
        currentFragmentStatus = number;
        viewPager.setCurrentItem(number);
    }
}
