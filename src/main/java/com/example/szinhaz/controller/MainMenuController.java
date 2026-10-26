package com.example.szinhaz.controller;

import com.example.szinhaz.model.Play;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainMenuController {

    @FXML
    private Label messageLabel;

    @FXML
    void handleTestButton(ActionEvent event) {
        Play testPlay = new Play("Hamlet", "John Doe", 150);
        messageLabel.setText("Loaded: " + testPlay.getTitle());
    }
}