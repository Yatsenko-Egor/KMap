package com.example.kmap;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class RouteImageAdapter extends PagerAdapter {
    private Context context;
    private int mImageCount;
    private StorageReference storageRef;

    private TouristRoute route;
    public RouteImageAdapter(Context context, TouristRoute route) {
        this.route = route;
        this.context = context;
        this.storageRef = FirebaseStorage.getInstance().getReference();
        mImageCount = route.points.size();
    }

    @Override
    public int getCount() {
        return mImageCount;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }
    @Override
    public Object instantiateItem( ViewGroup container, int position) {
        ImageView imageView = new ImageView(context);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(layoutParams);
        container.addView(imageView);
        StorageReference imageRef = storageRef.child(route.points.get(position).sight_id);
        imageRef.listAll().addOnSuccessListener(listResult -> {
            List<StorageReference> items = listResult.getItems();

            if (items.size() > 0) {
                StorageReference firstObject = items.get(0);
                firstObject.getDownloadUrl().addOnSuccessListener(uri -> {
                    Glide.with(context)
                            .load(uri)
                            .into(imageView);
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });


        return imageView;
    }


    @Override
    public void destroyItem( ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
    }
}
