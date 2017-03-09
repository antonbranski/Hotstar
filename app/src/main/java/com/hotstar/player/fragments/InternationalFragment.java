package com.hotstar.player.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.hotstar.player.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.w3c.dom.Text;

import java.util.HashMap;


public class InternationalFragment extends Fragment {
    private Activity mActivity = null;
    private SliderLayout internationalSection = null;
    private ImageView seasonsImageView1 = null;
    private ImageView seasonsImageView2 = null;

    private TextView descriptionTextView11 = null, descriptionTextView21 = null;
    private TextView subscriptionTextView12 = null, subscriptionTextView22 = null;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_international, container, false);

        // load international section
        internationalSection = (SliderLayout)v.findViewById(R.id.international_main_slider);
        loadInternationalSection(internationalSection);

        // load latest seasons section
        loadLatestSeasonsSection(v);
        return v;
    }

    protected void loadInternationalSection(SliderLayout sliderLayout)
    {
        HashMap<String,String> url_maps = new HashMap<String, String>();
        url_maps.put("Hannibal", "http://static2.hypable.com/wp-content/uploads/2013/12/hannibal-season-2-release-date.jpg");
        url_maps.put("Big Bang Theory", "http://tvfiles.alphacoders.com/100/hdclearart-10.png");
        url_maps.put("House of Cards", "http://cdn3.nflximg.net/images/3093/2043093.jpg");
        url_maps.put("Game of Thrones", "http://images.boomsbeat.com/data/images/full/19640/game-of-thrones-season-4-jpg.jpg");

        for(String name : url_maps.keySet())
        {
            TextSliderView textSliderView = new TextSliderView(mActivity.getBaseContext());
            textSliderView.description(name)
                    .image(url_maps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(internationalClickListener);

            //add your extra information
            textSliderView.bundle(new Bundle());
            textSliderView.getBundle().putString("extra", name);

            sliderLayout.addSlider(textSliderView);
            sliderLayout.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);
            sliderLayout.setDuration(4000);
        }
    }

    protected void loadLatestSeasonsSection(View v) {
        seasonsImageView1 = (ImageView) v.findViewById(R.id.international_seasons_imageview1);
        seasonsImageView2 = (ImageView) v.findViewById(R.id.international_seasons_imageview2);

        seasonsImageView1.setScaleType(ImageView.ScaleType.FIT_XY);
        seasonsImageView2.setScaleType(ImageView.ScaleType.FIT_XY);

        // ImageLoader imageLoader = ImageLoader.getInstance();
        // imageLoader.displayImage("http://static2.hypable.com/wp-content/uploads/2013/12/hannibal-season-2-release-date.jpg", seasonsImageView1);
        // imageLoader.displayImage("http://cdn3.nflximg.net/images/3093/2043093.jpg", seasonsImageView2);

        descriptionTextView11 = (TextView) v.findViewById(R.id.internationl_description_textview11);
        descriptionTextView21 = (TextView) v.findViewById(R.id.internationl_description_textview21);
        subscriptionTextView12 = (TextView) v.findViewById(R.id.international_subscription_textview12);
        subscriptionTextView22 = (TextView) v.findViewById(R.id.international_subscription_textview22);

        descriptionTextView11.setText("It's always Sunny In");
        descriptionTextView21.setText("Sons of Anarchy");
        subscriptionTextView12.setText("Ass Kickers United: Mac an");
        subscriptionTextView22.setText("Some Strange Eruption");
    }

    private BaseSliderView.OnSliderClickListener internationalClickListener = new BaseSliderView.OnSliderClickListener() {
        @Override
        public void onSliderClick(BaseSliderView baseSliderView) {

        }
    };
}