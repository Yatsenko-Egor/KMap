package com.example.kmap;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

public class ListOfTouristRoute extends AppCompatActivity {

    private RecyclerView recyclerView;
    TouristRouteAdapter adapter;

    public ActivityResultLauncher<Intent> routeResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listof_touristroute);
        createResultLaunchers();
        recyclerView = findViewById(R.id.recycler1);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TouristRouteAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    void createResultLaunchers(){
        routeResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == -2) {
                Intent data = result.getData();
                setResult(-2, data);
                finish();
            }
        });
    }

    // Function to tell the app to start getting
    // data from database on starting of the activity
    @Override protected void onStart()
    {
        super.onStart();
    }

    // Function to tell the app to stop getting
    // data from database on stopping of the activity
    @Override protected void onStop()
    {
        super.onStop();
    }
}