public final class Input {

    private Input() {}

    public static boolean isNonBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    public static boolean hasNoDelimiters(String s) {
        return s != null && !s.contains(",") && !s.contains(";");
    }

    public static boolean isValidAvatar(String s) {
        return isNonBlank(s)
                && (s.endsWith(".png") || s.endsWith(".jpg"))
                && hasNoDelimiters(s);
    }
}
