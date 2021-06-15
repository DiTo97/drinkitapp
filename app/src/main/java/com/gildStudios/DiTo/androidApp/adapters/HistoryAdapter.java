package com.gildStudios.DiTo.androidApp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.TextView;


import com.gildStudios.DiTo.androidApp.History;
import com.gildStudios.DiTo.androidApp.R;

import java.util.List;

public class HistoryAdapter extends ArrayAdapter<History> {

    private List<History> historyList;

    private Context mContext;

    public HistoryAdapter(Context context, int layoutResource, List<History> historyList) {
        super(context, layoutResource, historyList);

        this.mContext = context;
        this.historyList = historyList;
    }

    @Override @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View listRow;
        ViewHolder viewHolder;

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            listRow = inflater.inflate(R.layout.list_view_history, parent, false);
            viewHolder = new ViewHolder(listRow);
            listRow.setTag(viewHolder);
        } else {
            listRow = convertView;
            viewHolder = (ViewHolder) listRow.getTag();
        }

        History displayedInfo = historyList.get(position);
        boolean inLimit = displayedInfo.getTassoAlc() <= 0.5;

        viewHolder.data.setText(displayedInfo.getTimestampDate());
        viewHolder.stomach.setText(displayedInfo.getStomachStatus());
        viewHolder.tassoAlc.setText(String.valueOf(displayedInfo.getTassoAlc()));

        int colorGreen = mContext.getResources().getColor(R.color.okGreen);
        int colorRed   = mContext.getResources().getColor(R.color.noRed);

        viewHolder.tassoAlc.setTextColor(inLimit ? colorGreen : colorRed);
        return listRow;
    }

    private class ViewHolder {
        private final TextView data;
        private final TextView stomach;
        private final TextView tassoAlc;

        private ViewHolder(View listRow) {
            data = listRow.findViewById(R.id.listData);
            stomach = listRow.findViewById(R.id.listStomach);
            tassoAlc = listRow.findViewById(R.id.listAlcTass);
        }
    }
}