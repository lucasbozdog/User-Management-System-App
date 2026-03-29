import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("ClassCanBeRecord")
public class UserProfile {
    private final int id;
    private final String name;
    private final String username;
    private final String avatar;
    private final Set<String> interests;

    public UserProfile(int id, String name, String username, String avatar, Set<String> interests) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.avatar = avatar;
        this.interests = interests != null ? interests : new HashSet<>();
    }

    @SuppressWarnings("unused")
    public int getId() {
        return id;
    }

    @SuppressWarnings("unused")
    public String getName() {
        return name;
    }

    @SuppressWarnings("unused")
    public String getUsername() {
        return username;
    }

    @SuppressWarnings("unused")
    public String getAvatar() {
        return avatar;
    }

    @SuppressWarnings("unused")
    public String getInterestsText() {
        if (interests.isEmpty()) return "";
        return interests.stream().sorted().collect(Collectors.joining(", "));
    }

}
