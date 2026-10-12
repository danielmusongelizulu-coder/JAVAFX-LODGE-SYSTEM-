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
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.Interpolator;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LodgeBillingApp extends Application {

    private final ObservableList<Bill> billList = FXCollections.observableArrayList();
    private final ObservableList<Reservation> reservationList = FXCollections.observableArrayList();
    private final LocalUserStore userStore = new LocalUserStore();
    private final LocalLodgeStore lodgeStore = new LocalLodgeStore();
    private int billCounter = 0;
    private int reservationCounter = 0;
    private RotateTransition logoRotation;

    private static final String STYLESHEET =
            LodgeBillingApp.class.getResource("/lodge.css").toExternalForm();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("MUZU Lodge");
        try {
            lodgeStore.load(billList, reservationList);
            billCounter = highestIdNumber(billList.stream().map(Bill::getBillId).toList(), "B");
            reservationCounter = highestIdNumber(
                    reservationList.stream().map(Reservation::getReservationId).toList(), "R");
        } catch (IOException | RuntimeException ex) {
            Alert error = new Alert(AlertType.ERROR);
            error.setTitle("Unable to load lodge records");
            error.setHeaderText("The local lodge data could not be read.");
            error.setContentText(ex.getMessage());
            error.showAndWait();
            primaryStage.close();
            return;
        }
        showLoginScreen(primaryStage);
    }

    private int highestIdNumber(java.util.List<String> ids, String prefix) {
        return ids.stream()
                .filter(id -> id != null && id.startsWith(prefix))
                .map(id -> id.substring(prefix.length()))
                .filter(number -> number.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max().orElse(0);
    }

    private boolean persistRecords(Stage owner, Label statusLabel) {
        try {
            lodgeStore.save(billList, reservationList);
            return true;
        } catch (IOException ex) {
            setStatus(statusLabel, "Record changed in memory but could not be saved locally.", false);
            Alert error = new Alert(AlertType.ERROR);
            error.initOwner(owner);
            error.setTitle("Unable to save lodge records");
            error.setHeaderText("Your latest changes were not saved to this computer.");
            error.setContentText(ex.getMessage());
            error.showAndWait();
            return false;
        }
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
        stage.setTitle("MUZU Lodge | Welcome");

        VBox card = new VBox(18);
        card.getStyleClass().addAll("card", "auth-card");
        card.setEffect(createShadow());
        card.setMaxWidth(470);

        HBox brand = createBrand(66);
        brand.setAlignment(Pos.CENTER);
        Label intro = new Label("A more personal stay starts here.");
        intro.getStyleClass().add("label-sub");
        intro.setMaxWidth(Double.MAX_VALUE);
        intro.setAlignment(Pos.CENTER);

        Tab guestTab = new Tab("Guest login");
        Tab createTab = new Tab("Create account");
        Tab adminTab = new Tab("Admin login");
        TabPane modes = new TabPane(guestTab, createTab, adminTab);
        modes.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        modes.getStyleClass().add("auth-tabs");

        TextField userField = new TextField();
        userField.setPromptText("Username");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        GridPane fields = new GridPane();
        fields.setVgap(11);
        fields.add(userField, 0, 0);
        fields.add(passField, 0, 1);
        for (javafx.scene.Node node : fields.getChildren()) {
            GridPane.setHgrow(node, Priority.ALWAYS);
            if (node instanceof Region region) {
                region.setMaxWidth(Double.MAX_VALUE);
            }
        }

        Button submitButton = new Button("Sign in to your account");
        submitButton.setDefaultButton(true);
        submitButton.getStyleClass().add("button-primary");
        submitButton.setMaxWidth(Double.MAX_VALUE);
        Label feedback = new Label();
        feedback.setWrapText(true);
        feedback.setStyle("-fx-text-fill: #B34436; -fx-font-size: 12px;");

        Runnable updateMode = () -> {
            boolean creatingAccount = modes.getSelectionModel().getSelectedItem() == createTab;
            boolean adminLogin = modes.getSelectionModel().getSelectedItem() == adminTab;
            submitButton.setText(creatingAccount ? "Create your account"
                    : adminLogin ? "Sign in to admin console" : "Sign in to your account");
            intro.setText(creatingAccount ? "Create your personal MUZU Lodge account."
                    : adminLogin ? "Secure access to your lodge operations."
                    : "Welcome back. Your stay, all in one place.");
            feedback.setText("");
            feedback.setStyle("-fx-text-fill: #B34436; -fx-font-size: 12px;");
        };
        modes.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldTab, selectedTab) -> updateMode.run());

        submitButton.setOnAction(event -> {
            String username = userField.getText().trim();
            char[] password = passField.getText().toCharArray();
            try {
                if (modes.getSelectionModel().getSelectedItem() == createTab) {
                    if (!username.matches("(?i)[a-z0-9][a-z0-9._-]{0,31}") || username.equalsIgnoreCase("admin")) {
                        feedback.setText("Choose a username using letters, numbers, dots, _ or - (up to 32 characters).");
                    } else if (password.length == 0) {
                        feedback.setText("Choose a password.");
                    } else {
                        userStore.register(username, password);
                        userField.setText(username);
                        passField.clear();
                        modes.getSelectionModel().select(guestTab);
                        feedback.setText("Account created on this computer. Sign in to continue.");
                        feedback.setStyle("-fx-text-fill: #237244; -fx-font-size: 12px;");
                    }
                } else if (username.isBlank() || password.length == 0) {
                    feedback.setText("Enter your username and password.");
                } else if (modes.getSelectionModel().getSelectedItem() == adminTab) {
                    if (username.equals("admin") && passField.getText().equals("1234")) {
                        showMainLabScreen(stage);
                    } else {
                        feedback.setText("Admin credentials are not valid.");
                    }
                } else {
                    LocalUserStore.Account account = userStore.authenticate(username, password);
                    if (account == null) {
                        feedback.setText("We couldn't find an account with those credentials.");
                    } else {
                        showUserDashboard(stage, account);
                    }
                }
            } catch (IllegalArgumentException ex) {
                feedback.setText(ex.getMessage());
            } catch (IOException | GeneralSecurityException ex) {
                feedback.setText("We couldn't access the local account store: " + ex.getMessage());
            } finally {
                java.util.Arrays.fill(password, '\0');
            }
        });

        card.getChildren().addAll(brand, intro, modes, fields, submitButton, feedback);
        StackPane container = new StackPane(card);
        container.setPadding(new Insets(36));

        Scene loginScene = new Scene(container, 580, 670);
        loginScene.getStylesheets().add(STYLESHEET);
        stage.setScene(loginScene);
        stage.setMinWidth(520);
        stage.setMinHeight(620);
        stage.show();
    }

    private VBox createMetricCard(String title, String value, String caption) {
        Label titleLabel = new Label(title.toUpperCase(Locale.ROOT));
        titleLabel.getStyleClass().add("metric-caption");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("metric-value");
        Label detailLabel = new Label(caption);
        detailLabel.getStyleClass().add("label-sub");
        VBox card = new VBox(8, titleLabel, valueLabel, detailLabel);
        card.getStyleClass().addAll("card", "metric-card");
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private void showUserDashboard(Stage stage, LocalUserStore.Account account) {
        stopLogoRotation();
        stage.setTitle("MUZU Lodge | Guest dashboard");

        String guestName = account.username();
        ObservableList<Bill> guestBills = FXCollections.observableArrayList(
                billList.stream()
                        .filter(bill -> belongsToAccount(bill.getGuestName(), account))
                        .toList());
        ObservableList<Reservation> guestReservations = FXCollections.observableArrayList(
                reservationList.stream()
                        .filter(reservation -> belongsToAccount(reservation.getGuestName(), account))
                        .toList());

        HBox brand = createBrand(58);
        Label accountBadge = new Label("GUEST ACCOUNT");
        accountBadge.getStyleClass().add("account-badge");
        Button logoutButton = new Button("Log out");
        logoutButton.getStyleClass().add("button-secondary");
        logoutButton.setOnAction(event -> showLoginScreen(stage));
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        HBox header = new HBox(18, brand, headerSpacer, accountBadge, logoutButton);
        header.getStyleClass().add("dashboard-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 18, 12, 18));

        Label welcome = new Label("Welcome, " + guestName);
        welcome.getStyleClass().add("welcome-title");
        Label welcomeSubtitle = new Label("Your MUZU Lodge stay, thoughtfully brought together.");
        welcomeSubtitle.getStyleClass().add("label-sub");
        VBox welcomeCard = new VBox(9, welcome, welcomeSubtitle);
        welcomeCard.getStyleClass().addAll("card", "welcome-card");

        double outstanding = guestBills.stream().mapToDouble(Bill::getBalanceAmount).sum();
        HBox metrics = new HBox(14,
                createMetricCard("Your bills", Integer.toString(guestBills.size()), "Receipts ready to view"),
                createMetricCard("Reservations", Integer.toString(guestReservations.size()), "Your stays with us"),
                createMetricCard("Outstanding", String.format("K %.2f", outstanding), "Current balance due"));

        Label billsHeading = new Label("Your billing & receipts");
        billsHeading.getStyleClass().add("section-title");
        TableView<Bill> billsTable = new TableView<>(guestBills);
        billsTable.setPlaceholder(new Label("No bills are linked to your account yet."));
        billsTable.getColumns().add(tableColumn("Bill ID", "billId"));
        billsTable.getColumns().add(tableColumn("Room", "roomType"));
        billsTable.getColumns().add(tableColumn("Nights", "nights"));
        billsTable.getColumns().add(tableColumn("Total", "total"));
        billsTable.getColumns().add(tableColumn("Paid", "paid"));
        billsTable.getColumns().add(tableColumn("Balance due", "balance"));
        billsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        billsTable.setPrefHeight(240);
        billsTable.setRowFactory(table -> {
            TableRow<Bill> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showReceipt(stage, row.getItem());
                }
            });
            return row;
        });
        Button receiptButton = new Button("View receipt");
        receiptButton.getStyleClass().add("button-primary");
        receiptButton.disableProperty().bind(billsTable.getSelectionModel().selectedItemProperty().isNull());
        receiptButton.setOnAction(event ->
                showReceipt(stage, billsTable.getSelectionModel().getSelectedItem()));
        HBox billActions = new HBox(receiptButton);
        billActions.setAlignment(Pos.CENTER_RIGHT);
        VBox billsSection = new VBox(12, billsHeading, billsTable, billActions);
        billsSection.setId("guest-bills");
        billsSection.getStyleClass().addAll("card", "bill-card");

        Label reservationsHeading = new Label("Your reservations");
        reservationsHeading.getStyleClass().add("section-title");
        TableView<Reservation> reservationsTable = new TableView<>(guestReservations);
        reservationsTable.setPlaceholder(new Label("No reservations are linked to your account yet."));
        reservationsTable.getColumns().add(tableColumn("Reservation", "reservationId"));
        reservationsTable.getColumns().add(tableColumn("Room", "roomType"));
        reservationsTable.getColumns().add(tableColumn("Check-in", "checkIn"));
        reservationsTable.getColumns().add(tableColumn("Nights", "nights"));
        reservationsTable.getColumns().add(tableColumn("Status", "status"));
        reservationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        reservationsTable.setPrefHeight(220);
        VBox reservationsSection = new VBox(12, reservationsHeading, reservationsTable);
        reservationsSection.setId("guest-reservations");
        reservationsSection.getStyleClass().addAll("card", "reservation-card");

        Button billsLink = new Button("My bills");
        Button reservationsLink = new Button("My reservations");
        billsLink.getStyleClass().add("nav-button");
        reservationsLink.getStyleClass().add("nav-button");
        VBox content = new VBox(18, header, welcomeCard, metrics, billsSection, reservationsSection);
        content.setPadding(new Insets(22));
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        billsLink.setOnAction(event -> scrollToSection(scrollPane, content, billsSection));
        reservationsLink.setOnAction(event -> scrollToSection(scrollPane, content, reservationsSection));

        Label guestNavTitle = new Label("YOUR STAY");
        guestNavTitle.getStyleClass().add("nav-heading");
        VBox navigation = new VBox(9, guestNavTitle, billsLink, reservationsLink);
        navigation.getStyleClass().add("dashboard-nav");
        navigation.setPadding(new Insets(22, 12, 12, 12));
        navigation.setMinWidth(180);
        navigation.setMaxWidth(195);
        HBox workspace = new HBox(navigation, scrollPane);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        VBox layout = new VBox(workspace);
        layout.getStyleClass().add("dashboard-shell");
        VBox.setVgrow(workspace, Priority.ALWAYS);

        Scene scene = new Scene(layout, 1180, 820);
        scene.getStylesheets().add(STYLESHEET);
        stage.setMinWidth(860);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();
    }

    private <T> TableColumn<T, Object> tableColumn(String title, String property) {
        TableColumn<T, Object> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }

    private void scrollToSection(ScrollPane scrollPane, VBox content, Region section) {
        double scrollableHeight = content.getHeight() - scrollPane.getViewportBounds().getHeight();
        if (scrollableHeight > 0) {
            scrollPane.setVvalue(Math.min(1, section.getLayoutY() / scrollableHeight));
        }
    }

    private void showMainLabScreen(Stage stage) throws IOException {
        stopLogoRotation();
        stage.setTitle("MUZU Lodge | Admin Dashboard");

        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem newGuestItem = new MenuItem("New Guest");
        MenuItem logoutItem = new MenuItem("Logout");
        fileMenu.getItems().addAll(newGuestItem, new SeparatorMenuItem(), logoutItem);
        logoutItem.setOnAction(e -> showLoginScreen(stage));
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About MUZU Lodge");
        helpMenu.getItems().add(aboutItem);
        menuBar.getMenus().addAll(fileMenu, helpMenu);

        HBox brand = createBrand(54);
        Label adminBadge = new Label("ADMIN CONSOLE");
        adminBadge.getStyleClass().add("account-badge");
        Button logoutButton = new Button("Log out");
        logoutButton.getStyleClass().add("button-secondary");
        logoutButton.setOnAction(e -> showLoginScreen(stage));
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        HBox headerBox = new HBox(brand, headerSpacer, adminBadge, logoutButton);
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
        guestNameField.setPromptText("Guest name or registered username");
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

        formGrid.add(new Label("Guest / username"), 0, 0);
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
        Label rulesText = new Label("5+ nights: 5% off room charge\nExtras are not discounted.\n\nUse a guest's registered username to link bills and reservations to their account and receipts.\n\nRecords are saved locally on this computer.");
        rulesText.getStyleClass().add("label-sub");
        rulesText.setWrapText(true);
        infoCard.getChildren().addAll(infoTitle, pricesText, sep, rulesText);

        HBox topSection = new HBox(18, formCard, infoCard);
        topSection.setId("admin-overview");
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

        ObservableList<LocalUserStore.Account> accounts = FXCollections.observableArrayList(userStore.listAccounts());
        TableView<LocalUserStore.Account> accountsTable = new TableView<>(accounts);
        accountsTable.setPlaceholder(new Label("No guest accounts have been created yet."));
        TableColumn<LocalUserStore.Account, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().username()));
        TableColumn<LocalUserStore.Account, Integer> accountBillsColumn = new TableColumn<>("Bills");
        accountBillsColumn.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(
                countBillsForAccount(cell.getValue())).asObject());
        TableColumn<LocalUserStore.Account, Integer> accountReservationsColumn =
                new TableColumn<>("Reservations");
        accountReservationsColumn.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(
                countReservationsForAccount(cell.getValue())).asObject());
        accountsTable.getColumns().add(usernameColumn);
        accountsTable.getColumns().add(accountBillsColumn);
        accountsTable.getColumns().add(accountReservationsColumn);
        accountsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        accountsTable.setPrefHeight(230);

        Label accountsHeading = new Label("Manage accounts");
        accountsHeading.getStyleClass().add("section-title");
        Label accountHelp = new Label(
                "Deleting an account also permanently deletes its linked bills and reservations.");
        accountHelp.getStyleClass().add("label-sub");
        accountHelp.setWrapText(true);
        Button deleteAccountButton = new Button("Delete account and data");
        deleteAccountButton.getStyleClass().add("button-danger");
        deleteAccountButton.disableProperty().bind(
                accountsTable.getSelectionModel().selectedItemProperty().isNull());
        HBox accountActions = new HBox(deleteAccountButton);
        accountActions.setAlignment(Pos.CENTER_RIGHT);
        VBox accountsCard = new VBox(12, accountsHeading, accountHelp, accountsTable, accountActions);
        accountsCard.setId("admin-accounts");
        accountsCard.getStyleClass().addAll("card", "account-card");
        accountsCard.setEffect(createShadow());

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
                if (!persistRecords(stage, statusLabel)) {
                    return;
                }
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
                if (!persistRecords(stage, statusLabel)) {
                    return;
                }
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
        deleteBillButton.setOnAction(e -> {
            billList.remove(billTable.getSelectionModel().getSelectedItem());
            persistRecords(stage, statusLabel);
        });
        deleteReservationButton.setOnAction(e -> {
            reservationList.remove(reservationTable.getSelectionModel().getSelectedItem());
            persistRecords(stage, statusLabel);
        });
        deleteAccountButton.setOnAction(e -> {
            LocalUserStore.Account selected = accountsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                deleteUserAccount(stage, selected, accountsTable, statusLabel);
            }
        });
        checkInButton.setOnAction(e -> {
            Reservation selected = reservationTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            try {
                selected.checkIn();
                reservationTable.refresh();
                if (!persistRecords(stage, statusLabel)) {
                    return;
                }
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
                if (!persistRecords(stage, statusLabel)) {
                    return;
                }
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
        aboutItem.setOnAction(e -> {
            Alert about = new Alert(Alert.AlertType.INFORMATION);
            about.initOwner(stage);
            about.setTitle("About MUZU Lodge");
            about.setHeaderText("MUZU Lodge");
            about.setContentText("Guest billing and reservation management for your lodge.");
            about.showAndWait();
        });

        VBox contentArea = new VBox(16, headerBox, topSection, actionBox, billCard, reservationCard, accountsCard);
        contentArea.setPadding(new Insets(20));
        billCard.setId("admin-bills");
        reservationCard.setId("admin-reservations");
        ScrollPane scrollPane = new ScrollPane(contentArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        Button overviewLink = new Button("Overview & billing");
        Button billsLink = new Button("Guest bills");
        Button reservationsLink = new Button("Reservations");
        Button accountsLink = new Button("Manage accounts");
        overviewLink.getStyleClass().add("nav-button");
        billsLink.getStyleClass().add("nav-button");
        reservationsLink.getStyleClass().add("nav-button");
        accountsLink.getStyleClass().add("nav-button");
        overviewLink.setOnAction(e -> scrollToSection(scrollPane, contentArea, topSection));
        billsLink.setOnAction(e -> scrollToSection(scrollPane, contentArea, billCard));
        reservationsLink.setOnAction(e -> scrollToSection(scrollPane, contentArea, reservationCard));
        accountsLink.setOnAction(e -> scrollToSection(scrollPane, contentArea, accountsCard));
        Label navTitle = new Label("LODGE OPERATIONS");
        navTitle.getStyleClass().add("nav-heading");
        VBox navigation = new VBox(9, navTitle, overviewLink, billsLink, reservationsLink, accountsLink);
        navigation.getStyleClass().add("dashboard-nav");
        navigation.setPadding(new Insets(22, 12, 12, 12));
        navigation.setMinWidth(190);
        navigation.setMaxWidth(205);
        HBox workspace = new HBox(navigation, scrollPane);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        VBox mainLayout = new VBox(menuBar, workspace);
        mainLayout.getStyleClass().add("dashboard-shell");
        VBox.setVgrow(workspace, Priority.ALWAYS);

        Scene mainScene = new Scene(mainLayout, 1120, 900);
        mainScene.getStylesheets().add(STYLESHEET);
        stage.setMinWidth(900);
        stage.setMinHeight(700);
        stage.setScene(mainScene);
        stage.show();
    }

    private int countBillsForAccount(LocalUserStore.Account account) {
        return Math.toIntExact(billList.stream()
                .filter(bill -> belongsToAccount(bill.getGuestName(), account))
                .count());
    }

    private int countReservationsForAccount(LocalUserStore.Account account) {
        return Math.toIntExact(reservationList.stream()
                .filter(reservation -> belongsToAccount(reservation.getGuestName(), account))
                .count());
    }

    private void deleteUserAccount(Stage owner, LocalUserStore.Account account,
                                   TableView<LocalUserStore.Account> accountsTable, Label statusLabel) {
        int billCount = countBillsForAccount(account);
        int reservationCount = countReservationsForAccount(account);
        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.initOwner(owner);
        confirmation.setTitle("Delete guest account");
        confirmation.setHeaderText("Permanently delete " + account.username() + "?");
        confirmation.setContentText("This will also delete " + billCount + " bill(s) and "
                + reservationCount + " reservation(s). This cannot be undone.");
        ButtonType deleteButtonType = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        confirmation.getButtonTypes().setAll(deleteButtonType, ButtonType.CANCEL);
        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != deleteButtonType) {
            return;
        }

        List<Bill> previousBills = new ArrayList<>(billList);
        List<Reservation> previousReservations = new ArrayList<>(reservationList);
        billList.removeIf(bill -> belongsToAccount(bill.getGuestName(), account));
        reservationList.removeIf(reservation -> belongsToAccount(reservation.getGuestName(), account));
        if (!persistRecords(owner, statusLabel)) {
            billList.setAll(previousBills);
            reservationList.setAll(previousReservations);
            setStatus(statusLabel, "Account was not deleted because its linked records could not be saved.", false);
            return;
        }

        try {
            userStore.deleteAccount(account.username());
            accountsTable.getItems().remove(account);
            setStatus(statusLabel, "Deleted account " + account.username() + " and its linked data.", true);
        } catch (IOException | IllegalArgumentException ex) {
            billList.setAll(previousBills);
            reservationList.setAll(previousReservations);
            boolean dataRestored = persistRecords(owner, statusLabel);
            Alert error = new Alert(AlertType.ERROR);
            error.initOwner(owner);
            error.setTitle("Unable to delete account");
            error.setHeaderText(dataRestored
                    ? "The account was not deleted."
                    : "The account was not deleted and its linked records could not be restored.");
            error.setContentText(ex.getMessage());
            error.showAndWait();
        }
    }

    private boolean belongsToAccount(String guestName, LocalUserStore.Account account) {
        return guestName != null && (guestName.equalsIgnoreCase(account.username())
                || guestName.equalsIgnoreCase(account.legacyName()));
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

        HBox logo = createBrand(50);
        logo.setAlignment(Pos.CENTER);
        Label subtitle = new Label("GUEST PAYMENT RECEIPT");
        subtitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #718174;");
        VBox receiptHeader = new VBox(3, logo, subtitle);
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
        receiptDialog.setOnHidden(event -> stopLogoRotation());
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
