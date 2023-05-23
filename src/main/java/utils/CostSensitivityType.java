package utils;
    //scegliere tra i due tipi di cost sensitivity (sensibilità ai costi) per la classificazione dei file
    public enum CostSensitivityType {
        SENSITIVITY_THRESHOLD, // più semplice uno dei due
        SENSITIVITY_LEARNING //balancing dei costi di classificazione errata (FP e FN)
    }

