package com.mycompany.pdfproject;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;


public class App extends Application {

    public static Scene scene;
    public static Stage stage;
    
    public static void main(String[] args) throws IOException{
        launch(args);
    }
    
    public static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
        
        try{
            return fxmlLoader.load();
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
        System.exit(-1);
        return null;
    }
    
    @Override
    public void start(Stage stage) throws IOException{
        App.stage = stage;
        stage.setTitle("PDF Merger");
        
        FXMLLoader fxmlLoader = new FXMLLoader(MainController.class.getResource("/fxml/Main.fxml"));
        Parent parent = fxmlLoader.load();
        ((MainController)fxmlLoader.getController()).initializeListners();
        
        scene = new Scene(parent);
        stage.setScene(scene);
        stage.show();
    }
    
    public static List<Image> render(PDDocument doc) throws Exception{
        PDFRenderer pdfRenderer = new PDFRenderer(doc);
        
        List<Image> images = new ArrayList();
        
        for(int i = 0; i < doc.getNumberOfPages(); i++){
            BufferedImage bi = pdfRenderer.renderImage(i);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(bi,"png", os); 
            InputStream fis = new ByteArrayInputStream(os.toByteArray());

            Image image = new Image(fis);
            images.add(image);
        }
        return images;
    }
    
    public static List<List<Image>> render(List<PDDocument> docs) throws Exception{
        List<List<Image>> images = new ArrayList();
        ExecutorService threadPool = Executors.newFixedThreadPool(docs.size());
        List<Future<List<Image>>> futures = new ArrayList();
        
        for(PDDocument doc : docs){
            futures.add(
              threadPool.submit(()->{
                   return render(doc);
                })
            );
        }
        for(var f : futures){
            images.add(f.get());
        }
        return images;
    }
    
    public static List<File> selectFiles(){
        FileChooser fileChooser = new FileChooser();
        
        fileChooser.setTitle("Select Pdf");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Supported Files", "*.pdf", "*.jpg", "*.jpeg", "*.png"),
            new FileChooser.ExtensionFilter("Pdf Files", "*.pdf"),
            new FileChooser.ExtensionFilter("JPEG Files", "*.jpg", "*.jpeg"),
            new FileChooser.ExtensionFilter("PNG Files", "*.png")
        );

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);
        if(selectedFiles == null){
            selectedFiles = new ArrayList();
        }
        
        return selectedFiles;
    }
    
    public static PDDocument merge(List<PDPage> pages){
        PDDocument newDoc = new PDDocument();
        for(PDPage page : pages){
            newDoc.addPage(page);
        }
        return newDoc;
    }
    
    public static List<PDDocument> load(List<File> files) throws IOException{
        List<PDDocument> docs = new ArrayList();
        for(File f : files){
            if(f.getName().endsWith(".pdf")){
                docs.add(PDDocument.load(f));
            }
            else{
                PDDocument doc = new PDDocument();
                PDPage page = new PDPage();
                doc.addPage(page);
                
                BufferedImage image = ImageIO.read(f);
                
                PDImageXObject pdImage = LosslessFactory.createFromImage(doc, image);
                page.setMediaBox(new PDRectangle(pdImage.getWidth(), pdImage.getHeight()));
                try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                    contentStream.drawImage(pdImage, 0, 0, pdImage.getWidth(), pdImage.getHeight());
                }
                
                docs.add(doc);
            }
        }
        return docs;
    }
}