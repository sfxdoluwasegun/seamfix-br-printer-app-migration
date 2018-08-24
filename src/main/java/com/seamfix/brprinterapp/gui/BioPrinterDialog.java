package com.seamfix.brprinterapp.gui;

import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Window;

public class BioPrinterDialog extends BioPrinterStage{

    public BioPrinterDialog(Window window, Scene scene, String title, boolean resizable) {

        super(scene, title, true, resizable);
        initModality(Modality.APPLICATION_MODAL);
        initOwner(window);
    }
}
