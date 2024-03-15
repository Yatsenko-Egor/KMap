package com.example.kmap;
import static android.app.PendingIntent.getActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.FragmentManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.firebase.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.yandex.mapkit.MapKitFactory;

import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.geometry.Point;
import com.yandex.runtime.image.ImageProvider;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.lang.String;
public class MainActivity extends FragmentActivity  {
    private MapView mapView;
    private DatabaseReference data_base;
    static boolean[] selected_objects = {false, false, false, false};
    final private String SIGHTS = "Sights";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY);
        MapKitFactory.initialize(this);
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapview);
        Point startLocation = new Point(51.739670, 36.193779);
        float zoomValue = 10.f;
        mapView.getMap().move(new CameraPosition(startLocation, zoomValue, 0.00f, 0.00f));

        data_base = FirebaseDatabase.getInstance().getReference(SIGHTS);
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    private void moveToStartLocation() {
        Point startLocation = new Point(51.739670, 36.193779);
        float zoomValue = 10.f;
        mapView.getMap().move(new CameraPosition(startLocation, zoomValue, 0.00f, 0.00f));
    }

    private void showSights(){
        Query sights = data_base.orderByChild( "Sights");
        sights.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Sight object = snapshot.getValue(Sight.class);
                    if(selected_objects[object.type - 1]){
                        Log.e("TAG", "1" );
                        MapObjectCollection mapObjects = mapView.getMap().getMapObjects();
                        PlacemarkMapObject placemark = mapObjects.addPlacemark();
                        placemark.setGeometry(new Point(51.739670, 36.193779));
                        placemark.setIcon(ImageProvider.fromResource(getApplicationContext(), R.drawable.mark_location_black_foreground));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибок при получении данных
            }
        });
    }

    public void clickAttractionsButton(View view){
        MyDialogFragment dialogFragment = new MyDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "MyDialogFragment");
        showSights();
    }

    public static class MyDialogFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final String[] objects = {"Музеи", "Архитектура", "Памятники", "Парки"};
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Отображаемые объекты")
                    .setMultiChoiceItems(objects, selected_objects,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which, boolean isChecked) {
                                    selected_objects[which] = isChecked;
                                }
                            })
                    .setPositiveButton("Готово",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            })

                    .setNegativeButton("Отмена",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    dialog.cancel();
                                }
                            });

            return builder.create();
        }

    }
}

