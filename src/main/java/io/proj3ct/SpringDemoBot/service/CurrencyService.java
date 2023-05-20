package io.proj3ct.SpringDemoBot.service;


import io.proj3ct.SpringDemoBot.model.Currency;
import org.checkerframework.checker.units.qual.C;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class CurrencyService {

    public static Double getCurrencyRate(String message, Currency model) throws IOException, ParseException {
        URL url = new URL("https://www.nbrb.by/api/exrates/rates/" + message + "?parammode=2");
        Scanner scanner = new Scanner((InputStream) url.getContent());
        String result = "";
        while (scanner.hasNext()){
            result +=scanner.nextLine();
        }
        JSONObject object = new JSONObject(result);

        model.setCur_ID(object.getInt("Cur_ID"));
        model.setDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(object.getString("Date")));
        model.setCur_Abbreviation(object.getString("Cur_Abbreviation"));
        model.setCur_Scale(object.getInt("Cur_Scale"));
        model.setCur_Name(object.getString("Cur_Name"));
        model.setCur_OfficialRate(object.getDouble("Cur_OfficialRate"));


        return model.getCur_OfficialRate();

    }

    public static String convert(String from) throws IOException, ParseException {
        Currency currency = new Currency();
        double dollar = CurrencyService.getCurrencyRate("USD", currency);
        double tenge = CurrencyService.getCurrencyRate("KZT", currency);
        double byntotenge = 1000 / tenge;
        double byntodollar = 1 / dollar;
        double oneTengeInDollar = byntotenge/byntodollar;
        double oneDollarInTenge = byntodollar/byntotenge;

        if(from.endsWith("$")){
            double digit = Double.parseDouble(from.substring(0, from.length()-1));
            double result = digit * oneTengeInDollar;
            return result + " тенге";
        }
        if(from.endsWith("тенге")){
            double digit = Double.parseDouble(from.substring(0, from.length() - 5));
            double result = digit * oneDollarInTenge;
            return  result + " $";
        }
        return "No such currency";
    }

    private static String getFormatDate(Currency model) {
        return new SimpleDateFormat("dd MMM yyyy").format(model.getDate());
    }
}
