package utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DataManipulation {
       SimpleDateFormat formatoData;
       private Date data;

       public String dataManipulationFormat(String summary) throws ParseException {
        if(summary.contains("T")) {
            formatoData = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        }
        else {
            formatoData = new SimpleDateFormat("yyyy-MM-dd");
        }

        // Conversione della stringa in una data
             data = formatoData.parse(summary);

        // Definizione del formato di output desiderato
        SimpleDateFormat formatoOutput = new SimpleDateFormat("dd/MM/yyyy");

        return formatoOutput.format(data);
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
}
