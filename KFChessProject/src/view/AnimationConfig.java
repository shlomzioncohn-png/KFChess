package view;

public class AnimationConfig {
    public final double speedMetersPerSec;
    public final String nextStateWhenFinished;
    public final int framesPerSec;
    public final boolean isLoop;

    public AnimationConfig(double speedMetersPerSec, String nextStateWhenFinished,
                           int framesPerSec, boolean isLoop) {
        this.speedMetersPerSec = speedMetersPerSec;
        this.nextStateWhenFinished = nextStateWhenFinished;
        this.framesPerSec = framesPerSec;
        this.isLoop = isLoop;
    }

    public static AnimationConfig loadFromFile(String path) throws java.io.IOException {
        String text = java.nio.file.Files.readString(java.nio.file.Paths.get(path));

        double speed = extractDouble(text, "speed_m_per_sec");
        String nextState = extractString(text, "next_state_when_finished");
        int fps = (int) extractDouble(text, "frames_per_sec");
        boolean loop = extractBoolean(text, "is_loop");

        return new AnimationConfig(speed, nextState, fps, loop);
    }

    private static double extractDouble(String text, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*([0-9.]+)";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(text);
        if (!m.find()) throw new IllegalArgumentException("Missing key: " + key);
        return Double.parseDouble(m.group(1));
    }

    private static String extractString(String text, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([a-zA-Z_]+)\"";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(text);
        if (!m.find()) throw new IllegalArgumentException("Missing key: " + key);
        return m.group(1);
    }

    private static boolean extractBoolean(String text, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*(true|false)";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(text);
        if (!m.find()) throw new IllegalArgumentException("Missing key: " + key);
        return Boolean.parseBoolean(m.group(1));
    }

}