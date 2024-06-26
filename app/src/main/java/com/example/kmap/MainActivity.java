package com.example.kmap;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.app.Dialog;

import android.content.DialogInterface;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.map.CameraListener;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CameraUpdateReason;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.PolylineMapObject;
import com.yandex.mapkit.map.TextStyle;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.geometry.Point;

import com.yandex.mapkit.render.internal.Size;
import com.yandex.mapkit.transport.TransportFactory;
import com.yandex.mapkit.transport.masstransit.PedestrianRouter;
import com.yandex.mapkit.transport.masstransit.Route;
import com.yandex.mapkit.transport.masstransit.Session;
import com.yandex.mapkit.transport.masstransit.Session.RouteListener;
import com.yandex.mapkit.transport.masstransit.TimeOptions;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.lang.String;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends FragmentActivity {
    private MapView mapView;
    private FirebaseDatabase data_base;
    static boolean[] selected_objects = {false, false, false, false};
    static boolean [] changing_selected_objects = {false, false, false, false};
    final private String SIGHTS = "Sights";

    PedestrianRouter router;
    private List<Route> routes = new ArrayList<>();

    boolean route_is_tracked;
    private MapObjectCollection routesCollection;
    private MapObjectCollection oldRoutesCollection;
    private ArrayList<PlacemarkMapObject> route_placemarks;

    boolean USER_LOCATION_ENABLED = false;
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 1;

    private Map<Integer, ArrayList<PlacemarkMapObject>> placemarks_types;
    private Map<PlacemarkMapObject, Sight> placemarks;

    private UserLocationLayer userLocationLayer;
    private ActivityResultLauncher<Intent> tapResultLauncher;

    private ActivityResultLauncher<Intent> menuResultLauncher;
    Session PedestrianSession;

    TouristRoute tracked_route;

    boolean building_route = false;

    private Handler handler = new Handler();
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            // Вызов функции userMove, которая обновляет UI
            userMove();
            // Повторное выполнение этого же Runnable через 1000 миллисекунд
            handler.postDelayed(this, 1000);
        }
    };


    public class LoadingRoute extends AsyncTask<Void, Integer, Void> {
        //объект для отображения состояния загрузки

        private ProgressBar mProgressBar;

        TouristRoute new_route;

        public LoadingRoute(ProgressBar progressBar, TouristRoute route) {
            mProgressBar = progressBar;
            this.new_route = route;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Выполняется загрузка маршрута", Toast.LENGTH_SHORT);
            toast.show();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(7000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mProgressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            mProgressBar.setVisibility(View.GONE);
            if(userLocationLayer == null){
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Отключено местоположение. Построение маршрута невозможно.", Toast.LENGTH_SHORT);
                toast.show();
            }
            initializeRoute(new_route);
        }
    }

    private CameraListener cameraListener = new CameraListener() {
        @Override
        public void onCameraPositionChanged(@NonNull com.yandex.mapkit.map.Map map, @NonNull CameraPosition cameraPosition, @NonNull CameraUpdateReason cameraUpdateReason, boolean b) {
            if (b) {
                float zoom = cameraPosition.getZoom();
                for(Map.Entry<PlacemarkMapObject, Sight> element : placemarks.entrySet()){
                    PlacemarkMapObject placemark = element.getKey();
                    changeStyle(placemark, zoom);
                }

            }
        }
    };

    private void changeStyle(PlacemarkMapObject placemark, float zoom){
        float maxTextSize = 14.0f;
        float minZoom = 10.0f;
        float zoomFactor = 0.8f;
        float baseScale = 1.0f;
        float newTextSize = Math.max(5f, maxTextSize - (zoomFactor * (zoom - minZoom)));
        float newScale = baseScale * (zoom / 20.0f);
        if (zoom < 15.5f) {
            placemark.setText("");
        } else {
            placemark.setText(placemarks.get(placemark).name);
        }
        IconStyle iconStyle = new IconStyle();
        TextStyle textStyle = new TextStyle();
        textStyle.setSize(newTextSize);
        textStyle.setPlacement(TextStyle.Placement.BOTTOM);
        iconStyle.setScale(newScale);
        placemark.setTextStyle(textStyle);
        placemark.setIconStyle(iconStyle);
    }


    private MapObjectTapListener mapObjectTapListener = new MapObjectTapListener() {
        //слушатель для обработки нажатия на метку
        @Override
        public boolean onMapObjectTap(MapObject mapObject, Point point) {
            if(building_route){
                PlacemarkMapObject placemark = (PlacemarkMapObject) mapObject;
                Sight sight = placemarks.get(placemark);
                for(int i = 0; i < route_placemarks.size(); i++){
                    if(route_placemarks.get(i) == placemark){
                        tracked_route.points.remove(i);
                        if(tracked_route.points.size() <= 1){
                            deleteRoute();
                            building_route = true;
                            createRoute(tracked_route);
                        }
                        else
                            rebuildRoute();
                        return true;
                    }
                }
                if(route_is_tracked){
                    deleteRoute();
                    building_route = true;
                }
                MapPoint sight_point = new MapPoint(sight.longitude, sight.latitude, sight.id);
                tracked_route.points.add(sight_point);
                createRoute(tracked_route);
                return true;
            }
            else{
                Sight sight = placemarks.get((PlacemarkMapObject) mapObject);
                if(sight.type == 0){
                    return true;
                }
                if(route_is_tracked)
                    handler.removeCallbacks(runnableCode);
                Intent intent = new Intent(MainActivity.this, SightPage.class);
                intent.putExtra("sight", sight);
                tapResultLauncher.launch(intent);
                return true;
            }
        }
    };
    private InputListener onMapTapListener = new InputListener() {
        @Override
        public void onMapTap(@NonNull com.yandex.mapkit.map.Map map, @NonNull Point point) {
            if(building_route){
                if(route_is_tracked){
                    deleteRoute();
                    building_route = true;
                }
                tracked_route.points.add(new MapPoint(point.getLongitude(), point.getLatitude(), "-1"));
                createRoute(tracked_route);
            }
        }

        @Override
        public void onMapLongTap(@NonNull com.yandex.mapkit.map.Map map, @NonNull Point point) {
        }
    };

    public static class ChoiceSight extends DialogFragment {
        //диалоговое окно выбора типа достопримечательности
        private OnDialogClickListener listener;
        public interface OnDialogClickListener {
            void onPositiveButtonClick();
            void onNegativeButtonClick();
        }

        public void setOnDialogClickListener(OnDialogClickListener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final String[] objects = {"Музеи", "Архитектура", "Памятники", "Парки"};
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            boolean [] currently_selected_objects = Arrays.copyOf(selected_objects, selected_objects.length);
            builder.setTitle("Отображаемые объекты")
                    .setMultiChoiceItems(objects, currently_selected_objects,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which, boolean isChecked) {
                                    currently_selected_objects[which] = isChecked;
                                    changing_selected_objects[which] = true;
                                }
                            })
                    .setPositiveButton("Готово",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    selected_objects = Arrays.copyOf(currently_selected_objects,
                                            currently_selected_objects.length);
                                    if (listener != null) {
                                        listener.onPositiveButtonClick();
                                    }
                                }
                            })

                    .setNegativeButton("Отмена",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    if (listener != null) {
                                        listener.onNegativeButtonClick();
                                    }
                                    dialog.cancel();
                                }
                            });

            return builder.create();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY);
        MapKitFactory.initialize(this);
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapview);
        createResultLaunchers();
        Point startLocation = new Point(51.739670, 36.193779);
        moveCameraToPosition(startLocation);
        data_base = FirebaseDatabase.getInstance();
        routesCollection = mapView.getMap().getMapObjects().addCollection();
        createPlacemarkCollection();

        mapView.getMap().addInputListener(onMapTapListener);
        mapView.getMap().addCameraListener(cameraListener);
    }

    void createResultLaunchers(){
        tapResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                Sight sight = (Sight) data.getParcelableExtra("sight");
                ArrayList<MapPoint> points = new ArrayList<>();
                points.add(new MapPoint(sight.longitude, sight.latitude, sight.id));
                TouristRoute route = new TouristRoute("", "", points);
                checkingGeolocationToBuildRoute(route);
            }
            else{
                if(route_is_tracked)
                    handler.postDelayed(runnableCode, 2000);
            }
        });
        menuResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                Sight sight = (Sight) data.getParcelableExtra("sight");
                ArrayList<MapPoint> points = new ArrayList<>();
                points.add(new MapPoint(sight.longitude, sight.latitude, sight.id));
                TouristRoute route = new TouristRoute("", "", points);
                checkingGeolocationToBuildRoute(route);
            }
            else if(result.getResultCode() == -2){
                Intent data = result.getData();
                TouristRoute route = (TouristRoute) data.getParcelableExtra("route");
                checkingGeolocationToBuildRoute(route);
            }
            else if(result.getResultCode() == -3){
                if(route_is_tracked)
                    deleteRoute();
                View b = findViewById(R.id.build_route_button);
                b.setVisibility(View.VISIBLE);
                View c = findViewById(R.id.delete_route_button);
                c.setVisibility(View.VISIBLE);
                building_route = true;
                tracked_route = new TouristRoute();
                route_placemarks = new ArrayList<>();
            }
            else{
                if(route_is_tracked)
                    handler.postDelayed(runnableCode, 2000);
            }
        });
    }

    private void checkingGeolocationToBuildRoute(TouristRoute route){
        if(USER_LOCATION_ENABLED){
            routesCollection.clear();
            ProgressBar progressBar = findViewById(R.id.progressBar);
            LoadingRoute loading = new LoadingRoute(progressBar, route);
            loading.execute();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Включите местоположение");
        builder.setMessage("Для построение маршрута необходимо включить местопложение");
        builder.setPositiveButton("Включить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeLocation();
                ProgressBar progressBar = findViewById(R.id.progressBar);
                LoadingRoute loading = new LoadingRoute(progressBar, route);
                loading.execute();
            }
        });
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    //функция формирования точек для построения маршрута
    private void initializeRoute(TouristRoute route){
        Point current_location = userLocationLayer.cameraPosition().getTarget();
        route.points.add(0, new MapPoint(current_location.getLongitude(),
        current_location.getLatitude(), ""));
        if(route_is_tracked)
            deleteRoute();
        tracked_route = route;
        createRoute(route);
        handler.postDelayed(runnableCode, 2000);
    }

    //функция включения слоя на карте с меткой пользователя
    private void showUserLocation(){
        userLocationLayer = MapKitFactory.getInstance().createUserLocationLayer(mapView.getMapWindow());
        userLocationLayer.setVisible(true);
        userLocationLayer.setObjectListener(new UserLocationObjectListener() {
            @Override
            public void onObjectAdded(@NonNull UserLocationView userLocationView) {

            }

            @Override
            public void onObjectRemoved(@NonNull UserLocationView userLocationView) {

            }

            @Override
            public void onObjectUpdated(@NonNull UserLocationView userLocationView, @NonNull ObjectEvent objectEvent) {
            }
        });
    }

    private void userMove(){
        if(route_is_tracked && !building_route){
            Point new_location = userLocationLayer.cameraPosition().getTarget();
            MapPoint user_location = new MapPoint(new_location.getLongitude(), new_location.getLatitude(), "");
            ArrayList<MapPoint> new_points = new ArrayList<>();
            new_points.add(user_location);
            for(int i = 1; i < tracked_route.points.size(); i++){
                MapPoint point = tracked_route.points.get(i);
                if(!pointPassed(point, user_location)){
                    new_points.add(point);
                }
            }
            tracked_route.setPoints(new_points);
            if(tracked_route.points.size() <= 1){
                deleteRoute();
            }
            else {
                rebuildRoute();
            }
        }
    }

    boolean pointPassed(MapPoint a, MapPoint b){
        return Math.abs(a.latitude - b.latitude) < 0.0004 && Math.abs(a.longitude - b.longitude) < 0.0004;
    }


    boolean pointsEquals(MapPoint a, MapPoint b){
        return Math.abs(a.latitude - b.latitude) < 0.00001 && Math.abs(a.longitude - b.longitude) < 0.00001;
    }

    // Функция для создания туристического маршрута

    void requestRoute(TouristRoute route){
        router = TransportFactory.getInstance().createPedestrianRouter(); // иниициализация роутера для построения пешеходного маршрута
        Session.RouteListener pedestrianRouteListener = new RouteListener() {
            @Override
            public void onMasstransitRoutes(@NonNull List<Route> received_routes) {
                routes = received_routes;
                onRoutesUpdated();
            }

            @Override
            public void onMasstransitRoutesError(@NonNull Error error) {
            }
        }; //добавление слушаетеля, который сохранит полученные маршруты
        List<RequestPoint> points_for_request = new ArrayList<>(); // создание списка точек в маршруте
        ArrayList<MapPoint> map_points = route.getPoints();
        for(int i = 0; i < map_points.size(); i++){
            Point point = new Point(map_points.get(i).getLatitude(), map_points.get(i).getLongitude());
            RequestPointType point_type;
            if(Objects.equals(map_points.get(i).getSight_id(), "")){
                point_type = RequestPointType.VIAPOINT;
            }
            else{
                point_type = RequestPointType.WAYPOINT;
            }
            points_for_request.add(new RequestPoint(point, point_type, null, null));
        }
        PedestrianSession = router.requestRoutes(points_for_request, new TimeOptions(), pedestrianRouteListener); //запрос маршрутов
    }
    private void createRoute(TouristRoute route){
        requestRoute(route);

        createRoutePlacemarks(route);
        route_is_tracked = true;

        View b = findViewById(R.id.delete_route_button);
        b.setVisibility(View.VISIBLE);

    }

    private void createRoutePlacemarks(TouristRoute route){
        FirebaseDatabase data_base;
        data_base = FirebaseDatabase.getInstance();
        String SIGHTS = "Sights";
        TextStyle style = new TextStyle();
        style.setPlacement(TextStyle.Placement.BOTTOM);
        for(int i = 0; i < route.points.size(); i++){
            MapPoint point = route.points.get(i);
            if(!point.sight_id.equals("")){
                if(point.sight_id.equals("-1")){
                    PlacemarkMapObject placemark = mapView.getMap().getMapObjects().addPlacemark();// добавление метки на карту
                    placemark.addTapListener(mapObjectTapListener);
                    placemarks_types.get(0).add(placemark);
                    placemark.setGeometry(new Point(point.latitude, point.longitude));
                    placemark.setText("", style);
                    Sight object = new Sight("-1", "", 0, point.getLatitude(), point.getLongitude(), "");
                    placemark.setIcon(ImageProvider.fromResource(getApplicationContext(),R.drawable.mark_location_orange));
                    placemarks.put(placemark, object);
                    route_placemarks.add(placemark);
                    changeStyle(placemark, mapView.getMap().getCameraPosition().getZoom());
                }
                else{
                    Query sights = data_base.getReference(SIGHTS + '/' + point.sight_id);
                    sights.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Sight object = dataSnapshot.getValue(Sight.class);
                            object.setId(dataSnapshot.getKey());
                            PlacemarkMapObject placemark = null;
                            if(selected_objects[object.type-1]){
                                for(PlacemarkMapObject placemark_on_map: placemarks_types.get(object.type)){
                                    if(Objects.equals(placemarks.get(placemark_on_map).id, object.id)){
                                        placemark = placemark_on_map;
                                        break;
                                    }
                                }
                                placemarks_types.get(object.type).remove(placemark);
                                route_placemarks.add(placemark);
                            }
                            else{
                                placemark = mapView.getMap().getMapObjects().addPlacemark();
                                placemark.addTapListener(mapObjectTapListener);
                                placemark.setGeometry(new Point(object.latitude, object.longitude));
                                placemark.setText(object.name, style);
                                placemark.setIcon(ImageProvider.fromResource(getApplicationContext(), getImageForPlacemark(object.type)));
                                placemarks.put(placemark, object);
                                changeStyle(placemark, mapView.getMap().getCameraPosition().getZoom());
                                route_placemarks.add(placemark);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("Загрузка достопримечательностей", "Ошибка загрузки данных: " + databaseError.getMessage());
                        }
                    });
                }
            }

        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                correctPlacemark();
            }
        }, 50);
    }

    void correctPlacemark(){
        if(route_placemarks.size() != tracked_route.points.size()){
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    correctPlacemark();
                }
            }, 50);
            return;
        }
        ArrayList<PlacemarkMapObject> new_route_placemark = new ArrayList<>();
        boolean changed = false;
        for(int i = 0; i < tracked_route.points.size(); i++){
            PlacemarkMapObject current_placemark = route_placemarks.get(i);
            MapPoint point = tracked_route.points.get(i);
            if(!changed && Objects.equals(placemarks.get(current_placemark).id, point.sight_id)){
                new_route_placemark.add(current_placemark);
            }
            else{
                changed = true;
                for(PlacemarkMapObject placemark: route_placemarks){
                    if(Objects.equals(placemarks.get(placemark).id, point.sight_id)){
                        new_route_placemark.add(placemark);
                        break;
                    }
                }
            }
        }
        if(changed){
            route_placemarks = new_route_placemark;
        }
    }

    void deleteRoutePlacemarks(){
        for(int i = 0; i < route_placemarks.size(); i++){
            PlacemarkMapObject placemark = route_placemarks.get(i);
            Sight sight = placemarks.get(placemark);
            if(sight.type != 0 && selected_objects[sight.type - 1]){
                placemarks_types.get(sight.type).add(placemark);
            }
            else{
                placemarks.remove(placemark);
                mapView.getMap().getMapObjects().remove(placemark);
            }
        }
    }

    void deleteRoute(){
        routesCollection.clear();
        PedestrianSession.cancel();
        routes = new ArrayList<>();
        deleteRoutePlacemarks();
        route_is_tracked = false;
        building_route = false;
        route_placemarks.clear();
        View b = findViewById(R.id.delete_route_button);
        b.setVisibility(View.INVISIBLE);
    }

    //функция,запрашивающая у пользователя разрешение на получение геолокации
    private void requestLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                "android.permission.ACCESS_FINE_LOCATION" )
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"},
                    PERMISSIONS_REQUEST_FINE_LOCATION);
        }
    }

    // функция для обработи объектов на карте
    private void objectProcessing(){
        boolean display_objects = false;
        for(int i = 0; i < changing_selected_objects.length; i++){
            if(changing_selected_objects[i]){
                if(selected_objects[i]){
                    display_objects = true;
                }
                else{
                    removePlacemarkCollection(i + 1); // удаление объектов выбранного типа
                }
            }
        }
        if(display_objects){
            showSights(); //отображение объектов на карте
        }
    }

    //функция удаления объектов с карты
    private void removePlacemarkCollection(int type_of_sight){
        ArrayList<PlacemarkMapObject> placemark_list = placemarks_types.get(type_of_sight);
        MapObjectCollection placemarks_in_map = mapView.getMap().getMapObjects();
        if(placemark_list.isEmpty())
            return;
        for(int i = 0; i < placemark_list.size(); i++){
            PlacemarkMapObject placemark = placemark_list.get(i);
            placemarks_in_map.remove(placemark); //удаление метки с карты
            placemarks.remove(placemark); //удаление достопримечательности из списка используемых на карте
        }
        placemarks_types.get(type_of_sight).clear();
    }

    private void createPlacemarkCollection(){
        placemarks_types = new HashMap<>();
        placemarks_types.put(0, new ArrayList<>());
        placemarks_types.put(1, new ArrayList<>());
        placemarks_types.put(2, new ArrayList<>());
        placemarks_types.put(3, new ArrayList<>());
        placemarks_types.put(4, new ArrayList<>());

        placemarks = new HashMap<>();
        route_placemarks = new ArrayList<>();
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

    //функция для получения иконки объекта на карте по его типу
    private int getImageForPlacemark(int type){
        int [] images = {R.drawable.mark_location_blue,
                R.drawable.mark_location_red,
                R.drawable.mark_location_silver,
                R.drawable.mark_location_green};
        return images[type - 1];
    }

    // Функция для получения и отображения достопримечательностей на карте
    private void showSights(){
        Query sights = data_base.getReference(SIGHTS).orderByChild( "Sights"); // создание адреса для обращения к базе данных
        sights.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Sight object = snapshot.getValue(Sight.class); //получние достопримечательности из базы данных
                    object.setId(snapshot.getKey());
                    TextStyle style = new TextStyle();
                    style.setPlacement(TextStyle.Placement.BOTTOM);
                    if(route_is_tracked){
                        boolean skip_object = false;
                        for(PlacemarkMapObject placemark: route_placemarks){
                            Sight route_sight = placemarks.get(placemark);
                            if(Objects.equals(route_sight.id, object.id)){
                                skip_object = true;
                                break;
                            }
                        }
                        if(skip_object){
                            continue;
                        }
                    }
                    if(changing_selected_objects[object.type - 1] && selected_objects[object.type - 1]){
                        PlacemarkMapObject placemark = mapView.getMap().getMapObjects().addPlacemark();// добавление метки на карту
                        placemark.addTapListener(mapObjectTapListener);
                        placemarks_types.get(object.type).add(placemark);
                        placemark.setGeometry(new Point(object.latitude, object.longitude));
                        placemark.setText(object.name, style);
                        placemark.setIcon(ImageProvider.fromResource(getApplicationContext(), getImageForPlacemark(object.type)));
                        placemarks.put(placemark, object);
                        changeStyle(placemark, mapView.getMap().getCameraPosition().getZoom());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    void removeUnnecessaryMarks(){
        int i = 0;
        int j = 0;
        if(!building_route)
            j++;
        if((route_placemarks.size() == tracked_route.points.size() && building_route)||
                (route_placemarks.size() == tracked_route.points.size() - 1 && !building_route))
            return;
        while(j < tracked_route.points.size() && i < route_placemarks.size()){
            PlacemarkMapObject placemark = route_placemarks.get(i);
            MapPoint point = new MapPoint(placemark.getGeometry().getLongitude(), placemark.getGeometry().getLatitude(), "-1");
            if(!pointsEquals(point, tracked_route.points.get(j))){
                Sight sight = placemarks.get(placemark);
                if(sight.type != 0 && selected_objects[sight.type - 1]){
                    placemarks_types.get(sight.type).add(placemark);
                }
                else{
                    placemarks.remove(placemark);
                    mapView.getMap().getMapObjects().remove(placemark);
                }
                route_placemarks.remove(i);
            }
            else{
                i++;
                j++;
            }
        }
        if((route_placemarks.size() != tracked_route.points.size()&& building_route) ||
                (route_placemarks.size() != tracked_route.points.size() - 1 && !building_route)){
            PlacemarkMapObject placemark = route_placemarks.get(route_placemarks.size() - 1);
            Sight sight = placemarks.get(placemark);
            if(sight.type != 0 && selected_objects[sight.type - 1]){
                placemarks_types.get(sight.type).add(placemark);
            }
            else{
                placemarks.remove(placemark);
                mapView.getMap().getMapObjects().remove(placemark);
            }
            route_placemarks.remove(route_placemarks.size() - 1);
        }
    }

    void requestRebuildRoute(TouristRoute route){
        router = TransportFactory.getInstance().createPedestrianRouter(); // иниициализация роутера для построения пешеходного маршрута
        Session.RouteListener pedestrianRouteListener = new RouteListener() {
            @Override
            public void onMasstransitRoutes(@NonNull List<Route> received_routes) {
                routes = received_routes;
                updateRebuildedRoute();
            }

            @Override
            public void onMasstransitRoutesError(@NonNull Error error) {
            }
        }; //добавление слушаетеля, который сохранит полученные маршруты
        List<RequestPoint> points_for_request = new ArrayList<>(); // создание списка точек в маршруте
        ArrayList<MapPoint> map_points = route.getPoints();
        for(int i = 0; i < map_points.size(); i++){
            Point point = new Point(map_points.get(i).getLatitude(), map_points.get(i).getLongitude());
            RequestPointType point_type;
            if(Objects.equals(map_points.get(i).getSight_id(), "")){
                point_type = RequestPointType.VIAPOINT;
            }
            else{
                point_type = RequestPointType.WAYPOINT;
            }
            points_for_request.add(new RequestPoint(point, point_type, null, null));
        }
        PedestrianSession = router.requestRoutes(points_for_request, new TimeOptions(), pedestrianRouteListener); //запрос маршрутов
    }

    void updateRebuildedRoute(){
        oldRoutesCollection = routesCollection;
        MapObjectCollection new_routesCollection = mapView.getMap().getMapObjects().addCollection();
        if (routes.isEmpty()) {
            return;
        }

        for (int i = 0; i < routes.size(); i++) {
            Route route = routes.get(i);
            PolylineMapObject polyline = new_routesCollection.addPolyline(route.getGeometry());
            if (i == 0) {
                styleMainRoute(polyline);
            } else {
                styleAlternativeRoute(polyline);
            }
        }
        routesCollection = new_routesCollection;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                deleteOldRoute();
            }
        }, 100);
    }

    private void deleteOldRoute(){
        oldRoutesCollection.clear();
    }

    void rebuildRoute(){
        removeUnnecessaryMarks();
        requestRebuildRoute(tracked_route);
        route_is_tracked = true;
    }



    //функция для включения и выключения геолокации
    private void changeLocation(){
        if(USER_LOCATION_ENABLED){
            userLocationLayer.setVisible(false);
            USER_LOCATION_ENABLED = false;
            routesCollection.clear();
        }
        else{
            if(ContextCompat.checkSelfPermission(this,
                    "android.permission.ACCESS_FINE_LOCATION" )
                    == PackageManager.PERMISSION_GRANTED){
                if(userLocationLayer != null){
                    userLocationLayer.setVisible(true);
                }
                else{
                    showUserLocation();
                }
                USER_LOCATION_ENABLED = true;
            }
            else{
                requestLocationPermission();
                if(ContextCompat.checkSelfPermission(this,
                        "android.permission.ACCESS_FINE_LOCATION" )
                        == PackageManager.PERMISSION_GRANTED){
                    showUserLocation();
                    USER_LOCATION_ENABLED = true;
                }
                else{
                    Toast.makeText(this, "Невозможно получить доступ к геолокации",  Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void clickLocationButton(View view){
        changeLocation();
    }

    public void clickDeleteRouteButton(View view){
        if(building_route){
            View c= findViewById(R.id.build_route_button);
            c.setVisibility(View.GONE);
            building_route = false;
        }
        else{
            handler.removeCallbacks(runnableCode);
        }
        if(tracked_route.points.size() != 0){
            deleteRoute();
        }
        View b = findViewById(R.id.delete_route_button);
        b.setVisibility(View.GONE);
    }
    public void clickBuildRouteButton(View view){
        if(tracked_route.points.size() == 0){
            Toast.makeText(getApplicationContext(), "Недостаточно точек", Toast.LENGTH_SHORT).show();
            return;
        }
        deleteRoute();
        checkingGeolocationToBuildRoute(tracked_route);
        View c = findViewById(R.id.build_route_button);
        c.setVisibility(View.GONE);
        building_route = false;
    }

    public void clickAttractionsButton(View view){
        ChoiceSight dialogFragment = new ChoiceSight();
        Arrays.fill(changing_selected_objects, false);
        dialogFragment.setOnDialogClickListener(new ChoiceSight.OnDialogClickListener() {
            @Override
            public void onPositiveButtonClick() {
                objectProcessing();
            }

            @Override
            public void onNegativeButtonClick() {
                ;
            }
        });
        dialogFragment.show(getSupportFragmentManager(), "");
    }

    public void clickMenuButton(View view){
        if(route_is_tracked)
            handler.removeCallbacks(runnableCode);
        Intent intent = new Intent(MainActivity.this, Menu.class);
        menuResultLauncher.launch(intent);
    }

    public void clickRoutesButton(View view){
        if(!building_route){
            if(route_is_tracked)
                deleteRoute();
            View b = findViewById(R.id.build_route_button);
            b.setVisibility(View.VISIBLE);
            View c = findViewById(R.id.delete_route_button);
            c.setVisibility(View.VISIBLE);
            building_route = true;
            tracked_route = new TouristRoute();
            route_placemarks = new ArrayList<>();
        }
    }

    //функция перемещения камеры на заданную позицию
    private void moveCameraToPosition(@NonNull Point target) {
        mapView.getMap().move(
                new CameraPosition(target, 15.0f, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 2), null);
    }

    //функция для вывода маршрута на карту
    private void onRoutesUpdated() {
        routesCollection.clear();
        if (routes.isEmpty()) {
            return;
        }

        for (int i = 0; i < routes.size(); i++) {
            Route route = routes.get(i);
            PolylineMapObject polyline = routesCollection.addPolyline(route.getGeometry());
            if (i == 0) {
                styleMainRoute(polyline);
            } else {
                styleAlternativeRoute(polyline);
            }
        }
    }

    //функция для задания внешнего вида основного маршрута
    private void styleMainRoute(PolylineMapObject polyline) {
        polyline.setZIndex(10f);
        polyline.setStrokeColor(ContextCompat.getColor(this, R.color.button_color));
        polyline.setStrokeWidth(5f);
        polyline.setOutlineColor(ContextCompat.getColor(this,  R.color.button_color));
        polyline.setOutlineWidth(3f);
    }

    //функция для задания внешнего вида дополнительных маршрутов
    private void styleAlternativeRoute(PolylineMapObject polyline) {
        polyline.setZIndex(5f);
        polyline.setStrokeColor(ContextCompat.getColor(this, R.color.black));
        polyline.setStrokeWidth(4f);
        polyline.setOutlineColor(ContextCompat.getColor(this, R.color.black));
        polyline.setOutlineWidth(2f);
    }
}