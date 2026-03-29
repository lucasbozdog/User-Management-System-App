import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;

public class AdminController implements BaseController {

    private SceneRouter router;
    private DbUserRepository db;

    @FXML private Label statusLabel;

    @FXML private TextField insertIdField;
    @FXML private TextField insertNameField;
    @FXML private TextField insertUsernameField;
    @FXML private TextField insertAvatarField;


    @FXML private TextField updateUsernameField;
    @FXML private TextField updateNameField;
    @FXML private TextField updateAvatarField;

    @FXML private TextField deleteUsernameField;

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


        refresh();
    }

    public void back() {
        router.showRoleSelect();
    }

    public void refresh() {
        try {
            List<UserProfile> users = db.getAllUsers();
            usersTable.setItems(FXCollections.observableArrayList(users));
            statusLabel.setText("Loaded " + users.size() + " users");
        } catch (SQLException ex) {
            statusLabel.setText("DB error: " + ex.getMessage());
        }
    }

    public void insertUser() {
        try {
            String idText = insertIdField.getText();
            String name = insertNameField.getText();
            String username = insertUsernameField.getText();
            String avatar = insertAvatarField.getText();

            if (!Input.isNonBlank(idText)
                    || !Input.isNonBlank(name)
                    || !Input.isNonBlank(username)
                    || !Input.isValidAvatar(avatar)) {
                statusLabel.setText("Invalid input");
                return;
            }

            int id = Integer.parseInt(idText.trim());

            db.insertUser(id, name.trim(), username.trim(), avatar.trim());
            statusLabel.setText("Inserted user " + username.trim());
            refresh();

        } catch (NumberFormatException ex) {
            statusLabel.setText("ID must be an integer");
        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
        }
    }

    public void updateUser() {
        try {
            String username = updateUsernameField.getText().trim();
            String newName = updateNameField.getText().trim();
            String newAvatar = updateAvatarField.getText().trim();

            int changed = db.updateUserByUsername(username, newName, newAvatar);
            statusLabel.setText(changed == 0 ? "No user found" : "Updated " + username);
            refresh();
        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
        }
    }

    public void deleteUser() {
        try {
            String username = deleteUsernameField.getText().trim();
            int deleted = db.deleteUserByUsername(username);
            statusLabel.setText(deleted == 0 ? "No user found" : "Deleted " + username);
            refresh();
        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
        }
    }

}
