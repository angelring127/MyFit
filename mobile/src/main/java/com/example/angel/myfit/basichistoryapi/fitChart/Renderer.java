package com.example.angel.myfit.basichistoryapi.fitChart;

/**
 * Created by YOUNSANGHO on 2016-05-22.
 */
import android.graphics.Path;

interface Renderer {
    Path buildPath(float animationProgress, float animationSeek);
}
