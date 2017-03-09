package com.hotstar.player.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.hotstar.player.R;
import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.adplayer.core.VideoItem;
import com.hotstar.player.adplayer.player.PlayerActivity;
import com.hotstar.player.events.BusProvider;
import com.hotstar.player.events.ConnectorErrEvent;
import com.hotstar.player.events.GoToExtendFragmentEvent;
import com.hotstar.player.events.LoadedFeaturedVideoItemsEvent;
import com.hotstar.player.events.LoadedMoviesVideoItemsEvent;
import com.hotstar.player.model.TransitionExtendFragmentModel;
import com.hotstar.player.model.VideoItemsModel;
import com.hotstar.player.webservice.ServiceConnector;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;

public class MoviesFragment extends Fragment {
    private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + MoviesFragment.class.getSimpleName();

    private Activity mActivity = null;
    private SliderLayout trailersSection = null;

    private ImageView moviesImageView1 = null;
    private ImageView moviesImageView2 = null;
    private Button browseMovieButton = null;
    private TextView browseMovieTextView = null;

    private View parentView = null;
    private ArrayList<VideoItem> mMoviesContentList = null;

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
        parentView = inflater.inflate(R.layout.fragment_movies, container, false);

        // load main section
        // loadMoviesSection(v);

        // load trailers section
        trailersSection = (SliderLayout)parentView.findViewById(R.id.movies_trailers_slider);
        loadLatestTrailersSection(parentView);

        browseMovieButton = (Button) parentView.findViewById(R.id.movies_trailers_browse_button);
        browseMovieButton.setOnClickListener(browseMovieButtonClickListener);
        browseMovieTextView = (TextView) parentView.findViewById(R.id.movies_trailers_browse_textview);
        browseMovieTextView.setText("Browse Movies");
        return parentView;
    }

    protected void loadLatestTrailersSection(View parentView) {
        String testUrl = getResources().getString(R.string.usecase3Url);
        ServiceConnector serviceConnector = new ServiceConnector(mActivity);
        serviceConnector.doRequest(testUrl, new ServiceConnector.ResponseListener() {
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
        });
    }

    @Subscribe
    public void onLoadedMoviesVideoItemsEvent(LoadedMoviesVideoItemsEvent event) {
        mMoviesContentList = event.getVideoItems();
        for (int i=0; i < mMoviesContentList.size(); i++) {
            VideoItem videoItem = mMoviesContentList.get(i);

            TextSliderView defaultSliderView = new TextSliderView(mActivity.getBaseContext());
            defaultSliderView.description(videoItem.getTitle())
                    .image(videoItem.getThumbnail().getSmallThumbnailUrl())
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(latestTrailerClickListener);

            //add your extra information
            defaultSliderView.bundle(new Bundle());
            defaultSliderView.getBundle().putInt("content_index", i);

            trailersSection.addSlider(defaultSliderView);
            trailersSection.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);
            trailersSection.setDuration(30000);
        }
    }

    private BaseSliderView.OnSliderClickListener latestTrailerClickListener = new BaseSliderView.OnSliderClickListener() {
        @Override
        public void onSliderClick(BaseSliderView baseSliderView) {
            if (mMoviesContentList == null)
                return;

            int index = baseSliderView.getBundle().getInt("content_index");

            // start PlayerActivity to play content
            Intent intent = new Intent(mActivity, PlayerActivity.class);
            // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("CONTENT_INFO", mMoviesContentList.get(index));
            startActivity(intent);
        }
    };

    protected void loadMoviesSection(View v) {
        moviesImageView1 = (ImageView) v.findViewById(R.id.movies_main_imageview1);
        moviesImageView2 = (ImageView) v.findViewById(R.id.movies_main_imageview2);

        moviesImageView1.setScaleType(ImageView.ScaleType.FIT_XY);
        moviesImageView2.setScaleType(ImageView.ScaleType.FIT_XY);

        // ImageLoader imageLoader = ImageLoader.getInstance();
        // imageLoader.displayImage("http://static2.hypable.com/wp-content/uploads/2013/12/hannibal-season-2-release-date.jpg", moviesImageView1);
        // imageLoader.displayImage("http://cdn3.nflximg.net/images/3093/2043093.jpg", moviesImageView2);
    }

    private View.OnClickListener browseMovieButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            TransitionExtendFragmentModel transitionModel = new TransitionExtendFragmentModel(ExtendFragment.MOVIES_FRAGMENT, ExtendFragment.MOVIES_ALLMOVIES_FRAGMENT);
            GoToExtendFragmentEvent transitionEvent = new GoToExtendFragmentEvent(transitionModel);
            BusProvider.get().post(transitionEvent);
        }
    };

}