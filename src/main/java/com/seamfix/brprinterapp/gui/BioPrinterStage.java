package com.seamfix.brprinterapp.gui;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.ToString;

/**
 * Created by rukevwe on 8/2/2017.
 */

@ToString
public class BioPrinterStage extends Stage {


        public BioPrinterStage() {
            getIcons().add(new Image("/com/seamfix/brprinterapp/img/bioregistra-icon.png"));
        }


        public BioPrinterStage(Scene scene) {
            this();
            setScene(scene);
        }

        public BioPrinterStage(Scene scene, String title) {
            this(scene);
            setTitle(title);
        }

        public BioPrinterStage(Scene scene, String title, boolean dialogMode, boolean resizeable) {
            this(scene, title);
            if (dialogMode) {
                initModality(Modality.APPLICATION_MODAL);
            }
            setResizable(resizeable);
        }
}
