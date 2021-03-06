package org.pl.android.drively.ui.planner.events;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.pl.android.drively.R;
import org.pl.android.drively.data.model.Event;
import org.pl.android.drively.data.remote.FirebaseAnalyticsService;
import org.pl.android.drively.injection.ApplicationContext;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import mehdi.sakout.fancybuttons.FancyButton;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventsHolder> {
    private final FirebaseAnalyticsService firebaseAnalyticsService;
    DateTime currentDateTime;
    private List<Event> mEvents;
    private Context mContext;
    private DateTime dateTime;

    private List<Event> dayScheduleList;

    private EventsAdapterCallback eventsAdapterCallback;

    @Inject
    public EventsAdapter(@ApplicationContext Context context, FirebaseAnalyticsService firebaseAnalyticsService) {
        this.mEvents = new ArrayList<>();
        this.mContext = context;
        this.firebaseAnalyticsService = firebaseAnalyticsService;
        currentDateTime = new DateTime(Calendar.getInstance().getTime());
    }

    public void setCallback(EventsAdapterCallback eventsAdapterCallback) {
        this.eventsAdapterCallback = eventsAdapterCallback;
    }

    @Override
    public EventsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventsHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final EventsHolder holder, final int position) {
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
            SimpleDateFormat simpleDateformat = new SimpleDateFormat("E"); // the day of the week abbreviated
            String startDay = simpleDateformat.format(event.getStartTime());
            String endDay = simpleDateformat.format(event.getEndTime());
            holder.timeTextView.setText(startDay + " " + event.getStartTime().getHours() + ":" + String.format("%02d", event.getStartTime().getMinutes()) + " - " +
                    endDay + " " + event.getEndTime().getHours() + ":" + String.format("%02d", event.getEndTime().getMinutes()));
        }
        holder.addButton.setTag(0);
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

        if (Minutes.minutesBetween(currentDateTime, new DateTime(event.getEndTime())).getMinutes() < 30 && Minutes.minutesBetween(currentDateTime, new DateTime(event.getEndTime())).getMinutes() > 0) {
            holder.addButton.setTag(1);
            holder.addButton.setText(mContext.getString(R.string.navigate));
        } else if (dayScheduleList != null && dayScheduleList.contains(event)) {
            holder.addButton.setTag(2);
            holder.addButton.setBackgroundColor(mContext.getResources().getColor(R.color.colorLine));
            holder.addButton.setTextColor(mContext.getResources().getColor(R.color.gray_font));
            holder.addButton.setText(mContext.getString(R.string.cancel));
        } else {
            holder.addButton.setBackgroundColor(mContext.getResources().getColor(R.color.button_background));
            holder.addButton.setTextColor(mContext.getResources().getColor(R.color.white));
            holder.addButton.setText(mContext.getString(R.string.popup_events_info3_button));
            holder.addButton.setTag(3);
        }

        holder.addButton.setOnClickListener(view -> {
            firebaseAnalyticsService.reportEvent(holder.addButton.getText().toString(), this.getClass().getSimpleName(), event);
            if ((int) view.getTag() == 1) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + String.valueOf(event.getPlace().getLat()) + "," +
                        String.valueOf(event.getPlace().getLon()));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(mContext.getPackageManager()) != null) {
                    mContext.startActivity(mapIntent);
                }
            } else if ((int) view.getTag() == 2) {
                holder.addButton.setTag(3);
                holder.addButton.setBackgroundColor(mContext.getResources().getColor(R.color.button_background));
                holder.addButton.setTextColor(mContext.getResources().getColor(R.color.white));
                holder.addButton.setText(mContext.getString(R.string.remember_me));
                eventsAdapterCallback.eventsAdapterCallback(EventsAdapterAction.DELETE, event);
            } else if ((int) view.getTag() == 3) {
                holder.addButton.setTag(2);
                holder.addButton.setBackgroundColor(mContext.getResources().getColor(R.color.colorLine));
                holder.addButton.setTextColor(mContext.getResources().getColor(R.color.gray_font));
                holder.addButton.setText(mContext.getString(R.string.cancel));
                eventsAdapterCallback.eventsAdapterCallback(EventsAdapterAction.SAVE, event);

            }
        });


    }

    public void setDayScheduleList(List<Event> dayScheduleList) {
        this.dayScheduleList = dayScheduleList;
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }


 /*   public void setEvents(List<Event> events) {
        mEvents = events;
    }*/

    public void addEvents(List<Event> eventList) {
        mEvents.clear();
        mEvents.addAll(eventList);
    }

    public void clearEvents() {
        mEvents.clear();
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }


    enum EventsAdapterAction {
        SAVE,
        DELETE,
        LOG_ANALYTICS
    }

    interface EventsAdapterCallback {
        void eventsAdapterCallback(EventsAdapterAction eventsAdapterAction, Object additionalData);
    }

    class EventsHolder extends RecyclerView.ViewHolder {

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
        @BindView(R.id.addButton)
        FancyButton addButton;

        public EventsHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

}