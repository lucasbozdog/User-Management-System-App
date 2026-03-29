import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
@SuppressWarnings("ClassCanBeRecord")
public class SceneRouter {
    private final Stage stage;
    private final DbUserRepository db;

    public SceneRouter(Stage stage, DbUserRepository db) {
        this.stage = stage;
        this.db = db;
    }

    public void showRoleSelect() { setScene("/RoleSelect.fxml", 420, 260); }
    public void showAdmin()      { setScene("/AdminScene.fxml", 900, 600); }
    public void showUser()       { setScene("/UserScene.fxml", 900, 600); }

    private void setScene(String fxmlPath, int w, int h) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof BaseController bc) {
                bc.init(this, db);
            }

            Scene scene = new Scene(root, w, h);

            scene.getStylesheets().clear();
            scene.getStylesheets().add(
                    java.util.Objects.requireNonNull(
                            getClass().getResource("/app.css")
                    ).toExternalForm()
            );

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML: " + fxmlPath, e);
        }
    }

}
