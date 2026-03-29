import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        DbUserRepository db = new DbUserRepository(
                "jdbc:postgresql://localhost:5433/cg_app",
                "cg_app",
                "lucabozdog"
        );

        SceneRouter router = new SceneRouter(stage, db);
        stage.setTitle("CommonGround");
        router.showRoleSelect();
    }

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        launch(args);
    }
}
