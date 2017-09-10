package com.example.android.quakereport;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by nirmal vats on 6/17/2017.
 */

public class WordAdapter extends ArrayAdapter<Earthquake> {

    private static  final  String LOCATION_SEPERATOR=" of ";

    public WordAdapter(Context context, List<Earthquake> pEarthquake){
        super(context,0, pEarthquake);
    }


    @Override
    public View getView(int position,  View convertView, ViewGroup parent) {

        View view = convertView;

        if(view==null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_layout,parent,false);

        }

        Earthquake earthquake = getItem(position);
        TextView mag =(TextView)view.findViewById(R.id.magnitude);
        double magni=earthquake.getmMagnitude();
        DecimalFormat decimalFormat= new DecimalFormat("0.0");
        String magnitude = decimalFormat.format(magni);
        mag.setText(magnitude);

        // Set the proper background color on the magnitude circle.
        // Fetch the background from the TextView, which is a GradientDrawable.
        GradientDrawable magnitudeCircle = (GradientDrawable) mag.getBackground();

        // Get the appropriate background color based on the current earthquake magnitude
        int magnitudeColor = getMagnitudeColor(earthquake.getmMagnitude());

        // Set the color on the magnitude circle
        magnitudeCircle.setColor(magnitudeColor);

        TextView firstplace= (TextView) view.findViewById(R.id.pri_location);
        TextView secondplace= (TextView) view.findViewById(R.id.sec_location);
        String location =earthquake.getmPlace();
        String pri_location;
        String sec_location;
        if(location.contains(LOCATION_SEPERATOR)){
            String[] parts= location.split(LOCATION_SEPERATOR);
             pri_location=  parts[0]+LOCATION_SEPERATOR;
            sec_location= parts[1];
        }
        else
        {
            pri_location= getContext().getString(R.string.near_the);
            sec_location= location;
        }

        firstplace.setText(pri_location);
        secondplace.setText(sec_location);
        Date mdate= new Date(earthquake.getmDate());
        TextView date =(TextView) view.findViewById(R.id.date);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        String dateToDisplay= dateFormat.format(mdate);
        date.setText(dateToDisplay);

        TextView time =(TextView)view.findViewById(R.id.time);
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        String timetodisplay =timeFormat.format(mdate);
        time.setText(timetodisplay);

        return view;

    }

    private int getMagnitudeColor(double magnitude){
        int magnitudeColorResourceId;
        int floormagnitude = (int) Math.floor(magnitude);
        switch (floormagnitude){
            case 0:
            case 1:
                magnitudeColorResourceId = R.color.magnitude1;
                break;
            case 2:
                magnitudeColorResourceId = R.color.magnitude2;
                break;
            case 3:
                magnitudeColorResourceId = R.color.magnitude3;
                break;
            case 4:
                magnitudeColorResourceId = R.color.magnitude4;
                break;
            case 5:
                magnitudeColorResourceId = R.color.magnitude5;
                break;
            case 6:
                magnitudeColorResourceId = R.color.magnitude6;
                break;
            case 7:
                magnitudeColorResourceId = R.color.magnitude7;
                break;
            case 8:
                magnitudeColorResourceId = R.color.magnitude8;
                break;
            case 9:
                magnitudeColorResourceId = R.color.magnitude9;
                break;
            default:
                magnitudeColorResourceId = R.color.magnitude10plus;
                break;
        }
return ContextCompat.getColor(getContext(),magnitudeColorResourceId);
        }

    }


