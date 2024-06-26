package com.example.kmap;

import android.content.Context;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ImageAdapter extends PagerAdapter {
    private Context mContext;
    private int mImageCount;
    private StorageReference mStorageRef;

    private Sight sight;
    public ImageAdapter(Context context, Sight sight) {
        mContext = context;
        this.sight = sight;
        FirebaseStorage storage = FirebaseStorage.getInstance();
        mStorageRef = storage.getReference().child(sight.id);
        mStorageRef.listAll().addOnSuccessListener(listResult -> {
            mImageCount = listResult.getItems().size();
            notifyDataSetChanged();
        }).addOnFailureListener(e -> {
        });

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
    public Object instantiateItem(ViewGroup container, int position) {
        //функция получения картинки из базы данных и внесения изображения в ViewGroup
        ImageView imageView = new ImageView(mContext);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(layoutParams);
        container.addView(imageView);

        mStorageRef.listAll().addOnSuccessListener(listResult -> {
            if (position < listResult.getItems().size()) {
                StorageReference imageRef = listResult.getItems().get(position);
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Glide.with(mContext)
                            .load(uri)
                            .into(imageView);
                });
            }
        }).addOnFailureListener(e -> {
        });

        return imageView;
    }

    @Override
    public void destroyItem( ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
    }
}