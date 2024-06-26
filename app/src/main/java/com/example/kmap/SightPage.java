package com.example.kmap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.widget.ImageButton;
import android.widget.ImageView;
public class SightPage extends AppCompatActivity {
    Sight sight;
    private ImageButton dot1, dot2; // Объявляем переменные для точек
    private ViewPager viewPager;



    // Метод для обновления отображения точек
    private void updateDots(int currentPage) {
        switch (currentPage) {
            case 0:
                dot1.setImageResource(R.drawable.round_button3);
                dot2.setImageResource(R.drawable.round_button2);

                break;
            case 1:
                dot1.setImageResource(R.drawable.round_button2);
                dot2.setImageResource(R.drawable.round_button3);

                break;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sight_page);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);

        viewPager = findViewById(R.id.ViewPager);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        sight = (Sight) getIntent().getParcelableExtra("sight");

        TextView name = this.findViewById(R.id.SightName);
        name.setText(sight.name);

        TextView info = this.findViewById(R.id.SightInfo);
        info.setText(sight.info);

        ViewPager viewPager = findViewById(R.id.ViewPager);

        PagerAdapter adapter = new ImageAdapter(this, sight);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                // Обновляем отображение точек в зависимости от текущей страницы
                updateDots(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }
    public void clickBuildRouteButton(View view){
        Intent intent = new Intent();
        intent.putExtra("sight", sight);
        setResult(RESULT_OK, intent);
        finish();
    }

}