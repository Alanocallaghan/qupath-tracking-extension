package qupath.extension.tracking.gui.controllers.actions;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import qupath.lib.gui.commands.interfaces.PathCommand;

import java.io.File;


public class RecursiveSearchAction implements EventHandler, PathCommand {

    private final String regex;
    private final boolean recursive;
    private final ListView listView;
    private File directory;

    public RecursiveSearchAction(
            TextField directoryField,
            TextField regexField,
            CheckBox recursiveCheckBox,
            ListView listView) {
        super();
        this.directory = new File(directoryField.getText());
        this.regex = regexField.getText();
        this.recursive = recursiveCheckBox.isSelected();
        this.listView = listView;
    }
    @Override
    public void handle(Event event) {
        listView.setItems(searchDirectory(directory, regex, recursive));
    }

    private ObservableList<File> searchDirectory(File directory, String regex, boolean recursive) {
        ObservableList<File> fileArrayList = FXCollections.observableArrayList();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file: files) {
                if (file.isDirectory()) {
                    if (recursive) {
                        fileArrayList.addAll(searchDirectory(file, regex, true));
                    }
                } else {
                    if (file.getName().matches(regex)) {
                        fileArrayList.add(file);
                    }
                }
            }
        }
        return(fileArrayList);
    }
    @Override
    public void run() {
        handle(null);
    }

}
