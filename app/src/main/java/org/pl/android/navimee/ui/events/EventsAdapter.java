package org.pl.android.navimee.ui.events;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.joda.time.format.DateTimeFormat;
import org.pl.android.navimee.R;
import org.pl.android.navimee.data.model.Event;
import org.pl.android.navimee.injection.ActivityContext;
import org.pl.android.navimee.injection.ApplicationContext;
import org.pl.android.navimee.injection.ConfigPersistent;
import org.pl.android.navimee.util.ViewUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventsHolder> {
    private List<Event> mEvents;
    private Context mContext;

   @Inject EventsPresenter mEventsPresenter;

    @Inject
    public EventsAdapter(@ActivityContext Context context) {
        this.mEvents = new ArrayList<Event>();
        mContext = context;
    }

    @Override
    public EventsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventsHolder(view);
    }

    @Override
    public void onBindViewHolder(final EventsHolder holder, final int position) {
        Event event = mEvents.get(position);
        holder.nameTextView.setText(event.getTitle());
        if(event.getPlace() != null && event.getPlace().getGeoPoint() != null) {
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

        holder.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEventsPresenter.saveEvent(event);
            }
        });


    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }

 /*   public void setEvents(List<Event> events) {
        mEvents = events;
    }*/

    public void addEvent(Event event) {
        mEvents.add(event);
    }

    public void clearEvents() {
        mEvents.clear();
    }


    class EventsHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_name) TextView nameTextView;
        @BindView(R.id.text_address_name) TextView addressNameTextView;
        @BindView(R.id.text_address) TextView addressTextView;
        @BindView(R.id.viewTextCount) TextView countTextView;
        @BindView(R.id.viewTextTime) TextView timeTextView;
        @BindView(R.id.viewTextMaybe) TextView maybeTextView;
        @BindView(R.id.driveButton) TextView driveButton;
        @BindView(R.id.addButton) TextView addButton;

        public EventsHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }



}