package org.pl.android.navimee.ui.events;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.joda.time.format.DateTimeFormat;
import org.pl.android.navimee.R;
import org.pl.android.navimee.data.model.Event;
import org.pl.android.navimee.util.ViewUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventsHolder> {
    private List<Event> mEvents;

    @Inject
    public EventsAdapter() {
        this.mEvents = new ArrayList<>();
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
        holder.nameTextView.setText(event.name());
        holder.addressTextView.setText(event.place().name());
        holder.countTextView.setText(event.attending());
        holder.timeTextView.setText((ViewUtil.string2Date(event.endDate()).toString(DateTimeFormat.forPattern("HH:mm"))));
        holder.maybeTextView.setText(event.maybe());

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

        public EventsHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}