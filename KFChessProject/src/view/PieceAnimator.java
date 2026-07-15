package view;

public class PieceAnimator {

    private final String piecesRootFolder; // למשל "resources/pieces1/QW"
    private String currentState;           // למשל "idle"
    private int currentFrameIndex;
    private long currentFrameStartTime;
    private AnimationConfig currentConfig;

    public PieceAnimator(String piecesRootFolder) throws java.io.IOException {
        this.piecesRootFolder = piecesRootFolder;
        this.currentState = "idle";
        this.currentFrameIndex = 0;
        this.currentFrameStartTime = 0L;
        this.currentConfig = loadConfigFor(currentState);
    }

    private AnimationConfig loadConfigFor(String state) throws java.io.IOException {
        return AnimationConfig.loadFromFile(piecesRootFolder + "/states/" + state + "/config.json");
    }

    public void changeState(String newState, long currentClock) throws java.io.IOException {
        this.currentState = newState;
        this.currentFrameIndex = 0;
        this.currentFrameStartTime = currentClock;
        this.currentConfig = loadConfigFor(newState);
    }

    public void update(long currentClock) throws java.io.IOException {
        long msPerFrame = 1000L / currentConfig.framesPerSec;
        long elapsed = currentClock - currentFrameStartTime;

        if (elapsed < msPerFrame) {
            return; // עוד לא הגיע הזמן להחליף פריים
        }

        int totalFrames = countFrames(piecesRootFolder + "/states/" + currentState + "/sprites");
        int nextFrame = currentFrameIndex + 1;

        if (nextFrame >= totalFrames) {
            if (currentConfig.isLoop) {
                currentFrameIndex = 0;
            } else {
                changeState(currentConfig.nextStateWhenFinished, currentClock);
                return;
            }
        } else {
            currentFrameIndex = nextFrame;
        }
        currentFrameStartTime = currentClock;
    }

    //קודם, מתודת עזר  שסופרת כמה קבצי sprite יש בתיקייה
    private static int countFrames(String spritesFolderPath) {
        java.io.File folder = new java.io.File(spritesFolderPath);
        java.io.File[] files = folder.listFiles((dir, name) -> name.endsWith(".png"));
        return files == null ? 0 : files.length;
    }

    public String getCurrentSpritePath() {
        return piecesRootFolder + "/states/" + currentState + "/sprites/" + (currentFrameIndex + 1) + ".png";
    }

    public String getCurrentState() {
        return currentState;
    }
}