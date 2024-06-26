package com.example.kmap;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ListOfTypesSights extends AppCompatActivity {
    private ActivityResultLauncher<Intent> sightResultLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_types_sights);
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

    public void typeSelection(View view) {
        Button button1 = findViewById(R.id.button_TypeSights);
        Button button2 = findViewById(R.id.button2_TypeSights);
        Button button3 = findViewById(R.id.button3_TypeSights);
        Button button4 = findViewById(R.id.button4_TypeSights);
        int type_code = 0;
        if (view == button1) {type_code = 1;}
        else if (view == button2) {type_code =3;}
        else if (view == button3) {type_code =2;}
        else if (view == button4) {type_code =4;}
        Intent intent = new Intent(ListOfTypesSights.this, ListofSights.class);
        intent.putExtra("type", type_code);
        sightResultLauncher.launch(intent);
    }
}