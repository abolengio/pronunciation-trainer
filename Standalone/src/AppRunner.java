import ab.trainer.PropertiesProvider;
import ab.trainer.Trainer;
import ab.trainer.gui.FileChooser;
import ab.trainer.gui.TrainerFrame;

import javax.swing.*;

public class AppRunner {

    public static void main(String[] args) {
        Trainer trainer = new Trainer();
        JFrame applicationFrame = new JFrame();
        PropertiesProvider propertiesProvider = new PropertiesProvider("/home/ab/.pronunciation-trainer/user.properties");
        FileChooser fileChooser = new FileChooser(applicationFrame, propertiesProvider);
        new TrainerFrame(trainer, applicationFrame, fileChooser, propertiesProvider);
    }
}
