package com.guam.museumentry.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.estimote.sdk.DeviceId;
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
    private BeaconListAdapter.detailListener detailListener;

    public BeaconListAdapter(Context context, ArrayList<Beacon> beacons, detailListener detailListener) {
        this.context = context;
        this.beacons = beacons;
        inflater = LayoutInflater.from(context);
        this.detailListener = detailListener;
    }

    @Override
    public BeaconViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.beacon_item, parent, false);
        final BeaconViewHolder holder = new BeaconViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Beacon beacon = beacons.get(holder.getAdapterPosition());
                if (detailListener != null) {
                    detailListener.onItemClick(holder.getAdapterPosition(), beacon);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(BeaconViewHolder holder, int position) {
        Beacon beacon = beacons.get(position);
        holder.tvBeaconID.setText(beacon.getBeaconId());
        if (!TextUtils.isEmpty(beacon.getBeaconColor())) {
            holder.tvBeaconColor.setText(beacon.getBeaconColor());
            holder.tvName.setText(beacon.getBeaconName());
        } else {
            if (detailListener != null) {
                detailListener.fetchDetails(holder.getAdapterPosition(), DeviceId.fromString(beacon.getBeaconId()));
            }
//            FetchDetails.fetchBeaconDetailsByDeviceID(DeviceId.fromString(beacon.getBeaconId()));
        }
    }

    @Override
    public int getItemCount() {
        return beacons.size();
    }

    public interface detailListener {
        public void fetchDetails(int position, DeviceId deviceId);

        public void onItemClick(int position, Beacon beacon);
    }

    class BeaconViewHolder extends RecyclerView.ViewHolder {
        TextView tvBeaconID;
        TextView tvBeaconColor;
        TextView tvName;

        public BeaconViewHolder(View itemView) {
            super(itemView);
            tvBeaconID = (TextView) itemView.findViewById(R.id.tvBeaconID);
            tvBeaconColor = (TextView) itemView.findViewById(R.id.tvBeaconColor);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
        }
    }
}
