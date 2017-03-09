package com.hotstar.player.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.hotstar.player.R;
import com.hotstar.player.adapter.ImageGridViewAdapter;
import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.adplayer.core.VideoItem;
import com.hotstar.player.adplayer.player.PlayerActivity;
import com.hotstar.player.events.BusProvider;
import com.hotstar.player.events.ConnectorErrEvent;
import com.hotstar.player.events.GoToExtendFragmentEvent;
import com.hotstar.player.events.LoadedHotNowVideoItemsEvent;
import com.hotstar.player.model.TransitionExtendFragmentModel;
import com.hotstar.player.model.VideoItemsModel;
import com.hotstar.player.webservice.ServiceConnector;
import com.jess.ui.TwoWayAdapterView;
import com.jess.ui.TwoWayGridView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;


public class HotNowFragment extends Fragment{
    private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + HotNowFragment.class.getSimpleName();

    private Activity mActivity = null;
    private SliderLayout hotnowSection = null;

    private ImageView buzzingImageView1 = null;
    private ImageView buzzingImageView2 = null;
    private Button browseMovieButton = null;
    private TextView browseMovieTextView = null;

    private ArrayList<VideoItem> mHotnowContentList = null;

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
        View v = inflater.inflate(R.layout.fragment_hotnow, container, false);

        hotnowSection = (SliderLayout)v.findViewById(R.id.hotnow_main_slider);

        // load hotnow section
        loadHotNowSection();

        // load buzzing now section
        loadBuzzingNowSection(v);

        browseMovieButton = (Button) v.findViewById(R.id.hotnow_main_browse_button);
        browseMovieButton.setOnClickListener(browseMovieButtonClickListener);
        browseMovieTextView = (TextView) v.findViewById(R.id.hotnow_main_browse_textview);
        browseMovieTextView.setText("Play Movie");
        return v;
    }

    @Subscribe
    public void onLoadedHotNowVideoItemsEvent(LoadedHotNowVideoItemsEvent event) {
        mHotnowContentList = event.getVideoItems();
        for (int i=0; i < mHotnowContentList.size(); i++) {
            VideoItem videoItem = mHotnowContentList.get(i);

            TextSliderView defaultSliderView = new TextSliderView(mActivity.getBaseContext());
            defaultSliderView.description(videoItem.getTitle())
                    .image(videoItem.getThumbnail().getSmallThumbnailUrl())
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(hotnowClickListener);

            //add your extra information
            defaultSliderView.bundle(new Bundle());
            defaultSliderView.getBundle().putInt("content_index", i);

            hotnowSection.addSlider(defaultSliderView);
            hotnowSection.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);
            hotnowSection.setDuration(30000);
        }
    }

    @Subscribe
    public void onConnectorErrEvent(ConnectorErrEvent event) {

    }

    protected void loadHotNowSection()
    {
        String testUrl = getResources().getString(R.string.contentUrl);
        ServiceConnector serviceConnector = new ServiceConnector(mActivity);
        serviceConnector.doRequest(testUrl, new ServiceConnector.ResponseListener() {
            @Override
            public void onComplete(ArrayList<VideoItem> vodContentList, ArrayList<VideoItem> liveContentList) {
                ArrayList<VideoItem> contentList = new ArrayList<VideoItem>();
                contentList.addAll(vodContentList);
                contentList.addAll(liveContentList);

                VideoItemsModel videoModel = new VideoItemsModel(contentList);
                BusProvider.get().post(new LoadedHotNowVideoItemsEvent(videoModel));
            }

            @Override
            public void onErrorOccurred(String errMessage) {
                // BusProvider.get().post(new ConnectorErrEvent(errMessage));
            }
        });

        //
        // For Testing
        //
//        HashMap<String,String> url_maps = new HashMap<String, String>();
//        url_maps.put("Hannibal", "http://static2.hypable.com/wp-content/uploads/2013/12/hannibal-season-2-release-date.jpg");
//        url_maps.put("Big Bang Theory", "http://tvfiles.alphacoders.com/100/hdclearart-10.png");
//        url_maps.put("House of Cards", "http://cdn3.nflximg.net/images/3093/2043093.jpg");
//        url_maps.put("Game of Thrones", "http://images.boomsbeat.com/data/images/full/19640/game-of-thrones-season-4-jpg.jpg");
//
//        for(String name : url_maps.keySet())
//        {
//            DefaultSliderView defaultSliderView = new DefaultSliderView(mActivity.getBaseContext());
//            defaultSliderView.description(name)
//                    .image(url_maps.get(name))
//                    .setScaleType(BaseSliderView.ScaleType.Fit)
//                    .setOnSliderClickListener(this);
//
//            //add your extra information
//            defaultSliderView.bundle(new Bundle());
//            defaultSliderView.getBundle().putString("extra", name);
//
//            sliderLayout.addSlider(defaultSliderView);
//            sliderLayout.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);
//            sliderLayout.setDuration(4000);
//        }
    }

    protected void loadBuzzingNowSection(View v) {
        buzzingImageView1 = (ImageView) v.findViewById(R.id.hotnow_buzzing_imageview1);
        buzzingImageView2 = (ImageView) v.findViewById(R.id.hotnow_buzzing_imageview2);

        buzzingImageView1.setScaleType(ImageView.ScaleType.FIT_XY);
        buzzingImageView2.setScaleType(ImageView.ScaleType.FIT_XY);

        // ImageLoader imageLoader = ImageLoader.getInstance();
        // imageLoader.displayImage("http://cdn3.nflximg.net/images/3093/2043093.jpg", buzzingImageView1);
        // imageLoader.displayImage("http://static2.hypable.com/wp-content/uploads/2013/12/hannibal-season-2-release-date.jpg", buzzingImageView2);
    }

    private BaseSliderView.OnSliderClickListener hotnowClickListener = new BaseSliderView.OnSliderClickListener() {
        @Override
        public void onSliderClick(BaseSliderView baseSliderView) {
            if (mHotnowContentList == null)
                return;

            int index = baseSliderView.getBundle().getInt("content_index");

            // start PlayerActivity to play content
            Intent intent = new Intent(mActivity, PlayerActivity.class);
            // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("CONTENT_INFO", mHotnowContentList.get(index));
            startActivity(intent);
        }
    };

    private View.OnClickListener browseMovieButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
        }
    };
}