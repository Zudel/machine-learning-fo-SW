package org.example.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

   public class DataManipulation {
    public String DataManipulationFormat(String summary) throws ParseException {
        // Definizione del formato della data
        SimpleDateFormat formatoData = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");


        // Conversione della stringa in una data
        Date data = formatoData.parse(summary);

        // Definizione del formato di output desiderato
        SimpleDateFormat formatoOutput = new SimpleDateFormat("dd/MM/yyyy");

        // Formattazione della data in una stringa
        String dataFormattata = formatoOutput.format(data);
        return dataFormattata;
    }
}
