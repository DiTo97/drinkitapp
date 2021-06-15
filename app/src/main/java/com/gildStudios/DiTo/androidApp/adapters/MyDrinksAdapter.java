package com.gildStudios.DiTo.androidApp.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gildStudios.DiTo.androidApp.Drink;
import com.gildStudios.DiTo.androidApp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class MyDrinksAdapter extends ArrayAdapter<Drink> {

    private List<Drink> historyDrinkList;

    private Context mContext;
    private ProgressDialog pDialog;
    private LayoutInflater layoutInflater;

    private TextView noDrinksView;

    public MyDrinksAdapter(Context context, int layoutResource, List<Drink> historyDinkList, TextView noDrinksView) {
        super(context, layoutResource, historyDinkList);

        this.mContext = context;
        this.historyDrinkList = historyDinkList;
        this.noDrinksView = noDrinksView;
    }

    @Override @NonNull
    public View getView(final int position, View convertView, @NonNull final ViewGroup parentView) {
        View listRow;
        final MyDrinksAdapter.ViewHolder viewHolder;

        if(convertView == null) {
            layoutInflater = LayoutInflater.from(mContext);
            listRow = layoutInflater.inflate(R.layout.my_drink_card_final, parentView, false);
            viewHolder = new MyDrinksAdapter.ViewHolder(listRow);
            listRow.setTag(viewHolder);
        } else {
            listRow = convertView;
            viewHolder = (MyDrinksAdapter.ViewHolder) listRow.getTag();
        }

        final Drink displayedInfo = historyDrinkList.get(position);

        viewHolder.name.setText(displayedInfo.getName());

        double alcPercent = displayedInfo.getAlcholicPercent();

        String alcPercentString = alcPercent + "°";
        viewHolder.tax.setText(alcPercentString);
        viewHolder.photo.setImageResource(R.drawable.ic_loading);

        if(alcPercent <= 10)
            viewHolder.photo.setImageResource(R.drawable.cocktail_light);

        else if (alcPercent <= 20)
            viewHolder.photo.setImageResource(R.drawable.cocktail_medium);

        else
            viewHolder.photo.setImageResource(R.drawable.cocktail_strong);

        viewHolder.photo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Alert to confirm removal
                View alertView = layoutInflater.inflate(R.layout.alert6, parentView, false);
                final AlertDialog ad = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                        .setView(alertView)
                        .setCancelable(false)
                        .create();

                TextView cancel = alertView.findViewById(R.id.create_cancel);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ad.dismiss();
                    }
                });

                TextView confirm = alertView.findViewById(R.id.create_confirm);
                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String userId = FirebaseAuth.getInstance().getUid();
                        FirebaseDatabase.getInstance().getReference("CustomDrinks").child(userId).child(viewHolder.name.getText().toString()).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    remove(displayedInfo);
                                    notifyDataSetChanged();

                                    if(getCount() == 0) {
                                        if(noDrinksView != null) {
                                            noDrinksView.setVisibility(View.VISIBLE);
                                        }
                                        parentView.setVisibility(View.INVISIBLE);
                                    }
                                }
                                ad.dismiss();
                            }
                        });

                    }
                });


                ad.show();

                return false;
            }
        });
       /* viewHolder.photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Alert to confirm removal
                View alertView = layoutInflater.inflate(R.layout.alert6, parentView, false);
                final AlertDialog ad = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                        .setView(alertView)
                        .setCancelable(false)
                        .create();

                TextView cancel = alertView.findViewById(R.id.create_cancel);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ad.dismiss();
                    }
                });

                TextView confirm = alertView.findViewById(R.id.create_confirm);
                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String userId = FirebaseAuth.getInstance().getUid();
                        FirebaseDatabase.getInstance().getReference("CustomDrinks").child(userId).child(viewHolder.name.getText().toString()).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    remove(displayedInfo);
                                    notifyDataSetChanged();

                                    if(getCount() == 0) {
                                        if(noDrinksView != null) {
                                            noDrinksView.setVisibility(View.VISIBLE);
                                        }
                                        parentView.setVisibility(View.INVISIBLE);
                                    }
                                }
                                ad.dismiss();
                            }
                        });

                    }
                });


                ad.show();


            }
        });*/

        return listRow;
    }

    public static class ViewHolder {
        public final ImageView photo;
        public final TextView name;
        public final TextView tax;

        public ViewHolder(View listRow) {
            photo = listRow.findViewById(R.id.imageView);
            name = listRow.findViewById(R.id.myDrinkName);
            tax = listRow.findViewById(R.id.myDrinkAlc);
        }
    }

    private void displayProgressDialog(String displayMsg) {
        pDialog = new ProgressDialog(mContext);

        pDialog.setMessage(displayMsg);
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }
}

