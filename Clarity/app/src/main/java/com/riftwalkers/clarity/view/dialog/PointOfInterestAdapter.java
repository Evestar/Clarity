package com.riftwalkers.clarity.view.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.riftwalkers.clarity.R;
import com.riftwalkers.clarity.data.point_of_intrest.PoiType;
import com.riftwalkers.clarity.data.point_of_intrest.PointOfInterest;

import java.util.List;

public class PointOfInterestAdapter extends ArrayAdapter<PointOfInterest> {

    private ViewHolder viewHolder;

    public PointOfInterestAdapter(Context context, int resource, List<PointOfInterest> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return initView(position,convertView,parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return initView(position,convertView,parent);
    }

    private View initView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(this.getContext())
                    .inflate(R.layout.searchbox_spinner_listview, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.itemView = (TextView) convertView.findViewById(R.id.searchBoxTextView);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        PointOfInterest item = getItem(position);
        if (item != null) {
            if(item.getPoiType() == PoiType.Ligplaats) {
                viewHolder.itemView.setText(item.getLxmeTXT());
            } else {
                viewHolder.itemView.setText(item.getDescription());
            }
        }

        return convertView;
    }

    private static class ViewHolder {
        private TextView itemView;
    }
}
