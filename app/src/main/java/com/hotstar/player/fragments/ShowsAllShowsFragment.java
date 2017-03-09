package com.hotstar.player.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.hotstar.player.R;
import com.hotstar.player.adapter.ImageListViewAdapter;
import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.adplayer.core.VideoItem;
import com.hotstar.player.events.BusProvider;
import com.hotstar.player.events.ConnectorErrEvent;
import com.hotstar.player.events.GoToDetailFragmentEvent;
import com.hotstar.player.events.LoadedHotNowVideoItemsEvent;
import com.hotstar.player.events.LoadedShowsAllShowsVideoItemsEvent;
import com.hotstar.player.model.TransitionDetailFragmentModel;
import com.hotstar.player.model.VideoItemsModel;
import com.hotstar.player.webservice.ServiceConnector;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

public class ShowsAllShowsFragment extends Fragment {
    private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + ShowsAllShowsFragment.class.getSimpleName();

    private Activity mActivity = null;

    private View parentView = null;
    private ListView listView = null;
    private ImageListViewAdapter listViewAdapter = null;

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
        parentView = inflater.inflate(R.layout.fragment_shows_allshows, container, false);

        loadAllShowsItems(parentView);
        return parentView;
    }

    protected void loadAllShowsItems(View parentView) {
        String testUrl = getResources().getString(R.string.contentUrl);
        ServiceConnector serviceConnector = new ServiceConnector(mActivity);
        serviceConnector.doRequest(testUrl, new ServiceConnector.ResponseListener() {
            @Override
            public void onComplete(ArrayList<VideoItem> vodContentList, ArrayList<VideoItem> liveContentList) {
                ArrayList<VideoItem> contentList = new ArrayList<VideoItem>();
                contentList.addAll(vodContentList);
                contentList.addAll(liveContentList);

                VideoItemsModel videoModel = new VideoItemsModel(contentList);
                BusProvider.get().post(new LoadedShowsAllShowsVideoItemsEvent(videoModel));
            }

            @Override
            public void onErrorOccurred(String errMessage) {
                // BusProvider.get().post(new ConnectorErrEvent(errMessage));
            }
        });
    }

    @Subscribe
    public void onLoadedShowsAllShowsVideoItemEvent(LoadedShowsAllShowsVideoItemsEvent event) {
        listView = (ListView) parentView.findViewById(R.id.shows_allshows_listview);
        listViewAdapter = new ImageListViewAdapter(mActivity, event.getVideoItems());
        listView.setAdapter(listViewAdapter);
        listView.setOnItemClickListener(listViewItemClickListener);
    }

    private final AdapterView.OnItemClickListener listViewItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            AdVideoApplication.logger.e(LOG_TAG + "#onItemClickListener", "Selected Position = " + position);
            VideoItem videoItem = (VideoItem) listViewAdapter.getItem(position);

            TransitionDetailFragmentModel transitionModel = new TransitionDetailFragmentModel(videoItem);
            GoToDetailFragmentEvent transitionEvent = new GoToDetailFragmentEvent(transitionModel);
            BusProvider.get().post(transitionEvent);
        }
    };
}