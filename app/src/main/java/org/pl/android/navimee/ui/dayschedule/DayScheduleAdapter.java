package org.pl.android.navimee.ui.dayschedule;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.joda.time.format.DateTimeFormat;
import org.pl.android.navimee.R;
import org.pl.android.navimee.data.model.Event;
import org.pl.android.navimee.injection.ActivityContext;
import org.pl.android.navimee.util.ViewUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Wojtek on 2017-10-30.
 */

public class DayScheduleAdapter extends RecyclerView.Adapter<DayScheduleAdapter.DayScheduleHolder> {
    private List<Event> mEvents;
    private Context mContext;

    @Inject
    DaySchedulePresenter mDaySchedulePresenter;


    @Inject
    public DayScheduleAdapter(@ActivityContext Context context) {
        this.mEvents = new ArrayList<>();
        mContext = context;
    }

    @Override
    public DayScheduleHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day_schedule, parent, false);
        return new DayScheduleHolder(view);
    }

    @Override
    public void onBindViewHolder(final DayScheduleHolder holder, final int position) {
        Event event = mEvents.get(position);
        holder.nameTextView.setText(event.getTitle());
        if(event.getPlace() != null) {
            if(event.getPlace().getName() != null) {
                holder.addressNameTextView.setText(event.getPlace().getName());
            }
            if(event.getPlace().getAddress() != null) {
                holder.addressTextView.setText(event.getPlace().getAddress()+", "+event.getPlace().getCity());
            }
        }
        holder.countTextView.setText(String.valueOf(event.getRank()));
        if(event.getEndTime() != null) {
            holder.timeTextView.setText(event.getEndTime().getHours()+":"+String.format("%02d",event.getEndTime().getMinutes()));
        }
        holder.maybeTextView.setText(String.valueOf(event.getRank()));
        holder.driveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + String.valueOf(event.getPlace().getGeoPoint().getLatitude()) + "," +
                        String.valueOf(event.getPlace().getGeoPoint().getLongitude()));
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(mContext.getPackageManager()) != null) {
                        mContext.startActivity(mapIntent);
                    }
                }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDaySchedulePresenter.deleteEvent(event);
            }
        });


    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    public void setEvents(List<Event> events) {
        mEvents = events;
    }


    public void deleteEvent(Event event) {
        mEvents.remove(event);
    }

    class DayScheduleHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_name) TextView nameTextView;
        @BindView(R.id.text_address_name) TextView addressNameTextView;
        @BindView(R.id.text_address) TextView addressTextView;
        @BindView(R.id.viewTextCount) TextView countTextView;
        @BindView(R.id.viewTextTime) TextView timeTextView;
        @BindView(R.id.viewTextMaybe) TextView maybeTextView;
        @BindView(R.id.driveButton) TextView driveButton;
        @BindView(R.id.deleteButton) TextView deleteButton;

        public DayScheduleHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }



}