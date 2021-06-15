package com.gildStudios.DiTo.androidApp.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.gildStudios.DiTo.androidApp.Drink;
import com.gildStudios.DiTo.androidApp.FirebaseDbHelper;
import com.gildStudios.DiTo.androidApp.R;
import com.gildStudios.DiTo.androidApp.activities.HomeActivity;
import com.gildStudios.DiTo.androidApp.adapters.MyDrinksAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class MyDrinksFragment extends Fragment {

    private ArrayList<Drink> drinkList = new ArrayList<>();
    private TextView errorView;
    private GridView myDrinksListView;
    private MyDrinksAdapter drinksAdapter;

    private Drink inputDrink;

    private FloatingActionButton addDrinkButton;

    private ProgressDialog pDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(((HomeActivity) Objects.requireNonNull(getActivity()))
                .getSupportActionBar()).hide();
    }

    @Override
    public void onResume() {
        super.onResume();

        final String userId = FirebaseAuth.getInstance().getUid();
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("CustomDrinks").child(userId);

        displayProgressDialog("Loading up your Drinks");

        dbRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        pDialog.dismiss();
                        drinksAdapter.clear();

                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.hasChildren()) {
                                Drink temp;
                                for (DataSnapshot dataSnap : dataSnapshot.getChildren()){
                                    temp = new Drink(dataSnap.getKey(), dataSnap.getValue(Double.class));
                                    drinksAdapter.add(temp);
                                }

                                errorView.setVisibility(View.INVISIBLE);
                                myDrinksListView.setVisibility(View.VISIBLE);
                            }
                            else{
                                errorView.setVisibility(View.VISIBLE);
                                myDrinksListView.setVisibility(View.INVISIBLE);
                            }
                        }
                        else{
                            errorView.setVisibility(View.VISIBLE);
                            myDrinksListView.setVisibility(View.INVISIBLE);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );
    }


    @Override
    public View onCreateView(@NonNull final LayoutInflater layoutInflater, final ViewGroup containerGroup,
                             Bundle savedInstanceState) {

        final View myDrinkView = layoutInflater.inflate(R.layout.fragment_my_drinks, containerGroup, false);
        errorView = myDrinkView.findViewById(R.id.errorView2);
        myDrinksListView = myDrinkView.findViewById(R.id.myDrinksListView);

        addDrinkButton = myDrinkView.findViewById(R.id.add_new_drink);

        addDrinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View addView) {
                if(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("isOffline", false)) {
                    Toast.makeText(getContext(), "Exclusive to Logged users!",
                            Toast.LENGTH_LONG).show();
                } else {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                    if(currentUser != null) {
                        final String userId = currentUser.getUid();

                        if(!currentUser.isAnonymous()) {
                            final View alertView = layoutInflater.inflate(R.layout.alert3, containerGroup, false);
                            final AlertDialog addDrinkAd = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                                    .setView(alertView)
                                    .setCancelable(false)
                                    .create();

                            TextView createConfirm = alertView.findViewById(R.id.create_confirm);

                            createConfirm.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    EditText newDrinkName = alertView.findViewById(R.id.create_name_et);
                                    EditText newDrinkTax  = alertView.findViewById(R.id.create_tax_et);

                                    RadioButton low        = alertView.findViewById(R.id.low);
                                    RadioButton strong     = alertView.findViewById(R.id.strong);
                                    RadioButton veryStrong = alertView.findViewById(R.id.very_strong);

                                    String drinkName = newDrinkName.getText()
                                            .toString().trim();

                                    if(!drinkName.equals("")
                                            && !containsDrinkIgnoreCase(drinkName)) {
                                        if(drinkName.length() <= 18) {
                                            double drinkTax;

                                            if (!newDrinkTax.getText().toString().equals("")) {
                                                try {
                                                    drinkTax = Double.parseDouble(newDrinkTax.getText().toString());
                                                    inputDrink = new Drink(drinkName, drinkTax);
                                                    addDrinkAd.dismiss();
                                                } catch (NumberFormatException ex) {
                                                    Toast.makeText(getContext(), "Ensure that your data are correct", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                if (low.isChecked()) {
                                                    drinkTax = 10.0;
                                                    inputDrink = new Drink(drinkName, drinkTax);
                                                    addDrinkAd.dismiss();
                                                } else if (strong.isChecked()) {
                                                    drinkTax = 20.0;
                                                    inputDrink = new Drink(drinkName, drinkTax);
                                                    addDrinkAd.dismiss();
                                                } else if (veryStrong.isChecked()) {
                                                    drinkTax = 30.0;
                                                    inputDrink = new Drink(drinkName, drinkTax);
                                                    addDrinkAd.dismiss();
                                                } else
                                                    Toast.makeText(getContext(), "Select at least one option", Toast.LENGTH_SHORT).show();
                                            }


                                            if(inputDrink != null && inputDrink.getName() != null) {
                                                FirebaseDatabase.getInstance().getReference(FirebaseDbHelper.FIREBASE_columnDrinks)
                                                        .child(userId).child(inputDrink.getName()).setValue(inputDrink.getAlcholicPercent())
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (!task.isSuccessful())
                                                                    Toast.makeText(getContext(), "Error occured.", Toast.LENGTH_SHORT).show();
                                                                else {
                                                                    drinksAdapter.add(inputDrink);
                                                                    drinksAdapter.notifyDataSetChanged();

                                                                    if(drinksAdapter.getCount() == 1) {
                                                                        errorView.setVisibility(View.INVISIBLE);
                                                                        myDrinksListView.setVisibility(View.VISIBLE);
                                                                    }
                                                                }
                                                            }
                                                        });
                                            }
                                        } else
                                            Toast.makeText(getContext(), "Use a shorter name", Toast.LENGTH_SHORT).show();

                                    } else
                                        Toast.makeText(getContext(), "Use a different name for your Drink", Toast.LENGTH_SHORT).show();
                                }
                            });

                            TextView createCancel = alertView.findViewById(R.id.create_cancel);
                            createCancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    addDrinkAd.dismiss();
                                }
                            });

                            addDrinkAd.show();
                        } else
                            Toast.makeText(getContext(), "Exclusive to Logged users!", Toast.LENGTH_LONG).show();
                    } else
                        Toast.makeText(getContext(), "Sorry, an error has occoured.", Toast.LENGTH_LONG).show();
                }
            }
        });

        drinksAdapter = new MyDrinksAdapter(getContext(), R.layout.my_drink_card, drinkList, errorView);
        myDrinksListView.setAdapter(drinksAdapter);

        return myDrinkView;
    }

    private void displayProgressDialog(String displayMsg) {
        Objects.requireNonNull(((HomeActivity) Objects.requireNonNull(getActivity()))
                .getSupportActionBar()).hide();
        pDialog = new ProgressDialog(getActivity());

        pDialog.setMessage(displayMsg);
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    private boolean containsDrinkIgnoreCase(@NonNull String inputName) {
        if(drinksAdapter == null) {
            return false;
        } else {
            int drinksCount = drinksAdapter.getCount();

            for(int i = 0; i < drinksCount; ++i) {
                Drink currentDrink = drinksAdapter.getItem(i);
                assert currentDrink != null;

                if(inputName.equalsIgnoreCase(currentDrink.getName()))
                    return true;
            }
            return false;
        }
    }
}
