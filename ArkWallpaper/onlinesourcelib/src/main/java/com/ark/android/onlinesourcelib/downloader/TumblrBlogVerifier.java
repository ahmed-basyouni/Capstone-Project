package com.ark.android.onlinesourcelib.downloader;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by ahmed-basyouni on 4/25/17.
 */

public interface TumblrBlogVerifier {

    @GET("v2/blog/")
    Observable<BlogInfo> checkBlogInfo();

    public class BlogInfo {
        public Meta meta;
    }

    class Meta{
        public int status;
        String msg;
    }
}
