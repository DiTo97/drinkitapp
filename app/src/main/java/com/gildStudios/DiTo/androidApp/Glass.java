package com.gildStudios.DiTo.androidApp;

import android.os.Parcel;
import android.os.Parcelable;

public class Glass implements Parcelable {

    private  String label;
    private  double quantity;

    public Glass (String label, double quantity){
        this.label = label;
        this.quantity = quantity;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public  String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    protected Glass(Parcel in) {
        label = in.readString();
        quantity = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(label);
        parcel.writeDouble(quantity);
    }

    public static final Parcelable.Creator<Glass> CREATOR = new Parcelable.Creator<Glass>() {
        @Override
        public Glass createFromParcel(Parcel in) {
            return new Glass(in);
        }

        @Override
        public Glass[] newArray(int size) {
            return new Glass[size];
        }
    };
}
