package com.example.kmap;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Menu extends AppCompatActivity {

    private ActivityResultLauncher<Intent> sightResultLauncher;

    private ActivityResultLauncher<Intent> routeResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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
        routeResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == -2) {
                Intent data = result.getData();
                setResult(-2, data);
                finish();
            }
        });
    }
    public void ClickButton4(View view){
       finish();
    }

    public void ClickButton2(View view){
        Intent intent = new Intent(Menu.this,ListOfTypesSights.class);
        sightResultLauncher.launch(intent);
    }

    public void ClickButton3(View view){
        Intent intent = new Intent(Menu.this, ListOfTouristRoute.class);
        routeResultLauncher.launch(intent);
    }
    public void clickBuildRouteButton(View view){
        setResult(-3);
        finish();
    }
}
