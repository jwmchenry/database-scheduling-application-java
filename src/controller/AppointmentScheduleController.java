package controller;

import helper.AppointmentsHelper;
import helper.ContactsHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Appointment;
import model.Contact;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * This class is responsible for populating a table view of appointments.
 */
public class AppointmentScheduleController implements Initializable {

    Stage stage;
    Parent scene;

    private boolean isInitializing;

    @FXML
    private TableColumn<Appointment, Integer> userIDCol;

    @FXML
    private RadioButton allRdBtn;

    @FXML
    private TableColumn<Appointment, Integer> apptIDCol;

    @FXML
    private TableView<Appointment> apptTableView;

    @FXML
    private TableColumn<Appointment, Contact> contactCol;

    @FXML
    private TableColumn<Appointment, Integer> customerIDCol;

    @FXML
    private TableColumn<Appointment, String> descCol;

    @FXML
    private TableColumn<Appointment, LocalDateTime> endCol;

    @FXML
    private TableColumn<Appointment, String> locationCol;

    @FXML
    private RadioButton monthlyRdBtn;

    @FXML
    private TableColumn<Appointment, LocalDateTime> startCol;

    @FXML
    private TableColumn<Appointment, String> titleCol;

    @FXML
    private TableColumn<Appointment, String> typeCol;

    @FXML
    private RadioButton weeklyRdBtn;

    @FXML
    private ComboBox<String> monthlyCmbBox;

    @FXML
    private ComboBox<Integer> yearCmbBox;

    /**
     * This method deletes an appointment from the appointment database.
     * @param event
     * @throws SQLException
     */
    @FXML
    void onActionDeleteAppointment(ActionEvent event) throws SQLException {
        if (apptTableView.getSelectionModel().isEmpty()) {
            return;
        }

        AppointmentsHelper.delete(apptTableView.getSelectionModel().getSelectedItem().getApptID());
        populateAppointments();
    }

    /**
     * This method closes the application.
     * @param event
     */
    @FXML
    void onActionExit(ActionEvent event) {
        System.exit(0);
    }

    /**
     * This method returns to the main menu.
     * @param event
     * @throws IOException
     */
    @FXML
    void onActionMenuBack(ActionEvent event) throws IOException {

        stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        scene = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/MainMenu.fxml")));
        stage.setScene(new Scene(scene));
        stage.show();

    }

    /**
     * This method moves to the update window where an appointment can be updated, automatically populating the information.
     * @param event
     * @throws IOException
     * @throws SQLException
     */
    @FXML
    void onActionUpdateAppointment(ActionEvent event) throws IOException, SQLException {

        if (apptTableView.getSelectionModel().isEmpty()) {
            return;
        }

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/view/UpdateAppointment.fxml"));
        loader.load();

        UpdateAppointmentController UAController = loader.getController();
        UAController.sendAppointment(apptTableView.getSelectionModel().getSelectedItem());

        stage = (Stage) ((Button)event.getSource()).getScene().getWindow();
        scene = loader.getRoot();
        stage.setScene(new Scene(scene));
        stage.show();

    }

    /**
     * This method populates the table view with appointments from the database.
     * @throws SQLException
     */
    public void populateAppointments() throws SQLException {

        ResultSet rs = AppointmentsHelper.read();
        ObservableList<Appointment> appointmentsList = FXCollections.observableArrayList();

        while (rs.next()) {
            int apptID = rs.getInt("Appointment_ID");
            String title = rs.getString("Title");
            String description = rs.getString("Description");
            String location = rs.getString("Location");
            String type = rs.getString("Type");

            LocalDateTime startDateTime = rs.getTimestamp("Start").toLocalDateTime();
            DateTimeFormatter startFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
            String start = startDateTime.format(startFormatter);
            LocalDateTime parsedStart = LocalDateTime.parse(start, startFormatter);
            ZonedDateTime zonedStartUTC = ZonedDateTime.of(parsedStart, ZoneId.of("UTC"));
            ZonedDateTime zonedStartUser = zonedStartUTC.withZoneSameInstant(ZoneId.systemDefault());
            LocalDateTime zonedLocalStart = zonedStartUser.toLocalDateTime();
            start = zonedLocalStart.format(startFormatter);

            LocalDateTime endDateTime = rs.getTimestamp("End").toLocalDateTime();
            DateTimeFormatter endFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
            String end = endDateTime.format(endFormatter);
            LocalDateTime parsedEnd = LocalDateTime.parse(end, endFormatter);
            ZonedDateTime zonedEndUTC = ZonedDateTime.of(parsedEnd, ZoneId.of("UTC"));
            ZonedDateTime zonedEndUser = zonedEndUTC.withZoneSameInstant(ZoneId.systemDefault());
            LocalDateTime zonedLocalEnd = zonedEndUser.toLocalDateTime();
            end = zonedLocalEnd.format(startFormatter);

            int customerID = rs.getInt("Customer_ID");
            int userID = rs.getInt("User_ID");
            int contactID = rs.getInt("Contact_ID");

            String contactName = null;
            ResultSet rsContacts = ContactsHelper.read();
            while (rsContacts.next()) {
                if (contactID == rsContacts.getInt("Contact_ID")) {
                    contactName = rsContacts.getString("Contact_Name");
                }
            }

            Contact contact = new Contact(contactID, contactName);

            Appointment appointment = new Appointment(apptID, title, description, location, type, start, end, customerID,
                    userID, contact);
            appointmentsList.add(appointment);
        }

        apptTableView.setItems(appointmentsList);

        apptIDCol.setCellValueFactory(new PropertyValueFactory<>("apptID"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        startCol.setCellValueFactory(new PropertyValueFactory<>("start"));
        endCol.setCellValueFactory(new PropertyValueFactory<>("end"));
        customerIDCol.setCellValueFactory(new PropertyValueFactory<>("customerID"));
        userIDCol.setCellValueFactory(new PropertyValueFactory<>("userID"));
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));


    }

    /**
     * This is an overloaded method that populates appointments.
     * @param numberOfDays
     * @throws SQLException
     */
    public void populateAppointments(int numberOfDays) throws SQLException {

        ResultSet rs = AppointmentsHelper.read();
        ObservableList<Appointment> appointmentsList = FXCollections.observableArrayList();

        while (rs.next()) {
            int apptID = rs.getInt("Appointment_ID");
            String title = rs.getString("Title");
            String description = rs.getString("Description");
            String location = rs.getString("Location");
            String type = rs.getString("Type");

            LocalDateTime startDateTime = rs.getTimestamp("Start").toLocalDateTime();
            DateTimeFormatter startFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
            String start = startDateTime.format(startFormatter);
            LocalDateTime parsedStart = LocalDateTime.parse(start, startFormatter);
            ZonedDateTime zonedStartUTC = ZonedDateTime.of(parsedStart, ZoneId.of("UTC"));
            ZonedDateTime zonedStartUser = zonedStartUTC.withZoneSameInstant(ZoneId.systemDefault());
            LocalDateTime zonedLocalStart = zonedStartUser.toLocalDateTime();
            start = zonedLocalStart.format(startFormatter);


            LocalDateTime endDateTime = rs.getTimestamp("End").toLocalDateTime();
            DateTimeFormatter endFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
            String end = endDateTime.format(endFormatter);
            LocalDateTime parsedEnd = LocalDateTime.parse(end, endFormatter);
            ZonedDateTime zonedEndUTC = ZonedDateTime.of(parsedEnd, ZoneId.of("UTC"));
            ZonedDateTime zonedEndUser = zonedEndUTC.withZoneSameInstant(ZoneId.systemDefault());
            LocalDateTime zonedLocalEnd = zonedEndUser.toLocalDateTime();
            end = zonedLocalEnd.format(startFormatter);

            int customerID = rs.getInt("Customer_ID");
            int userID = rs.getInt("User_ID");
            int contactID = rs.getInt("Contact_ID");

            String contactName = null;
            ResultSet rsContacts = ContactsHelper.read();
            while (rsContacts.next()) {
                if (contactID == rsContacts.getInt("Contact_ID")) {
                    contactName = rsContacts.getString("Contact_Name");
                }
            }

            Contact contact = new Contact(contactID, contactName);

            Appointment appointment = new Appointment(apptID, title, description, location, type, start, end, customerID,
                    userID, contact);

            if (startDateTime.isBefore(LocalDateTime.now().plusDays(numberOfDays)) && startDateTime.isAfter(LocalDateTime.now())) {
                appointmentsList.add(appointment);
            }

        }

        apptTableView.setItems(appointmentsList);

        apptIDCol.setCellValueFactory(new PropertyValueFactory<>("apptID"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        startCol.setCellValueFactory(new PropertyValueFactory<>("start"));
        endCol.setCellValueFactory(new PropertyValueFactory<>("end"));
        customerIDCol.setCellValueFactory(new PropertyValueFactory<>("customerID"));
        userIDCol.setCellValueFactory(new PropertyValueFactory<>("userID"));
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));


    }

    /**
     * This is an overloaded method that populates appointments.
     * @param monthToChoose
     * @param yearToChoose
     * @throws SQLException
     */
    public void populateAppointments( int monthToChoose, int yearToChoose) throws SQLException {

        ResultSet rs = AppointmentsHelper.read();
        ObservableList<Appointment> appointmentsList = FXCollections.observableArrayList();

        while (rs.next()) {
            int apptID = rs.getInt("Appointment_ID");
            String title = rs.getString("Title");
            String description = rs.getString("Description");
            String location = rs.getString("Location");
            String type = rs.getString("Type");

            LocalDateTime startDateTime = rs.getTimestamp("Start").toLocalDateTime();
            DateTimeFormatter startFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
            String start = startDateTime.format(startFormatter);
            LocalDateTime parsedStart = LocalDateTime.parse(start, startFormatter);
            ZonedDateTime zonedStartUTC = ZonedDateTime.of(parsedStart, ZoneId.of("UTC"));
            ZonedDateTime zonedStartUser = zonedStartUTC.withZoneSameInstant(ZoneId.systemDefault());
            LocalDateTime zonedLocalStart = zonedStartUser.toLocalDateTime();
            start = zonedLocalStart.format(startFormatter);


            LocalDateTime endDateTime = rs.getTimestamp("End").toLocalDateTime();
            DateTimeFormatter endFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
            String end = endDateTime.format(endFormatter);
            LocalDateTime parsedEnd = LocalDateTime.parse(end, endFormatter);
            ZonedDateTime zonedEndUTC = ZonedDateTime.of(parsedEnd, ZoneId.of("UTC"));
            ZonedDateTime zonedEndUser = zonedEndUTC.withZoneSameInstant(ZoneId.systemDefault());
            LocalDateTime zonedLocalEnd = zonedEndUser.toLocalDateTime();
            end = zonedLocalEnd.format(startFormatter);

            int customerID = rs.getInt("Customer_ID");
            int userID = rs.getInt("User_ID");
            int contactID = rs.getInt("Contact_ID");

            String contactName = null;
            ResultSet rsContacts = ContactsHelper.read();
            while (rsContacts.next()) {
                if (contactID == rsContacts.getInt("Contact_ID")) {
                    contactName = rsContacts.getString("Contact_Name");
                }
            }

            Contact contact = new Contact(contactID, contactName);

            Appointment appointment = new Appointment(apptID, title, description, location, type, start, end, customerID,
                    userID, contact);

            if (startDateTime.getMonthValue() == monthToChoose && startDateTime.getYear() == yearToChoose) {
                appointmentsList.add(appointment);
            }

        }

        apptTableView.setItems(appointmentsList);

        apptIDCol.setCellValueFactory(new PropertyValueFactory<>("apptID"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        startCol.setCellValueFactory(new PropertyValueFactory<>("start"));
        endCol.setCellValueFactory(new PropertyValueFactory<>("end"));
        customerIDCol.setCellValueFactory(new PropertyValueFactory<>("customerID"));
        userIDCol.setCellValueFactory(new PropertyValueFactory<>("userID"));
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));


    }

    /**
     * This method is run when the window is initialized and takes care of populating combo boxes and table views.
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        isInitializing = true;
        try {
            populateAppointments();
        }
        catch (SQLException e) {
            System.out.println("SQL error populating appointments. Exception: " + e);
        }

        ObservableList<String> monthList = FXCollections.observableArrayList();
        monthList.addAll("January", "February", "March", "April", "May", "June", "July", "August", "September",
                "October", "November", "December");
        monthlyCmbBox.setItems(monthList);

        ObservableList<Integer> yearList = FXCollections.observableArrayList();
        yearList.addAll(LocalDateTime.now().getYear(), LocalDateTime.now().plusYears(1).getYear(),
                LocalDateTime.now().plusYears(2).getYear(), LocalDateTime.now().plusYears(3).getYear(),
                LocalDateTime.now().plusYears(4).getYear());
        yearCmbBox.setItems(yearList);
        monthlyCmbBox.getSelectionModel().selectFirst();
        yearCmbBox.getSelectionModel().selectFirst();
        isInitializing = false;


    }

    /**
     * This method shows the appointments from a month/year standpoint.
     * @param event
     * @throws SQLException
     */
    @FXML
    void onActionMonthlyRadBtn(ActionEvent event) throws SQLException {
        monthlyCmbBox.setVisible(true);
        yearCmbBox.setVisible(true);


        populateAppointments(monthlyCmbBox.getSelectionModel().getSelectedIndex() + 1, yearCmbBox.getValue());
    }

    /**
     * This method shows appointments from a weekly standpoint.
     * @param event
     * @throws SQLException
     */
    @FXML
    void onActionWeeklyRadBtn(ActionEvent event) throws SQLException {
        populateAppointments(7);
        monthlyCmbBox.setVisible(false);
        yearCmbBox.setVisible(false);
    }

    /**
     * This method shows all appointments.
     * @param event
     * @throws SQLException
     */
    @FXML
    void onActionAllRadBtn(ActionEvent event) throws SQLException {
        populateAppointments();
        monthlyCmbBox.setVisible(false);
        yearCmbBox.setVisible(false);
    }

    /**
     * This method allows the user to select a month to populate the appointments.
     * @param event
     * @throws SQLException
     */
    @FXML
    void onActionSelectMonth(ActionEvent event) throws SQLException {
        if (!isInitializing) {
            populateAppointments(monthlyCmbBox.getSelectionModel().getSelectedIndex() + 1, yearCmbBox.getValue());
        }

    }

    /**
     * This method allows the user to select a year to populate the appointments.
     * @param event
     * @throws SQLException
     */
    @FXML
    void onActionSelectYear(ActionEvent event) throws SQLException {
        if (!isInitializing) {

           populateAppointments(monthlyCmbBox.getSelectionModel().getSelectedIndex() + 1, yearCmbBox.getValue());
        }
    }
}
