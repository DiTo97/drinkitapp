package com.gildStudios.DiTo.androidApp;

import android.os.Parcel;
import android.os.Parcelable;

public class Cocktail implements Parcelable {
    private Drink drink;
    private Glass glass;
    private int time;

    public Cocktail(Drink drink, Glass glass, int time) {
        this.drink = drink;
        this.glass = glass;
        this.time = time;
    }

    protected Cocktail (Parcel in) {
        drink = in.readParcelable(Drink.class.getClassLoader());
        glass = in.readParcelable(Glass.class.getClassLoader());
        time = in.readInt();
    }

    public Drink getDrink() {
        return drink;
    }

    public Glass getGlass() {
        return glass;
    }

    public int getTime() {
        return time;
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(drink, i);
        parcel.writeParcelable(glass, i);
        parcel.writeInt(time);
    }

    public static final Parcelable.Creator<Cocktail> CREATOR = new Parcelable.Creator<Cocktail>() {
        @Override
        public Cocktail createFromParcel(Parcel in) {
            return new Cocktail(in);
        }

        @Override
        public Cocktail[] newArray(int size) {
            return new Cocktail[size];
        }
    };

}
