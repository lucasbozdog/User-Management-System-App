import java.sql.*;
import java.util.*;

@SuppressWarnings("ClassCanBeRecord")
public class DbUserRepository {
    private final String url;
    private final String user;
    private final String password;

    public DbUserRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    @SuppressWarnings("SqlNoDataSourceInspection")
    public void insertUser(int id, String name, String username, String avatar) throws SQLException {
        String sql = "INSERT INTO users(id, name, username, avatar) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setString(2, name);
            ps.setString(3, username);
            ps.setString(4, avatar);

            ps.executeUpdate();
        }
    }

    @SuppressWarnings("SqlNoDataSourceInspection")
    public int updateUserByUsername(String username, String newName, String newAvatar) throws SQLException {
        String sql = "UPDATE users SET name = ?, avatar = ? WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newName);
            ps.setString(2, newAvatar);
            ps.setString(3, username);

            return ps.executeUpdate(); // returns how many rows changed
        }
    }

    @SuppressWarnings("SqlNoDataSourceInspection")
    public int deleteUserByUsername(String username) throws SQLException {
        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            return ps.executeUpdate(); // 1 if deleted, 0 if not found
        }
    }

    @SuppressWarnings("SqlNoDataSourceInspection")
    public Integer findUserIdByUsername(String username) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
            return null;
        }
    }

    @SuppressWarnings("SqlNoDataSourceInspection")
    public boolean addConnectionByUsername(String u1, String u2) throws SQLException {
        Integer id1 = findUserIdByUsername(u1);
        Integer id2 = findUserIdByUsername(u2);
        if (id1 == null || id2 == null) return false;
        if (id1.equals(id2)) return false;

        int a = Math.min(id1, id2);
        int b = Math.max(id1, id2);

        String sql = "INSERT INTO connections(user_id, friend_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, a);
            ps.setInt(2, b);
            return ps.executeUpdate() == 1; // true if inserted, false if already existed
        }
    }

    @SuppressWarnings("SqlNoDataSourceInspection")
    public boolean removeConnectionByUsername(String u1, String u2) throws SQLException {
        Integer id1 = findUserIdByUsername(u1);
        Integer id2 = findUserIdByUsername(u2);
        if (id1 == null || id2 == null) return false;
        if (id1.equals(id2)) return false;

        int a = Math.min(id1, id2);
        int b = Math.max(id1, id2);

        String sql = "DELETE FROM connections WHERE user_id = ? AND friend_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, a);
            ps.setInt(2, b);
            return ps.executeUpdate() == 1;
        }
    }

    @SuppressWarnings("SqlNoDataSourceInspection")
    public List<UserProfile> getAllUsers() throws SQLException {
        String sql = "SELECT id, name, username, avatar FROM users ORDER BY id";
        List<UserProfile> out = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                out.add(new UserProfile(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("username"),
                        rs.getString("avatar"),
                        getInterestsByUserId(id)
                ));
            }
        }
        return out;
    }

    @SuppressWarnings("SqlNoDataSourceInspection")
    public List<String> getConnectionsUsernames(String username) throws SQLException {
        Integer id = findUserIdByUsername(username);
        if (id == null) return Collections.emptyList();

        String sql =
                "SELECT u.username " +
                        "FROM connections c " +
                        "JOIN users u ON (u.id = CASE WHEN c.user_id = ? THEN c.friend_id ELSE c.user_id END) " +
                        "WHERE c.user_id = ? OR c.friend_id = ? " +
                        "ORDER BY u.username";

        List<String> out = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, id);
            ps.setInt(3, id);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getString("username"));
            }
        }
        return out;
    }

    @SuppressWarnings("SqlNoDataSourceInspection")
    public Set<String> getInterestsByUserId(int userId) throws SQLException {
        String sql =
                "SELECT i.label " +
                        "FROM user_interests ui " +
                        "JOIN interests i ON i.id = ui.interest_id " +
                        "WHERE ui.user_id = ? " +
                        "ORDER BY i.label";

        Set<String> out = new LinkedHashSet<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getString("label"));
            }
        }
        return out;
    }

    @SuppressWarnings("SqlNoDataSourceInspection")
    public boolean addInterestByUsername(String username, String interestLabel) throws SQLException {
        if (username == null || username.trim().isEmpty()) return false;
        if (interestLabel == null || interestLabel.trim().isEmpty()) return false;

        Integer userId = findUserIdByUsername(username.trim());
        if (userId == null) return false;

        String label = interestLabel.trim();

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                Integer interestId = findInterestIdByLabel(conn, label);
                if (interestId == null) {
                    insertInterestIfMissing(conn, label);
                    interestId = findInterestIdByLabel(conn, label);
                }
                if (interestId == null) {
                    conn.rollback();
                    return false;
                }

                String linkSql =
                        "INSERT INTO user_interests(user_id, interest_id) " +
                                "VALUES (?, ?) " +
                                "ON CONFLICT DO NOTHING";

                try (PreparedStatement ps = conn.prepareStatement(linkSql)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, interestId);
                    int changed = ps.executeUpdate();
                    conn.commit();
                    return changed == 1; // true if new link inserted, false if already existed
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    @SuppressWarnings("SqlNoDataSourceInspection")
    public boolean removeInterestByUsername(String username, String interestLabel) throws SQLException {
        if (username == null || username.trim().isEmpty()) return false;
        if (interestLabel == null || interestLabel.trim().isEmpty()) return false;

        Integer userId = findUserIdByUsername(username.trim());
        if (userId == null) return false;

        String label = interestLabel.trim();

        try (Connection conn = getConnection()) {
            Integer interestId = findInterestIdByLabel(conn, label);
            if (interestId == null) return false;

            String sql = "DELETE FROM user_interests WHERE user_id = ? AND interest_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setInt(2, interestId);
                return ps.executeUpdate() == 1;
            }
        }
    }

    @SuppressWarnings("SqlNoDataSourceInspection")
    private Integer findInterestIdByLabel(Connection conn, String label) throws SQLException {
        String sql = "SELECT id FROM interests WHERE label = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, label);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
                return null;
            }
        }
    }

    @SuppressWarnings("SqlNoDataSourceInspection")
    private void insertInterestIfMissing(Connection conn, String label) throws SQLException {
        String sql = "INSERT INTO interests(label) VALUES (?) ON CONFLICT DO NOTHING";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, label);
            ps.executeUpdate();
        }
    }
}


