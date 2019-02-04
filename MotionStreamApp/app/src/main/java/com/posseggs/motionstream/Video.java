package com.posseggs.motionstream;

import android.net.Uri;

public class Video {

    private Uri uri;
    private Boolean autoplay;

    public Video()
    {
        //Default uri
        //setUri("rtmp://184.72.239.149/vod/mp4:bigbuckbunny_1500.mp4");
    }

    public Video(String uri, Boolean autoplay)
    {
        try
        {
            setUri(uri);
            setAutoplay(autoplay);
        }
        catch (Exception ex)
        {
            throw new IllegalArgumentException("Error occurred: " + ex.getMessage());
        }
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(String uri)
    {
        if (uri != "")
        {
            /*if (uri.matches("(^www\\.)?([A-Z]|[a-z])+([\\-\\.]{1}[a-z0-9]+)*[.]([a-z]){2,5}"))
            { */
                this.uri = Uri.parse(uri);
            /*}
            else
            {
                throw new IllegalArgumentException("Uri is invalid!");
            }
            */
        }
        else {
            throw new IllegalArgumentException("Uri is empty!");
        }
    }

    public Boolean getAutoplay() {
        return autoplay;
    }

    public void setAutoplay(Boolean autoplay)
    {
        if (autoplay == true || autoplay == false)
            this.autoplay = autoplay;
        else
            throw new IllegalArgumentException("Invalid autoplay!");
    }
}
