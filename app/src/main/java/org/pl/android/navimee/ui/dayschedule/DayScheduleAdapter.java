package org.pl.android.navimee.ui.dayschedule;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.pl.android.navimee.R;
import org.pl.android.navimee.data.model.Event;
import org.pl.android.navimee.injection.ActivityContext;
import org.pl.android.navimee.util.ViewUtil;

import java.util.ArrayList;
import java.util.Calendar;
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
    DateTime currentDateTime;

    @Inject
    DaySchedulePresenter mDaySchedulePresenter;


    @Inject
    public DayScheduleAdapter(@ActivityContext Context context) {
        this.mEvents = new ArrayList<>();
        mContext = context;
        currentDateTime = new DateTime(Calendar.getInstance().getTime());
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
        if(event.getEndTime() != null) {
            holder.timeTextView.setText(event.getEndTime().getHours()+":"+String.format("%02d",event.getEndTime().getMinutes()));
        }
        if(event.getRank() >= 0 && event.getRank() <= 20) {
            holder.imageCount.setImageResource(R.drawable.ranking_1_24dp);
        }  else if(event.getRank() >= 21 && event.getRank() <= 40) {
            holder.imageCount.setImageResource(R.drawable.ranking_2_24dp);
        } else if(event.getRank() >= 41 && event.getRank() <= 60) {
            holder.imageCount.setImageResource(R.drawable.ranking_3_24dp);
        } else if(event.getRank() >= 61 && event.getRank() <= 80) {
            holder.imageCount.setImageResource(R.drawable.ranking_4_24dp);
        } else {
            holder.imageCount.setImageResource(R.drawable.ranking_5_24dp);
        }

        if(Minutes.minutesBetween(currentDateTime, new DateTime(event.getEndTime())).getMinutes() < 30) {
            holder.driveButton.setImageResource(R.drawable.go_now_24dp);
            holder.driveButton.setTag(1);
        }  else  {
            holder.driveButton.setImageResource(R.drawable.ringing_bell_24dp);
            holder.driveButton.setEnabled(false);
        }

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

        @BindView(R.id.imageCount)
        ImageView imageCount;
        @BindView(R.id.text_name) TextView nameTextView;
        @BindView(R.id.text_address_name) TextView addressNameTextView;
        @BindView(R.id.text_address) TextView addressTextView;
        @BindView(R.id.viewTextTime) TextView timeTextView;
        @BindView(R.id.viewTextMaybe) TextView maybeTextView;
        @BindView(R.id.driveButton)
        FloatingActionButton driveButton;
        @BindView(R.id.deleteButton)
        ImageButton deleteButton;

        public DayScheduleHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }



}