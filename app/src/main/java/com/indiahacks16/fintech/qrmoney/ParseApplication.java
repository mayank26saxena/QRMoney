package com.indiahacks16.fintech.qrmoney;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by Mayank on 08-01-2016.
 */
public class ParseApplication extends Application {

    public static final String YOUR_APPLICATION_ID = "srTc44iIXP6l3c0885gew63p5pkpxt9GHJcYzcrZ" ;
    public static final String YOUR_CLIENT_KEY = "x5atHqcIYxRy0WLzvrvuoVxN2hbTON2ngVyVpTwA" ;

    @Override
    public void onCreate() {
        super.onCreate();

        // Add your initialization code here
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, YOUR_APPLICATION_ID, YOUR_CLIENT_KEY);
    }
}
