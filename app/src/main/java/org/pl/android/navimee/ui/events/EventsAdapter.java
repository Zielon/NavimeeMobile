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
        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(mEvents.get(position));
        Event event = gson.fromJson(jsonElement, Event.class);
        holder.nameTextView.setText(event.getName());
        if(event.getPlace() != null && event.getPlace().getName() != null) {
            holder.addressTextView.setText(event.getPlace().getName());
        }
        holder.countTextView.setText(String.valueOf(event.getAttending_count()));
        if(event.getEnd_time() != null) {
            holder.timeTextView.setText(ViewUtil.string2Date(event.getEnd_time()).toString(DateTimeFormat.forPattern("HH:mm")));
        }
        holder.maybeTextView.setText(String.valueOf(event.getMaybe_count()));
        holder.driveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (event.getPlace().getLon() != null && event.getPlace().getLat() != null) {
                    Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + String.valueOf(event.getPlace().getLat()) + "," +
                            String.valueOf(event.getPlace().getLon()) + "( " + event.getPlace().getName() + ")");

                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(mContext.getPackageManager()) != null) {
                        mContext.startActivity(mapIntent);
                    }
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

    public void setEvents(Map<String, Object> events) {
        mEvents = new ArrayList(events.values());
    }

    class EventsHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_name) TextView nameTextView;
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