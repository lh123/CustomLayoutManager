package com.example.liuhui.customlayoutmanager;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.liuhui.customlayoutmanager.layoutmanager.HiveLayoutManager;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Random mRandom = new Random();
    private Handler mHandler;

    private RecyclerView mRecyclerView;
    private Adapter mAdapter = new Adapter();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        init();
    }

    private void init() {
        mRecyclerView.setLayoutManager(new HiveLayoutManager());
        mRecyclerView.setAdapter(mAdapter);
        mHandler = new Handler();
        getImg(7);
    }

    public void add(View view) {
        getImg(1);
    }

    private void getImg(int count){
        for (int i = 0;i<count;i++){
            ThreadPool.getInstance().execute(new ImgRunnable());
        }
    }

    public void remove(View view) {
        if (mAdapter.getItemCount() <= 0){
            return;
        }
        int removePosition = mRandom.nextInt(mAdapter.getItemCount());
        mAdapter.removeImg(removePosition);
        mAdapter.notifyItemRemoved(removePosition);
    }

    class ImgRunnable implements Runnable{
        @Override
        public void run() {
            try {
                URL url = new URL("http://tu.ihuan.me/tu/api/me_all_story_json/");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                final ImgBean img = new Gson().fromJson(reader, ImgBean.class);
                reader.close();
                inputStream.close();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        int insertPosition = 0;
                        if (mAdapter.getItemCount() > 0) {
                            insertPosition = mRandom.nextInt(mAdapter.getItemCount());
                        }
                        mAdapter.addImg(insertPosition, img);
                        mAdapter.notifyItemInserted(insertPosition);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),"error",Toast.LENGTH_SHORT).show();
            }
        }
    }

    class Adapter extends RecyclerView.Adapter<Adapter.Holder> {

        private List<ImgBean> mImgs = new ArrayList<>();

        private void addImg(int position,ImgBean imgBean){
            mImgs.add(position,imgBean);
        }

        private void removeImg(int position){
            mImgs.remove(position);
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item, parent, false);
            return new Holder(item);
        }

        @Override
        public void onBindViewHolder(final Holder holder, final int position) {
            Glide.with(MainActivity.this).load(mImgs.get(position).getLink()).centerCrop().transform(new HiveTransformer(getApplicationContext())).into(holder.img);
        }

        @Override
        public int getItemCount() {
            return mImgs.size();
        }

        class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView img;

            Holder(View itemView) {
                super(itemView);
                img = (ImageView) itemView.findViewById(R.id.text);
                img.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                DetailActivity.startActivity(MainActivity.this,mImgs.get(getAdapterPosition()));
            }
        }
    }
}
