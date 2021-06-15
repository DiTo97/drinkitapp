package com.gildStudios.DiTo.androidApp;

import android.content.Context;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class History {

    private String timestampDate;
    private double tassoAlc;
    private String stomachStatus;
    private ArrayList<Drink> myDrinks;
    private Context context;

    private ArrayList<Drink> historyDrinks;


    public History(long utcData, double tassoAlc, int stomachStatus, Context context) {
        Date utcDate = new Date(utcData * 1000);
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        this.timestampDate = dateFormat.format(utcDate);
        this.context=context;
        this.tassoAlc = tassoAlc;
        this.historyDrinks = new ArrayList<>();

        switch(stomachStatus) {
            case User.Widmark.STOMACO_allEmpty:
                this.stomachStatus = context.getString(R.string.stomach_empty_history);
                break;
            case User.Widmark.STOMACO_notEmpty:
                this.stomachStatus = context.getString(R.string.stomach_medium);
                break;
            case User.Widmark.STOMACO_allYouCanEat:
                this.stomachStatus = context.getString(R.string.stomach_full_history);
                break;
        }
    }

    public String getTimestampDate() {
        return timestampDate;
    }

    public double getTassoAlc() {
        return tassoAlc;
    }

    public String getStomachStatus() {
        return stomachStatus;
    }

    public ArrayList<Drink> getHistoryDrinks() {
        return historyDrinks;
    }

    public void setHistoryDrinks(ArrayList<Drink> historyDrinks) {
        this.historyDrinks = historyDrinks;
    }
}
