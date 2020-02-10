package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;


import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller implements Initializable {

    @FXML
    TextField efi_edit;
    @FXML
    TextField efi_name;
    @FXML
    Label status_create_new;
    @FXML
    Button delete_entry_button;
    @FXML
    ListView<String> listview_create_new;
    @FXML
    ListView<String> list_entry;
    @FXML
    ListView<String> listview_change_order;
    @FXML
    ListView<String> listview_delete_efi;

    private ArrayList<String> string;
    private String[] boot_entry_order;
    private String efi_path;
    private ObservableList<String> list=FXCollections.observableArrayList();
    private ObservableList<String> list_create_new=FXCollections.observableArrayList();
    private ObservableList<String> list_delete=FXCollections.observableArrayList();
    private final ObservableList<String> list_change_order=FXCollections.observableArrayList();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeLogic();
    }

    public void refreshlist(ActionEvent event){
        initializeLogic();
    }

    public void choose_efi(ActionEvent event){
        boolean efi_part_selection=true;
        if(listview_create_new.getSelectionModel().isEmpty())
        {
            efi_part_selection = false;
        }
        if(efi_part_selection)
        {
        String efi_part = listview_create_new.getFocusModel().getFocusedItem();
        System.out.println(efi_part);
        CommandGenerator.execute("mount " + efi_part + " /mnt");
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File("/mnt"));
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("EFI File", "*.efi"));
            File file = fileChooser.showOpenDialog(null);
            efi_path=file.getAbsolutePath();
            efi_edit.setText(efi_path);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        }
        else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Choose the choose the EFI partation  before click finish.", ButtonType.OK);
            alert.showAndWait();
        }

    }

    public void close(ActionEvent event){
        System.exit(0);
    }

    public void create_entry(ActionEvent event){
        boolean efi_part_selection=true;
        if(listview_create_new.getSelectionModel().isEmpty())
        {
            efi_part_selection = false;
        }

        if(efi_name.getText().trim().equals(""))
        {
            Alert alert = new Alert(Alert.AlertType.WARNING, "EFI entry name can't be empty.", ButtonType.OK);
            alert.showAndWait();
        }
        else if( ! efi_part_selection)
        {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Choose the choose the EFI partation  before click finish.", ButtonType.OK);
            alert.showAndWait();
        }
        else if(efi_edit.getText().trim().equals(""))
        {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Choose the *.efi file before click finish.", ButtonType.OK);
            alert.showAndWait();
        }
         else {
            String efi_loaction = efi_edit.getText().substring(4, efi_edit.getLength()).replace("/","\\\\").replace(" ","\\ ");
            CommandGenerator commandGenerator = new CommandGenerator();
            String disk = listview_create_new.getFocusModel().getFocusedItem();
            String disk_name = strMatcher("/dev/[a-z]*", disk).get(0);
            String partation = strMatcher("(\\d)(\\d*)", disk).get(0);
            String entry_name = efi_name.getText().replace(" ","\\ ");
            String command = "efibootmgr -c -d " + disk_name + " -p " + partation + " -L " + entry_name + " -l " + efi_loaction;
            status_create_new.setText(command);
            String alertContent = "Do you want to create a Boot entry " + entry_name + " with Efi loader " + efi_loaction + " ?";
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, alertContent, ButtonType.YES, ButtonType.NO);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {
                System.out.println(command);
                commandGenerator.execute(command);
                initializeLogic();
            }
        }
    }

    public void delete_entry(ActionEvent event)
    {
        boolean efi_part_selection=true;
        if(listview_delete_efi.getSelectionModel().isEmpty())
        {
            efi_part_selection = false;
        }
        if( ! efi_part_selection)
        {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Select the EFI entry which you want to delete.", ButtonType.OK);
            alert.showAndWait();
        }
        else
        {
            String boot_entry = listview_delete_efi.getFocusModel().getFocusedItem();
            String boot_num   = strMatcher("(\\d)(\\d*)",boot_entry).get(0);
            //System.out.println(boot_num);
            CommandGenerator commandGenerator=new CommandGenerator();
            String command = "efibootmgr -b " + boot_num + " -B";

            String alertContent = "Do you want to delete " + boot_entry + " ?";
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, alertContent, ButtonType.YES, ButtonType.NO);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {
                System.out.println(command);
                commandGenerator.execute(command);
                initializeLogic();
            }
        }
    }

    public void up_entry(ActionEvent event)
    {
        boolean boot_entry_selection=true;
        if(listview_change_order.getSelectionModel().isEmpty())
        {
            boot_entry_selection = false;
        }
        if(boot_entry_selection) {
            System.out.println("Up entry clicked");
            int item = listview_change_order.getSelectionModel().getSelectedIndex();
            String itemText = listview_change_order.getSelectionModel().getSelectedItem();
            if (item != 0) {
                String itemSwap = listview_change_order.getItems().get(item - 1);
                list_change_order.set(item, itemSwap);
                list_change_order.set(item - 1, itemText);
                listview_change_order.getSelectionModel().select(item - 1);
            }
        }

        else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "First select anyone of the boot entry to alter", ButtonType.OK);
            alert.showAndWait();
        }
    }
    public void down_entry(ActionEvent event)
    {
        boolean boot_entry_selection=true;
        if(listview_change_order.getSelectionModel().isEmpty())
        {
            boot_entry_selection = false;
        }
        if(boot_entry_selection) {
        System.out.println("Down entry clicked");
        int item = listview_change_order.getSelectionModel().getSelectedIndex();
        String itemText = listview_change_order.getSelectionModel().getSelectedItem();
        if(item != list_change_order.size()-1) {
            String itemSwap = listview_change_order.getItems().get(item + 1);
            list_change_order.set(item, itemSwap);
            list_change_order.set(item + 1, itemText);
            listview_change_order.getSelectionModel().select(item + 1);
        }
    }

        else {
        Alert alert = new Alert(Alert.AlertType.WARNING, "First select anyone of the boot entry to alter", ButtonType.OK);
        alert.showAndWait();
    }

    }

    public void change_boot_order(ActionEvent event){
        String result="";
        for(int i=0;i<list_change_order.size();i++)
        {
            result += strMatcher("[0-9][0-9][0-9][0-9]",list_change_order.get(i)).get(0)+" ";
        }
        System.out.println(result);
        CommandGenerator commandGenerator=new CommandGenerator();
        String command ="efibootmgr -o "+result.trim().replace(" ",",");

        String alertContent = "Do you want to alter the boot order ?";
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, alertContent, ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            System.out.println(command);
            commandGenerator.execute(command);
            initializeLogic();
        }
    }

    public void reset_entry(ActionEvent event)
    {
        initializeLogic();
    }

    private ArrayList<String> strMatcher(String match, String data){
        ArrayList<String> result=new ArrayList<>();
        Pattern pattern=Pattern.compile(match);
        Matcher matcher=pattern.matcher(data);
        while (matcher.find())
        {
            //System.out.println(matcher.group());
            result.add(matcher.group());
        }
        return result;
    }

    private void initializeLogic(){
        //listview_create_new.setItems(list);
        ArrayList<String> partation;
        String array="";
        list_change_order.clear();
        list.clear();
        list_delete.clear();
        list_create_new.clear();
        String result = CommandGenerator.execute("fdisk -l");
        String str="/dev/[a-z]*[0-9]";
        partation=strMatcher(str,result);
        String efi_list= CommandGenerator.execute("efibootmgr");
        String patn = "Boot[0-9](.*)[^\\n]";
        ArrayList<String> order_text=strMatcher("[0-9][0-9][0-9][0-9]",strMatcher("BootOrder: (.*)",efi_list).get(0));
        for(int i=0;i<order_text.size();i++)
        {
            array += strMatcher("Boot"+order_text.get(i)+"(.*)",efi_list).get(0)+"\n";
        }
        boot_entry_order = array.split("\n");
        string=strMatcher(patn,efi_list);
        list.addAll(string);
        list_entry.setItems(list);
        list_create_new.addAll(partation);
        listview_create_new.setItems(list_create_new);
        list_delete.addAll(string);
        listview_delete_efi.setItems(list_delete);
        list_change_order.addAll(boot_entry_order);
        listview_change_order.setItems(list_change_order);
    }
}