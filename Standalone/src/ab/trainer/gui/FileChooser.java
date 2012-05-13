package ab.trainer.gui;

import ab.trainer.ApplicationProperty;
import ab.trainer.PropertiesProvider;

import javax.swing.*;
import java.io.File;

public class FileChooser {

    private JFrame applicationFrame;
    private PropertiesProvider propertiesProvider;

    public FileChooser(JFrame applicationFrame, PropertiesProvider propertiesProvider) {
        this.applicationFrame = applicationFrame;
        this.propertiesProvider = propertiesProvider;
    }

    public void selectFile(FileSelectedListener fileSelectedListener) {
        // Note: source for ExampleFileFilter can be found in FileChooserDemo,
        // under the demo/jfc directory in the Java 2 SDK, Standard Edition.
        /*
                ExampleFileFilter filter = new ExampleFileFilter();
                filter.addExtension("mp3");
                filter.setDescription("JPG & GIF Images");
                chooser.setFileFilter(filter);
        */
        JFileChooser chooser = new JFileChooser();
        String lastOpenedDirStr = propertiesProvider.get(ApplicationProperty.lastOpenedDirectory);
        if(lastOpenedDirStr != null){
            File lastOpenedDirectory = new File(lastOpenedDirStr);
            if(lastOpenedDirectory.exists())
                chooser.setCurrentDirectory(lastOpenedDirectory);
        }
        int returnVal = chooser.showOpenDialog(this.applicationFrame);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            fileSelectedListener.fileSelected(selectedFile);
            propertiesProvider.set(ApplicationProperty.lastOpenedDirectory, selectedFile.getParent());
        }
    }
}
