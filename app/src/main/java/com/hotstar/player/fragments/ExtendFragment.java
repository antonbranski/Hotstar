package com.hotstar.player.fragments;

import android.os.Bundle;
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
import com.hotstar.player.events.BackToBaseFragmentEvent;
import com.hotstar.player.events.BusProvider;
import com.hotstar.player.model.TransitionBaseFragmentModel;
import com.hotstar.player.model.TransitionExtendFragmentModel;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class ExtendFragment extends BaseFragment {
    private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + ExtendFragment.class.getSimpleName();

    public final static int NON_FRAGMENT = 0;
    public final static int SHOWS_FRAGMENT = 1;
    public final static int MOVIES_FRAGMENT = 2;

    public final static int SHOWS_CHANNEL_FRAGMENT = 101;
    public final static int SHOWS_ALLSHOWS_FRAGMENT = 102;
    public final static int SHOWS_COLLECTIONS_FRAGMENT = 103;

    public final static int MOVIES_GENRES_FRAGMENT = 201;
    public final static int MOVIES_BLOCKBUSTER_FRAGMENT = 202;
    public final static int MOVIES_ALLMOVIES_FRAGMENT = 203;

    private int currentFragmentType = NON_FRAGMENT;
    private int currentSubFragmentType = NON_FRAGMENT;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        AdVideoApplication.logger.i(LOG_TAG + "#onCreateView", "onCreateView is called");

        View v = inflater.inflate(R.layout.fragment_extend, container, false);
        final View actionBarView = v.findViewById(R.id.extend_custom_actionbar);
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

        showMenuButton(LEFT_BTN_BACK);
        configureView(v);
        return v;
    }

    /**
     * set view type
     *
     * @param transitionModel
     */
    public void setFragmentType(TransitionExtendFragmentModel transitionModel) {
        currentFragmentType = transitionModel.getTargetFragmentType();
        currentSubFragmentType = transitionModel.getTargetFragmentSubType();
    }

    /**
     * configure View
     *
     * @param parentView
     */
    private void configureView(View parentView) {
        if (currentFragmentType == SHOWS_FRAGMENT) {
            configureShowsView(parentView);
        }
        else if (currentFragmentType == MOVIES_FRAGMENT) {
            configureMoviesView(parentView);
        }

    }

    /**
     * configure view with shows
     *
     * @param parentView
     */
    private void configureShowsView(View parentView) {
        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                mActivity.getSupportFragmentManager(), FragmentPagerItems.with(mActivity.getBaseContext())
                .add(R.string.shows_channels_title, ShowsChannelFragment.class)
                .add(R.string.shows_allshows_title, ShowsAllShowsFragment.class)
                .add(R.string.shows_collections_title, ShowsCollectionsFragment.class)
                .create());

        viewPager = (ViewPager) parentView.findViewById(R.id.extend_viewpager);
        viewPager.setAdapter(adapter);

        viewPagerTab = (SmartTabLayout) parentView.findViewById(R.id.extend_viewpagertab);
        viewPagerTab.setViewPager(viewPager);
        setTitle("Shows");
    }

    /**
     * configure view with movies
     *
     * @param parentView
     */
    private void configureMoviesView(View parentView) {
        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                mActivity.getSupportFragmentManager(), FragmentPagerItems.with(mActivity.getBaseContext())
                .add(R.string.movies_genres_title, MoviesGenresFragment.class)
                .add(R.string.movies_blockbuster_title, MoviesBlockBusterFragment.class)
                .add(R.string.movies_allmovies_title, MoviesAllMoviesFragment.class)
                .create());

        viewPager = (ViewPager) parentView.findViewById(R.id.extend_viewpager);
        viewPager.setAdapter(adapter);

        viewPagerTab = (SmartTabLayout) parentView.findViewById(R.id.extend_viewpagertab);
        viewPagerTab.setViewPager(viewPager);
        setTitle("Movies");
    }

    @Override
    public void onClickButtonBack() {
        // TransitionBaseFragmentModel baseFragmentModel = new TransitionBaseFragmentModel(BaseFragment.HOT_NOW_FRAGMENT);
        TransitionBaseFragmentModel baseFragmentModel = new TransitionBaseFragmentModel(BaseFragment.FEATURED_FRAGMENT);
        BackToBaseFragmentEvent event = new BackToBaseFragmentEvent(baseFragmentModel);
        BusProvider.get().post(event);
    }
}