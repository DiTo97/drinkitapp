package com.gildStudios.DiTo.androidApp.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.gildStudios.DiTo.androidApp.Drink;
import com.gildStudios.DiTo.androidApp.customs.CustomSQLiteDatabase;
import com.gildStudios.DiTo.androidApp.FirebaseDbHelper;
import com.gildStudios.DiTo.androidApp.FullScreenDialog;
import com.gildStudios.DiTo.androidApp.Glass;
import com.gildStudios.DiTo.androidApp.Cocktail;
import com.gildStudios.DiTo.androidApp.R;
import com.gildStudios.DiTo.androidApp.User;
import com.gildStudios.DiTo.androidApp.activities.HomeActivity;
import com.gildStudios.DiTo.androidApp.adapters.ListDrinkAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;
import static com.gildStudios.DiTo.androidApp.DrinkItApplication.glassesList;

// TODO: Toast più chiari nella selezione dei Drink
// TODO: Menu del FullDialog

public class DrinkFragment extends Fragment {

    //Flow abilitato o disabilitato
    boolean isFlowEnabled;
    final ArrayList<Cocktail> activeCocktails = new ArrayList<>();
    TextView currentBAC;
    ImageView flowIcon;

    //Quantità
    TextView quantityNumber, increaseNumber, decreaseNumber;


    ConstraintLayout bottomList;

    RelativeLayout scrollView;
    CustomSQLiteDatabase mainDb = new CustomSQLiteDatabase(getActivity());
    TextView listSize;

    //Importo la lista completa dei drinks presenti nel database
    private ArrayList<Drink> drinks;
    private ArrayList<Glass> glasses = glassesList;

    private int stomachStatus;
    Drink input;

    private AutoCompleteTextView actvDrinkNames;

    private int drinksNumber;

    ProgressDialog pDialog;

    private ArrayList<String> glassLabel = new ArrayList<>();

    //Dichiaro lista di drink e adapter per la RecyclerView
    private ArrayList<Cocktail> cocktails;
    private ListDrinkAdapter listDrinkAdapter;

    private DataSetObserver dataSetObserver;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1001) {
            if(resultCode == RESULT_OK)
                listSize.setText(getString(R.string.drink_added, cocktails.size()));
            else
                Log.d(getClass().getSimpleName(), "resultCode is not OK: " + resultCode);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        //listDrinkAdapter.registerDataSetObserver(dataSetObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        //listDrinkAdapter.unregisterDataSetObserver(dataSetObserver);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((HomeActivity) getActivity()).getSupportActionBar().hide();

        cocktails = new ArrayList<>();
        listDrinkAdapter = new ListDrinkAdapter(cocktails, R.layout.cardview, getContext());

        drinksNumber = 1;

    /*    dataSetObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if(listDrinkAdapter.isEmpty()) {
                    listView.setVisibility(View.GONE);
                } else {
                    listView.setVisibility(View.VISIBLE);
                }
            }
        };
    }

    public static void logWhatIsDrunk(String customMsg) {
        if(drunks != null && glassBevuti != null) {
            if(customMsg != null) {
                Log.d("WhatIsDrunk", customMsg);
            }

            if(drunks.isEmpty()) {
                Log.d("WhatIsDrunk", "Drink bevuti > Niente");
            } else {
                for(int i = 0; i < drunks.size(); i++) {
                    Log.d("WhatIsDrunk", "Drink bevuti > "
                            + drunks.get(i).getName() + " - "  + glassBevuti.get(i).getLabel());
                }
            }
        } */
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView (final LayoutInflater inflater, final ViewGroup container,
                              Bundle savedInstanceState){
        final View drinkView = inflater.inflate(R.layout.drink3, container, false);

        scrollView = drinkView.findViewById(R.id.drinklayout);
        final Button addCocktail = drinkView.findViewById(R.id.add_cocktail);

        quantityNumber = drinkView.findViewById(R.id.tv_number_quantity);
        increaseNumber = drinkView.findViewById(R.id.tv_increase);
        decreaseNumber = drinkView.findViewById(R.id.tv_decrease);

        final Button calculate = drinkView.findViewById(R.id.calculate);
        final EditText minutesField = drinkView.findViewById(R.id.minutes);
        final ImageView beer = drinkView.findViewById(R.id.ivBottomList);
        final SeekBar burgerBar = drinkView.findViewById(R.id.burgerBar);
        currentBAC = drinkView.findViewById(R.id.currentBAC);
        flowIcon = drinkView.findViewById(R.id.iconFlow);

        flowIcon.setVisibility(View.GONE);
        currentBAC.setVisibility(View.GONE);

        //Valore iniziale della Seekbar
        burgerBar.setProgress(50);

        //TODO: Solo per la presentazione
        //flowIcon.setVisibility(View.GONE);
        //currentBAC.setVisibility(View.GONE);

        /*if(PreferenceManager.getDefaultSharedPreferences(getContext()).contains("isFlowEnabled")) {
            isFlowEnabled = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("isFlowEnabled", false);
            if(!isFlowEnabled)
                stopFlow();
            else
                startFlow();
        }*/

        //Cambio immagine in base all'attivazione di Flow
        flowIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isFlowEnabled)
                    stopFlow();
                else
                    startFlow();
            }
        });

        listSize = drinkView.findViewById(R.id.textView46);
        listSize.setText(getString(R.string.drink_added, cocktails.size()));

        bottomList = drinkView.findViewById(R.id.constraintLayout6);

        bottomList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDrinkList();
            }
        });
        beer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDrinkList();
            }
        });

        // TODO: Cambiare con il TimePicker https://www.tutlane.com/tutorial/android/android-timepicker-with-examples
        minutesField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                Pattern minuti = Pattern.compile("^[0-9]{1,4}$");

                if(hasFocus && minutesField.getText().toString().equals("0"))
                    minutesField.setText("");
                else {
                    if (minutesField.getText().toString().equals(""))
                        minutesField.setText("0");
                    else {
                        if(!minuti.matcher(minutesField.getText().toString()).matches())
                            minutesField.setText("1000");
                    }
                }
            }
        });

        increaseNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboardFrom(getContext(), drinkView);

                if(Integer.parseInt(quantityNumber.getText().toString()) < 9) {
                    quantityNumber.setText(String.valueOf(Integer.parseInt(quantityNumber.getText().toString()) + 1));
                    drinksNumber++;
                }
            }
        });

        decreaseNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboardFrom(getContext(), drinkView);

                if(Integer.parseInt(quantityNumber.getText().toString()) > 1) {
                    quantityNumber.setText(String.valueOf(Integer.parseInt(quantityNumber.getText().toString()) - 1));
                    drinksNumber--;
                }
            }
        });

    /*    increaseNumber.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideKeyboardFrom(getContext(), drinkView);
                return false;
            }
        }); */

    /*    spinnerQuantity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                drinksNumber = i + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                drinksNumber = 1;
            }
        }); */

        //Inizializzazione Drinks
        drinks = mainDb.getAllDrinks();

        //Inizializzazione ArrayList per la ListView di ricerca
        final ArrayList<String> drinkLabel = new ArrayList<>();

        for(Drink x : drinks) {
            if(x != null) {
                drinkLabel.add(x.getName());
            }
        }

        if(!PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("isOffline", false)) {
            final String userId = FirebaseAuth.getInstance().getUid();
            DatabaseReference userPref = FirebaseDatabase.getInstance().getReference(FirebaseDbHelper.FIREBASE_columnDrinks).child(userId);

            userPref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChildren()) {
                            int listIndex = 0;
                            for (DataSnapshot dataSnap : dataSnapshot.getChildren()) {
                                Drink temp = new Drink(dataSnap.getKey(), dataSnap.getValue(Double.class));
                                drinks.add(listIndex++, temp);
                                drinkLabel.add(temp.getName());
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        //Inizializzazione GlassLabel
        if(glassLabel.isEmpty())
            for (Glass x : glassesList)
                glassLabel.add(x.getLabel());


        // Adapter contenente i nomi di tutti i drink
        final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.select_dialog_item, drinkLabel);

        // TODO: Keyboard has to hide onItemSelected
        actvDrinkNames = drinkView.findViewById(R.id.cocktails_textview);
        actvDrinkNames.setThreshold(1);//will start working from first character
        actvDrinkNames.setAdapter(spinnerAdapter);//setting the adapter data into the AutoCompleteTextView

        actvDrinkNames.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                hideKeyboardFrom(getContext(), drinkView);
            }
        });


        //Adapter Glass
        final ArrayAdapter<String> glassesAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, glassLabel);

        final Spinner spinner = drinkView.findViewById(R.id.spinnerGlass);
        glassesAdapter.setDropDownViewResource(R.layout.simple_spinner_item_text);
        spinner.setAdapter(glassesAdapter);

        spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideKeyboardFrom(getContext(), drinkView);
                return false;
            }
        });


        //Aggiunta dei Drinks bevuti
        //TODO: Creare adapter Glass e Drink per prendere l'intera variablie, non solo il nome

        addCocktail.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        hideKeyboardFrom(getContext(), drinkView);

                        String selectedDrink = actvDrinkNames.getText().toString();
                        boolean knownDrink = false;

                        // Aggiungo il Drink e il Glass
                        for(Drink x : drinks) {
                            if(x == null)
                                continue;

                            if (x.getName().equals(selectedDrink)) {

                                for (Glass y : glasses) {
                                    String selectedGlass = spinner.getSelectedItem().toString();
                                    if (y.getLabel().equals(selectedGlass)) {

                                        int minutes = Integer.parseInt(minutesField.getText().toString());

                                        for(int i = 0; i < drinksNumber; ++i) {

                                            cocktails.add(new Cocktail(x, y, minutes));

                                            listSize.setText(getString(R.string.drink_added,
                                                    cocktails.size()));
                                        }
                                        //cambiare colore quando aggiungo un drink e ricambiarlo un secondo dopo
                                        listSize.setTextColor(getResources().getColor(R.color.goldChianti));

                                        final Handler handler = new Handler();
                                        handler.postDelayed(new Runnable(){
                                            @Override
                                            public void run(){
                                                listSize.setTextColor(getResources().getColor(R.color.whiteClean));
                                            }
                                        }, 1000);

                                        Collections.sort(cocktails, new Comparator<Cocktail>() {
                                            public int compare(Cocktail first, Cocktail second) {
                                                return Integer.compare(second.getTime(), first.getTime());
                                            }
                                        });

                                        break;
                                    }
                                }
                                knownDrink = true;
                                break;
                            }
                        }

                        //Gestione avvisi
                        if(!knownDrink && !actvDrinkNames.getText().toString().equals(""))
                            Toast.makeText(getContext(), getString(R.string.drink_notfound), Toast.LENGTH_SHORT).show();
                        else {
                            actvDrinkNames.setText("");
                            quantityNumber.setText("1");
                            drinksNumber = 1;
                            minutesField.setText("0");
                            //spinner.setSelection(0);
                            //logWhatIsDrunk("Cocktail added");
                        }
                    }
                }
        );

        calculate.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!cocktails.isEmpty()) {
                            int minutes;

                            int stomachLevel = burgerBar.getProgress();
                            Log.d("DrinkFragment", "" + stomachLevel);

                            if(stomachLevel < 10)
                                stomachStatus = 0;
                            else if (stomachLevel < 65)
                                stomachStatus = 1;
                            else
                                stomachStatus = 2;

                            //logWhatIsDrunk("Tax calculation");

                            baloonTest(stomachStatus, cocktails);
                        } else
                            Toast.makeText(getContext(), getString(R.string.missing_fields), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        TextView createDrink = drinkView.findViewById(R.id.createDrink);

        createDrink.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("isOffline", false)) {
                            Toast.makeText(getContext(), getString(R.string.excl_log), Toast.LENGTH_LONG).show();
                        }
                        else {
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (currentUser != null) {
                                final String userId = currentUser.getUid();

                                if (!currentUser.isAnonymous()) {
                                    final View alertView = inflater.inflate(R.layout.alert3, container, false);
                                    final AlertDialog ad = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                                            .setView(alertView)
                                            .setCancelable(false)
                                            .create();

                                    TextView createConfirm = alertView.findViewById(R.id.create_confirm);

                                    createConfirm.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            EditText newDrinkName = alertView.findViewById(R.id.create_name_et);
                                            EditText newDrinkTax = alertView.findViewById(R.id.create_tax_et);
                                            RadioButton low = alertView.findViewById(R.id.low);
                                            RadioButton strong = alertView.findViewById(R.id.strong);
                                            RadioButton veryStrong = alertView.findViewById(R.id.very_strong);

                                            String drinkName;
                                            if (!newDrinkName.getText().toString().equals("") && !containsIgnoreCase(drinkLabel, newDrinkName.getText().toString()) && (
                                                    !newDrinkName.getText().toString().contains(".") &&         //
                                                    !newDrinkName.getText().toString().contains(",") &&         //
                                                    !newDrinkName.getText().toString().contains("$") &&         // Firebase path must not contains these characters
                                                    !newDrinkName.getText().toString().contains("#") &&         //
                                                    !newDrinkName.getText().toString().contains("[") &&         //
                                                    !newDrinkName.getText().toString().contains("]"))           //
                                            ) {
                                                if(newDrinkName.getText().toString().length() <= 18) {
                                                    drinkName = newDrinkName.getText().toString();
                                                    double drinkTax;

                                                    if (!newDrinkTax.getText().toString().equals("")) {
                                                        try {
                                                            drinkTax = Double.parseDouble(newDrinkTax.getText().toString());
                                                            input = new Drink(drinkName, drinkTax);
                                                            ad.dismiss();
                                                        } catch (NumberFormatException ex) {
                                                            Toast.makeText(getContext(), getString(R.string.drink_fields), Toast.LENGTH_SHORT).show();
                                                        }
                                                    } else {
                                                        if (low.isChecked()) {
                                                            drinkTax = 10.0;
                                                            input = new Drink(drinkName, drinkTax);
                                                            ad.dismiss();
                                                        } else if (strong.isChecked()) {
                                                            drinkTax = 20.0;
                                                            input = new Drink(drinkName, drinkTax);
                                                            ad.dismiss();
                                                        } else if (veryStrong.isChecked()) {
                                                            drinkTax = 30.0;
                                                            input = new Drink(drinkName, drinkTax);
                                                            ad.dismiss();
                                                        } else
                                                            Toast.makeText(getContext(), getString(R.string.drink_option), Toast.LENGTH_SHORT).show();
                                                    }


                                                    if (input.getName() != null) {
                                                        FirebaseDatabase.getInstance().getReference(FirebaseDbHelper.FIREBASE_columnDrinks)
                                                                .child(userId).child(input.getName()).setValue(input.getAlcholicPercent())
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (!task.isSuccessful())
                                                                            Toast.makeText(getContext(), getString(R.string.drink_error), Toast.LENGTH_SHORT).show();
                                                                        else {

                                                                            drinks.add(input);

                                                                            //drinkLabel.add(input.getName());
                                                                            spinnerAdapter.add(input.getName());
                                                                            actvDrinkNames.setText(input.getName());
                                                                            //spinnerAdapter.notifyDataSetChanged();

                                                                        }
                                                                    }
                                                                });
                                                    }
                                                } else
                                                    Toast.makeText(getContext(), getString(R.string.drink_short), Toast.LENGTH_SHORT).show();

                                            } else
                                                Toast.makeText(getContext(), getString(R.string.drink_name), Toast.LENGTH_SHORT).show();

                                        }
                                    });

                                    TextView createCancel = alertView.findViewById(R.id.create_cancel);
                                    createCancel.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            ad.dismiss();
                                        }
                                    });

                                    ad.show();
                                } else
                                    Toast.makeText(getContext(), getString(R.string.offline_unusable), Toast.LENGTH_LONG).show();
                            } else
                                Toast.makeText(getContext(), getString(R.string.drink_error), Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );

        return drinkView;
    }

    double magicNumber;
    double alcholGrams = 0;

    public void baloonTest(final int stomachStatus, final ArrayList<Cocktail> cocktails) {

        final ArrayList<Drink>   drinks     = new ArrayList<>();
        final ArrayList<Glass>   glasses    = new ArrayList<>();
        final ArrayList<Integer> drinkTimes = new ArrayList<>();

        for(Cocktail cocktail : cocktails) {
            drinks.add(cocktail.getDrink());
            glasses.add(cocktail.getGlass());
            drinkTimes.add(cocktail.getTime());
        }

        alcholGrams = Drink.alcholGlassCalc(drinks, glasses, drinkTimes);

        switch (stomachStatus) {
            case User.Widmark.STOMACO_notEmpty:
                alcholGrams += 4;
                break;
            case User.Widmark.STOMACO_allEmpty:
                alcholGrams += 8;
                break;
        }

        if(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("isOffline", false)) {
            int gender;
            int age = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("age", 0);
            int height = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("height", 0);
            int bonesType = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("bonesType", -1);
            if(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("gender", false))
                gender = 1;
            else
                gender = 2;
            magicNumber = User.Widmark.NumeroMagico(gender, age, height, bonesType);

            double calcResult = alcholGrams * 1.055 / magicNumber;

            new CalculateLimitTask().execute("http://ip-api.com/json?fields=countryCode",
                        Double.toString(calcResult));
        } else {
            final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                final String userId = currentUser.getUid();
                final FirebaseDatabase dbInstamce = FirebaseDatabase.getInstance();
                final DatabaseReference dbRef = dbInstamce.getReference("Users")
                        .child(userId).child("magicNumber");

//                final DatabaseReference dbActiveRef = dbInstamce.getReference("UsersInfo")
//                        .child(userId).child("drinksActive");

                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            magicNumber = dataSnapshot.getValue(Double.class);

                            double calcResult = alcholGrams * 1.055 / magicNumber;

                            Locale currentLocale = Locale.getDefault();

                            DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(currentLocale);
                            otherSymbols.setDecimalSeparator('.');
                            otherSymbols.setGroupingSeparator('.');

                            DecimalFormat doubleFormat = new DecimalFormat("0.00", otherSymbols);

                            String calcFormat = doubleFormat.format(calcResult);

                            if(!currentUser.isAnonymous()) {
                                Map<String, String> historyMap = new HashMap<>();

                                long unixTime    = System.currentTimeMillis() / 1000L;
                                String historyId = String.valueOf(unixTime);

                                historyMap.put("stomachStatus", "" + stomachStatus);
                                historyMap.put("resultTax", "" + calcFormat);

                                Log.d("DrinkFragment", historyMap.toString());

                                DatabaseReference historyRef = FirebaseDatabase.getInstance()
                                        .getReference("DrinkHistory")
                                        .child(userId)
                                        .child(historyId);

                                Log.d("DrinkFragment", "UserId: " + userId);

                                historyRef.setValue(historyMap);

                                DatabaseReference drinksListRef = historyRef.child("drinksList");
                                HashMap<String, Object> unixTimesMap = new HashMap<>();

                                // Sorted from older to newer
                                for(int i = 0; i < drinks.size(); i++) {
                                    long timeInSec     = drinkTimes.get(i) * 60;
                                    long unixTimeInSec = unixTime - timeInSec;

                                    String sUnixTimeInSec = String.valueOf(unixTimeInSec);

                                    String drinkName  = drinks.get(i).getRemoteTag();
                                    String glassLabel = glasses.get(i).getLabel();

                                    if(drinkName == null) {
                                        drinkName = drinks.get(i).getName();
                                    }

                                    Log.d("DrinkFragment", drinkName
                                            + " " + glassLabel);

                                    if(unixTimesMap.containsKey(sUnixTimeInSec)) {
                                        HashMap<String, Object> drinksMap = (HashMap<String, Object>) unixTimesMap.get(sUnixTimeInSec);

                                        if(drinksMap.containsKey(drinkName)) {
                                            HashMap<String, String> glassesMap = (HashMap<String, String>) drinksMap.get(drinkName);

                                            if(glassesMap.containsKey(glassLabel)) {
                                                int glassCount = Integer.valueOf(glassesMap
                                                        .get(glassLabel)) + 1;

                                                glassesMap.put(glassLabel, String.valueOf(glassCount));
                                            } else {
                                                glassesMap.put(glassLabel, String.valueOf(1));
                                            }
                                            drinksMap.put(drinkName, glassesMap);
                                        } else {
                                            HashMap<String, String> glassesMap = new HashMap<>();
                                            glassesMap.put(glassLabel, String.valueOf(1));

                                            drinksMap.put(drinkName, glassesMap);
                                        }
                                    } else {
                                        HashMap<String, Object> drinksMap  = new HashMap<>();
                                        HashMap<String, String> glassesMap = new HashMap<>();

                                        glassesMap.put(glassLabel, String.valueOf(1));
                                        drinksMap.put(drinkName, glassesMap);

                                        unixTimesMap.put(sUnixTimeInSec, drinksMap);
                                    }
                                }

                                Log.d("LoginFragment", "unixTimesMap > Done");
                                drinksListRef.setValue(unixTimesMap);
                            }

                            new CalculateLimitTask()
                                    .execute("http://ip-api.com/json?fields=countryCode", calcFormat);

                        } // TODO: Toast for no magicNumber found
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    public class CalculateLimitTask extends AsyncTask<String, String, String[]> {

        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage(getString(R.string.drink_dialog));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String[] doInBackground(String... connParams) {
            BufferedReader bufferedReader    = null;
            HttpURLConnection httpConnection = null;

            try {
                URL url = new URL(connParams[0]);
                httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection.connect();

                InputStream inputStream = httpConnection.getInputStream();

                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder outBuffer = new StringBuilder();
                String singleLine = "";

                while((singleLine = bufferedReader.readLine()) != null) {
                    outBuffer.append(singleLine).append("\n");
                    Log.d("DrinkFragment", "jsonResponse > " + singleLine);
                }
                return new String[] { outBuffer.toString(), connParams[1] };
            } catch(IOException e) {
                e.printStackTrace();
                return new String[] { null, connParams[1] };
            } finally {
                if(httpConnection != null) {
                    httpConnection.disconnect();
                }
                try {
                    if(bufferedReader != null) {
                        bufferedReader.close();
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(String[] connResult) {
            super.onPostExecute(connResult);

            float alcoholLimit = 0.5f;

            try {
                if(connResult[0] != null) {
                    String countryCode = new JSONObject(connResult[0])
                            .getString("countryCode");

                    if(getActivity() != null) {
                        CustomSQLiteDatabase sqLiteDatabase = new CustomSQLiteDatabase(getActivity()
                                .getApplicationContext());

                        float queryResponse = sqLiteDatabase.getAlcoholLimit(countryCode);

                        if (queryResponse != -1 && queryResponse != -2) {
                            alcoholLimit = queryResponse;
                        }
                    }
                }
            } catch(JSONException e) {
                e.printStackTrace();
            } finally {
                if(pDialog.isShowing()) {
                    pDialog.dismiss();
                }

                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                Fragment fm = new CalculatorFragment();
                Bundle refreshArgs = new Bundle();
                Log.d("fama",connResult[1]);
                refreshArgs.putString("tasso", connResult[1]);
                refreshArgs.putFloat("alcoholLimit", alcoholLimit);
                refreshArgs.putInt("stomaco", stomachStatus);
                Log.d("DrinkFragment", "alcoholLimit: " + alcoholLimit);
                fm.setArguments(refreshArgs);
                transaction.setCustomAnimations(R.anim.transition_from_right,
                        R.anim.transition_to_left,R.anim.transition_from_left,R.anim.transition_to_right);
                transaction.replace(R.id.homeWrapper, fm);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        }
    }

//    public static void adjustListViewHeight(@NonNull ListView listView) {
//        ListAdapter adapter = listView.getAdapter();
//
//        if (adapter == null) {
//            return;
//        }
//
//        ViewGroup vg = listView;
//        int totalHeight = 0;
//        for (int i = 0; i < adapter.getCount(); i++) {
//            View listItem = adapter.getView(i, null, vg);
//            listItem.measure(0, 0);
//            totalHeight += listItem.getMeasuredHeight();
//        }
//
//        ViewGroup.LayoutParams par = listView.getLayoutParams();
//        par.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
//        listView.setLayoutParams(par);
//        listView.requestLayout();
//    }

    private boolean containsIgnoreCase(ArrayList<String> lista, String name) {
        for(String x : lista)
            if(x.equalsIgnoreCase(name))
                return true;

        return false;
    }

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void openDrinkList() {
        Bundle fullScreenBundle = new Bundle();
        fullScreenBundle.putParcelableArrayList("cocktails", cocktails);

        FullScreenDialog fullScreenDialog = FullScreenDialog.display(getFragmentManager(), fullScreenBundle);
        fullScreenDialog.setTargetFragment(this, 1001);
    }

    public void startFlow() {

        //TODO: Download da FireBase dei Cocktails attivi e metterli nell'activeCocktails
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        final String userId = currentUser.getUid();

        DatabaseReference userPref = FirebaseDatabase.getInstance().getReference("DrinkHistory").child(userId);
        userPref.addListenerForSingleValueEvent(new ValueEventListener() {

            //TODO: DrinkHistory su Firebase devono diventare Flow, qua si scaricheranno i Drink attivi del Flow corrente, e il BAC
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChildren()) {
                        if (dataSnapshot.hasChild("BAC")) {
                            currentBAC.setText(dataSnapshot.child("BAC").getValue().toString());
                        }

                        if (dataSnapshot.hasChild("activeDrinks")) {
                             // activeCocktails = dataSnapshot.child("activeDrinks").getArrayList<Cocktail>(); o qualcosa del genere
                             cocktails.addAll(activeCocktails);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        flowIcon.setImageResource(R.drawable.flow_enabled);
        currentBAC.setVisibility(View.VISIBLE);

        PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                .putBoolean("isFlowEnabled", true)
                .apply();

        isFlowEnabled = true;
    }

    public void stopFlow() {
        for (Cocktail x : cocktails) {
            if(activeCocktails.contains(x))
                cocktails.remove(x);
        }

        activeCocktails.clear();

        flowIcon.setImageResource(R.drawable.flow_disabled);
        currentBAC.setVisibility(View.GONE);

        PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                .putBoolean("isFlowEnabled", false)
                .apply();

        isFlowEnabled = false;
    }

}

