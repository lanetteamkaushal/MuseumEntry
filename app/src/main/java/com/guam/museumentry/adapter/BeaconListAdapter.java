package com.guam.museumentry.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.guam.museumentry.R;
import com.guam.museumentry.beans.Beacon;

import java.util.ArrayList;

/**
 * Created by lcom75 on 25/10/16.
 */

public class BeaconListAdapter extends RecyclerView.Adapter<BeaconListAdapter.BeaconViewHolder> {

    Context context;
    ArrayList<Beacon> beacons;
    LayoutInflater inflater;

    public BeaconListAdapter(Context context, ArrayList<Beacon> beacons) {
        this.context = context;
        this.beacons = beacons;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public BeaconViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.beacon_item, parent, false);
        BeaconViewHolder holder = new BeaconViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(BeaconViewHolder holder, int position) {
        Beacon beacon = beacons.get(position);
        holder.tvBeaconID.setText(beacon.getBeaconId());
    }

    @Override
    public int getItemCount() {
        return beacons.size();
    }

    class BeaconViewHolder extends RecyclerView.ViewHolder {
        TextView tvBeaconID;
        TextView tvBeaconColor;

        public BeaconViewHolder(View itemView) {
            super(itemView);
            tvBeaconID = (TextView) itemView.findViewById(R.id.tvBeaconID);
            tvBeaconColor = (TextView) itemView.findViewById(R.id.tvBeaconColor);
        }
    }
}
