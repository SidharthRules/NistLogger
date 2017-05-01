package edu.nist.nistlogger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by SiD on 4/7/2017.
 */
public class MyBaseAdapter extends BaseAdapter {
    Context context;

    ArrayList<MyData> datalist;

    public MyBaseAdapter(Context context, ArrayList<MyData> list) {

        this.context = context;
        datalist = list;
    }

    @Override
    public int getCount() {

        return datalist.size();
    }

    @Override
    public Object getItem(int position) {

        return datalist.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup arg2) {
        MyData datafetcher = datalist.get(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.logger, null);

        }

        TextView num = (TextView) convertView.findViewById(R.id.textView2);
        num.setText(datafetcher.getNumber());
        TextView date_view = (TextView) convertView.findViewById(R.id.textView3);
        date_view.setText(datafetcher.getDate());
        ImageView is_picked = (ImageView) convertView.findViewById(R.id.imageView2);
        if(datafetcher.getPicked().equals("yes"))
            is_picked.setImageResource(R.drawable.yes);
        else if (datafetcher.getPicked().equals("no"))
            is_picked.setImageResource(R.drawable.no);

        return convertView;
    }
}

