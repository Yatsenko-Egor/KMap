package com.example.kmap;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.TextStyle;
import com.yandex.runtime.image.ImageProvider;

import java.util.ArrayList;
import java.util.List;

public class SightAdapter extends RecyclerView.Adapter<SightAdapter.SightViewHolder> {

    private List<Sight> sightList;
    private LayoutInflater inflater;
    private int type_of_sight;

    public interface OnItemClickListener {
        void onItemClick(Sight sight);
    }

    private ListofSights context;
    public SightAdapter(Context context, int type_of_sight) {
        this.context = (ListofSights) context;
        this.inflater = LayoutInflater.from(context);
        this.sightList = new ArrayList<>();
        this.type_of_sight = type_of_sight;
        loadData();
    }

    @NonNull
    @Override
    public SightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sight_info, parent, false);
        return new SightViewHolder(view, new OnItemClickListener() {
            @Override
            public void onItemClick(Sight sight) {
                Intent intent = new Intent(context, SightPage.class);
                intent.putExtra("sight", sight);
                context.sightResultLauncher.launch(intent);
            }
        });
    }

    private void loadData() {
        FirebaseDatabase data_base;
        data_base = FirebaseDatabase.getInstance();
        String SIGHTS = "Sights";
        Query sights = data_base.getReference(SIGHTS).orderByChild( "Sights"); // создание адреса для обращения к базе данных
        sights.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sightList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Sight object = snapshot.getValue(Sight.class); //получние достопримечательности из базы данных
                    object.setId(snapshot.getKey());
                    if(object.type == type_of_sight)
                        sightList.add(object);
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("SightAdapter", "Ошибка загрузки данных: " + databaseError.getMessage());
            }
        });

    }

    @Override
    public void onBindViewHolder(@NonNull SightViewHolder holder, int position) {
        Sight sight = sightList.get(position);
        holder.sight = sight;
        holder.name.setText(sight.name);
        holder.info.setText(sight.info);

    }

    @Override
    public int getItemCount() {
        return sightList.size();
    }

    public static class SightViewHolder extends RecyclerView.ViewHolder {
        TextView name, info;
        ImageButton button;
        ImageButton button2;
        OnItemClickListener listener;
        Sight sight;

        public SightViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            this.listener = listener;
            name = itemView.findViewById(R.id.name);
            info = itemView.findViewById(R.id.info);
            button = itemView.findViewById(R.id.button);
            button2 = itemView.findViewById(R.id.showButton);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Handle button click to notify the listener
                    listener.onItemClick(sight);
                }
            });
            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (info.getMaxLines() == 5) {
                        info.setMaxLines(Integer.MAX_VALUE); // Show the entire text
                    } else {
                        info.setMaxLines(5); // Show a limited number of lines
                    }
                }
            });
        }
    }
}