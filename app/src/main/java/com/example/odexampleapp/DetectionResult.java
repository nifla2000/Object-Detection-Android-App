package com.example.odexampleapp;

import android.graphics.RectF;

public class DetectionResult {
    private RectF location;
    private int category;
    private float score;

    public DetectionResult(RectF location, int category, float score) {
        this.location = location;
        this.category = category;
        this.score = score;
    }

    public RectF getLocation() {
        return location;
    }

    public Integer getCategory() {
        return category;
    }

    public float getScore() {
        return score;
    }
    public void setLocation(RectF location) {
        this.location = location;
    }

}


