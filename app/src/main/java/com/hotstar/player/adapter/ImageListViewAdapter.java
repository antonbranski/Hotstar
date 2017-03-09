package com.hotstar.player.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hotstar.player.R;
import com.hotstar.player.adplayer.core.VideoItem;
import com.hotstar.player.adplayer.manager.OverlayAdResourceManager;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ImageListViewAdapter extends BaseAdapter {

    private Context mContext;
    private static LayoutInflater mInflater = null;
    private ArrayList<VideoItem> mVideoItems = new ArrayList<>();

    public ImageListViewAdapter (Activity a, ArrayList<VideoItem> videoItems) {
        mInflater = (LayoutInflater) a.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        mContext = a.getBaseContext();
        mVideoItems.addAll(videoItems);
    }

    public void setVideoItems(ArrayList<VideoItem> videoItems) {
        mVideoItems.clear();
        mVideoItems.addAll(videoItems);
    }

    public void addVideoItems(ArrayList<VideoItem> videoItems) {
        mVideoItems.addAll(videoItems);
    }

    @Override
    public int getCount() {
        if (mVideoItems.size() == 0)
            return 0;

        return mVideoItems.size();
    }

    @Override
    public Object getItem(int position) {
        if (mVideoItems.size() == 0)
           return null;

        if (position >= mVideoItems.size())
            return mVideoItems.get(0);

        return mVideoItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if(convertView == null)
            vi = mInflater.inflate(R.layout.shows_list_row, null);

        TextView title = (TextView)vi.findViewById(R.id.shows_list_title_textview);         // title
        TextView subTitle = (TextView)vi.findViewById(R.id.shows_list_subtitle_textview);   // subtitle
        ImageView thumbImageView = (ImageView)vi.findViewById(R.id.shows_list_thumbnail_imageview); // thumb image
        thumbImageView.setScaleType(ImageView.ScaleType.FIT_XY);

        // Setting all values in listview
        // ImageLoader imageLoader = ImageLoader.getInstance();
        if (position < mVideoItems.size()) {
            VideoItem videoItem = mVideoItems.get(position);
            title.setText(videoItem.getTitle());
            subTitle.setText("");
            // imageLoader.displayImage(videoItem.getThumbnail().getSmallThumbnailUrl(), thumbImageView);
            OverlayAdResourceManager.getInstance().display(videoItem.getThumbnail().getLargeThumbnailUrl(), thumbImageView);
        }

        return vi;
    }
}