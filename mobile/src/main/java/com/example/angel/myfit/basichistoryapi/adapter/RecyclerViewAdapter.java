package com.example.angel.myfit.basichistoryapi.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.angel.myfit.basichistoryapi.R;
import com.example.angel.myfit.basichistoryapi.Utilities;
import com.example.angel.myfit.basichistoryapi.activity.MainActivity;
import com.example.angel.myfit.basichistoryapi.model.Workout;
import com.example.angel.myfit.basichistoryapi.model.WorkoutReport;
import com.example.angel.myfit.basichistoryapi.model.WorkoutTypes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by chris.black on 5/2/15.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> implements View.OnClickListener {

    private List<Workout> items;
    private OnItemClickListener onItemClickListener;
    private String timeDesc = "오늘";
    private boolean animate = true;

    public RecyclerViewAdapter(List<Workout> items, Context context) {
        this.items = items;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * Do some fun animation for remove and add. If the data is fresh or some how smaller,
     * than just reset the data set normally.
     *
     * @param newItems
     * @param time
     */
    public void setItems(final List<Workout> newItems, final String time) {
        timeDesc = time;
        if(items.size() > 1 && newItems.size() > 1 && newItems.size() >= items.size()) {
            items.set(0, newItems.get(0));
            notifyItemChanged(0);
            for (int i = items.size() - 1; i > 0; i--) {
                if(i > lastPosition) {
                    items.remove(i);
                    notifyItemRemoved(i);
                }
            }
            for (int i = 1; i < newItems.size(); i++) {
                if(i > lastPosition) {
                    items.add(newItems.get(i));
                    notifyItemInserted(i);
                } else {
                    items.set(i, newItems.get(i));
                    notifyItemChanged(i);
                }
            }
        } else {
            items.clear();
            items.addAll(newItems);
            notifyDataSetChanged();
        }
    }

    public void setNeedsAnimate() {
        lastPosition = 0;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
        v.setOnClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Workout item;
        item = items.get(position);

        if(item.type == WorkoutTypes.TIME.getValue()) {
            holder.text.setText(timeDesc);
        } else {
            holder.text.setText(WorkoutTypes.getWorkOutTextById(item.type));
        }
        holder.image.setImageResource(WorkoutTypes.getImageById(item.type));
        holder.itemView.setTag(item);
        Bitmap bitmap = ((BitmapDrawable)holder.image.getDrawable()).getBitmap();
        Palette palette = Palette.generate(bitmap);
        int vibrant = palette.getVibrantColor(0x000000);
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(MainActivity.DATE_FORMAT);
        if(item.type == WorkoutTypes.TIME.getValue()) {
            holder.detail.setText("활동: " + WorkoutReport.getDurationBreakdown(item.duration));
        }else if(item.type == WorkoutTypes.WALKING.getValue()) {
            holder.detail.setText(item.stepCount + " steps");
        } else {
            holder.detail.setText(WorkoutReport.getDurationBreakdown(item.duration));
        }

        holder.image.setBackgroundColor(Utilities.lighter(vibrant, 0.4f));
        holder.container.setBackgroundColor(vibrant);
        setAnimation(holder.container, position);
    }

    private Context context;
    private int lastPosition = 0;

    /**
     * Here is the key method to apply the animation
     */
    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            animation.setStartOffset(100 * position);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override public void onClick(final View v) {
        // Give some time to the ripple to finish the effect
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(v, (Workout) v.getTag());
        }
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        /*
        이미지 및 보여줄 자료를 뷰에 잡아주도록 하는 클래스
        recycle클래스 이너 클래스
         */
        public ImageView image;
        public TextView text;
        public TextView detail;
        public LinearLayout container;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            text = (TextView) itemView.findViewById(R.id.text);
            detail = (TextView) itemView.findViewById(R.id.summary_text);
            container = (LinearLayout) itemView.findViewById(R.id.container);
        }
    }

    public interface OnItemClickListener {

        void onItemClick(View view, Workout viewModel);

    }
}