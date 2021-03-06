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

import com.hotstar.player.R;
import com.hotstar.player.adapter.ImageListViewAdapter;
import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.adplayer.core.VideoItem;
import com.hotstar.player.events.BusProvider;
import com.hotstar.player.events.ConnectorErrEvent;
import com.hotstar.player.events.GoToDetailFragmentEvent;
import com.hotstar.player.events.LoadedShowsAllShowsVideoItemsEvent;
import com.hotstar.player.events.LoadedShowsChannelVideoItemsEvent;
import com.hotstar.player.events.LoadedShowsCollectionsVideoItemsEvent;
import com.hotstar.player.model.TransitionDetailFragmentModel;
import com.hotstar.player.model.VideoItemsModel;
import com.hotstar.player.webservice.ServiceConnector;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

public class ShowsCollectionsFragment extends Fragment {
    private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + ShowsCollectionsFragment.class.getSimpleName();

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
        parentView = inflater.inflate(R.layout.fragment_shows_collections, container, false);

        loadCollectionsItems(parentView);
        return parentView;
    }

    protected void loadCollectionsItems(View parentView) {
        String testUrl = getResources().getString(R.string.contentUrl);
        ServiceConnector serviceConnector = new ServiceConnector(mActivity);
        serviceConnector.doRequest(testUrl, new ServiceConnector.ResponseListener() {
            @Override
            public void onComplete(ArrayList<VideoItem> vodContentList, ArrayList<VideoItem> liveContentList) {
                ArrayList<VideoItem> contentList = new ArrayList<VideoItem>();
                contentList.addAll(vodContentList);
                contentList.addAll(liveContentList);

                VideoItemsModel videoModel = new VideoItemsModel(contentList);
                BusProvider.get().post(new LoadedShowsCollectionsVideoItemsEvent(videoModel));
            }

            @Override
            public void onErrorOccurred(String errMessage) {
                // BusProvider.get().post(new ConnectorErrEvent(errMessage));
            }
        });
    }

    @Subscribe
    public void onLoadedShowsCollectionsVideoItemEvent(LoadedShowsChannelVideoItemsEvent event) {
        listView = (ListView) parentView.findViewById(R.id.shows_collections_listview);
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