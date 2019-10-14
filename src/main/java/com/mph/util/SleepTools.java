package com.mph.util;

import static java.lang.Thread.sleep;

public class SleepTools {

    public static void currentSleep(int millis){
        try {
            sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
