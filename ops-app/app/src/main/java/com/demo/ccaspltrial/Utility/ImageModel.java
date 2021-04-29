package com.demo.ccaspltrial.Utility;

import android.graphics.Bitmap;

public class ImageModel {

    public int imageId;
    public String imageURL;
    public Bitmap imageBitmap;
    public String timeStamp;

    public ImageModel(){}

    public void setImageId(int imageId)
    {
        this.imageId=imageId;
    }

    public int getImageId()
    {
        return imageId;
    }

    public void setImageURL(String imageURL)
    {
        this.imageURL=imageURL;
    }

    public String getImageURL()
    {
        return imageURL;
    }

    public void setTimeStamp(String timeStamp)
    {
        this.timeStamp=timeStamp;
    }

    public String getTimeStamp()
    {
        return timeStamp;
    }

    public void setImageBitmap(Bitmap imageBitmap)
    {
        this.imageBitmap=imageBitmap;
    }

    public Bitmap getImageBitmap()
    {
        return imageBitmap;
    }
}
