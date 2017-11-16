package com.bignerdranch.android.photogallery;

import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aiska on 11/7/17.
 */

public class PhotoGalleryFragment extends Fragment {
    private int standardColumns = 3;

    private static final String TAG = "PhotoGalleryFragment";
    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private int lastFetchedPage = 1;

    public static PhotoGalleryFragment newInstance(){
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemsTask().execute();

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_photo_gallery,container,false);
        mPhotoRecyclerView = (RecyclerView)v.findViewById(R.id.fragment_photo_gallery_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),standardColumns));
        mPhotoRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                DisplayMetrics metrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

                int widthPixels = metrics.widthPixels;
                int heightPixels = metrics.heightPixels;
                GridLayoutManager layoutManager = (GridLayoutManager)mPhotoRecyclerView.getLayoutManager();

                float scaleFactor = metrics.density;
                float widthDp = widthPixels/scaleFactor;
                float heightDp = heightPixels/scaleFactor;
                Log.i(TAG, "widthDp :" +widthDp +"height dp: " + heightDp );

                if ( widthDp > heightDp ){
                    layoutManager.setSpanCount(5);
                }

                mPhotoRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState){
                PhotoAdapter adapter = (PhotoAdapter) recyclerView.getAdapter();
                int lastPosition = adapter.getLastBoundPosition();
                GridLayoutManager layoutManager = (GridLayoutManager)recyclerView.getLayoutManager();
                int loadBufferPosition = 1;
                if (lastPosition >= adapter.getItemCount() - layoutManager.getSpanCount() - loadBufferPosition){
                    new FetchItemsTask().execute(lastPosition + 1);
                }
            }
            @Override
            public void onScrolled(RecyclerView recyclerView,int dx, int dy){
                super.onScrolled(recyclerView,dx,dy);
                /*if (!recyclerView.canScrollVertically(dy)){
                    if (dy>0){
                        PhotoAdapter adapter = (PhotoAdapter)recyclerView.getAdapter();
                        int lastPosition = adapter.getLastBoundPosition();
                        new FetchItemsTask().execute(lastPosition + 1);
                    }
                }*/
            }
        });
        setupAdapter();
        return v;


    }

    private void setupAdapter() {
        if (isAdded()){
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private ImageView mItemImageView;

        public PhotoHolder(View itemView) {
            super(itemView);
            mItemImageView = (ImageView) itemView.findViewById(R.id.item_image_view);
        }

        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }
    }
        private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>{
            private List<GalleryItem> mGalleryItems;
            private int lastBoundPosition;
            public int getLastBoundPosition(){
                return lastBoundPosition;
            }
            public PhotoAdapter(List<GalleryItem> galleryItems){
                mGalleryItems = galleryItems;
            }
            @Override
            public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                View view = inflater.inflate(R.layout.list_item_gallery,viewGroup,false);
                return new PhotoHolder(view);
            }
            @Override
            public void onBindViewHolder(PhotoHolder photoHolder, int position){
                GalleryItem galleryItem = mGalleryItems.get(position);
                Drawable placeholder = getResources().getDrawable(R.drawable.images);
                photoHolder.bindDrawable(placeholder);
                lastBoundPosition = position;
            }
            @Override
            public int getItemCount(){
                return mGalleryItems.size();
            }
        }


    private class FetchItemsTask extends AsyncTask<Integer,Void,List<GalleryItem>>{
        @Override
        protected List<GalleryItem> doInBackground(Integer...params){
             return new FlickrFetchr().fetchItems(lastFetchedPage);


        }
        @Override
        protected void onPostExecute(List<GalleryItem> items){
            if (lastFetchedPage > 1){
                mItems.addAll(items);
                mPhotoRecyclerView.getAdapter().notifyDataSetChanged();
            }
            else {
                mItems = items;
                setupAdapter();
            }
            lastFetchedPage++;
        }

    }
}
