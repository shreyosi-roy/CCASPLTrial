package com.demo.ccaspltrial.Utility;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TextView;

import com.demo.ccaspltrial.R;

import java.util.ArrayList;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

    ArrayList<VideoModel> videoList;
    Context context;
    LayoutInflater inflater;

    public VideoAdapter(){}

    public VideoAdapter(Context context, ArrayList<VideoModel> list)
    {
        this.context=context;
        videoList=list;
        inflater=LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public VideoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view=inflater.inflate(R.layout.training_listitem, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoAdapter.ViewHolder holder, int position) {

        //setting title to video in item
        holder.video_title.setText(videoList.get(position).getVideoTitle());

        //setting url of video to item
        holder.webview.loadData(videoList.get(position).getVideoUrl(), "text/html", "UTF-8");
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {

        WebView webview;
        TextView video_title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            webview=(WebView)itemView.findViewById(R.id.video_webview);
            video_title=(TextView)itemView.findViewById(R.id.training_title);

            webview.getSettings().setJavaScriptEnabled(true);
            webview.setWebChromeClient(new WebChromeClient());
        }
    }
}
