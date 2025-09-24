package main.java.utils;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileHelper {

    private final Stage stage;

    public FileHelper(Stage stage) {
        this.stage = stage;
    }

    public static String getTimeStampString(){
        return java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("ddMMyy_HHmmss"));
    }

    // Save string to a file at a given path
    public void saveFile(String filePath, String data) throws Exception {
        Path path = Path.of(filePath);
        Files.createDirectories(path.getParent()); // ensure folder exists
        Files.write(path, data.getBytes());
    }

    // Prompt user to choose save location, then save
    public void saveFileAt(String root, String prefix, String sufix, String data) throws Exception {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Position");
        chooser.setInitialDirectory(new File(root));

        // timestamped default filename
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("ddMMyy_HHmmss"));
        chooser.setInitialFileName(prefix + timestamp + "." + sufix);

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Gomoku Position Files", "*." + sufix)
        );

        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            saveFile(file.getAbsolutePath(), data);
        }
    }

    // Read file content from a given path
    public String readFile(String filePath) throws Exception {
        Path path = Path.of(filePath);
        return Files.readString(path);
    }

    // Prompt user to choose file, then read content
    public String readFileAt(String root, String sufix) throws Exception {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import");
        chooser.setInitialDirectory(new File(root));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Gomoku Files", "*." + sufix)
        );

        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            return readFile(file.getAbsolutePath());
        }
        return null;
    }
}
