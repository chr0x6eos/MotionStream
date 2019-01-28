package com.posseggs.motionstream;

import android.net.Uri;

public class Video {

    private Uri uri;

    public Video(String uri)
    {
        try
        {
            setUri(uri);
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
}
