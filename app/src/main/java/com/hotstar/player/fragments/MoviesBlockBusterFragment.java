package com.hotstar.player.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.hotstar.player.R;
import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.adplayer.core.VideoItem;
import com.hotstar.player.events.BusProvider;
import com.hotstar.player.events.ConnectorErrEvent;
import com.hotstar.player.events.GoToDetailFragmentEvent;
import com.hotstar.player.events.LoadedMoviesAllMoviesVideoItemsEvent;
import com.hotstar.player.events.LoadedMoviesBlockBusterVideoItemsEvent;
import com.hotstar.player.model.TransitionDetailFragmentModel;
import com.hotstar.player.model.VideoItemsModel;
import com.hotstar.player.webservice.ServiceConnector;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

public class MoviesBlockBusterFragment extends Fragment {
    private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + MoviesBlockBusterFragment.class.getSimpleName();

    private Activity mActivity = null;
    private View parentView = null;

    private ArrayList<VideoItem> mBlockBusterContentList = null;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        BusProvider.get().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
        BusProvider.get().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.fragment_movies_blockbuster, container, false);

        loadBlockBusterItems(parentView);
        return parentView;
    }

    protected void loadBlockBusterItems(View parentView) {
        String testUrl = getResources().getString(R.string.contentUrl);
        ServiceConnector serviceConnector = new ServiceConnector(mActivity);
        serviceConnector.doRequest(testUrl, new ServiceConnector.ResponseListener() {
            @Override
            public void onComplete(ArrayList<VideoItem> vodContentList, ArrayList<VideoItem> liveContentList) {
                ArrayList<VideoItem> contentList = new ArrayList<VideoItem>();
                contentList.addAll(vodContentList);
                contentList.addAll(liveContentList);

                VideoItemsModel videoModel = new VideoItemsModel(contentList);
                BusProvider.get().post(new LoadedMoviesBlockBusterVideoItemsEvent(videoModel));
            }

            @Override
            public void onErrorOccurred(String errMessage) {
                // BusProvider.get().post(new ConnectorErrEvent(errMessage));
            }
        });
    }

    @Subscribe
    public void onLoadedMoviesBlockBusterVideoItemsEvent(LoadedMoviesBlockBusterVideoItemsEvent event) {
        SliderLayout blockBusterSection = (SliderLayout)parentView.findViewById(R.id.movies_blockbuster_slider);
        mBlockBusterContentList = event.getVideoItems();

        for (int i=0; i < mBlockBusterContentList.size(); i++) {
            VideoItem videoItem = mBlockBusterContentList.get(i);

            DefaultSliderView defaultSliderView = new DefaultSliderView(mActivity.getBaseContext());
            defaultSliderView.description(videoItem.getTitle())
                    .image(videoItem.getThumbnail().getSmallThumbnailUrl())
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(sliderClickListener);

            //add your extra information
            defaultSliderView.bundle(new Bundle());
            defaultSliderView.getBundle().putInt("content_index", i);

            blockBusterSection.addSlider(defaultSliderView);
            blockBusterSection.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);
            blockBusterSection.setDuration(30000);
        }
    }

    private final BaseSliderView.OnSliderClickListener sliderClickListener = new BaseSliderView.OnSliderClickListener() {
        @Override
        public void onSliderClick(BaseSliderView baseSliderView) {
            if (mBlockBusterContentList == null)
                return;

            // goto detail fragment
            int index = baseSliderView.getBundle().getInt("content_index");
            VideoItem videoItem = mBlockBusterContentList.get(index);

            TransitionDetailFragmentModel transitionModel = new TransitionDetailFragmentModel(videoItem);
            GoToDetailFragmentEvent transitionEvent = new GoToDetailFragmentEvent(transitionModel);
            BusProvider.get().post(transitionEvent);
        }
    };
}