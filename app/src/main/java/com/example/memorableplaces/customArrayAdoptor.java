package com.example.memorableplaces;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class customArrayAdoptor extends ArrayAdapter<String> {

    ArrayList<String> all;

    public customArrayAdoptor( Context context, int resource, ArrayList<String> objects) {
        super(context, resource, objects);
        all=objects;
    }

    @NonNull
    @Override
    public View getView(int position,  View convertView,  ViewGroup parent) {
        View view = convertView;

        if(view == null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_view, parent, false);
        }

        TextView tv = view.findViewById(R.id.textView);
        tv.setText(all.get(position));

        if(position !=0){
            tv.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            tv.setText(Integer.toString(position)+". " +all.get(position));
        }
        return view;
    }
}
