package qupath.extension.tracking.gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import qupath.extension.tracking.gui.controllers.actions.ChooseDirAction;
import qupath.extension.tracking.gui.controllers.actions.RecursiveSearchAction;
import qupath.extension.tracking.gui.controllers.actions.RunBatchAnalysisAction;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static qupath.lib.gui.tma.TMASummaryViewer.logger;


public class BatchAnalysisController implements Initializable {

    @FXML
    public TextField InputDirectoryField, OutputDirectoryField;
    @FXML
    private TextField RegexField, OutputFileName;
    @FXML
    private Button ChooseInputDirButton, SearchButton, ChooseOutputDirButton, RunButton;
    @FXML
    private CheckBox RecursiveCheckBox;
    @FXML
    private ListView FileList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        InputDirectoryField.setText("/home/alan/Documents/github/Tracking.Visualisations/resources/Tracking Folder");
        RegexField.setText(".*Tracking data for image 05.txt");
        OutputDirectoryField.setText(
                System.getProperty("user.home") +
                        File.separator +
                        "Documents"
        );

        RecursiveCheckBox.setSelected(true);
        ChooseInputDirButton.setOnAction(new ChooseDirAction(this.InputDirectoryField));
        SearchButton.setOnAction(
            new RecursiveSearchAction(
                InputDirectoryField,
                RegexField,
                RecursiveCheckBox,
                FileList
            )
        );
        ChooseOutputDirButton.setOnAction(new ChooseDirAction(this.OutputDirectoryField));
        RunButton.setOnAction(
            new RunBatchAnalysisAction(
                FileList,
                OutputDirectoryField,
                OutputFileName
            )
        );
    }
}
