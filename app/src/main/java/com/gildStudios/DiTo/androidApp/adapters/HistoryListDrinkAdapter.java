package com.gildStudios.DiTo.androidApp.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.gildStudios.DiTo.androidApp.Drink;
import com.gildStudios.DiTo.androidApp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.List;

public class HistoryListDrinkAdapter extends ArrayAdapter<Drink> {

    private List<Drink> historyDrinkList;

    private Context mContext;
    private ProgressDialog pDialog;

    public HistoryListDrinkAdapter(Context context, int layoutResource, List<Drink> historyDinkList) {
        super(context, layoutResource, historyDinkList);

        this.mContext = context;
        this.historyDrinkList = historyDinkList;
    }

    @Override @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View listRow;
        final HistoryListDrinkAdapter.ViewHolder viewHolder;

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            listRow = inflater.inflate(R.layout.selected_drink3_white, parent, false);
            viewHolder = new HistoryListDrinkAdapter.ViewHolder(listRow);
            listRow.setTag(viewHolder);
        } else {
            listRow = convertView;
            viewHolder = (HistoryListDrinkAdapter.ViewHolder) listRow.getTag();
        }

        Drink displayedInfo = historyDrinkList.get(position);

        viewHolder.drinkName.setText(displayedInfo.getName());
        viewHolder.drinkQuantity.setText(String.valueOf(displayedInfo.getQuantityLabel()));
        viewHolder.drinkPic.setImageResource(R.drawable.ic_loading);
        viewHolder.drinkTime.setText(displayedInfo.getUtcTime());

        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("drinksImages")
                .child(displayedInfo.getRemoteTag() + ".jpg");

        final RequestManager glideLoader = Glide.with(mContext);

        storageReference.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri imageUri) {
                        Log.d("Uri >", imageUri.toString());
                        RequestOptions options = new RequestOptions()
                                .centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .priority(Priority.HIGH);

                        glideLoader.clear(viewHolder.drinkPic);

                        glideLoader.load(imageUri)
                                .apply(options)
                                .into(viewHolder.drinkPic);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("uri","ciao");
                        viewHolder.drinkPic.setImageResource(R.drawable.cocktail_medium);
                    }
                });

        return listRow;
    }

    private class ViewHolder {private final TextView drinkName;
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

    private void displayProgressDialog(String displayMsg) {
        pDialog = new ProgressDialog(mContext);

        pDialog.setMessage(displayMsg);
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }
}

