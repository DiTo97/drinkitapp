package com.gildStudios.DiTo.androidApp;

public class Limit {
    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    private String place;

    public double getBound() {
        return bound;
    }

    public void setBound(double bound) {
        this.bound = bound;
    }

    private double bound;

    public Limit(String place, double bound){
        this.place = place;
        this.bound = bound;
    }
}
