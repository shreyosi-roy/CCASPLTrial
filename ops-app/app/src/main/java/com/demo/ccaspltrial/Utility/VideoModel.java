package com.demo.ccaspltrial.Utility;

public class VideoModel {

    public String video_title;
    public String video_url;

    public VideoModel(){}

    public VideoModel(String title, String url)
    {
        video_title=title;
        video_url=url;
    }

    public void setVideoTitle(String title)
    {
        video_title=title;
    }

    public void setVideoUrl(String url)
    {
        video_url=url;
    }

    public String getVideoTitle()
    {
        return video_title;
    }

    public String getVideoUrl()
    {
        return video_url;
    }
}
