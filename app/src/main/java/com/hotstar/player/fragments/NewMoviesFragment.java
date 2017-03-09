package com.hotstar.player.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import com.hotstar.player.adplayer.manager.OverlayAdResourceManager;
import com.hotstar.player.custom.SamsungPhoneInfo;
import com.hotstar.player.events.BusProvider;
import com.hotstar.player.events.ConnectorErrEvent;
import com.hotstar.player.events.GoToExtendFragmentEvent;
import com.hotstar.player.events.LoadedFeaturedVideoItemsEvent;
import com.hotstar.player.events.LoadedMoviesVideoItemsEvent;
import com.hotstar.player.events.LoadedUsecase4VideoItemEvent;
import com.hotstar.player.events.LoadedUsecase5VideoItemEvent;
import com.hotstar.player.model.TransitionExtendFragmentModel;
import com.hotstar.player.model.VideoItemsModel;
import com.hotstar.player.webservice.ConnectorUrlRequest;
import com.hotstar.player.webservice.ServiceConnector;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;

public class NewMoviesFragment extends Fragment {
    private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + NewMoviesFragment.class.getSimpleName();

    private Activity mActivity = null;
    private SliderLayout moviesMainSection = null;

    private ImageView moviesImageView1 = null;
    private ImageView moviesImageView2 = null;
    private Button browseMovieButton = null;
    private TextView browseMovieTextView = null;

    private View parentView = null;
    private ArrayList<VideoItem> mMoviesContentList = null;
    private VideoItem usecase4VideoItem = null;
    private VideoItem usecase5VideoItem = null;

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

        parentView = inflater.inflate(R.layout.fragment_newmovies, container, false);

        // create content list
        mMoviesContentList = new ArrayList<>();

        // load main Movies section
        moviesMainSection = (SliderLayout)parentView.findViewById(R.id.newmovies_main_slider);
        moviesMainSection.addOnPageChangeListener(mainMoviesPageChangeListener);
        loadMoviesMainSection(parentView);

        browseMovieButton = (Button) parentView.findViewById(R.id.newmovies_main_browse_button);
        browseMovieButton.setOnClickListener(browseMovieButtonClickListener);
        browseMovieTextView = (TextView) parentView.findViewById(R.id.newmovies_main_browse_textview);

        // load sub Movies section
        // loadMoviesSubSection(parentView);
        return parentView;
    }

    protected void loadMoviesMainSection(View parentView) {
        String usecase2URL = getResources().getString(R.string.usecase2Url);
        ServiceConnector usecase2Connector = new ServiceConnector(mActivity);
        usecase2Connector.doRequest(usecase2URL, responseListener);

        String usecase3URL = getResources().getString(R.string.usecase3Url);
        ServiceConnector usecase3Connector = new ServiceConnector(mActivity);
        usecase3Connector.doRequest(usecase3URL, responseListener);

        String usecase8URL = getResources().getString(R.string.usecase8Url);
        ServiceConnector usecase8Connector = new ServiceConnector(mActivity);
        usecase8Connector.doRequest(
                ConnectorUrlRequest.buildURL(usecase8URL, HotStarApplication.getInstance().getLocation(), SamsungPhoneInfo.getInstance().modelString()),
                responseListener);

        String usecase9URL = getResources().getString(R.string.usecase9Url);
        ServiceConnector usecase9Connector = new ServiceConnector(mActivity);
        usecase9Connector.doRequest(
                ConnectorUrlRequest.buildURL(usecase9URL, HotStarApplication.getInstance().getLocation(), SamsungPhoneInfo.getInstance().modelString()),
                responseListener);
    }

    protected void loadMoviesSubSection(View parentView) {
        String testUrl4 = getResources().getString(R.string.usecase4Url);
        ServiceConnector serviceConnector4 = new ServiceConnector(mActivity);
        serviceConnector4.doRequest(testUrl4, new ServiceConnector.ResponseListener() {
            @Override
            public void onComplete(ArrayList<VideoItem> vodContentList, ArrayList<VideoItem> liveContentList) {
                ArrayList<VideoItem> contentList = new ArrayList<VideoItem>();
                contentList.addAll(vodContentList);
                contentList.addAll(liveContentList);

                VideoItemsModel videoModel = new VideoItemsModel(contentList);
                BusProvider.get().post(new LoadedUsecase4VideoItemEvent(videoModel));
            }

            @Override
            public void onErrorOccurred(String errMessage) {
                // BusProvider.get().post(new ConnectorErrEvent(errMessage));
            }
        });

        String testUrl5 = getResources().getString(R.string.usecase5Url);
        ServiceConnector serviceConnector5 = new ServiceConnector(mActivity);
        serviceConnector5.doRequest(testUrl5, new ServiceConnector.ResponseListener() {
            @Override
            public void onComplete(ArrayList<VideoItem> vodContentList, ArrayList<VideoItem> liveContentList) {
                ArrayList<VideoItem> contentList = new ArrayList<VideoItem>();
                contentList.addAll(vodContentList);
                contentList.addAll(liveContentList);

                VideoItemsModel videoModel = new VideoItemsModel(contentList);
                BusProvider.get().post(new LoadedUsecase5VideoItemEvent(videoModel));
            }

            @Override
            public void onErrorOccurred(String errMessage) {
                // BusProvider.get().post(new ConnectorErrEvent(errMessage));
            }
        });
    }

    @Subscribe
    public void onLoadedUsecase4VideoItemEvent(LoadedUsecase4VideoItemEvent event) {
        moviesImageView1 = (ImageView) parentView.findViewById(R.id.newmovies_sub_imageview1);
        moviesImageView1.setScaleType(ImageView.ScaleType.FIT_XY);
        moviesImageView1.setOnClickListener(imageView1ClickListener);

        // ImageLoader imageLoader = ImageLoader.getInstance();
        // imageLoader.displayImage(event.getVideoItems().get(0).getThumbnail().getLargeThumbnailUrl(), moviesImageView1);
        OverlayAdResourceManager.getInstance().display(event.getVideoItems().get(0).getThumbnail().getLargeThumbnailUrl(), moviesImageView1);

        usecase4VideoItem = event.getVideoItems().get(0);
    }

    @Subscribe
    public void onLoadedUsecase5VideoItemEvent(LoadedUsecase5VideoItemEvent event) {
        moviesImageView2 = (ImageView) parentView.findViewById(R.id.newmovies_sub_imageview2);
        moviesImageView2.setScaleType(ImageView.ScaleType.FIT_XY);
        moviesImageView2.setOnClickListener(imageView2ClickListener);

        // ImageLoader imageLoader = ImageLoader.getInstance();
        // imageLoader.displayImage(event.getVideoItems().get(0).getThumbnail().getLargeThumbnailUrl(), moviesImageView2);
        OverlayAdResourceManager.getInstance().display(event.getVideoItems().get(0).getThumbnail().getLargeThumbnailUrl(), moviesImageView2);

        usecase5VideoItem = event.getVideoItems().get(0);
    }


    @Subscribe
    public void onLoadedMoviesVideoItemsEvent(LoadedMoviesVideoItemsEvent event) {
        ArrayList<VideoItem> newVideoList = event.getVideoItems();
        int originalSize = mMoviesContentList.size();

        mMoviesContentList.addAll(newVideoList);
        for (int i=0; i < newVideoList.size(); i++) {
            VideoItem videoItem = newVideoList.get(i);

            DefaultSliderView defaultSliderView = new DefaultSliderView(mActivity.getBaseContext());
            defaultSliderView.description(videoItem.getTitle())
                    .image(videoItem.getThumbnail().getLargeThumbnailUrl())
                    .setScaleType(BaseSliderView.ScaleType.CenterInside)
                    .setOnSliderClickListener(mainMoviesClickListener);

            //add your extra information
            defaultSliderView.bundle(new Bundle());
            defaultSliderView.getBundle().putInt("content_index", originalSize + i);

            moviesMainSection.addSlider(defaultSliderView);
            moviesMainSection.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);
            moviesMainSection.setDuration(30000);
        }
    }

    /**
     *
     * Response Listener class variable
     *
     */
    private ServiceConnector.ResponseListener responseListener = new ServiceConnector.ResponseListener() {
        @Override
        public void onComplete(ArrayList<VideoItem> vodContentList, ArrayList<VideoItem> liveContentList) {
            ArrayList<VideoItem> contentList = new ArrayList<VideoItem>();
            contentList.addAll(vodContentList);
            contentList.addAll(liveContentList);

            VideoItemsModel videoModel = new VideoItemsModel(contentList);
            BusProvider.get().post(new LoadedMoviesVideoItemsEvent(videoModel));
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
    private final BaseSliderView.OnSliderClickListener mainMoviesClickListener = new BaseSliderView.OnSliderClickListener() {
        @Override
        public void onSliderClick(BaseSliderView baseSliderView) {
            if (mMoviesContentList == null)
                return;

            int index = baseSliderView.getBundle().getInt("content_index");
            if (index >= mMoviesContentList.size())
                return;

            // start PlayerActivity to play content
            Intent intent = new Intent(mActivity, PlayerActivity.class);
            // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("CONTENT_INFO", mMoviesContentList.get(index));
            startActivity(intent);
        }
    };

    private final ViewPagerEx.OnPageChangeListener mainMoviesPageChangeListener = new ViewPagerEx.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {
        }

        @Override
        public void onPageSelected(int i) {
            if (mMoviesContentList == null) {
                browseMovieTextView.setText("");
                return;
            }

            int index = moviesMainSection.getCurrentSlider().getBundle().getInt("content_index");
            if (index < mMoviesContentList.size()) {
                browseMovieTextView.setText(mMoviesContentList.get(index).getTitle());
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {
        }
    };

    private final View.OnClickListener browseMovieButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mMoviesContentList == null)
                return;

            int index = moviesMainSection.getCurrentSlider().getBundle().getInt("content_index");
            if (index >= mMoviesContentList.size())
                return;

            // start PlayerActivity to play content
            Intent intent = new Intent(mActivity, PlayerActivity.class);
            // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("CONTENT_INFO", mMoviesContentList.get(index));
            startActivity(intent);
        }
    };

    private final View.OnClickListener imageView1ClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (usecase4VideoItem == null)
                return;

            // start PlayerActivity to play content
            Intent intent = new Intent(mActivity, PlayerActivity.class);
            // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("CONTENT_INFO", usecase4VideoItem);
            startActivity(intent);
        }
    };

    private final View.OnClickListener imageView2ClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (usecase5VideoItem == null)
                return;

            // start PlayerActivity to play content
            Intent intent = new Intent(mActivity, PlayerActivity.class);
            // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("CONTENT_INFO", usecase5VideoItem);
            startActivity(intent);
        }
    };


}