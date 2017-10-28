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

import org.joda.time.format.DateTimeFormat;
import org.pl.android.navimee.R;
import org.pl.android.navimee.data.model.Event;
import org.pl.android.navimee.injection.ActivityContext;
import org.pl.android.navimee.injection.ApplicationContext;
import org.pl.android.navimee.injection.ConfigPersistent;
import org.pl.android.navimee.util.ViewUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventsHolder> {
    private List<Event> mEvents;
    private Context mContext;

    @Inject
    public EventsAdapter(@ActivityContext Context context) {
        this.mEvents = new ArrayList<>();
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
        final Event event = mEvents.get(position);
        holder.nameTextView.setText(event.name);
        if(event.place != null && event.place.name != null) {
            holder.addressTextView.setText(event.place.name);
        }
        holder.countTextView.setText(String.valueOf(event.attending_count));
        if(event.end_time != null) {
            holder.timeTextView.setText((ViewUtil.string2Date(event.end_time).toString(DateTimeFormat.forPattern("HH:mm"))));
        }
        holder.maybeTextView.setText(String.valueOf(event.maybe_count));
        holder.driveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (event.place.latitude != null && event.place.latitude != null) {
                    Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + String.valueOf(event.place.latitude) + "," +
                            String.valueOf(event.place.longitude) + "( " + event.place.name + ")");

                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(mContext.getPackageManager()) != null) {
                        mContext.startActivity(mapIntent);
                    }
                }
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

    class EventsHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_name) TextView nameTextView;
        @BindView(R.id.text_address) TextView addressTextView;
        @BindView(R.id.viewTextCount) TextView countTextView;
        @BindView(R.id.viewTextTime) TextView timeTextView;
        @BindView(R.id.viewTextMaybe) TextView maybeTextView;
        @BindView(R.id.driveButton) TextView driveButton;

        public EventsHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }



}