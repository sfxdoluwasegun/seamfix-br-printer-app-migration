package com.seamfix.brprinterapp.FargoPrinter;

import lombok.extern.log4j.Log4j;

@Log4j
public class PrinterClass {

    public static void main(String[] args) {

        startPrinting("HDP5000 Card Printer");

    }

    public static void startPrinting(String printerName) {
        // Make first thread
        MakeCardOperation m_makeCard1;

        // Each thread of the Java application may interact with the Fargo
        // Printer SDK independently as long as they do so with seperate
        // instances of the FargoPrinter class.
        m_makeCard1 = new MakeCardOperation(printerName);

        // Do the calls to actually produce the card.
        m_makeCard1.produceCard();

        // So the JRE is happy, exit with 0.
//        System.exit(0);
    }
}
