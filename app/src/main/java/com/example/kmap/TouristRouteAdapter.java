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
import java.util.Iterator;
import java.util.List;

public class TouristRouteAdapter extends RecyclerView.Adapter<TouristRouteAdapter.TouristRouteViewHolder> {

    private List<TouristRoute> TouristRouteList;
    private LayoutInflater inflater;

    private ListOfTouristRoute context;

    public interface OnItemClickListener {
        void onItemClick(TouristRoute TouristRoute);
    }
    public TouristRouteAdapter(Context context) {
        this.context = (ListOfTouristRoute) context;
        this.inflater = LayoutInflater.from(this.context);
        this.TouristRouteList = new ArrayList<>();
        loadData();
    }

    @NonNull
    @Override
    public TouristRouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.touristroute_info, parent, false);
        return new TouristRouteViewHolder(view, new OnItemClickListener() {
            @Override
            public void onItemClick(TouristRoute route) {
                Intent intent = new Intent(context, TouristRoutePage.class);
                intent.putExtra("route", route);
                context.routeResultLauncher.launch(intent);
            }
        });
    }

    private void loadData() {
        FirebaseDatabase data_base;
        data_base = FirebaseDatabase.getInstance();
        String TOURISTROUTES = "TouristRoutes";
        Query sights = data_base.getReference(TOURISTROUTES); // создание адреса для обращения к базе данных
        sights.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                TouristRouteList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TouristRoute route = new TouristRoute();
                    Iterable<DataSnapshot> data = snapshot.getChildren();
                    Iterator<DataSnapshot> iterator = data.iterator();
                    route.setInfo(iterator.next().getValue(String.class));
                    route.setName(iterator.next().getValue(String.class));
                    data = iterator.next().getChildren();
                    iterator = data.iterator();
                    ArrayList<MapPoint> points = new ArrayList<>();
                    while(iterator.hasNext()){
                        MapPoint point;
                        point = iterator.next().getValue(MapPoint.class);
                        points.add(point);
                    }
                    route.setPoints(points);
                    TouristRouteList.add(route);
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("TouristRouteAdapter", "Ошибка загрузки данных: " + databaseError.getMessage());
            }
        });

    }

    @Override
    public void onBindViewHolder(@NonNull TouristRouteViewHolder holder, int position) {
        TouristRoute route = TouristRouteList.get(position);
        holder.TouristRoute = route;
        holder.name.setText(route.name);
        holder.info.setText(route.info);

    }

    @Override
    public int getItemCount() {
        return TouristRouteList.size();
    }

    public static class TouristRouteViewHolder extends RecyclerView.ViewHolder {
        TextView name, info;
        ImageButton button;
        ImageButton button2;
        OnItemClickListener listener;
        TouristRoute TouristRoute;

        public TouristRouteViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            this.listener = listener;
            name = itemView.findViewById(R.id.name);
            info = itemView.findViewById(R.id.info);
            button = itemView.findViewById(R.id.button);
            button2 = itemView.findViewById(R.id.showButton);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(TouristRoute);
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