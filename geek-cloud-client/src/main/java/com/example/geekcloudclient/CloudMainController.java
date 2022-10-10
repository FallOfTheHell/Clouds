package com.example.geekcloudclient;



import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.CloudMessage;
import model.FileMessage;
import model.FileRequest;
import model.ListMessage;
import server.nio.DaemonThreadFactory;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class CloudMainController implements Initializable {
    public ListView<String> clientView;
    public ListView<String> serverView;
    public TextArea renameClient;
    public TextArea renameServer;
    private String currentDirectory;

    private Network<ObjectDecoderInputStream, ObjectEncoderOutputStream> network;

    private Socket socket;

    private boolean needReadMessages = true;
    private DaemonThreadFactory factory;

    @FXML
    public void downloadFile(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        network.getOutputStream().writeObject(new FileRequest(fileName));
    }

    public void sendToServer(ActionEvent actionEvent) throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        network.getOutputStream().writeObject(new FileMessage(Path.of(currentDirectory).resolve(fileName)));
    }

    public void deleteFilesClient(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        Files.delete(Paths.get("server_files", fileName));
        fillView(serverView, getFiles(fileName)); // тут обновление очень коряво работает,
        // не понимаю что нужно передать в getFiles() чтобы нормально список обновлялся...
    }

    public void deleteFilesServer(ActionEvent actionEvent) throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        Files.delete(Paths.get(currentDirectory, fileName));
        fillView(clientView, getFiles(currentDirectory));
    }

    public void renameFileClient(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        Path source0 = Paths.get("server_files", fileName);
        Files.move(source0, source0.resolveSibling(renameClient.getText().trim()));
        serverView.getItems().addAll(); // тут тоже почему-то не обновляется список
    }
    public void renameFileServer(ActionEvent actionEvent) throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        Path source0 = Paths.get(currentDirectory, fileName);
        Files.move(source0, source0.resolveSibling(renameServer.getText().trim()));
        fillView(clientView, getFiles(currentDirectory));
    }



    public static void deleteDirectory(File directory) throws IOException {
        Files.walk(directory.toPath())
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .forEach(File::delete);
    }



    private static void dir(){
        String[] pathnames;
        File f = new File("server_files");
        pathnames = f.list();
        for (String pathname : pathnames) {
            System.out.println(pathname);
        }
    }

    private void readMessages() {
        try {
            while (needReadMessages) {
                CloudMessage message = (CloudMessage) network.getInputStream().readObject();
                if (message instanceof FileMessage fileMessage) {
                    Files.write(Path.of(currentDirectory).resolve(fileMessage.getFileName()), fileMessage.getBytes());
                    Platform.runLater(() -> fillView(clientView, getFiles(currentDirectory)));
                } else if (message instanceof ListMessage listMessage) {
                    Platform.runLater(() -> fillView(serverView, listMessage.getFiles()));
                }
            }
        } catch (Exception e) {
            System.err.println("Server off");
            e.printStackTrace();
        }
    }

    private void initNetwork() {
        try {
            socket = new Socket("localhost", 8189);
            network = new Network<>(
                    new ObjectDecoderInputStream(socket.getInputStream()),
                    new ObjectEncoderOutputStream(socket.getOutputStream())
            );
            factory.getThread(this::readMessages, "cloud-client-read-thread")
                    .start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        needReadMessages = true;
        factory = new DaemonThreadFactory();
        initNetwork();
        setCurrentDirectory(System.getProperty("user.home"));
        fillView(clientView, getFiles(currentDirectory));
        clientView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selected = clientView.getSelectionModel().getSelectedItem();
                File selectedFile = new File(currentDirectory + "/" + selected);
                if (selectedFile.isDirectory()) {
                    setCurrentDirectory(currentDirectory + "/" + selected);
                }
            }
        });
    }
    private void setCurrentDirectory(String directory) {
        currentDirectory = directory;
        fillView(clientView, getFiles(currentDirectory));
    }
    private void fillView(ListView<String> view, List<String> data) {
        view.getItems().clear();
        view.getItems().addAll(data);
    }
    private List<String> getFiles(String directory) {
        // file.txt 125 b
        // dir [DIR]
        File dir = new File(directory);
        if (dir.isDirectory()) {
            String[] list = dir.list();
            if (list != null) {
                List<String> files = new ArrayList<>(Arrays.asList(list));
                files.add(0, "..");
                return files;
            }
        }
        return List.of();
    }

}