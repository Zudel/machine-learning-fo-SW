package Control;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Metrics {
    public int countLOC(String filePath) throws IOException {
        int loc = 0; // inizializza il contatore di LOC a 0
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(filePath)); // crea un BufferedReader per leggere il file
        while ((line = reader.readLine()) != null) { // leggi il file linea per linea
            if (!line.trim().isEmpty()) { // controlla se la linea non è vuota
                loc++; // incrementa il contatore di LOC se la linea non è vuota
            }
        }

        reader.close(); // chiudi il BufferedReader

        System.out.println("Numero di linee di codice (LOC): " + loc); // stampa il numero di LOC
        return loc;
    }
}
