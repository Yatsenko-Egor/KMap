package com.example.kmap;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.core.view.View;

public class ListofSights extends AppCompatActivity {

    private RecyclerView recyclerView;
    int type_code;
    SightAdapter adapter;
    DatabaseReference mbase;

    public ActivityResultLauncher<Intent> sightResultLauncher;
    public interface RecyclerViewClickListener {
        public void recyclerViewListClicked(View v, int position);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        type_code = getIntent().getIntExtra("type", 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listof_sights);

        mbase
                = FirebaseDatabase.getInstance().getReference("Sights");

        recyclerView = findViewById(R.id.recycler1);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this));

        FirebaseRecyclerOptions<Sight> options
                = new FirebaseRecyclerOptions.Builder<Sight>()
                .setQuery(mbase, Sight.class)
                .build();
        adapter = new SightAdapter(this, type_code);
        recyclerView.setAdapter(adapter);
        createResultLaunchers();
    }
    void createResultLaunchers(){
        sightResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }

    @Override protected void onStart()
    {
        super.onStart();
    }
    @Override protected void onStop()
    {
        super.onStop();
    }
}