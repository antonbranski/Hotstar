package com.hotstar.player.fragments;

import android.graphics.Typeface;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hotstar.player.R;
import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.adplayer.core.VideoItem;
import com.hotstar.player.custom.CustomButtonTouchListener;
import com.hotstar.player.adplayer.manager.OverlayAdResourceManager;
import com.hotstar.player.events.BackToExtendFragmentEvent;
import com.hotstar.player.events.BusProvider;
import com.hotstar.player.model.TransitionExtendFragmentModel;
import com.nostra13.universalimageloader.core.ImageLoader;

import babushkatext.BabushkaText;
import mehdi.sakout.fancybuttons.FancyButton;

public class DetailFragment extends BaseFragment {
    private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + ExtendFragment.class.getSimpleName();

    VideoItem videoItem = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        AdVideoApplication.logger.i(LOG_TAG + "#onCreateView", "onCreateView is called");

        View v = inflater.inflate(R.layout.fragment_detail, container, false);
        final View actionBarView = v.findViewById(R.id.detail_custom_actionbar);
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

        setTitle("Bombay Velvet");
        configureView(v);
        return v;
    }

    /**
     * Set video item for view
     *
     * @param videoItem
     */
    public void setVideoItem(VideoItem videoItem) {
        this.videoItem = videoItem;
    }

    private void configureView(View parentView) {
        if (videoItem == null)
            return;

        if (parentView == null)
            return;

        ImageView imageView = (ImageView) parentView.findViewById(R.id.detail_main_imageview);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        // ImageLoader imageLoader = ImageLoader.getInstance();
        // imageLoader.displayImage(videoItem.getThumbnail().getLargeThumbnailUrl(), imageView);
        OverlayAdResourceManager.getInstance().display(videoItem.getThumbnail().getLargeThumbnailUrl(), imageView);

        BabushkaText descriptionView = (BabushkaText) parentView.findViewById(R.id.detail_detail_textview);
        descriptionView.addPiece(new BabushkaText.Piece.Builder("Bombay Velvet\n\n")
                .backgroundColor(mActivity.getResources().getColor(R.color.main_color_300))
                .textColor(mActivity.getResources().getColor(R.color.main_color_000))
                .textSizeRelative(1.4f)
                .style(Typeface.BOLD)
                .build());
        descriptionView.addPiece(new BabushkaText.Piece.Builder("145 min | Drama | 2015\n\n")
                .backgroundColor(mActivity.getResources().getColor(R.color.main_color_300))
                .textColor(mActivity.getResources().getColor(R.color.main_color_000))
                .textSizeRelative(1.2f)
                .style(Typeface.BOLD)
                .build());
        descriptionView.addPiece(new BabushkaText.Piece.Builder("Anurag Kashyap's torrid tale about Johnny Balraj (Ranbir Kappor), a man desperate" +
                "to make it big in the city of Bombay but who eventually gets sucked into a void of deception and greed. Also starring Anushka Sharma, Karan Johar and Kay Kay Menon.")
                .backgroundColor(mActivity.getResources().getColor(R.color.main_color_300))
                .textColor(mActivity.getResources().getColor(R.color.main_color_100))
                .build());
        descriptionView.display();

        FancyButton likeButton = (FancyButton) parentView.findViewById(R.id.custom_tab4_like_button);
        likeButton.setOnClickListener(likeButtonClickListener);

        FancyButton dislikeButton = (FancyButton) parentView.findViewById(R.id.custom_tab4_dislike_button);
        dislikeButton.setOnClickListener(dislikeButtonClickListener);

        FancyButton addToListButton = (FancyButton) parentView.findViewById(R.id.custom_tab4_addtolist_button);
        addToListButton.setOnClickListener(addToListButtonClickListener);
        addToListButton.setVisibility(View.GONE);

        FancyButton shareButton = (FancyButton) parentView.findViewById(R.id.custom_tab4_share_button);
        shareButton.setOnClickListener(shareButtonClickListener);
    }

    @Override
    public void onClickButtonBack() {
        TransitionExtendFragmentModel extendFragmentModel = new TransitionExtendFragmentModel(ExtendFragment.NON_FRAGMENT);
        BackToExtendFragmentEvent event = new BackToExtendFragmentEvent(extendFragmentModel);
        BusProvider.get().post(event);
    }

    private final View.OnClickListener likeButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AdVideoApplication.logger.e(LOG_TAG+"#onLikeButton", "Like button is called.");
        }
    };

    private final View.OnClickListener dislikeButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AdVideoApplication.logger.e(LOG_TAG+"#onDislikeButton", "Dislike button is called.");
        }
    };

    private final View.OnClickListener addToListButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AdVideoApplication.logger.e(LOG_TAG+"#onAddToListButton", "AddToList button is called.");
        }
    };

    private final View.OnClickListener shareButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AdVideoApplication.logger.e(LOG_TAG+"#onShareButton", "Share button is called.");
        }
    };
}