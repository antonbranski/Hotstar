package com.hotstar.player.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hotstar.player.R;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * Class to control of elements in the list menu
 *
 * @author Leonardo Salles
 */
public class MenuLazyAdapter extends BaseAdapter {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;
    private static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;
    private static LayoutInflater mInflater = null;
    private final ArrayList<Object> data = new ArrayList<> ();
    private final TreeSet<Integer> mSeparatorsSet = new TreeSet<> ();
    private int currentPosition = 0;

    private Context mContext = null;

    public MenuLazyAdapter (Activity a) {
        mInflater = (LayoutInflater) a.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        mContext = a.getBaseContext();
    }

    public void AddedAllItems () {
        notifyDataSetChanged ();
    }

    public void addItem (final Object item) {
        data.add (item);

    }

    @Override
    public int getItemViewType (int position) {
        return mSeparatorsSet.contains (position) ? TYPE_SEPARATOR : TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount () {
        return TYPE_MAX_COUNT;
    }

    public int getCount () {
        return data.size ();
    }

    public Object getItem (int position) {
        return data.get (position);
    }

    public long getItemId (int position) {
        return position;
    }

    public void setCurrentPosition(int position) {
        currentPosition = position;
        notifyDataSetChanged();
    }

    public View getView (int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        int type = getItemViewType (position);

        if (convertView == null) {

            holder = new ViewHolder ();

            switch (type) {
                case TYPE_SEPARATOR:
                    break;

                case TYPE_ITEM:
                    convertView = mInflater.inflate (R.layout.menu_list_item, null);
                    holder.textView = (TextView) convertView.findViewById (R.id.title_menu);
                    break;
            }

            assert convertView != null;
            convertView.setTag (holder);
        } else {
            holder = (ViewHolder) convertView.getTag ();
        }

        String title = data.get (position).toString ();

        if (type == TYPE_SEPARATOR) {
            holder.textView.setText (title);
        }
        else if (type == TYPE_ITEM) {
            convertView.setBackgroundColor (Color.WHITE);
            holder.textView.setText (title);
            if (position == currentPosition) {
                // set highlight color
                holder.textView.setTextColor(mContext.getResources().getColor(R.color.main_color_500));
            }
            else {
                // set normal color
                holder.textView.setTextColor(mContext.getResources().getColor(R.color.main_color_100));
            }
        }

        return convertView;
    }

    public class ViewHolder {
        public RelativeLayout backlayout;
        public TextView textView;
    }
}