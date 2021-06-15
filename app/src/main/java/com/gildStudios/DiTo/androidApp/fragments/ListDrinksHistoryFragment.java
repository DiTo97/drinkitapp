package com.gildStudios.DiTo.androidApp.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import com.gildStudios.DiTo.androidApp.Drink;
import com.gildStudios.DiTo.androidApp.R;
import com.gildStudios.DiTo.androidApp.activities.HomeActivity;
import com.gildStudios.DiTo.androidApp.adapters.HistoryListDrinkAdapter;

import java.util.ArrayList;
import java.util.Objects;


public class ListDrinksHistoryFragment extends Fragment {

    private ArrayList<Drink> drinkList;
    private TextView errorView;
    private GridView listView;
    private TextView dateView;
    private String dateString;
    private HistoryListDrinkAdapter historyListDrinkAdapter;
    private ProgressDialog pDialog;

    public ListDrinksHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Objects.requireNonNull(((HomeActivity) Objects.requireNonNull(getActivity()))
                .getSupportActionBar()).hide();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View listDrinkView = inflater.inflate(R.layout.fragment_list_drinks_history, container, false);
        errorView = listDrinkView.findViewById(R.id.errorView);
        listView = listDrinkView.findViewById(R.id.grid_list_2);
        Bundle listbundle = this.getArguments();

        if(listbundle != null) {
            errorView.setVisibility(View.INVISIBLE);
            drinkList = listbundle.getParcelableArrayList("historyList");
            dateString = listbundle.getString("date");
            Log.d("cosuccio", dateString);
            dateView = listDrinkView.findViewById(R.id.dataHistory);
            dateView.setText(dateString);

            historyListDrinkAdapter = new HistoryListDrinkAdapter(
                    getContext()
                    , R.layout.selected_drink3
                    , drinkList);
            listView.setAdapter(historyListDrinkAdapter);

        } else {
            listView.setVisibility(View.INVISIBLE);
            errorView.setVisibility(View.VISIBLE);
        }
        return listDrinkView;
    }
}
