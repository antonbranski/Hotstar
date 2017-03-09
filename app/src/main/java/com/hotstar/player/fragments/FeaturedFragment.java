package com.hotstar.player.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.hotstar.player.R;
import com.hotstar.player.adapter.ImageListViewAdapter;
import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.adplayer.core.VideoItem;
import com.hotstar.player.adplayer.player.PlayerActivity;
import com.hotstar.player.events.BusProvider;
import com.hotstar.player.events.ConnectorErrEvent;
import com.hotstar.player.events.GoToDetailFragmentEvent;
import com.hotstar.player.events.LoadedFeaturedVideoItemsEvent;
import com.hotstar.player.events.LoadedHotNowVideoItemsEvent;
import com.hotstar.player.events.LoadedShowsAllShowsVideoItemsEvent;
import com.hotstar.player.events.LoadedShowsChannelVideoItemsEvent;
import com.hotstar.player.model.TransitionDetailFragmentModel;
import com.hotstar.player.model.VideoItemsModel;
import com.hotstar.player.webservice.ServiceConnector;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

public class FeaturedFragment extends Fragment {
    private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + FeaturedFragment.class.getSimpleName();

    private Activity mActivity = null;
    private SliderLayout featuredSection = null;

    private View parentView = null;
    private ListView listView = null;
    private ImageListViewAdapter listViewAdapter = null;

    private ArrayList<VideoItem> mFeaturedContentList = null;

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
        parentView = inflater.inflate(R.layout.fragment_featured, container, false);

        featuredSection = (SliderLayout)parentView.findViewById(R.id.featured_main_slider);
        // loadFeaturedItems(parentView);
        return parentView;
    }

    protected void loadFeaturedItems(View parentView) {
        String testUrl = getResources().getString(R.string.usecase1Url);
        ServiceConnector serviceConnector = new ServiceConnector(mActivity);
        serviceConnector.doRequest(testUrl, new ServiceConnector.ResponseListener() {
            @Override
            public void onComplete(ArrayList<VideoItem> vodContentList, ArrayList<VideoItem> liveContentList) {
                ArrayList<VideoItem> contentList = new ArrayList<VideoItem>();
                contentList.addAll(vodContentList);
                contentList.addAll(liveContentList);

                VideoItemsModel videoModel = new VideoItemsModel(contentList);
                BusProvider.get().post(new LoadedFeaturedVideoItemsEvent(videoModel));
            }

            @Override
            public void onErrorOccurred(String errMessage) {
                // BusProvider.get().post(new ConnectorErrEvent(errMessage));
            }
        });
    }

    @Subscribe
    public void onLoadedFeaturedVideoItemsEvent(LoadedFeaturedVideoItemsEvent event) {
        mFeaturedContentList = event.getVideoItems();
        for (int i=0; i < mFeaturedContentList.size(); i++) {
            VideoItem videoItem = mFeaturedContentList.get(i);

            TextSliderView defaultSliderView = new TextSliderView(mActivity.getBaseContext());
            defaultSliderView.description(videoItem.getTitle())
                    .image(videoItem.getThumbnail().getSmallThumbnailUrl())
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(featuredClickListener);

            //add your extra information
            defaultSliderView.bundle(new Bundle());
            defaultSliderView.getBundle().putInt("content_index", i);

            featuredSection.addSlider(defaultSliderView);
            featuredSection.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);
            featuredSection.setDuration(30000);
        }
    }

    private BaseSliderView.OnSliderClickListener featuredClickListener = new BaseSliderView.OnSliderClickListener() {
        @Override
        public void onSliderClick(BaseSliderView baseSliderView) {
            if (mFeaturedContentList == null)
                return;

            int index = baseSliderView.getBundle().getInt("content_index");

            // start PlayerActivity to play content
            Intent intent = new Intent(mActivity, PlayerActivity.class);
            // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("CONTENT_INFO", mFeaturedContentList.get(index));
            startActivity(intent);
        }
    };

    private final AdapterView.OnItemClickListener listViewItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            AdVideoApplication.logger.e(LOG_TAG+"#onItemClickListener", "Selected Position = " + position);
            VideoItem videoItem = (VideoItem) listViewAdapter.getItem(position);

            TransitionDetailFragmentModel transitionModel = new TransitionDetailFragmentModel(videoItem);
            GoToDetailFragmentEvent transitionEvent = new GoToDetailFragmentEvent(transitionModel);
            BusProvider.get().post(transitionEvent);
        }
    };

}