package Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DataManipulation {
       SimpleDateFormat formatoData;
       private Date data;

       public String DataManipulationFormat(String summary) throws ParseException {
        if(summary.contains("T")) {
            formatoData = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        }

        else {
            formatoData = new SimpleDateFormat("yyyy-MM-dd");
        }

        // Conversione della stringa in una data
        if (formatoData != null)
             data = formatoData.parse(summary);

        // Definizione del formato di output desiderato
        SimpleDateFormat formatoOutput = new SimpleDateFormat("dd/MM/yyyy");

        // Formattazione della data in una stringa
        String dataFormattata = formatoOutput.format(data);
        return dataFormattata;
    }
    //convert la data in formato stringa del tipo "dd/MM/yyyy" in un oggetto di tipo Date
    public Date convertStringToDate(String dataString) throws ParseException {
        if(dataString.contains("T")) {
            formatoData = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        }
        else {
            formatoData = new SimpleDateFormat("dd/MM/yyyy");
        }
        data = formatoData.parse(dataString);
        return data;
    }

    public String convertDate(Date inputDate) throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        Date date = inputFormat.parse(inputDate.toString());

        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
        String outputDate = outputFormat.format(date);

           return outputDate;
    }
}
