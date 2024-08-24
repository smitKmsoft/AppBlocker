package com.km.appblocker;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {

    Activity activity;
    ArrayList<AppInfo> installedAppList;

    public MainAdapter(Activity mainActivity, ArrayList<AppInfo> installedAppList) {
        this.activity = mainActivity;
        this.installedAppList = installedAppList;
    }

    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item,parent,false);
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {

        AppInfo appInfo = installedAppList.get(position);

        holder.appName.setText(appInfo.appName);

        if (appInfo.isBlock) {
            holder.isBlock.setChecked(true);
        } else {
            holder.isBlock.setChecked(false);
        }

        holder.isBlock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ((MainActivity) activity).updateList(appInfo.packageName,true); // Implement blockApp method
                } else {
                    ((MainActivity) activity).updateList(appInfo.packageName,false); // Implement unblockApp method
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return installedAppList.size();
    }

    public class MainViewHolder extends RecyclerView.ViewHolder {
        ImageView appLogo;
        TextView appName;
        ToggleButton isBlock;

        public MainViewHolder(@NonNull View itemView) {
            super(itemView);

            appLogo = itemView.findViewById(R.id.appLogo);
            appName = itemView.findViewById(R.id.appName);
            isBlock = itemView.findViewById(R.id.isBlock);
        }
    }
}
