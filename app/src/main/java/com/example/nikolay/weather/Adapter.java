package com.example.nikolay.weather;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;


public class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    List<WeatherList> weatherList;
    String city;
    Context context;

    public Adapter(List<WeatherList> weatherList, String city, Context context) {
        this.weatherList = weatherList;
        this.city = city;
        this.context = context;
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView tvTime;
        ImageView ivIcon;
        TextView tvTemperature;
        TextView tvDetail;

        public ItemViewHolder(View itemView) {
            super(itemView);

            tvTime = (TextView) itemView.findViewById(R.id.tv_time);
            ivIcon = (ImageView) itemView.findViewById(R.id.iv_icon);
            tvTemperature = (TextView) itemView.findViewById(R.id.tv_temperature);
            tvDetail = (TextView) itemView.findViewById(R.id.tv_detail);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case TYPE_HEADER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_header, parent, false);
                return new HeaderViewHolder(view);
            case TYPE_ITEM:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
                return new ItemViewHolder(view);
            default:
                return null;
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        TextView tvCity;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            tvCity = (TextView) itemView.findViewById(R.id.tv_city);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).tvCity.setText(city);
        } else {
            WeatherList weather = weatherList.get(position-1);
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            String time = weather.getDtTxt().substring(11, 16);
            String[] date = weather.getDtTxt().substring(5, 10).split("-");
            itemViewHolder.tvTime.setText(time + " " + date[1] + "." + date[0]);
            Picasso.get().load("http://openweathermap.org/img/w/" + weather.getWeather().get(0).getIcon() + ".png").into(itemViewHolder.ivIcon);
            itemViewHolder.tvTemperature.setText(weather.getMain().getTemp().toString() + " ℃");
            itemViewHolder.tvDetail.setText(weather.getWeather().get(0).getDescription().toUpperCase());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;
        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }


    @Override
    public int getItemCount() {
        return weatherList.size();
    }

}