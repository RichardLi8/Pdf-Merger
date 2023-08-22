/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pdfproject;

import java.util.HashMap;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Richa
 */
public class ImageCellController {
    public ImageView image;
    public Label name;
    public VBox vbox;

    public FlowPane DisplayPane;
    public int id;
    public HashMap<Integer, Node> locations;
    public List documents;
    
    @FXML
    private void onDragDetected(MouseEvent event) {
//        System.out.println("Drag Detected:" + name.getText());
        vbox.getStyleClass().remove("normal");
        vbox.getStyleClass().add("dragging");
        Dragboard dragboard = vbox.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.putString(""+id);
        dragboard.setContent(content);
        event.consume();
    }
    
    public void update(int dragId, int curId){
        Node dragCell = locations.get(dragId);
        Node curCell = locations.get(curId);
        int dragIndex = DisplayPane.getChildren().indexOf(dragCell);
        int curIndex = DisplayPane.getChildren().indexOf(curCell);
        
//        System.out.println("Drag Index: " + dragIndex);
//        System.out.println("Curr Index: " + curIndex);
        
        var node = DisplayPane.getChildren().remove(dragIndex);
        DisplayPane.getChildren().add(curIndex, node);
        
        var doc = documents.remove(dragIndex);
        documents.add(curIndex, doc);
    }
    @FXML
    private void onDragEntered(DragEvent event) {
        if (event.getGestureSource() != vbox && event.getDragboard().hasString()) {
            int dragId = Integer.parseInt(event.getDragboard().getString());
            int curId = id;
            update(dragId, curId);
        }
        event.consume();
    }
    @FXML
    private void onDragDone(DragEvent event){
        vbox.getStyleClass().remove("dragging");
        vbox.getStyleClass().add("normal");
        event.consume();
    }
    @FXML
    private void DeleteCell(){
        Node curCell = locations.get(id);
        int curIndex = DisplayPane.getChildren().indexOf(curCell);
        DisplayPane.getChildren().remove(curIndex);
        documents.remove(curIndex);
    }
}
