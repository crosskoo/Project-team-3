package com.jeyun.rhdms.adapter.pill;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jeyun.rhdms.PillUpdateActivity;
import com.jeyun.rhdms.adapter.wrapper.PillInfo;
import com.jeyun.rhdms.databinding.ItemPillInfoBinding;
import java.util.List;

public class PillInfoAdapter extends RecyclerView.Adapter<PillInfoAdapter.ItemViewHolder>
{

    private List<PillInfo> infoList;
    public PillInfoAdapter(List<PillInfo> itemList)
    {
        this.infoList = itemList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        ItemPillInfoBinding binding = ItemPillInfoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position)
    {
        PillInfo item = infoList.get(position);
        ItemPillInfoBinding binding = holder.binding;
        binding.pillDate.setText(item.date);
        binding.pillTake.setText(item.status);

        holder.itemView.setOnClickListener(v ->
        {
            Context context = v.getContext();
            Intent intent_switch = getIntentSwitch(context, item);
            context.startActivity(intent_switch);
        });
    }

    public static @NonNull Intent getIntentSwitch(Context context, PillInfo item)
    {
        Intent intent_switch = new Intent(context, PillUpdateActivity.class);
        intent_switch.putExtra("SUBJECT_ID", "1076");
        intent_switch.putExtra("pill", item.pill);
        intent_switch.putExtra("ARM_DT", item.date);
        intent_switch.putExtra("TAKEN_ST", item.raw_status);
        intent_switch.putExtra("ARM_ST_TM", item.start_time);
        intent_switch.putExtra("ARM_ED_TM", item.end_time);
        intent_switch.putExtra("TAKEN_TM", item.taken_time);
        intent_switch.putExtra("ARM_TP", item.time_type);
        return intent_switch;
    }

    @Override
    public int getItemCount()
    {
        return infoList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setInfoList(List<PillInfo> infoList)
    {
        this.infoList = infoList;
        notifyDataSetChanged();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder
    {
        private final ItemPillInfoBinding binding;

        public ItemViewHolder(ItemPillInfoBinding binding)
        {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
