package com.hotstar.player.fragments;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.hotstar.player.HotStarApplication;
import com.hotstar.player.R;
import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.adplayer.core.VideoItem;
import com.hotstar.player.adplayer.player.PlayerActivity;
import com.hotstar.player.custom.SamsungPhoneInfo;
import com.hotstar.player.events.BusProvider;
import com.hotstar.player.events.ConnectorErrEvent;
import com.hotstar.player.events.GoToExtendFragmentEvent;
import com.hotstar.player.events.LoadedFeaturedVideoItemsEvent;
import com.hotstar.player.events.LoadedShowsVideoItemsEvent;
import com.hotstar.player.model.HotStarUserInfo;
import com.hotstar.player.model.TransitionExtendFragmentModel;
import com.hotstar.player.model.VideoItemsModel;
import com.hotstar.player.webservice.ConnectorUrlRequest;
import com.hotstar.player.webservice.ServiceConnector;
import com.squareup.otto.Subscribe;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

import mehdi.sakout.fancybuttons.FancyButton;

public class ShowsFragment extends Fragment {
    private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + ShowsFragment.class.getSimpleName();

    private Activity mActivity = null;
    private SliderLayout showsSection = null;
    private SliderLayout hindiSection = null;

    private Button browseShowsButton = null;
    private TextView browseShowsTextView = null;
    private Button browseMainButton = null;
    private TextView browseMainTextView = null;

    private View parentView = null;

    private ArrayList<VideoItem> mShowsContentList = null;

    @Override
    public void onAttach(Activity activity) {
        AdVideoApplication.logger.i(LOG_TAG + "#onAttach", "onAttach is called");
        super.onAttach(activity);

        mActivity = activity;
        BusProvider.get().register(this);
    }

    @Override
    public void onDetach() {
        AdVideoApplication.logger.i(LOG_TAG + "#onDetach", "onDetach is called");
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
        AdVideoApplication.logger.i(LOG_TAG + "#onCreateView", "onCreateView is called");

        parentView = inflater.inflate(R.layout.fragment_shows, container, false);

        // create content list
        mShowsContentList = new ArrayList<>();

        // load hotnow section
        showsSection = (SliderLayout)parentView.findViewById(R.id.shows_main_slider);
        showsSection.addOnPageChangeListener(mainShowsPageChangeListener);
        loadShowsSection(parentView);

        browseMainButton = (Button) parentView.findViewById(R.id.shows_main_browse_button);
        browseMainButton.setOnClickListener(browseMainButtonClickListener);
        browseMainTextView = (TextView) parentView.findViewById(R.id.shows_main_browse_textview);

        // load hindi section
        // hindiSection = (SliderLayout)v.findViewById(R.id.shows_hindi_slider);
        // loadHindiSection(hindiSection);
        //
        // browseShowsButton = (Button) parentView.findViewById(R.id.shows_hindi_browse_button);
        // browseShowsButton.setOnClickListener(browseShowsClickListener);
        // browseShowsTextView = (TextView) parentView.findViewById(R.id.shows_hindi_browse_textview);
        // browseShowsTextView.setText("Browse Shows");

        return parentView;
    }

    protected void loadShowsSection(View parentView) {
        String usecase1URL = getResources().getString(R.string.usecase1Url);
        ServiceConnector usecase1Connector = new ServiceConnector(mActivity);
        usecase1Connector.doRequest(usecase1URL, responseListener);

        String usecase4URL = getResources().getString(R.string.usecase4Url);
        ServiceConnector usecase4Connector = new ServiceConnector(mActivity);
        usecase4Connector.doRequest(usecase4URL, responseListener);

        String usecase5URL = getResources().getString(R.string.usecase5Url);
        ServiceConnector usecase5Connector = new ServiceConnector(mActivity);
        usecase5Connector.doRequest(usecase5URL, responseListener);

        String usecase6URL = getResources().getString(R.string.usecase6Url);
        ServiceConnector usecase6Connector = new ServiceConnector(mActivity);
        usecase6Connector.doRequest(
                ConnectorUrlRequest.buildURL(usecase6URL, HotStarApplication.getInstance().getLocation(), SamsungPhoneInfo.getInstance().modelString()),
                responseListener);

        String usecase7URL = getResources().getString(R.string.usecase7Url);
        ServiceConnector usecase7Connector = new ServiceConnector(mActivity);
        usecase7Connector.doRequest(
                ConnectorUrlRequest.buildURL(usecase7URL, HotStarApplication.getInstance().getLocation(), SamsungPhoneInfo.getInstance().modelString()),
                responseListener);

        String usecase10URL = getResources().getString(R.string.usecase10Url);
        ServiceConnector usecase10Connector = new ServiceConnector(mActivity);
        usecase10Connector.doRequest(
                ConnectorUrlRequest.buildURL(usecase10URL, HotStarApplication.getInstance().getLocation(), SamsungPhoneInfo.getInstance().modelString()),
                responseListener);

        String usecase11URL = getResources().getString(R.string.usecase11Url);
        ServiceConnector usecase11Connector = new ServiceConnector(mActivity);
        usecase11Connector.doRequest(
                ConnectorUrlRequest.buildURL(usecase11URL, HotStarApplication.getInstance().getLocation(), SamsungPhoneInfo.getInstance().modelString()),
                responseListener);

    }

    protected void loadHindiSection(SliderLayout sliderLayout) {
        HashMap<String,String> url_maps = new HashMap<String, String>();
        url_maps.put("Hannibal", "http://static2.hypable.com/wp-content/uploads/2013/12/hannibal-season-2-release-date.jpg");
        url_maps.put("Big Bang Theory", "http://tvfiles.alphacoders.com/100/hdclearart-10.png");
        url_maps.put("House of Cards", "http://cdn3.nflximg.net/images/3093/2043093.jpg");
        url_maps.put("Game of Thrones", "http://images.boomsbeat.com/data/images/full/19640/game-of-thrones-season-4-jpg.jpg");

        for(String name : url_maps.keySet())
        {
            DefaultSliderView defaultSliderView = new DefaultSliderView(mActivity.getBaseContext());
            defaultSliderView.description(name)
                    .image(url_maps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(hindiClickListener);

            //add your extra information
            defaultSliderView.bundle(new Bundle());
            defaultSliderView.getBundle().putString("extra", name);

            sliderLayout.addSlider(defaultSliderView);
            sliderLayout.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);
            sliderLayout.setDuration(4000);
        }
    }

    /**
     * BusProvider event handle function
     *
     * @param event
     */
    @Subscribe
    public void onLoadedShowsVideoItemsEvent(LoadedShowsVideoItemsEvent event) {
        AdVideoApplication.logger.i(LOG_TAG + "#onLoadedShowsVideoItemsEvent",
                "Original Content = " + mShowsContentList.size() + " Added Content = " + event.getVideoItems().size());

        ArrayList<VideoItem> newVideoList = event.getVideoItems();
        int originalSize = mShowsContentList.size();

        mShowsContentList.addAll(newVideoList);
        for (int i=0; i < newVideoList.size(); i++) {
            VideoItem videoItem = newVideoList.get(i);

            DefaultSliderView defaultSliderView = new DefaultSliderView(mActivity.getBaseContext());
            defaultSliderView.description(videoItem.getTitle())
                    .image(videoItem.getThumbnail().getLargeThumbnailUrl())
                    .setScaleType(BaseSliderView.ScaleType.CenterInside)
                    .setOnSliderClickListener(showsClickListener);

            //add your extra information
            defaultSliderView.bundle(new Bundle());
            defaultSliderView.getBundle().putInt("content_index", originalSize + i);

            showsSection.addSlider(defaultSliderView);
            showsSection.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);
            showsSection.setDuration(30000);
        }
    }

    /**
     *
     * Response Listener class variable
     *
     */
    private ServiceConnector.ResponseListener responseListener =  new ServiceConnector.ResponseListener() {
        @Override
        public void onComplete(ArrayList<VideoItem> vodContentList, ArrayList<VideoItem> liveContentList) {
            ArrayList<VideoItem> contentList = new ArrayList<VideoItem>();
            contentList.addAll(vodContentList);
            contentList.addAll(liveContentList);

            VideoItemsModel videoModel = new VideoItemsModel(contentList);
            BusProvider.get().post(new LoadedShowsVideoItemsEvent(videoModel));
        }

        @Override
        public void onErrorOccurred(String errMessage) {
            // BusProvider.get().post(new ConnectorErrEvent(errMessage));
        }
    };

    /**
     *
     * Click Listener class variables
     *
     */
    private BaseSliderView.OnSliderClickListener hindiClickListener = new BaseSliderView.OnSliderClickListener() {
        @Override
        public void onSliderClick(BaseSliderView baseSliderView) {

        }
    };

    private final View.OnClickListener browseMainButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

        }
    };

    private final View.OnClickListener browseShowsClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            /*
            TransitionExtendFragmentModel transitionModel = new TransitionExtendFragmentModel(ExtendFragment.SHOWS_FRAGMENT, ExtendFragment.SHOWS_ALLSHOWS_FRAGMENT);
            GoToExtendFragmentEvent transitionEvent = new GoToExtendFragmentEvent(transitionModel);
            BusProvider.get().post(transitionEvent);
            */
        }
    };

    private final BaseSliderView.OnSliderClickListener showsClickListener = new BaseSliderView.OnSliderClickListener() {
        @Override
        public void onSliderClick(BaseSliderView baseSliderView) {
            if (mShowsContentList == null)
                return;

            int index = baseSliderView.getBundle().getInt("content_index");
            if (index >= mShowsContentList.size())
                return;

            // start PlayerActivity to play content
            Intent intent = new Intent(mActivity, PlayerActivity.class);
            // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("CONTENT_INFO", mShowsContentList.get(index));
            startActivity(intent);
        }
    };

    private final ViewPagerEx.OnPageChangeListener mainShowsPageChangeListener = new ViewPagerEx.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            if (mShowsContentList == null) {
                browseMainTextView.setText("");
                return;
            }

            int index = showsSection.getCurrentSlider().getBundle().getInt("content_index");
            if (index < mShowsContentList.size()) {
                browseMainTextView.setText(mShowsContentList.get(index).getTitle());
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };
}