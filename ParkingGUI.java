import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class ParkingGUI extends Application {

    private ParkingService service = new ParkingService();
    private Stage primaryStage;

    private final String BG = "#0f172a";
    private final String PANEL = "#1e293b";
    private final String TOP = "#111827";
    private final String TEXT = "#f8fafc";
    private final String MUTED = "#94a3b8";
    private final String BLUE = "#3b82f6";
    private final String GREEN = "#22c55e";
    private final String RED = "#ef4444";
    private final String ORANGE = "#f97316";
    private final String PURPLE = "#8b5cf6";
    private final String CYAN = "#0ea5e9";

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        showLoginScreen();
    }

    private void showLoginScreen() {
        Label title = new Label("Parking System");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        title.setTextFill(Color.web(TEXT));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(260);
        styleField(usernameField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(260);
        styleField(passwordField);

        Label message = new Label();
        message.setTextFill(Color.web(RED));

        Button loginBtn = new Button("Login");
        styleBtn(loginBtn, BLUE);

        loginBtn.setOnAction(e -> {
            User user = service.login(
                    usernameField.getText().trim(),
                    passwordField.getText().trim());

            if (user != null) {
                showDashboard(user);
            } else {
                message.setText("Invalid username or password");
            }
        });

        passwordField.setOnAction(e -> loginBtn.fire());

        VBox card = new VBox(16, title, usernameField, passwordField, loginBtn, message);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(35));
        stylePanel(card);

        StackPane layout = new StackPane(card);
        layout.setStyle("-fx-background-color:" + BG + ";");
        layout.setPadding(new Insets(30));

        primaryStage.setTitle("Parking System – Login");
        primaryStage.setScene(new Scene(layout, 420, 360));
        primaryStage.show();
    }

    private void showDashboard(User user) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + BG + ";");

        Label welcome = new Label("Welcome, " + user.getUsername()
                + "  |  Role: " + user.getRole());
        welcome.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        welcome.setTextFill(Color.web(TEXT));

        Button logoutBtn = new Button("Logout");
        styleBtn(logoutBtn, RED);
        logoutBtn.setOnAction(e -> showLoginScreen());

        HBox topBar = new HBox(welcome, logoutBtn);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setSpacing(20);
        topBar.setPadding(new Insets(14, 18, 14, 18));
        topBar.setStyle(
                "-fx-background-color:" + TOP + ";" +
                        "-fx-border-color:#334155;" +
                        "-fx-border-width:0 0 1 0;"
        );

        root.setTop(topBar);

        VBox centerPanel = null;

        switch (user.getRoleEnum()) {
            case ADMIN -> centerPanel = buildAdminPanel();
            case ENTRY -> centerPanel = buildEntryPanel();
            case EXIT -> centerPanel = buildExitPanel();
        }

        StackPane centerWrapper = new StackPane(centerPanel);
        centerWrapper.setPadding(new Insets(24));
        root.setCenter(centerWrapper);

        primaryStage.setTitle("Parking System – Dashboard (" + user.getRole() + ")");
        primaryStage.setScene(new Scene(root, 800, 620));
    }

    private VBox buildEntryPanel() {
        Label heading = sectionTitle("Entry Station");

        Label spotsLabel = new Label();
        refreshSpotsLabel(spotsLabel);

        TextField plateField = new TextField();
        plateField.setPromptText("Enter plate number e.g. ABC-123");
        plateField.setMaxWidth(320);
        styleField(plateField);

        TextArea ticketArea = new TextArea();
        ticketArea.setEditable(false);
        ticketArea.setPrefHeight(150);
        ticketArea.setPromptText("Ticket details will appear here...");
        styleTextArea(ticketArea);

        Button parkBtn = new Button("Park Car & Print Ticket");
        styleBtn(parkBtn, GREEN);

        parkBtn.setOnAction(e -> {
            try {
                Ticket t = service.parkCar(plateField.getText());

                if (t != null) {
                    ticketArea.setText(
                            "=====================================\n" +
                                    "           PARKING TICKET            \n" +
                                    "=====================================\n" +
                                    "Ticket #  : " + t.getTicketId() + "\n" +
                                    "Plate     : " + t.getPlateNumber() + "\n" +
                                    "Spot      : " + t.getSpotId() + "\n" +
                                    "Entry Time: " + t.getEntryTime() + "\n" +
                                    "=====================================\n" +
                                    "Rate: 10 EGP first hour, 5 EGP/hr after"
                    );

                    plateField.clear();
                    refreshSpotsLabel(spotsLabel);
                    showAlert("Success", "Car parked successfully! Ticket #" + t.getTicketId(),
                            Alert.AlertType.INFORMATION);
                } else {
                    ticketArea.setText("Parking is FULL. No free spots available.");
                    showAlert("Parking Full", "No free spots available!",
                            Alert.AlertType.WARNING);
                }

            } catch (IllegalArgumentException ex) {
                ticketArea.setText("Error: " + ex.getMessage());
                showAlert("Invalid Input", ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        VBox panel = new VBox(16, heading, spotsLabel, plateField, parkBtn, ticketArea);
        panel.setPadding(new Insets(26));
        stylePanel(panel);
        return panel;
    }

    private VBox buildExitPanel() {
        Label heading = sectionTitle("Exit Station");

        Label instruction = new Label("Enter Ticket ID or Plate Number:");
        instruction.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        instruction.setTextFill(Color.web(MUTED));

        TextField searchField = new TextField();
        searchField.setPromptText("Ticket ID or Plate Number");
        searchField.setMaxWidth(280);
        styleField(searchField);

        Button searchBtn = new Button("Search");
        styleBtn(searchBtn, CYAN);

        TextArea detailsArea = new TextArea();
        detailsArea.setEditable(false);
        detailsArea.setPrefHeight(110);
        detailsArea.setPromptText("Ticket details will appear here...");
        styleTextArea(detailsArea);

        Label feeLabel = new Label();
        feeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 17));
        feeLabel.setTextFill(Color.web(ORANGE));

        Button exitBtn = new Button("Process Payment & Exit");
        styleBtn(exitBtn, ORANGE);
        exitBtn.setDisable(true);

        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefHeight(120);
        resultArea.setPromptText("Payment receipt will appear here...");
        styleTextArea(resultArea);

        searchBtn.setOnAction(e -> {
            String input = searchField.getText().trim();
            int ticketId = -1;

            try {
                ticketId = Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                Ticket foundTicket = service.findActiveTicketByPlate(input);
                if (foundTicket != null) {
                    ticketId = foundTicket.getTicketId();
                }
            }

            if (ticketId != -1) {
                String details = service.getTicketDetails(ticketId);

                if (details != null) {
                    detailsArea.setText(details);

                    Ticket ticket = null;
                    for (Ticket t : service.getActiveTickets()) {
                        if (t.getTicketId() == ticketId) {
                            ticket = t;
                            break;
                        }
                    }

                    if (ticket != null) {
                        java.time.LocalDateTime now = java.time.LocalDateTime.now();
                        java.time.Duration duration = java.time.Duration.between(
                                java.time.LocalDateTime.parse(ticket.getEntryTime()), now);

                        long minutes = duration.toMinutes();
                        long hours = minutes / 60;

                        if (minutes % 60 > 0) hours++;
                        if (hours == 0) hours = 1;

                        double estimatedFees = (hours <= 1) ? 10 : 10 + (hours - 1) * 5;
                        feeLabel.setText(String.format("Estimated Fee: %.2f EGP", estimatedFees));
                    }

                    exitBtn.setDisable(false);
                    resultArea.clear();

                } else {
                    detailsArea.setText("Ticket not found or already exited.");
                    exitBtn.setDisable(true);
                    feeLabel.setText("");
                }

            } else {
                detailsArea.setText("Invalid Ticket ID or Plate Number.");
                exitBtn.setDisable(true);
                feeLabel.setText("");
            }
        });

        exitBtn.setOnAction(e -> {
            String input = searchField.getText().trim();
            int ticketId = -1;

            try {
                ticketId = Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                Ticket foundTicket = service.findActiveTicketByPlate(input);
                if (foundTicket != null) {
                    ticketId = foundTicket.getTicketId();
                }
            }

            if (ticketId != -1 && showConfirmDialog("Confirm Payment",
                    "Are you sure you want to process payment and exit?")) {

                double fees = service.exitCar(ticketId);

                if (fees >= 0) {
                    resultArea.setText(
                            "=====================================\n" +
                                    "           PAYMENT RECEIPT           \n" +
                                    "=====================================\n" +
                                    "Ticket ID : " + ticketId + "\n" +
                                    "Total Fee : " + fees + " EGP\n" +
                                    "Status    : PAID\n" +
                                    "====================================="
                    );

                    searchField.clear();
                    detailsArea.clear();
                    feeLabel.setText("");
                    exitBtn.setDisable(true);

                    showAlert("Payment Successful",
                            "Payment of " + fees + " EGP processed successfully. Gate opening...",
                            Alert.AlertType.INFORMATION);
                } else {
                    resultArea.setText("Error processing payment.");
                }
            }
        });

        VBox panel = new VBox(14, heading, instruction, searchField, searchBtn,
                detailsArea, feeLabel, exitBtn, resultArea);
        panel.setPadding(new Insets(26));
        stylePanel(panel);
        return panel;
    }

    private VBox buildAdminPanel() {
        Label heading = sectionTitle("Admin Panel");

        Button addSpotBtn = new Button("Add Spot");
        Button deleteSpotBtn = new Button("Delete Spot");
        styleBtn(addSpotBtn, BLUE);
        styleBtn(deleteSpotBtn, RED);

        TextField spotIdField = new TextField();
        spotIdField.setPromptText("Spot ID");
        spotIdField.setMaxWidth(90);
        styleField(spotIdField);

        Label spotsCountLabel = new Label("Total Spots: " + service.getTotalSpots()
                + "   |   Free: " + service.getFreeSpots());
        spotsCountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        spotsCountLabel.setTextFill(Color.web(TEXT));

        Label spotMsg = new Label();
        spotMsg.setTextFill(Color.web(RED));

        addSpotBtn.setOnAction(e -> {
            service.addSpot();
            spotsCountLabel.setText("Total Spots: " + service.getTotalSpots()
                    + "   |   Free: " + service.getFreeSpots());
            showAlert("Spot Added", "New parking spot added successfully!",
                    Alert.AlertType.INFORMATION);
        });

        deleteSpotBtn.setOnAction(e -> {
            String spotIdText = spotIdField.getText().trim();

            if (spotIdText.isEmpty()) {
                spotMsg.setText("Enter Spot ID to delete.");
                return;
            }

            try {
                int spotId = Integer.parseInt(spotIdText);

                if (showConfirmDialog("Confirm Delete",
                        "Are you sure you want to delete spot #" + spotId + "?")) {

                    boolean success = service.removeSpot(spotId);

                    if (success) {
                        spotsCountLabel.setText("Total Spots: " + service.getTotalSpots()
                                + "   |   Free: " + service.getFreeSpots());

                        spotMsg.setText("");
                        spotIdField.clear();

                        showAlert("Spot Deleted",
                                "Parking spot #" + spotId + " deleted successfully!",
                                Alert.AlertType.INFORMATION);
                    } else {
                        spotMsg.setText("Spot #" + spotId + " not found or is occupied.");
                    }
                }

            } catch (NumberFormatException ex) {
                spotMsg.setText("Invalid Spot ID. Enter a number.");
            }
        });

        HBox spotRow = new HBox(12, addSpotBtn, spotIdField, deleteSpotBtn, spotsCountLabel);
        spotRow.setAlignment(Pos.CENTER_LEFT);

        Label userHeading = smallHeading("User Management");

        TextField unField = new TextField();
        unField.setPromptText("Username");
        unField.setMaxWidth(170);
        styleField(unField);

        PasswordField pwField = new PasswordField();
        pwField.setPromptText("Password");
        pwField.setMaxWidth(170);
        styleField(pwField);

        ComboBox<User.Role> roleBox = new ComboBox<>();
        roleBox.getItems().addAll(User.Role.values());
        roleBox.setValue(User.Role.ENTRY);
        roleBox.setStyle(
                "-fx-background-color:" + PANEL + ";" +
                        "-fx-text-fill:white;" +
                        "-fx-background-radius:10;" +
                        "-fx-border-color:#334155;" +
                        "-fx-border-radius:10;" +
                        "-fx-padding:6;"
        );

        Button addUserBtn = new Button("Add User");
        Button removeUserBtn = new Button("Remove User");
        Button updateUserBtn = new Button("Update User");

        styleBtn(addUserBtn, GREEN);
        styleBtn(removeUserBtn, RED);
        styleBtn(updateUserBtn, PURPLE);

        Label userMsg = new Label();
        userMsg.setTextFill(Color.web(MUTED));

        addUserBtn.setOnAction(e -> {
            if (unField.getText().trim().isEmpty() || pwField.getText().trim().isEmpty()) {
                userMsg.setText("Username and password cannot be empty!");
                return;
            }

            boolean ok = service.addUser(unField.getText().trim(),
                    pwField.getText().trim(), roleBox.getValue());

            userMsg.setText(ok ? "User added." : "Username already exists.");

            if (ok) {
                unField.clear();
                pwField.clear();
            }
        });

        removeUserBtn.setOnAction(e -> {
            String username = unField.getText().trim();

            if (username.isEmpty()) {
                userMsg.setText("Enter username to remove.");
                return;
            }

            if (showConfirmDialog("Confirm Delete",
                    "Are you sure you want to delete user '" + username + "'?")) {

                boolean ok = service.removeUser(username);

                userMsg.setText(ok ? "User removed." : "User not found or cannot delete last Admin.");
            }
        });

        updateUserBtn.setOnAction(e -> {
            String username = unField.getText().trim();

            if (username.isEmpty()) {
                userMsg.setText("Enter username to update.");
                return;
            }

            String newPassword = pwField.getText().trim();

            boolean ok = service.updateUser(username,
                    newPassword.isEmpty() ? null : newPassword,
                    roleBox.getValue());

            userMsg.setText(ok ? "User updated." : "User not found.");

            if (ok) {
                unField.clear();
                pwField.clear();
            }
        });

        Label repHeading = smallHeading("Reports");

        Button parkedReportBtn = new Button("Parked Cars");
        Button revenueReportBtn = new Button("Revenue & Shifts");
        Button usersReportBtn = new Button("All Users");

        styleBtn(parkedReportBtn, BLUE);
        styleBtn(revenueReportBtn, BLUE);
        styleBtn(usersReportBtn, BLUE);

        TextArea reportArea = new TextArea();
        reportArea.setEditable(false);
        reportArea.setPrefHeight(170);
        styleTextArea(reportArea);

        parkedReportBtn.setOnAction(e -> {
            StringBuilder sb = new StringBuilder("Parked Cars Report\n");
            sb.append("=====================================\n");

            var active = service.getActiveTickets();

            if (active.isEmpty()) {
                sb.append("No cars currently parked.");
            } else {
                for (Ticket t : active) {
                    sb.append("Ticket #").append(t.getTicketId())
                            .append(" | Plate: ").append(t.getPlateNumber())
                            .append(" | Spot: ").append(t.getSpotId())
                            .append(" | Since: ").append(t.getEntryTime()).append("\n");
                }
            }

            reportArea.setText(sb.toString());
        });

        revenueReportBtn.setOnAction(e -> {
            StringBuilder sb = new StringBuilder("Revenue / Shifts Report\n");
            sb.append("=====================================\n");
            sb.append(String.format("Total Revenue : %.2f EGP%n", service.getTotalRevenue()));
            sb.append("Completed Tickets: ").append(service.getCompletedTickets().size()).append("\n\n");

            for (Ticket t : service.getCompletedTickets()) {
                sb.append("Ticket #").append(t.getTicketId())
                        .append(" | Plate: ").append(t.getPlateNumber())
                        .append(" | Fees: ").append(t.getFees()).append(" EGP\n");
            }

            reportArea.setText(sb.toString());
        });

        usersReportBtn.setOnAction(e -> {
            StringBuilder sb = new StringBuilder("Users List\n");
            sb.append("=====================================\n");

            for (User u : service.getAllUsers()) {
                sb.append(u.getUsername()).append(" [").append(u.getRole()).append("]\n");
            }

            reportArea.setText(sb.toString());
        });

        HBox repBtns = new HBox(10, parkedReportBtn, revenueReportBtn, usersReportBtn);

        Separator s1 = new Separator();
        Separator s2 = new Separator();

        VBox panel = new VBox(13,
                heading,
                spotRow,
                spotMsg,
                s1,
                userHeading,
                new HBox(10, unField, pwField, roleBox),
                new HBox(10, addUserBtn, removeUserBtn, updateUserBtn),
                userMsg,
                s2,
                repHeading,
                repBtns,
                reportArea
        );

        panel.setPadding(new Insets(26));
        stylePanel(panel);

        return panel;
    }

    private void refreshSpotsLabel(Label label) {
        label.setText("Free Spots: " + service.getFreeSpots()
                + " / " + service.getTotalSpots());

        label.setFont(Font.font("Arial", FontWeight.BOLD, 15));

        label.setTextFill(service.getFreeSpots() > 0
                ? Color.web(GREEN)
                : Color.web(RED));
    }

    private Label sectionTitle(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lbl.setTextFill(Color.web(TEXT));
        return lbl;
    }

    private Label smallHeading(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        lbl.setTextFill(Color.web(TEXT));
        return lbl;
    }

    private void styleBtn(Button btn, String color) {
        String normalStyle =
                "-fx-background-color:" + color + ";" +
                        "-fx-text-fill:white;" +
                        "-fx-font-size:13;" +
                        "-fx-font-weight:bold;" +
                        "-fx-background-radius:12;" +
                        "-fx-padding:9 18;" +
                        "-fx-cursor:hand;" +
                        "-fx-effect:dropshadow(gaussian, rgba(0,0,0,0.45),10,0,0,4);";

        String hoverStyle =
                "-fx-background-color:" + color + ";" +
                        "-fx-text-fill:white;" +
                        "-fx-font-size:13;" +
                        "-fx-font-weight:bold;" +
                        "-fx-background-radius:12;" +
                        "-fx-padding:9 18;" +
                        "-fx-cursor:hand;" +
                        "-fx-opacity:0.85;" +
                        "-fx-effect:dropshadow(gaussian, rgba(0,0,0,0.65),14,0,0,6);";

        btn.setStyle(normalStyle);

        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(normalStyle));
    }

    private void styleField(TextInputControl field) {
        field.setStyle(
                "-fx-background-color:#020617;" +
                        "-fx-text-fill:#f8fafc;" +
                        "-fx-prompt-text-fill:#64748b;" +
                        "-fx-background-radius:12;" +
                        "-fx-border-color:#334155;" +
                        "-fx-border-radius:12;" +
                        "-fx-border-width:1;" +
                        "-fx-padding:10;" +
                        "-fx-font-size:13;"
        );
    }

    private void styleTextArea(TextArea area) {
        area.setStyle(
                "-fx-control-inner-background:#020617;" +
                        "-fx-text-fill:#22c55e;" +
                        "-fx-prompt-text-fill:#64748b;" +
                        "-fx-font-family:Consolas;" +
                        "-fx-font-size:13;" +
                        "-fx-background-radius:12;" +
                        "-fx-border-color:#334155;" +
                        "-fx-border-radius:12;"
        );
    }

    private void stylePanel(Region panel) {
        panel.setStyle(
                "-fx-background-color:" + PANEL + ";" +
                        "-fx-background-radius:22;" +
                        "-fx-border-radius:22;" +
                        "-fx-border-color:#334155;" +
                        "-fx-border-width:1;" +
                        "-fx-effect:dropshadow(gaussian, rgba(0,0,0,0.55),18,0,0,8);"
        );
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean showConfirmDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);
        return result == ButtonType.OK;
    }

    public static void main(String[] args) {
        launch(args);
    }
}