package com.example.kmap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

public class TouristRoutePage extends AppCompatActivity {
    TouristRoute route = new TouristRoute();

    private ViewPager viewPager;
    private PagerAdapter mAdapter;
    private List<ImageButton> dots = new ArrayList<>();
    private void updateDots(int currentPage) {
        for (int i = 0; i < dots.size(); i++) {
            ImageButton dot = dots.get(i);
            if (i == currentPage) {
                dot.setImageResource(R.drawable.round_button3); // Изменяем изображение выбранной точки
            } else {
                dot.setImageResource(R.drawable.round_button2); // Изменяем изображение остальных точек
            }
        }
    }

    // Метод для обновления отображения точек
    private void addDotIndicator(LinearLayout dotsLayout, int position, Context context) {
        dotsLayout.removeAllViews(); // Очищаем dotsLayout перед добавлением новых точек

        for (int i = 0; i < dots.size(); i++) {
            ImageButton dot = new ImageButton(context);
            // Используем новый объект ImageButton для каждой точки
            dot.setLayoutParams(new LinearLayout.LayoutParams(50, 50)); // Увеличиваем размер точек
            dot.setPadding(10, 10, 10, 10);
            if (i == 0) {
                dot.setImageResource(R.drawable.round_button3);
            } else {
                dot.setImageResource(R.drawable.round_button2);
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            // Устанавливаем большие отступы между точками
            dotsLayout.addView(dot, params);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touristroute_page);

        route = getIntent().getParcelableExtra("route");
        TextView name = findViewById(R.id.TouristRouteName);
        name.setText(route.name);

        TextView info = findViewById(R.id.TouristRouteInfo);
        info.setText(route.info);

        viewPager = findViewById(R.id.ViewPager);
        PagerAdapter adapter = new RouteImageAdapter(this, route);
        viewPager.setAdapter(adapter);

        LinearLayout dotsLayout = findViewById(R.id.dotsLayout);
        for (int i = 0; i < adapter.getCount(); i++) {
            ImageButton dot = new ImageButton(this);
            dot.setLayoutParams(new LinearLayout.LayoutParams(50, 50)); // Увеличиваем размер точек
            dot.setPadding(10, 10, 10, 10);
            dot.setImageResource(R.drawable.round_button2);
            if (i==0){
                dot.setImageResource(R.drawable.round_button3);
            }
            dots.add(dot);

            final int position = i;
            dot.setOnClickListener(v -> {
                viewPager.setCurrentItem(position);
            });

            dotsLayout.addView(dot);
        }

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position); // Обновляем изображения точек
            }
        });
    }
    public void clickBuildRouteButton(View view){
        Intent intent = new Intent();
        intent.putExtra("route", route);
        setResult(-2, intent);
        finish();
    }

}
