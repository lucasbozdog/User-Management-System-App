public class RoleSelectController implements BaseController {
    private SceneRouter router;

    @Override
    public void init(SceneRouter router, DbUserRepository db) {
        this.router = router;
    }

    public void goAdmin()  { router.showAdmin(); }
    public void goUser()   { router.showUser(); }
}
