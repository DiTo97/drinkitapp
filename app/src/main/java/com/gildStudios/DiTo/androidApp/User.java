package com.gildStudios.DiTo.androidApp;

import android.util.Log;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class User {
    private int age;
    private int bonesType; // 1 Piccola, 2 Media, 3 Grossa.
    private int gender; // 1 Maschio, 2 Femmina.
    private int height;
    private int id;

    private int remoteId;

    private String email;
    private String password;
    private String username;

    private double magicNumber;

    private Date lastUpdatedAt;

    public User() {
    }

    public User(int age, int height, int gender, int bonesType) {
        this.age = age;
        this.height = height;
        this.gender = gender;
        this.bonesType = bonesType;
    }

    public User(int id, String username, String password, String email, double magicNumber) {
        this.id = id;

        this.email = email;
        this.password = password;
        this.username = username;

        this.magicNumber = magicNumber;
    }

    public User(int id, int remoteId, String email, String username, String password, double magicNumber, String lastUpdatedAt) {
        this.id = id;
        this.remoteId = remoteId;

        this.email = email;
        this.password = password;
        this.username = username;

        this.magicNumber = magicNumber;

        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            this.lastUpdatedAt = dateFormat.parse(lastUpdatedAt);
        } catch (java.text.ParseException e) {
            this.lastUpdatedAt = null;
        }
    }

    public User(int age, int height, int gender, int bonesType, String username, String password, String email) {
        this.age = age;
        this.bonesType = bonesType;
        this.gender = gender;
        this.height = height;
        this.id = -1;

        this.email = email;
        this.password = password;
        this.username = username;

        this.magicNumber = Widmark.NumeroMagico(gender, age, height, bonesType);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(int remoteId) {
        this.remoteId = remoteId;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
        updateMagicNumber();
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
        updateMagicNumber();
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
        updateMagicNumber();
    }

    public int getBonesType() {
        return bonesType;
    }

    public void setBonesType(int bonesType) {
        this.bonesType = bonesType;
        updateMagicNumber();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(Date lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public double getMagicNumber() {
        return magicNumber;
    }

    private void updateMagicNumber() {
        this.magicNumber = Widmark.NumeroMagico(gender, age, height, bonesType);
    }

    public static class Widmark {
        public static final int STOMACO_allEmpty = 0;
        public static final int STOMACO_notEmpty = 1;
        public static final int STOMACO_allYouCanEat = 2;

        private static final int SEX_isMale = 1;
        private static final int SEX_isFemale = 2;

        private static final int BONES_skinnySize = 0;
        private static final int BONES_mediumSize = 1;
        private static final int BONES_largeSize = 2;

        private static double TBWMen(int age, int height, double pF) {
            return 2.447 - (0.0952 * age) + (0.1074 * height) + (0.3362 * pF);
        }

        private static double TBWWomen(int age, int height, double pF) {
            return 0.203 - (0.07 * age) + (0.1069 * height) + 0.2466 * pF;
        }

        private static double fattoreWidmark(int gender, int age, int height, double pF) {
            double fW;
            double weightConstant = (0.8 * pF) * 1.055;

            if (gender == SEX_isMale) {
                fW = TBWMen(age, height, pF) / weightConstant;
            } else {
                fW = TBWWomen(age, height, pF) / weightConstant;
            }

            return fW;
        }

        public static double NumeroMagico(int gender, int age, int height, int bonesType) {
            double pF = pesoForma(gender, height, bonesType);
            double fW = fattoreWidmark(gender, age, height, pF);

            return pF != -1 ? fW * pF : pF;
        }

        private static double pesoForma(int gender, int height, int bonesType) {
            double resultWeight;

            if (gender == SEX_isMale) {
                resultWeight = height - 100 - ((height - 150) / 4);
            } else if (gender == SEX_isFemale) {
                resultWeight = height - 100 - ((height - 150) / 2);
            } else {
                Log.i("Calcolo PF", "Sesso non Riconosciuto!");
                return -1;
            }

            switch (bonesType) {
                case BONES_skinnySize:
                    resultWeight = resultWeight - (resultWeight * 5 / 100);
                    break;
                case BONES_mediumSize:
                    break;
                case BONES_largeSize:
                    resultWeight = resultWeight + (resultWeight * 5 / 100);
                    break;
                default:
                    Log.i("Calcolo PF", "Bones-Type non Riconosciuto!");
                    return -1;
            }

            return resultWeight;
        }

        public static double tassoAlcolico(User alcholAddicted, int stomachStatus, ArrayList<Drink> drinkList) {
            double alcholGrams = Drink.alcholCalc(drinkList);

            switch (stomachStatus) {
                case STOMACO_notEmpty:
                    alcholGrams += 4;
                    break;
                case STOMACO_allYouCanEat:
                    alcholGrams += 8;
                    break;
            }

            return alcholGrams * 1.055 / alcholAddicted.magicNumber;
        }


    }
}