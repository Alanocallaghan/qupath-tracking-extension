package qupath.extension.tracking.gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import qupath.extension.tracking.gui.controllers.actions.ChooseDirAction;
import qupath.extension.tracking.gui.controllers.actions.RunBatchAnalysisAction;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;


public class BatchAnalysisController implements Initializable {

    @FXML
    TextField DirectoryField, RegexField;
    @FXML
    Button ChooseDirButton, RunButton;
    @FXML
    CheckBox RecursiveCheckBox;


    public void setDirectory(File directory) {
        this.directory = directory;
    }

    File directory = new File(System.getProperty("user.home"));
    private boolean recursive;
    private String regex;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        RunButton.setOnAction(new RunBatchAnalysisAction(directory, regex, recursive));
        ChooseDirButton.setOnAction(new ChooseDirAction(this));
    }
}
