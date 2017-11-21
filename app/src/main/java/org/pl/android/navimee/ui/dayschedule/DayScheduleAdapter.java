package org.pl.android.navimee.ui.dayschedule;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.pl.android.navimee.R;
import org.pl.android.navimee.data.model.Event;
import org.pl.android.navimee.injection.ActivityContext;

import java.util.ArrayList;
import java.util.List;

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
        final Event event = mEvents.get(position);
        holder.nameTextView.setText(event.getName());
        if(event.getPlace() != null && event.getPlace().getName() != null) {
            holder.addressTextView.setText(event.getPlace().getName());
        }
        holder.countTextView.setText(String.valueOf(event.getAttending_count()));
        if(event.getEnd_time() != null) {
            holder.timeTextView.setText(event.getEnd_time()/*+":"+String.format("%02d", event.end_time.getMinutes())*/);
        }
        holder.maybeTextView.setText(String.valueOf(event.getMaybe_count()));
        holder.driveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (event.getPlace().getLatitude() != null && event.getPlace().getLongitude() != null) {
                    Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + String.valueOf(event.getPlace().getLatitude()) + "," +
                            String.valueOf(event.getPlace().getLongitude()) + "( " + event.getPlace().getName() + ")");

                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(mContext.getPackageManager()) != null) {
                        mContext.startActivity(mapIntent);
                    }
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

    public void setEvents(List<Event> list) {
        mEvents = list;
    }

    class DayScheduleHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_name) TextView nameTextView;
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