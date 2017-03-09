package com.hotstar.player.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.hotstar.player.R;
import com.hotstar.player.adplayer.manager.OverlayAdResourceManager;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class ImageGridViewAdapter extends ArrayAdapter<String> {

	private Context mContext;

	public ImageGridViewAdapter(Context context, int resourceId,
								 List<String> items) {
		super(context, resourceId, items);
		mContext = context;
	}


	private class ViewHolder {
		ImageView imageView1;
		ImageView imageView2;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		String imageUrl = getItem(position);

		LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.image_grid_cell, null);
			holder = new ViewHolder();
			holder.imageView1 = (ImageView) convertView.findViewById(R.id.image_grid_cell_imageview1);
			holder.imageView1.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();

		// ImageLoader imageLoader = ImageLoader.getInstance();
		// imageLoader.displayImage(imageUrl, holder.imageView1);
		OverlayAdResourceManager.getInstance().display(imageUrl, holder.imageView1);

		return holder.imageView1;
	}
}
