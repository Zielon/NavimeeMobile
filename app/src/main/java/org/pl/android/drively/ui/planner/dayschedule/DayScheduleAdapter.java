package org.pl.android.drively.ui.planner.dayschedule;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rilixtech.materialfancybutton.MaterialFancyButton;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Minutes;
import org.pl.android.drively.R;
import org.pl.android.drively.data.model.Event;
import org.pl.android.drively.injection.ActivityContext;

import java.text.SimpleDateFormat;
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
    private DateTime dateTime;

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
            DateTime startTime = new DateTime(event.getStartTime());
            DateTime endTime = new DateTime(event.getEndTime());
            if (Days.daysBetween(startTime.withTimeAtStartOfDay(), dateTime.withTimeAtStartOfDay()).getDays() == 0
                    && Days.daysBetween(endTime.withTimeAtStartOfDay(), dateTime.withTimeAtStartOfDay()).getDays() == 0) {
                holder.timeTextView.setText(event.getStartTime().getHours() + ":" + String.format("%02d", event.getStartTime().getMinutes()) + " - " +
                        event.getEndTime().getHours() + ":" + String.format("%02d", event.getEndTime().getMinutes()));
            } else {
                SimpleDateFormat simpleDateformat = new SimpleDateFormat("E"); // the day of the week abbreviated
                String startDay = simpleDateformat.format(event.getStartTime());
                String endDay = simpleDateformat.format(event.getEndTime());
                holder.timeTextView.setText(startDay + " " + event.getStartTime().getHours() + ":" + String.format("%02d", event.getStartTime().getMinutes()) + " - " +
                        endDay + " " + event.getEndTime().getHours() + ":" + String.format("%02d", event.getEndTime().getMinutes()));
            }
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
            holder.deleteButton.setTag(2);
            holder.deleteButton.setText(R.string.navigate);
        } else {
            holder.deleteButton.setTag(1);
            holder.deleteButton.setBackgroundColor(mContext.getResources().getColor(R.color.colorLine));
            holder.deleteButton.setTextColor(mContext.getResources().getColor(R.color.gray_font));
            holder.deleteButton.setText(R.string.cancel);
        }
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((int) view.getTag() == 1) {
                    mDaySchedulePresenter.deleteEvent(event);
                } else if ((int) view.getTag() == 2) {
                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + String.valueOf(event.getPlace().getLat()) + "," +
                            String.valueOf(event.getPlace().getLon()));
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

    public void setEvents(List<Event> events) {
        mEvents = events;
    }


    public void deleteEvent(Event event) {
        mEvents.remove(event);
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
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
        @BindView(R.id.deleteButton)
        MaterialFancyButton deleteButton;

        public DayScheduleHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }
}