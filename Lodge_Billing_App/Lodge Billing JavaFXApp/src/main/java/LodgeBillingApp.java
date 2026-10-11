import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.Interpolator;

import java.time.LocalDate;

public class LodgeBillingApp extends Application {

    private final ObservableList<Bill> billList = FXCollections.observableArrayList();
    private final ObservableList<Reservation> reservationList = FXCollections.observableArrayList();
    private int billCounter = 3;
    private int reservationCounter = 0;
    private RotateTransition logoRotation;

    private static final String STYLESHEET =
            LodgeBillingApp.class.getResource("/lodge.css").toExternalForm();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("MUZU Lodge");
        showLoginScreen(primaryStage);
    }

    private DropShadow createShadow() {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(30, 65, 38, 0.12));
        shadow.setRadius(18);
        shadow.setOffsetY(5);
        return shadow;
    }

    private HBox createBrand(double logoSize) {
        StackPane mark = new StackPane();
        mark.setPrefSize(logoSize, logoSize);
        mark.setMinSize(logoSize, logoSize);
        mark.setMaxSize(logoSize, logoSize);

        Circle badge = new Circle(logoSize / 2, Color.web("#176B3A"));
        Ellipse leaf = new Ellipse(logoSize * 0.13, logoSize * 0.075);
        leaf.setFill(Color.web("#E7B85C"));
        leaf.setRotate(-38);
        leaf.setTranslateX(logoSize * 0.30);
        leaf.setTranslateY(-logoSize * 0.30);
        Label monogram = new Label("M");
        monogram.setStyle("-fx-font-size: " + (logoSize * 0.48) + "px; -fx-font-weight: bold; -fx-text-fill: white;");
        mark.getChildren().addAll(badge, leaf, monogram);
        mark.setEffect(createShadow());

        logoRotation = new RotateTransition(Duration.seconds(10), mark);
        logoRotation.setByAngle(360);
        logoRotation.setInterpolator(Interpolator.LINEAR);
        logoRotation.setCycleCount(Animation.INDEFINITE);
        logoRotation.play();

        Label name = new Label("MUZU Lodge");
        name.setStyle("-fx-font-size: " + (logoSize * 0.40) + "px; -fx-font-weight: bold; -fx-text-fill: #173D27;");
        Label tagline = new Label("STAY CLOSE TO NATURE");
        tagline.getStyleClass().add("label-sub");
        VBox wordmark = new VBox(2, name, tagline);
        wordmark.setAlignment(Pos.CENTER_LEFT);
        HBox brand = new HBox(14, mark, wordmark);
        brand.setAlignment(Pos.CENTER_LEFT);
        return brand;
    }

    private void stopLogoRotation() {
        if (logoRotation != null) {
            logoRotation.stop();
            logoRotation = null;
        }
    }

    private void showLoginScreen(Stage stage) {
        stopLogoRotation();

        VBox card = new VBox(22);
        card.getStyleClass().add("card");
        card.setEffect(createShadow());
        card.setMaxWidth(420);
        card.setAlignment(Pos.CENTER);

        HBox brand = createBrand(64);
        brand.setAlignment(Pos.CENTER);
        Label subLabel = new Label("Welcome back. Sign in to manage your lodge.");
        subLabel.getStyleClass().add("label-sub");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(14);
        grid.setAlignment(Pos.CENTER);

        TextField userField = new TextField();
        userField.setPromptText("Username");
        userField.setPrefWidth(270);
        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.setPrefWidth(270);

        grid.add(new Label("Username"), 0, 0);
        grid.add(userField, 0, 1);
        grid.add(new Label("Password"), 0, 2);
        grid.add(passField, 0, 3);

        Button loginButton = new Button("Sign in to MUZU Lodge");
        loginButton.setDefaultButton(true);
        loginButton.getStyleClass().add("button-primary");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #B34436; -fx-font-size: 12px;");

        loginButton.setOnAction(e -> {
            String username = userField.getText().trim();
            String password = passField.getText().trim();
            if (username.equals("admin") && password.equals("1234")) {
                showMainLabScreen(stage);
            } else if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please fill in both fields.");
            } else {
                errorLabel.setText("Invalid credentials. Try admin / 1234.");
            }
        });

        card.getChildren().addAll(brand, subLabel, grid, loginButton, errorLabel);
        StackPane container = new StackPane(card);
        container.setPadding(new Insets(40));

        Scene loginScene = new Scene(container, 520, 560);
        loginScene.getStylesheets().add(STYLESHEET);
        stage.setScene(loginScene);
        stage.show();
    }

    private void showMainLabScreen(Stage stage) {
        stopLogoRotation();
        stage.setTitle("MUZU Lodge | Admin Dashboard");

        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem newGuestItem = new MenuItem("New Guest");
        MenuItem logoutItem = new MenuItem("Logout");
        fileMenu.getItems().addAll(newGuestItem, new SeparatorMenuItem(), logoutItem);
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About MUZU Lodge");
        helpMenu.getItems().add(aboutItem);
        menuBar.getMenus().addAll(fileMenu, helpMenu);

        HBox brand = createBrand(54);
        Label adminBadge = new Label("ADMIN CONSOLE");
        adminBadge.setStyle("-fx-background-color: #E9F2E9; -fx-text-fill: #23633A; -fx-font-weight: bold; -fx-padding: 8px 12px; -fx-background-radius: 16px;");
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        HBox headerBox = new HBox(brand, headerSpacer, adminBadge);
        headerBox.getStyleClass().add("dashboard-header");
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(12, 18, 12, 18));

        VBox formCard = new VBox(12);
        formCard.getStyleClass().addAll("card", "form-card");
        formCard.setEffect(createShadow());
        Label formTitle = new Label("Guest, Reservation & Billing Details");
        formTitle.getStyleClass().add("section-title");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(12);
        formGrid.setVgap(10);
        ColumnConstraints labels = new ColumnConstraints();
        labels.setMinWidth(105);
        ColumnConstraints inputs = new ColumnConstraints();
        inputs.setHgrow(Priority.ALWAYS);
        formGrid.getColumnConstraints().addAll(labels, inputs);

        TextField guestNameField = new TextField();
        guestNameField.setPromptText("Guest full name");
        ComboBox<String> roomTypeBox = new ComboBox<>();
        roomTypeBox.getItems().addAll("Standard", "Deluxe", "Family");
        roomTypeBox.setValue("Standard");
        roomTypeBox.setMaxWidth(Double.MAX_VALUE);
        TextField nightsField = new TextField("1");
        TextField extrasField = new TextField("0");
        TextField paidField = new TextField("0");
        DatePicker checkInField = new DatePicker(LocalDate.now());
        checkInField.setMaxWidth(Double.MAX_VALUE);
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Special requests or reservation notes");
        notesArea.setPrefRowCount(2);

        formGrid.add(new Label("Guest name"), 0, 0);
        formGrid.add(guestNameField, 1, 0);
        formGrid.add(new Label("Room type"), 0, 1);
        formGrid.add(roomTypeBox, 1, 1);
        formGrid.add(new Label("Check-in"), 0, 2);
        formGrid.add(checkInField, 1, 2);
        formGrid.add(new Label("Nights"), 0, 3);
        formGrid.add(nightsField, 1, 3);
        formGrid.add(new Label("Extras (K)"), 0, 4);
        formGrid.add(extrasField, 1, 4);
        formGrid.add(new Label("Amount paid (K)"), 0, 5);
        formGrid.add(paidField, 1, 5);
        formGrid.add(new Label("Notes"), 0, 6);
        formGrid.add(notesArea, 1, 6);
        formCard.getChildren().addAll(formTitle, formGrid);

        VBox infoCard = new VBox(12);
        infoCard.getStyleClass().addAll("card", "info-card");
        infoCard.setEffect(createShadow());
        infoCard.setPrefWidth(290);
        infoCard.setMinWidth(250);
        Label infoTitle = new Label("A thoughtful stay");
        infoTitle.getStyleClass().add("section-title");
        Label pricesText = new Label("Standard     K 350 / night\nDeluxe         K 550 / night\nFamily          K 750 / night");
        pricesText.setStyle("-fx-line-spacing: 7px; -fx-text-fill: #425747; -fx-font-size: 13px;");
        Separator sep = new Separator();
        Label rulesText = new Label("5+ nights: 5% off room charge\nExtras are not discounted.\n\nReservations added here are managed by the admin. Bills and reservations remain available for this session.");
        rulesText.getStyleClass().add("label-sub");
        rulesText.setWrapText(true);
        infoCard.getChildren().addAll(infoTitle, pricesText, sep, rulesText);

        HBox topSection = new HBox(18, formCard, infoCard);
        HBox.setHgrow(formCard, Priority.ALWAYS);

        Button calculateButton = new Button("Preview total");
        calculateButton.getStyleClass().add("button-secondary");
        Button saveButton = new Button("Save bill");
        saveButton.getStyleClass().add("button-primary");
        Button reservationButton = new Button("Add reservation");
        reservationButton.getStyleClass().add("button-primary");
        Label statusLabel = new Label("Ready to manage your lodge.");
        statusLabel.setWrapText(true);
        HBox actionBox = new HBox(10, calculateButton, saveButton, reservationButton, statusLabel);
        actionBox.getStyleClass().add("action-bar");
        actionBox.setAlignment(Pos.CENTER_LEFT);
        actionBox.setPadding(new Insets(2, 0, 2, 0));

        TableView<Bill> billTable = new TableView<>();
        billTable.setItems(billList);
        billTable.setPlaceholder(new Label("No bills have been saved yet."));
        TableColumn<Bill, String> colBill = new TableColumn<>("Bill ID");
        colBill.setCellValueFactory(new PropertyValueFactory<>("billId"));
        TableColumn<Bill, String> colGuest = new TableColumn<>("Guest");
        colGuest.setCellValueFactory(new PropertyValueFactory<>("guestName"));
        TableColumn<Bill, String> colRoom = new TableColumn<>("Room");
        colRoom.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        TableColumn<Bill, Integer> colNights = new TableColumn<>("Nights");
        colNights.setCellValueFactory(new PropertyValueFactory<>("nights"));
        TableColumn<Bill, String> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        TableColumn<Bill, String> colPaid = new TableColumn<>("Paid");
        colPaid.setCellValueFactory(new PropertyValueFactory<>("paid"));
        TableColumn<Bill, String> colBalance = new TableColumn<>("Balance due");
        colBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
        billTable.getColumns().add(colBill);
        billTable.getColumns().add(colGuest);
        billTable.getColumns().add(colRoom);
        billTable.getColumns().add(colNights);
        billTable.getColumns().add(colTotal);
        billTable.getColumns().add(colPaid);
        billTable.getColumns().add(colBalance);
        billTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        billTable.setPrefHeight(190);

        Button viewReceiptButton = new Button("View receipt");
        viewReceiptButton.getStyleClass().add("button-secondary");
        Button deleteBillButton = new Button("Delete selected bill");
        deleteBillButton.getStyleClass().add("button-danger");
        viewReceiptButton.disableProperty().bind(billTable.getSelectionModel().selectedItemProperty().isNull());
        deleteBillButton.disableProperty().bind(billTable.getSelectionModel().selectedItemProperty().isNull());
        HBox billActions = new HBox(8, viewReceiptButton, deleteBillButton);
        billActions.setAlignment(Pos.CENTER_RIGHT);
        Label billHeading = new Label("Guest bills");
        billHeading.getStyleClass().add("section-title");
        Region billSpacer = new Region();
        HBox.setHgrow(billSpacer, Priority.ALWAYS);
        HBox billHeadingRow = new HBox(billHeading, billSpacer, billActions);
        billHeadingRow.setAlignment(Pos.CENTER_LEFT);
        VBox billCard = new VBox(10, billHeadingRow, billTable);
        billCard.getStyleClass().addAll("card", "bill-card");
        billCard.setEffect(createShadow());

        TableView<Reservation> reservationTable = new TableView<>();
        reservationTable.setItems(reservationList);
        reservationTable.setPlaceholder(new Label("No reservations yet. Add one using the form above."));
        TableColumn<Reservation, String> colReservation = new TableColumn<>("Reservation ID");
        colReservation.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        TableColumn<Reservation, String> colResGuest = new TableColumn<>("Guest");
        colResGuest.setCellValueFactory(new PropertyValueFactory<>("guestName"));
        TableColumn<Reservation, String> colResRoom = new TableColumn<>("Room");
        colResRoom.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        TableColumn<Reservation, String> colCheckIn = new TableColumn<>("Check-in");
        colCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        TableColumn<Reservation, Integer> colResNights = new TableColumn<>("Nights");
        colResNights.setCellValueFactory(new PropertyValueFactory<>("nights"));
        TableColumn<Reservation, String> colNotes = new TableColumn<>("Notes");
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));
        TableColumn<Reservation, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        TableColumn<Reservation, String> colCheckedIn = new TableColumn<>("Checked in");
        colCheckedIn.setCellValueFactory(new PropertyValueFactory<>("checkedInAt"));
        TableColumn<Reservation, String> colCheckedOut = new TableColumn<>("Checked out");
        colCheckedOut.setCellValueFactory(new PropertyValueFactory<>("checkedOutAt"));
        reservationTable.getColumns().add(colReservation);
        reservationTable.getColumns().add(colResGuest);
        reservationTable.getColumns().add(colResRoom);
        reservationTable.getColumns().add(colCheckIn);
        reservationTable.getColumns().add(colResNights);
        reservationTable.getColumns().add(colNotes);
        reservationTable.getColumns().add(colStatus);
        reservationTable.getColumns().add(colCheckedIn);
        reservationTable.getColumns().add(colCheckedOut);
        reservationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        reservationTable.setPrefHeight(190);

        Button checkInButton = new Button("Check in");
        checkInButton.getStyleClass().add("button-primary");
        checkInButton.setDisable(true);
        Button checkOutButton = new Button("Check out");
        checkOutButton.getStyleClass().add("button-secondary");
        checkOutButton.setDisable(true);
        Button deleteReservationButton = new Button("Delete selected reservation");
        deleteReservationButton.getStyleClass().add("button-danger");
        deleteReservationButton.disableProperty().bind(reservationTable.getSelectionModel().selectedItemProperty().isNull());
        reservationTable.getSelectionModel().selectedItemProperty().addListener((observable, oldSelection, selection) -> {
            checkInButton.setDisable(selection == null || !selection.isConfirmed());
            checkOutButton.setDisable(selection == null || !selection.isCheckedIn());
        });
        Label reservationHeading = new Label("Reservations");
        reservationHeading.getStyleClass().add("section-title");
        Region reservationSpacer = new Region();
        HBox.setHgrow(reservationSpacer, Priority.ALWAYS);
        HBox reservationActions = new HBox(8, checkInButton, checkOutButton, deleteReservationButton);
        reservationActions.setAlignment(Pos.CENTER_RIGHT);
        HBox reservationHeadingRow = new HBox(reservationHeading, reservationSpacer, reservationActions);
        reservationHeadingRow.setAlignment(Pos.CENTER_LEFT);
        VBox reservationCard = new VBox(10, reservationHeadingRow, reservationTable);
        reservationCard.getStyleClass().addAll("card", "reservation-card");
        reservationCard.setEffect(createShadow());

        if (billList.isEmpty()) {
            billList.add(new Bill("B002", "Mary Banda", "Standard", 2, 800.00, 500.00, 300.00));
            billList.add(new Bill("B003", "John Phiri", "Deluxe", 5, 2812.50, 1000.00, 1812.50));
        }

        calculateButton.setOnAction(e -> {
            try {
                String guest = validatedGuest(guestNameField);
                int nights = validatedNights(nightsField);
                double extras = validatedAmount(extrasField, "Extras");
                double paid = validatedAmount(paidField, "Amount paid");
                double total = calculateTotal(roomTypeBox.getValue(), nights, extras);
                setStatus(statusLabel, String.format("Preview for %s: K %.2f total · K %.2f balance", guest, total, total - paid), true);
            } catch (IllegalArgumentException ex) {
                setStatus(statusLabel, ex.getMessage(), false);
            }
        });

        saveButton.setOnAction(e -> {
            try {
                String guest = validatedGuest(guestNameField);
                String room = roomTypeBox.getValue();
                int nights = validatedNights(nightsField);
                double extras = validatedAmount(extrasField, "Extras");
                double paid = validatedAmount(paidField, "Amount paid");
                double total = calculateTotal(room, nights, extras);
                double balance = total - paid;

                billCounter++;
                String billId = String.format("B%03d", billCounter);
                billList.add(new Bill(billId, guest, room, nights, total, paid, balance));
                billTable.getSelectionModel().selectLast();
                setStatus(statusLabel, String.format("Saved %s · Total K %.2f · Balance K %.2f", billId, total, balance), true);
            } catch (IllegalArgumentException ex) {
                setStatus(statusLabel, ex.getMessage(), false);
            }
        });

        reservationButton.setOnAction(e -> {
            try {
                String guest = validatedGuest(guestNameField);
                int nights = validatedNights(nightsField);
                LocalDate checkIn = checkInField.getValue();
                if (checkIn == null) {
                    throw new IllegalArgumentException("Choose a check-in date.");
                }
                reservationCounter++;
                String reservationId = String.format("R%03d", reservationCounter);
                reservationList.add(new Reservation(reservationId, guest, roomTypeBox.getValue(), checkIn.toString(),
                        nights, notesArea.getText().trim()));
                reservationTable.getSelectionModel().selectLast();
                setStatus(statusLabel, "Reservation " + reservationId + " added and confirmed.", true);
            } catch (IllegalArgumentException ex) {
                setStatus(statusLabel, ex.getMessage(), false);
            }
        });

        viewReceiptButton.setOnAction(e -> showReceipt(stage, billTable.getSelectionModel().getSelectedItem()));
        billTable.setRowFactory(tv -> {
            TableRow<Bill> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showReceipt(stage, row.getItem());
                }
            });
            return row;
        });
        deleteBillButton.setOnAction(e -> billList.remove(billTable.getSelectionModel().getSelectedItem()));
        deleteReservationButton.setOnAction(e -> reservationList.remove(reservationTable.getSelectionModel().getSelectedItem()));
        checkInButton.setOnAction(e -> {
            Reservation selected = reservationTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            try {
                selected.checkIn();
                reservationTable.refresh();
                checkInButton.setDisable(true);
                checkOutButton.setDisable(false);
                setStatus(statusLabel, selected.getGuestName() + " checked in at " + selected.getCheckedInAt() + ".", true);
            } catch (IllegalStateException ex) {
                setStatus(statusLabel, ex.getMessage(), false);
            }
        });
        checkOutButton.setOnAction(e -> {
            Reservation selected = reservationTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            try {
                selected.checkOut();
                reservationTable.refresh();
                checkInButton.setDisable(true);
                checkOutButton.setDisable(true);
                setStatus(statusLabel, selected.getGuestName() + " checked out at " + selected.getCheckedOutAt() + ".", true);
            } catch (IllegalStateException ex) {
                setStatus(statusLabel, ex.getMessage(), false);
            }
        });

        newGuestItem.setOnAction(e -> {
            guestNameField.clear();
            roomTypeBox.setValue("Standard");
            checkInField.setValue(LocalDate.now());
            nightsField.setText("1");
            extrasField.setText("0");
            paidField.setText("0");
            notesArea.clear();
            setStatus(statusLabel, "Ready for a new guest.", true);
        });
        logoutItem.setOnAction(e -> showLoginScreen(stage));
        aboutItem.setOnAction(e -> {
            Alert about = new Alert(Alert.AlertType.INFORMATION);
            about.initOwner(stage);
            about.setTitle("About MUZU Lodge");
            about.setHeaderText("MUZU Lodge");
            about.setContentText("Guest billing and reservation management for your lodge.");
            about.showAndWait();
        });

        VBox contentArea = new VBox(16, headerBox, topSection, actionBox, billCard, reservationCard);
        contentArea.setPadding(new Insets(20));
        ScrollPane scrollPane = new ScrollPane(contentArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox mainLayout = new VBox(menuBar, scrollPane);
        mainLayout.getStyleClass().add("dashboard-shell");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Scene mainScene = new Scene(mainLayout, 1120, 900);
        mainScene.getStylesheets().add(STYLESHEET);
        stage.setMinWidth(900);
        stage.setMinHeight(700);
        stage.setScene(mainScene);
        stage.show();
    }

    private String validatedGuest(TextField guestNameField) {
        String guest = guestNameField.getText().trim();
        if (guest.isEmpty()) {
            throw new IllegalArgumentException("Enter the guest's name.");
        }
        return guest;
    }

    private int validatedNights(TextField nightsField) {
        try {
            int nights = Integer.parseInt(nightsField.getText().trim());
            if (nights < 1) {
                throw new IllegalArgumentException("Nights must be at least 1.");
            }
            return nights;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Enter a valid whole number of nights.");
        }
    }

    private double validatedAmount(TextField field, String fieldName) {
        try {
            double amount = Double.parseDouble(field.getText().trim());
            if (!Double.isFinite(amount) || amount < 0) {
                throw new IllegalArgumentException(fieldName + " must be a non-negative amount.");
            }
            return amount;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Enter a valid amount for " + fieldName.toLowerCase() + ".");
        }
    }

    private double calculateTotal(String room, int nights, double extras) {
        double rate = switch (room) {
            case "Deluxe" -> 550.00;
            case "Family" -> 750.00;
            default -> 350.00;
        };
        double roomCharge = rate * nights;
        if (nights >= 5) {
            roomCharge *= 0.95;
        }
        return roomCharge + extras;
    }

    private void setStatus(Label statusLabel, String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (success ? "#237244" : "#B34436") + "; -fx-font-size: 12px;");
    }

    private void showReceipt(Stage owner, Bill bill) {
        if (bill == null) {
            return;
        }

        Dialog<Void> receiptDialog = new Dialog<>();
        receiptDialog.initOwner(owner);
        receiptDialog.initModality(Modality.WINDOW_MODAL);
        receiptDialog.setTitle("MUZU Lodge receipt");
        receiptDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        receiptDialog.getDialogPane().getStylesheets().add(STYLESHEET);

        Label lodgeName = new Label("MUZU Lodge");
        lodgeName.setStyle("-fx-font-size: 23px; -fx-font-weight: bold; -fx-text-fill: #173D27;");
        Label subtitle = new Label("GUEST PAYMENT RECEIPT");
        subtitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #718174;");
        VBox receiptHeader = new VBox(3, lodgeName, subtitle);
        receiptHeader.setAlignment(Pos.CENTER);

        VBox details = new VBox(10);
        details.getChildren().addAll(
                receiptLine("Receipt / bill number", bill.getBillId()),
                receiptLine("Guest", bill.getGuestName()),
                receiptLine("Room", bill.getRoomType()),
                receiptLine("Length of stay", bill.getNights() + (bill.getNights() == 1 ? " night" : " nights")),
                new Separator(),
                receiptLine("Total", bill.getTotal()),
                receiptLine("Amount paid", bill.getPaid()),
                receiptLine("Balance due", bill.getBalance())
        );

        Label thanks = new Label("Thank you for choosing MUZU Lodge.");
        thanks.getStyleClass().add("label-sub");
        VBox receipt = new VBox(18, receiptHeader, new Separator(), details, thanks);
        receipt.setPadding(new Insets(12));
        receipt.setPrefWidth(370);
        receiptDialog.getDialogPane().setContent(receipt);
        receiptDialog.showAndWait();
    }

    private HBox receiptLine(String labelText, String valueText) {
        Label label = new Label(labelText);
        label.getStyleClass().add("label-sub");
        Label value = new Label(valueText);
        value.setStyle("-fx-font-weight: bold; -fx-text-fill: #26382A;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(12, label, spacer, value);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
