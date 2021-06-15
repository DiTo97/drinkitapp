package com.gildStudios.DiTo.androidApp.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.gildStudios.DiTo.androidApp.R;
import com.gildStudios.DiTo.androidApp.User;
import com.gildStudios.DiTo.androidApp.activities.HomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.HashMap;

public class UserDataFragment extends Fragment {

    private static final String ARG_someInt = "Position";

    private String mParam1;
    private static int age, gender, height, bones;

    private HashMap<String,String> homeAddress;

    boolean redirect = false;

    public static com.gildStudios.DiTo.androidApp.fragments.UserDataFragment newInstance(int position) {
       UserDataFragment fragment = new UserDataFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_someInt, position);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((HomeActivity) getActivity()).getSupportActionBar().hide();

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_someInt);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        final View userDataView = inflater.inflate(R.layout.fragment_user_data, container, false);
        FloatingActionButton button = userDataView.findViewById(R.id.confirm_button);
        final EditText ageText = userDataView.findViewById(R.id.age);
        final EditText heightText = userDataView.findViewById(R.id.height);
        final RadioGroup genderChoice = userDataView.findViewById(R.id.stomach);
        final RadioGroup bonesChoice = userDataView.findViewById(R.id.ossatura);
        final RadioButton man = userDataView.findViewById(R.id.man);
        final RadioButton light = userDataView.findViewById(R.id.piccola);
        final RadioButton medium = userDataView.findViewById(R.id.media);

        String tvProfile = getString(R.string.you_te);

        genderChoice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                DrinkFragment.hideKeyboardFrom(getContext(), userDataView);
            }
        });

        bonesChoice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                DrinkFragment.hideKeyboardFrom(getContext(), userDataView);
            }
        });

        if(this.getArguments() != null) {
            Bundle redirectBundle = this.getArguments();
            redirect = redirectBundle.getBoolean("Redirect");
        }

        if(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("isOffline", false)) {
            if(PreferenceManager.getDefaultSharedPreferences(getContext()).contains("age"))
                ageText.setText(String.valueOf(PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("age", -1)));

            if(PreferenceManager.getDefaultSharedPreferences(getContext()).contains("height"))
                heightText.setText(String.valueOf(PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("height", -1)));

            if(PreferenceManager.getDefaultSharedPreferences(getContext()).contains("gender")) {
                boolean sesso = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("gender", false);
                if (sesso)
                    genderChoice.check(R.id.man);
                else
                    genderChoice.check(R.id.woman);
            }

            if(PreferenceManager.getDefaultSharedPreferences(getContext()).contains("bonesType")) {
                int ossatura = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("bonesType", -1);
                if (ossatura == 0)
                    bonesChoice.check(R.id.piccola);
                else if (ossatura == 1)
                    bonesChoice.check(R.id.media);
                else if (ossatura == 2)
                    bonesChoice.check(R.id.grossa);
            }
            tvProfile += "?";
        } else {
            TextView cheerUser = userDataView.findViewById(R.id.tv_profileTitle);

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            assert currentUser != null;

            if(!currentUser.isAnonymous()
                    && currentUser.getDisplayName() != null) {
                String firstName = currentUser.getDisplayName().split(" ")[0];

                if(firstName != null) {
                    tvProfile += ", <b>" + firstName + "</b>?";
                } else {
                    tvProfile += "?";
                }
            } else {
                tvProfile += "?";
            }

            final String userId = currentUser.getUid();
            DatabaseReference userPref = FirebaseDatabase.getInstance().getReference("UsersInfo").child(userId);
            userPref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChildren()) {
                            if (dataSnapshot.hasChild("age"))
                                ageText.setText(dataSnapshot.child("age").getValue().toString());

                            if (dataSnapshot.hasChild("height"))
                                heightText.setText(dataSnapshot.child("height").getValue().toString());

                            if (dataSnapshot.hasChild("gender")) {
                                if (dataSnapshot.child("gender").getValue().toString().equals("true"))
                                    genderChoice.check(R.id.man);
                                else
                                    genderChoice.check(R.id.woman);
                            }

                            if (dataSnapshot.hasChild("bonesType")) {
                                if (Integer.parseInt(dataSnapshot.child("bonesType").getValue().toString()) == 0)
                                    bonesChoice.check(R.id.piccola);
                                else if (Integer.parseInt(dataSnapshot.child("bonesType").getValue().toString()) == 1)
                                    bonesChoice.check(R.id.media);
                                else
                                    bonesChoice.check(R.id.grossa);
                            }

                            if(dataSnapshot.hasChild("homeAddress")) {
                                DataSnapshot homeSnap = dataSnapshot.child("homeAddress");
                                if(homeSnap.hasChild("fullAddress")) {
                                    homeAddress = new HashMap<>();

                                    homeAddress.put("fullAddress", homeSnap.child("fullAddress").getValue().toString());
                                    homeAddress.put("lat", homeSnap.child("lat").getValue().toString());
                                    homeAddress.put("lon", homeSnap.child("lon").getValue().toString());
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        Spanned finalTVProfile = Html.fromHtml(getString(R.string.text_welcomeMsg, tvProfile)
                .replace("\n","<br />"));

        TextView tvCheerProfile = userDataView.findViewById(R.id.textViewProfile);
        tvCheerProfile.setText(finalTVProfile);

        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!ageText.getText().toString().equals("") && !heightText.getText().toString().equals("") && genderChoice.getCheckedRadioButtonId() != -1 && bonesChoice.getCheckedRadioButtonId() != -1) {
                            try{
                                age = Integer.parseInt(ageText.getText().toString());
                                height = Integer.parseInt(heightText.getText().toString());
                                if(age < 14) {
                                    Toast.makeText(getContext(), getString(R.string.too_young), Toast.LENGTH_SHORT).show();
                                    return;
                                } else if ( height < 50 ){
                                    Toast.makeText(getContext(), getString(R.string.too_short), Toast.LENGTH_SHORT).show();
                                    return;
                                } else if (age > 130){
                                    Toast.makeText(getContext(), getString(R.string.too_old), Toast.LENGTH_SHORT).show();
                                    return;
                                } else if ( height > 250){
                                    Toast.makeText(getContext(), getString(R.string.too_tall), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }catch(NumberFormatException e){
                                return;
                            }

                            if (man.isChecked())
                                gender = 1;
                            else
                                gender = 2;

                            if (light.isChecked())
                                bones = 0;
                            else if (medium.isChecked())
                                bones = 1;
                            else
                                bones = 2;
                            double magicNumber;
                            try {
                                magicNumber = Double.valueOf(new DecimalFormat("0.00")
                                        .format(User.Widmark
                                                .NumeroMagico(gender, age, height, bones)));
                            } catch (Exception e) {
                                magicNumber = User.Widmark
                                        .NumeroMagico(gender, age, height, bones);
                            }

                            if(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("isOffline", false)) {
                                PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                                        .putInt("age", age)
                                        .putInt("bonesType", bones)
                                        .putBoolean("gender", gender == 1)
                                        .putInt("height", height)
                                        .apply();
                            //    Toast.makeText(getContext(), "Saved Data", Toast.LENGTH_SHORT).show();

                                if (!redirect) {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            getActivity().onBackPressed();
                                        }
                                    }, 750);
                                } else {
                                    DrinkFragment fragment = new DrinkFragment();
                                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                    transaction.setCustomAnimations(R.anim.transition_from_right,
                                            R.anim.transition_to_left, R.anim.transition_from_left, R.anim.transition_to_right);
                                    transaction.replace(R.id.homeWrapper, fragment);
                                    transaction.addToBackStack(null);
                                    transaction.commit();
                                }

                            } else {
                                final String userId = FirebaseAuth.getInstance().getUid();
                                final DatabaseReference dbRef2 = FirebaseDatabase.getInstance().getReference("UsersInfo").child(userId);
                                HashMap<String, Object> hMap = new HashMap<>();
                                hMap.put("age", age);
                                hMap.put("bonesType", bones);
                                hMap.put("gender", gender == 1);
                                hMap.put("height", height);

                                if(homeAddress != null) {
                                    hMap.put("homeAddress", homeAddress);
                                }
                                final double finalMagicNumber = magicNumber;
                                dbRef2.setValue(hMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                                            Log.d("magicNumber", "" + finalMagicNumber);
                                            dbRef.child("magicNumber").setValue(finalMagicNumber);

                                            Log.i("USERDATA", "" + age + " " + height + " " + gender + " " + bones);
                                            dbRef.keepSynced(true);
                                            dbRef2.keepSynced(true);
                                        //    Toast.makeText(getContext(), "Data saved", Toast.LENGTH_SHORT).show();
                                            if (!redirect) {
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        getActivity().onBackPressed();
                                                    }
                                                }, 750);
                                            } else {
                                                DrinkFragment fragment = new DrinkFragment();
                                                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                                transaction.setCustomAnimations(R.anim.transition_from_right,
                                                        R.anim.transition_to_left, R.anim.transition_from_left, R.anim.transition_to_right);
                                                transaction.replace(R.id.homeWrapper, fragment);
                                                transaction.addToBackStack(null);
                                                transaction.commit();
                                            }
                                        } else {
                                            Toast.makeText(getContext(), "Data not valid", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            }

                        } else {
                            Toast.makeText(getContext(), getString(R.string.missing_fields), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

        );
        return userDataView;
    }
}


