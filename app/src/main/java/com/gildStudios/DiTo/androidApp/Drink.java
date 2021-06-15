package com.gildStudios.DiTo.androidApp;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class Drink implements Parcelable {
    private double alcholQuantity;
    private double alcholicPercent;
    private static final DecimalFormat hourFormat = new DecimalFormat("#.##");
    private static final DecimalFormat minuteFormat = new DecimalFormat("#");

    public String getQuantityLabel() {
        return quantityLabel;
    }

    public static double oztoL = 0.0295735;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;
    private String quantityLabel;
    private String remoteTag;

    public String getUtcTime() {
        return utcTime;
    }

    public void setUtcTime(String utcTime) {
        this.utcTime = utcTime;
    }

    private String utcTime;

    public Drink(String name, ArrayList<Drink> drinkIngredients) {
        this.name = name;
        this.alcholicPercent = calcAlPercent(drinkIngredients);
    }

    public Drink(String name, String quantityLabel) {
        this.name = name;
        this.quantityLabel = quantityLabel;
    }

    public Drink(String remoteTag, String name, String quantityLabel, String utcTime) {
        this.name = name;
        this.quantityLabel = quantityLabel;
        this.remoteTag = remoteTag;
        this.utcTime = utcTime;
    }


    public Drink(String remoteTag, String name, String quantityLabel) {
        this.name = name;
        this.quantityLabel = quantityLabel;
        this.remoteTag = remoteTag;
    }

    public Drink(String remoteTag, String name, double alcholicPercent) {
        this.name = name;
        this.alcholicPercent = alcholicPercent;
        this.remoteTag = remoteTag;
    }

    public Drink(String name, double alcholicPercent) {
        this.name = name;
        this.alcholicPercent = alcholicPercent;
    }

    public Drink( double alcholQuantity, double alcholicPercent){
        this.alcholQuantity = alcholQuantity;
        this.alcholicPercent = alcholicPercent;
    }


    public double getAlcholicPercent() {
        return alcholicPercent;
    }

    public void setAlcholicPercent(double alcholicPercent) {
        this.alcholicPercent = alcholicPercent;
    }

    public double getAlcholQuantity() {
        return alcholQuantity;
    }

    public void setAlcholQuantity(double alcholQuantity) {
        this.alcholQuantity = alcholQuantity;
    }

    private double calcAlPercent(ArrayList<Drink> drinkIngredients){
        double totalGrams = alcholCalc(drinkIngredients);
        double totalLiters = 0.0;

        for(Drink drink : drinkIngredients) {
            totalLiters += drink.getAlcholQuantity();
        }

        return Math.floor((totalGrams / totalLiters) * 100) / 100;
    }

    public static double alcholCalc(ArrayList<Drink> drinkList){
        double alcholGrams = 0.0;

        for(Drink drink : drinkList) {
            alcholGrams += drinkGrams(drink);
        }

        return alcholGrams;
    }

    private static double drinkGrams(Drink drink) {
        return drink.getAlcholicPercent() * drink.getAlcholQuantity();
    }

    public static double alcholGlassCalc(ArrayList<Drink> drinkList, ArrayList<Glass> glasslist, ArrayList<Integer> minutesList){
        double alcholGrams = 0.0;

        for(int i = 0; i < drinkList.size(); i++) {
            alcholGrams += GlassGrams(drinkList.get(i), glasslist.get(i), minutesList.get(i));
        }
        Log.d("CALCOLO", "grammi totali: " + alcholGrams);
        return alcholGrams;
    }

    private static double GlassGrams(Drink drink, Glass glass, int minutes) {
        Log.d("CALCOLO", drink.getAlcholicPercent() + " * " + glass.getQuantity() + " " + minutes + " minuti fa");
        //Il corpo umano smaltisce 7gr di alcol all'ora
        double drinkGrams = (drink.getAlcholicPercent() * glass.getQuantity() * 8) - (7 * minutes / 60f);
        Log.d("CALCOLO", "grammi parziali: " + drinkGrams);

        //Controllo che non venga aggiunto un numero negativo
        if(drinkGrams > 0)
            return drinkGrams;
        else
            return 0;
    }

    public static long setAlcholicTaxTime (double tasso){
        double t;
        if(tasso<= 1.0) {
            t = 2000 * tasso / 2; //la proporzione è volutamnete dimezzata per rendere più evidente la animazione
        }else if(tasso >1 && tasso <= 2){
            t = 1000;
        }else t = 2000* tasso/4;
        return (long) t;
    }


    public static int[] soberUp(double tasso , int stomachStatus){
        double soberspeed;
        if(stomachStatus == 0){
            soberspeed = 0.15;
        }else soberspeed = 0.10;

        String val =hourFormat.format(tasso/soberspeed);
        return parseHour(val);
    }

    public static int[] canDrive(double tasso, int stomachStatus){
        double soberspeed;
        if(stomachStatus == 0){
            soberspeed = 0.15;
        }else soberspeed = 0.10;

        double taxToDrive = tasso - 0.50;
        taxToDrive = taxToDrive > 0.0 ? taxToDrive : 0.0;

        String val = hourFormat.format(taxToDrive/soberspeed);
        return parseHour(val);
    }

    public static int[] parseHour(String val){

        if(val.contains(","))
            val = val.replace(',', '.');

        String[] arr = val.split("\\.");

        int[] intArr = new int[2];
        intArr[0] = Integer.parseInt(arr[0]);

        if(arr.length > 1) {
            String minutesPerCent = "0." + arr[1];
            intArr[1] = (int) (Double.parseDouble(minutesPerCent) * 60);
        }
        return intArr;
    }

    public static int msCalc(int[] time){
        int a = time[0]*60*60*1000;
        int b = time[1]*60*1000;
        return a+b;
    }




    public static double newtaxAfter(double tasso, int stomachStatus,double ore){
        final double a;
        if(stomachStatus == 0){
            a =tasso-(0.15*ore);
            if(a < 0.0){
                return 0.0;
            }
            return a;
        }else {
            a = tasso-(0.10*ore);
            if(a < 0.0){
                return 0.0;
            }
            return a;
        }
    }

    protected Drink(Parcel in) {
        alcholQuantity = in.readDouble();
        alcholicPercent = in.readDouble();
        quantityLabel = in.readString();
        name = in.readString();
        remoteTag = in.readString();
        utcTime = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(alcholQuantity);
        dest.writeDouble(alcholicPercent);
        dest.writeString(quantityLabel);
        dest.writeString(name);
        dest.writeString(remoteTag);
        dest.writeString(utcTime);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Drink> CREATOR = new Parcelable.Creator<Drink>() {
        @Override
        public Drink createFromParcel(Parcel in) {
            return new Drink(in);
        }

        @Override
        public Drink[] newArray(int size) {
            return new Drink[size];
        }
    };

    public String getRemoteTag() {
        return remoteTag;
    }

    public void setRemoteTag(String remoteTag) {
        this.remoteTag = remoteTag;
    }
}
