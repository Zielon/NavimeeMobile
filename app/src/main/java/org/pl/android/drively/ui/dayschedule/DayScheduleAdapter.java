package org.pl.android.drively.ui.dayschedule;

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

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.pl.android.drively.R;
import org.pl.android.drively.data.model.Event;
import org.pl.android.drively.injection.ActivityContext;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DayScheduleAdapter extends RecyclerView.Adapter<DayScheduleAdapter.DayScheduleHolder> {
    DateTime currentDateTime;
    @Inject
    DaySchedulePresenter mDaySchedulePresenter;
    private List<Event> mEvents;
    private Context mContext;


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
        if (event.getPlace() != null) {
            if (event.getPlace().getName() != null) {
                holder.addressNameTextView.setText(event.getPlace().getName());
            }
            if (event.getPlace().getAddress() != null) {
                holder.addressTextView.setText(event.getPlace().getAddress() + ", " + event.getPlace().getCity());
            }
        }
        if (event.getEndTime() != null) {
            holder.timeTextView.setText(event.getEndTime().getHours() + ":" + String.format("%02d", event.getEndTime().getMinutes()));
        }
        if (event.getRank() == 1) {
            holder.imageCount.setImageResource(R.mipmap.ranking_1);
        } else if (event.getRank() == 2) {
            holder.imageCount.setImageResource(R.mipmap.ranking_2);
        } else if (event.getRank() == 3) {
            holder.imageCount.setImageResource(R.mipmap.ranking_3);
        } else if (event.getRank() == 4) {
            holder.imageCount.setImageResource(R.mipmap.ranking_4);
        } else {
            holder.imageCount.setImageResource(R.mipmap.ranking_5);
        }

        if (Minutes.minutesBetween(currentDateTime, new DateTime(event.getEndTime())).getMinutes() < 30) {
            holder.driveButton.setImageResource(R.drawable.go_now_24dp);
            holder.driveButton.setTag(1);
        } else {
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
        @BindView(R.id.text_name)
        TextView nameTextView;
        @BindView(R.id.text_address_name)
        TextView addressNameTextView;
        @BindView(R.id.text_address)
        TextView addressTextView;
        @BindView(R.id.viewTextTime)
        TextView timeTextView;
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