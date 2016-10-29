package com.example.liuhui.customlayoutmanager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by liuhui on 2016/10/29.
 */

public class DetailActivity extends AppCompatActivity {

    private static final String EXTRA_DATA = "img";

    private PhotoView mPhotoView;
    private ProgressBar mLoadingView;
    private TextView mTitle;
    private TextView mDes;
    private ViewGroup mContainer;

    private boolean mIsShow;

    public static void startActivity(Context context,ImgBean imgBean){
        Intent intent = new Intent(context,DetailActivity.class);
        intent.putExtra(EXTRA_DATA,imgBean);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);
        mPhotoView = (PhotoView) findViewById(R.id.photo_view);
        mLoadingView = (ProgressBar) findViewById(R.id.loading);
        mContainer = (ViewGroup) findViewById(R.id.container);
        mTitle = (TextView) findViewById(R.id.title);
        mDes = (TextView) findViewById(R.id.describe);
        ImgBean bean = getIntent().getParcelableExtra(EXTRA_DATA);
        mTitle.setText(bean.getTitle());
        mDes.setText(bean.getDescribe());
        mLoadingView.setVisibility(View.VISIBLE);
        mIsShow = true;
        Glide.with(this)
                .load(bean.getLink())
                .override(Target.SIZE_ORIGINAL,Target.SIZE_ORIGINAL)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        Toast.makeText(getApplicationContext(),"error",Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        mLoadingView.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(mPhotoView);
        mPhotoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                showOrHide();
            }

            @Override
            public void onOutsidePhotoTap() {
                showOrHide();
            }
        });

    }

    private void showOrHide(){
        if (mIsShow) {
            mIsShow = false;
            mContainer.animate().translationY(mContainer.getHeight());
        }else {
            mIsShow = true;
            mContainer.animate().translationY(0);
        }
    }
}
