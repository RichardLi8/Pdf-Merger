/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.pdfproject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;



/**
 * FXML Controller class
 *
 * @author Richa
 */
public class MainController{
    public Button AddFilesButton;
    public Button MergePDFButton;
    public ToggleButton ToggleView;
    public ScrollPane ParentPane;
    public FlowPane DisplayPane;
    public Button ClearButton;
    
    
    enum View{PDF_View, Page_View};
    View Mode = View.PDF_View;
    List<PDF> pdfs = new ArrayList();
    List<Page> pages = new ArrayList();
    HashMap<Integer, Node> locations = new HashMap();
    
    static class PDF{
        PDDocument doc;
        String name;
        Image image;
        
        PDF(String name, PDDocument doc, Image image){
            this.name = name;
            this.doc = doc;
            this.image = image;
        }
    }
    static class Page{
        PDPage page;
        String name;
        Image image;
        
        Page(String name, PDPage page, Image image){
            this.name = name;
            this.page = page;
            this.image = image;
        }
    }
    
    void initializeListners(){
        ParentPane.widthProperty().addListener((observable, oldValue, newValue)->{
            DisplayPane.setPrefWrapLength(newValue.doubleValue());
        });
        ParentPane.addEventFilter(ScrollEvent.ANY, event -> {
            
        });
    }
    
    void addToLists(List<File> files, List<PDDocument> documents, List<List<Image>> images){
        for(int i = 0; i < files.size(); i++){
            PDF pdf = new PDF(files.get(i).getName(), documents.get(i), images.get(0).get(0));
            pdfs.add(pdf);
            
            for(int j = 0; j < documents.get(i).getNumberOfPages(); j++){
                Page page = new Page(j + "_" + files.get(i).getName(), documents.get(i).getPage(j), images.get(i).get(j));
                pages.add(page);
            }
        }
    }
    
    public void createImageCell(Image image, String name, int id) throws IOException{
        FXMLLoader fxmlLoader = new FXMLLoader(MainController.class.getResource("/fxml/ImageCell.fxml"));
        
        Node cell = fxmlLoader.load();
        ImageCellController controller = fxmlLoader.getController();
        
        controller.image.setImage(image);
        controller.name.setText(name);
        
        controller.DisplayPane = DisplayPane;
        controller.locations = locations;
        controller.id = id;
        
        if(Mode == View.PDF_View){
            controller.documents = pdfs;
        }
        else if(Mode == View.Page_View){
            controller.documents = pages;
        }
        
        locations.put(id, cell);
        DisplayPane.getChildren().add(cell);
    }
    
    void Display() throws IOException{
        locations.clear();
        DisplayPane.getChildren().clear();
        
        if(Mode == View.PDF_View){
            for(int i = 0; i < pdfs.size(); i++){
                PDF pdf = pdfs.get(i);
                createImageCell(pdf.image, pdf.name, i);
            }
        }
        else if(Mode == View.Page_View){
            for(int i = 0; i < pages.size(); i++){
                Page page = pages.get(i);
                createImageCell(page.image, page.name, i);
            }
        }
    }
    
    @FXML
    public void AddFiles() throws Exception{
        var files = App.selectFiles();
        var docs = App.load(files);
        var images = App.render(docs);
        addToLists(files, docs, images);
        System.out.println("Done");
        Display();
    }
    @FXML
    public void MergePDF() throws IOException{
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Pdf Files", "*.pdf"));
        fileChooser.setTitle("Merge PDF");
        fileChooser.setInitialFileName("Merged");
        File save = fileChooser.showSaveDialog(App.stage);
        
        if(save == null) return;
        
        List<PDPage> p = new ArrayList();
        if(Mode == View.PDF_View){
            for(PDF pdf : pdfs){
                for(PDPage page : pdf.doc.getPages()){
                    p.add(page);
                }
            }
        }
        else if(Mode == View.Page_View){
            for(Page page : pages){
                p.add(page.page);
            }
        }
        
        PDDocument merged = App.merge(p);
        merged.save(save);
    }
    @FXML
    public void UpdateButton() throws IOException{
        if(Mode == View.PDF_View){
            ToggleView.setText("Page View");
            Mode = View.Page_View;
        }
        else if(Mode == View.Page_View){
            ToggleView.setText("PDF View");
            Mode = View.PDF_View;
        }
        Display();
    }
    @FXML
    public void ClearButton() throws IOException{
        for(var pdf : pdfs){
            pdf.doc.close();
        }
        pdfs.clear();
        pages.clear();
        DisplayPane.getChildren().clear();
        locations.clear();
    }
    @FXML
    public void onDragOver(DragEvent event){
        if(event.getDragboard().hasString()){
            double y = event.getY();
            double ScrollPaneHeight = ParentPane.getViewportBounds().getHeight();
            double vValue = ParentPane.getVvalue();
            
//            System.out.println("Y: " + y);
//            System.out.println("Scroll: " + ScrollPaneHeight);
//            System.out.println("V: " + vValue);
                       
            double threshold = /*10*/ ScrollPaneHeight/40;
            double speed = /*0.03*/ ScrollPaneHeight/13333;
            if(y <= threshold) ParentPane.setVvalue(vValue-speed);
            else if(y >= ScrollPaneHeight-threshold) ParentPane.setVvalue(vValue+speed);
            
        }
    }
}
