package edu.rosehulman.roselabs.sharewithme.FormatData;

import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Date;

/**
 * Created by Thais Faria on 1/30/2016.
 */
public class FormatData {

    public static String formatPrice(EditText price){
        String formattedPrice = price.getText().toString();

        if(formattedPrice.contains(".")){
            int decimal = formattedPrice.indexOf(".");

            if(formattedPrice.length() -1 < decimal + 2){
                for(int i = formattedPrice.length() -1; i < decimal + 2; i++){
                    formattedPrice += 0;
                }
            }else if (formattedPrice.length() - 1 > decimal + 2) {
                formattedPrice = formattedPrice.substring(0, decimal + 3);
            }
            //else is right, do nothing
        }else{
            formattedPrice += ".00";
        }

        return formattedPrice;
    }

    public static String formatDateFromPicker(DatePicker date){
        String formattedDate = date.getYear() + "/" + (date.getMonth()+1) + "/" + date.getDayOfMonth();

        return formattedDate;
    }

    public static String formatDateToAmerican(String databaseFormat){
        String[] dateArray = databaseFormat.split("/");

        String formattedDate = dateArray[1] + "/" + dateArray[2] + "/" + dateArray[0];

        return formattedDate;
    }
}
