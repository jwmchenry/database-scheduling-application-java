package main;

import helper.AppointmentsHelper;
import helper.CustomersHelper;
import helper.JDBC;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.xml.transform.Result;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

import static java.time.ZoneId.systemDefault;


public class Main extends Application {

    public static int customerIDCounter = 1;
    public static int apptIDCounter = 1;

    public static int newCustomerID() throws SQLException {

        ResultSet rs = CustomersHelper.read();

        while (rs.next()) {

           customerIDCounter = rs.getInt("Customer_ID");

        }

        customerIDCounter++;

        return customerIDCounter;

    }

    public static int newApptID() throws SQLException {

        ResultSet rs = AppointmentsHelper.read();

        while (rs.next()) {

            apptIDCounter = rs.getInt("Appointment_ID");

        }

        apptIDCounter++;

        return apptIDCounter;
    }


    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/Login.fxml")));
        primaryStage.setTitle("Login Page");
        primaryStage.setScene(new Scene(root,450,400));
        primaryStage.show();
    }

    public static void main(String[] args) {
        JDBC.openConnection();
        launch(args);
        JDBC.closeConnection();

    }
}
