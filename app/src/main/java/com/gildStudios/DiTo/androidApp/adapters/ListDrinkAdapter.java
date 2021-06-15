package com.gildStudios.DiTo.androidApp.adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.gildStudios.DiTo.androidApp.Cocktail;
import com.gildStudios.DiTo.androidApp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class  ListDrinkAdapter extends ArrayAdapter<Cocktail> {

    private List<Cocktail> listDrinks;
    private Context context;
    private MutableLiveData<Integer> liveCount;

    public ListDrinkAdapter(List<Cocktail> listDrinks, int layoutResource, Context context) {
        super(context, layoutResource, listDrinks);

        this.listDrinks = listDrinks;
        this.context = context;
        this.liveCount = new MutableLiveData<>();
        this.liveCount.setValue(getCount());
    }

    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        View row;
        final ViewHolder holder;


        if(convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            row = layoutInflater.inflate(R.layout.selected_drink3, parent, false);
            holder = new ViewHolder(row);
            row.setTag(holder);
        } else {
            row = convertView;
            holder = (ViewHolder) row.getTag();
        }

        final Cocktail cocktail = listDrinks.get(position);

        holder.drinkName.setText(cocktail.getDrink().getName());
        holder.drinkQuantity.setText(cocktail.getGlass().getLabel());
        holder.drinkTime.setText(cocktail.getTime() + " " + context.getString(R.string.tv_minutes_ago));
        holder.drinkPic.setImageResource(R.drawable.ic_loading);



        /*holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog ad = new AlertDialog.Builder(getContext())
                        .create();
                ad.setCancelable(false);
                ad.setMessage(context.getString(R.string.card_remove));
                ad.setButton(Dialog.BUTTON_NEGATIVE, context.getString(R.string.create_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                ad.setButton(Dialog.BUTTON_POSITIVE, context.getString(R.string.create_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        remove(cocktail);
                        liveCount.setValue(getCount());
                    }
                });

                ad.show();
                return true;
            }
        });*/




        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("drinksImages")
                .child(cocktail.getDrink().getRemoteTag() + ".jpg");


        final RequestManager glideLoader = Glide.with(context);

        storageReference.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri imageUri) {
                        Log.d("Uri >", imageUri.toString());
                        RequestOptions options = new RequestOptions()
                                .centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .priority(Priority.HIGH);

                        glideLoader.clear(holder.drinkPic);

                        glideLoader.load(imageUri)
                                .apply(options)
                                .into(holder.drinkPic);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("uri","ciao");
                holder.drinkPic.setImageResource(R.drawable.cocktail_medium);
            }
        });




    /*    holder.remove.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        remove(cocktail);
                        liveCount.setValue(getCount());
                    }
                }
        );  */

        return row;
    }

    private class ViewHolder {

        private final TextView drinkName;
        private final TextView drinkQuantity;
        private final TextView drinkTime;
        //private final ImageView remove;
        private final CardView cardView;
        private final ImageView drinkPic;

        private ViewHolder(@NonNull View itemView) {
            drinkName = itemView.findViewById(R.id.selected_drink);
            drinkQuantity = itemView.findViewById(R.id.drinkQuantity);
            //remove = itemView.findViewById(R.id.drinkRmv);
            drinkPic = itemView.findViewById(R.id.imageDrink);
            drinkTime = itemView.findViewById(R.id.drink_time);
            cardView = itemView.findViewById(R.id.card_view);

        }
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    public MutableLiveData<Integer> getLiveCount() {
        return liveCount;
    }

    @Override
    public void remove(@Nullable Cocktail cocktailObj) {
        super.remove(cocktailObj);
        liveCount.setValue(getCount());
    }

    @Override
    public void clear() {
        super.clear();
        liveCount.setValue(getCount());
    }

}
