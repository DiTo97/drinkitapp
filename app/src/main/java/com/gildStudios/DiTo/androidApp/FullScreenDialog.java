package com.gildStudios.DiTo.androidApp;

import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.gildStudios.DiTo.androidApp.adapters.ListDrinkAdapter;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class FullScreenDialog extends DialogFragment {
    private Toolbar toolbar;
    ListDrinkAdapter listDrinkAdapter;


    Bundle fullScreenBundle;
    private ArrayList<Cocktail> cocktails;

    private int nSelected;
    private ArrayList<Integer> itemPositions;

    public static String TAG = "FullStringDialog";

    public static FullScreenDialog display(FragmentManager fragmentManager, Bundle fullScreenBundle) {

        FullScreenDialog fullScreenDialog = new FullScreenDialog();
        fullScreenDialog.show(fragmentManager, TAG);
        fullScreenDialog.setArguments(fullScreenBundle);

        return fullScreenDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog);

        assert (getArguments() != null);
        fullScreenBundle = getArguments();

        // Multi-select init
        nSelected     = 0;
        itemPositions = new ArrayList<>();
    }

    @Override
    public @Nullable View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.drinklist_fulldialog, container, false);

        toolbar = v.findViewById(R.id.listDrinkToolbar);

        final ArrayList<Cocktail> cocktails = fullScreenBundle.getParcelableArrayList("cocktails");

        final GridView lvDrinks = v.findViewById(R.id.grid_list);

        listDrinkAdapter = new ListDrinkAdapter(cocktails, R.layout.selected_drink3, getContext());

        if(listDrinkAdapter.isEmpty()) {
            lvDrinks.setVisibility(View.INVISIBLE);
        }

        lvDrinks.setAdapter(listDrinkAdapter);
        lvDrinks.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);

        lvDrinks.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int position, long id, boolean isChecked) {
                nSelected = lvDrinks.getCheckedItemCount();

                // Setting CAB title
                actionMode.setTitle(getString(R.string.drink_added, nSelected));

                if(isChecked) {
                    itemPositions.add(position);
                } else {
                    itemPositions.remove((Integer) position);
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater menuInflater = actionMode.getMenuInflater();
                menuInflater.inflate(R.menu.menu_listdrink_single, menu);

                toolbar.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                final int deleteSize = itemPositions.size();
                int itemId = menuItem.getItemId();

                if(itemId == R.id.clear_list) {
                    for(int i = itemPositions.size() - 1; i >= 0; --i) {
                        listDrinkAdapter.remove(listDrinkAdapter
                                .getItem(itemPositions.get(i)));
                    }
                }
                Toast.makeText(getContext(), getContext().getString(R.string.full_dialog_delete, deleteSize),
                        Toast.LENGTH_SHORT).show();

                // Multi-select reset
                nSelected = 0;
                itemPositions.clear();

                actionMode.finish();
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                // Multi-select reset
                nSelected = 0;
                itemPositions.clear();

                toolbar.setVisibility(View.VISIBLE);
            }
        });


        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar.inflateMenu(R.menu.menu_listdrink);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        listDrinkAdapter.getLiveCount().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                toolbar.setTitle(getString(R.string.drink_added, listDrinkAdapter.getCount()));
            }
        });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                listDrinkAdapter.clear();
                return true;
            }
        });
        toolbar.setTitleTextColor(getResources().getColor(R.color.whiteClean));
        toolbar.setSubtitleTextColor(getResources().getColor(R.color.whiteClean));
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();

        if(dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;

            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setWindowAnimations(R.style.AppTheme_Slide);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tellTargetYouGotResults(RESULT_OK);
    }

    private void tellTargetYouGotResults(int requestCode) {
        Fragment targetFragment = getTargetFragment();
        if (targetFragment != null) {
            targetFragment.onActivityResult(getTargetRequestCode(), requestCode, null);
        }
    }

}
