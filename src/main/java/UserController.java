import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class UserController implements BaseController {

    private SceneRouter router;
    private DbUserRepository db;

    @FXML private Label statusLabel;

    @FXML private TextField u1Field;
    @FXML private TextField u2Field;

    @FXML private TextField ru1Field;
    @FXML private TextField ru2Field;

    @FXML private TextField showUserField;

    @FXML private TextField interestUserField;
    @FXML private TextField addInterestField;
    @FXML private TextField removeInterestField;


    @FXML private ListView<String> connectionsList;

    @FXML private TableView<UserProfile> usersTable;
    @FXML private TableColumn<UserProfile, Integer> idCol;
    @FXML private TableColumn<UserProfile, String> nameCol;
    @FXML private TableColumn<UserProfile, String> usernameCol;
    @FXML private TableColumn<UserProfile, String> avatarCol;
    @FXML private TableColumn<UserProfile, String> interestsCol;


    @Override
    public void init(SceneRouter router, DbUserRepository db) {
        this.router = router;
        this.db = db;

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        avatarCol.setCellValueFactory(new PropertyValueFactory<>("avatar"));
        interestsCol.setCellValueFactory(new PropertyValueFactory<>("interestsText"));


        refreshUsers();
        statusLabel.setText("");
    }

    public void back() {
        router.showRoleSelect();
    }

    public void refreshUsers() {
        try {
            List<UserProfile> users = db.getAllUsers();
            usersTable.setItems(FXCollections.observableArrayList(users));

            connectionsList.getItems().clear();
            showUserField.clear();

            u1Field.clear();
            u2Field.clear();
            ru1Field.clear();
            ru2Field.clear();

            statusLabel.setText("Loaded " + users.size() + " users (connections cleared)");
        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
        }
    }


    public void addConnection() {
        try {
            String u1 = u1Field.getText().trim();
            String u2 = u2Field.getText().trim();

            if (u1.isEmpty() || u2.isEmpty()) {
                statusLabel.setText("Both usernames are required");
                return;
            }

            boolean ok = db.addConnectionByUsername(u1, u2);
            statusLabel.setText(ok ? "Connected " + u1 + " <-> " + u2
                    : "Could not connect (missing user / same user / already connected)");
        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
        }
    }

    public void removeConnection() {
        try {
            String u1 = ru1Field.getText().trim();
            String u2 = ru2Field.getText().trim();

            if (u1.isEmpty() || u2.isEmpty()) {
                statusLabel.setText("Both usernames are required");
                return;
            }

            boolean ok = db.removeConnectionByUsername(u1, u2);
            statusLabel.setText(ok ? "Removed connection " + u1 + " <-> " + u2
                    : "No connection removed (not connected / missing user)");
        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
        }
    }

    public void loadConnections() {
        try {
            String u = showUserField.getText().trim();
            if (u.isEmpty()) {
                statusLabel.setText("Username is required");
                return;
            }

            List<String> names = db.getConnectionsUsernames(u);
            connectionsList.setItems(FXCollections.observableArrayList(names));
            statusLabel.setText("Loaded " + names.size() + " connections for " + u);
        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
        }
    }

    public void addInterest() {
        try {
            String username = interestUserField.getText().trim();
            String raw = addInterestField.getText().trim();

            if (username.isEmpty() || raw.isEmpty()) {
                statusLabel.setText("Username and interest are required");
                return;
            }

            int addedCount = 0;
            for (String part : raw.split(",")) {
                String label = part.trim();
                if (label.isEmpty()) continue;
                if (db.addInterestByUsername(username, label)) {
                    addedCount++;
                }
            }

            refreshUsers();
            statusLabel.setText("Added " + addedCount + " interest(s) to " + username);
        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
        }
    }

    public void removeInterest() {
        try {
            String username = interestUserField.getText().trim();
            String label = removeInterestField.getText().trim();

            if (username.isEmpty() || label.isEmpty()) {
                statusLabel.setText("Username and interest are required");
                return;
            }

            boolean ok = db.removeInterestByUsername(username, label);
            refreshUsers();
            statusLabel.setText(ok ? "Removed interest '" + label + "' from " + username
                    : "Nothing removed (user/interest missing or not linked)");
        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
        }
    }

}
