package view;

import models.enums.PieceColor;
import models.enums.PieceType;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PieceImageLoader {

    private static final int FRAME_DURATION_MS = 150;
    private static final String[] STATE_FOLDERS = {"idle", "move", "jump", "short_rest", "long_rest"};

    private final String rootPath;
    private final int cellSize;
    private final Map<ImageKey, List<Img>> frames = new HashMap<>();

    public PieceImageLoader(String rootPath, int cellSize) {
        this.rootPath = rootPath;
        this.cellSize = cellSize;
        preloadAllImages();
    }

    public Img getFrame(PieceRenderSnapshot piece) {
        ImageKey key = new ImageKey(piece.type(), piece.color(), piece.stateFolder());
        List<Img> animationFrames = frames.get(key);
        if (animationFrames == null || animationFrames.isEmpty()) {
            throw new IllegalStateException("Missing frames for " + key);
        }
        int frameIndex = calculateFrameIndex(piece.stateElapsedMillis(), animationFrames.size());
        return animationFrames.get(frameIndex).copy();
    }

    private int calculateFrameIndex(long elapsedMillis, int frameCount) {
        long rawIndex = elapsedMillis / FRAME_DURATION_MS;
        return (int) (rawIndex % frameCount);
    }

    private void preloadAllImages() {
        for (PieceType type : PieceType.values()) {
            for (PieceColor color : PieceColor.values()) {
                for (String stateFolder : STATE_FOLDERS) {
                    loadAnimation(type, color, stateFolder);
                }
            }
        }
    }

    private String pieceFolder(PieceType type, PieceColor color) {
        return typeCode(type) + colorCode(color);
    }

    private String typeCode(PieceType type) {
        return switch (type) {
            case KING -> "K"; case QUEEN -> "Q"; case ROOK -> "R";
            case BISHOP -> "B"; case KNIGHT -> "N"; case PAWN -> "P";
        };
    }

    private String colorCode(PieceColor color) {
        return switch (color) {
            case WHITE -> "W"; case BLACK -> "B";
        };
    }

    private void loadAnimation(PieceType type, PieceColor color, String stateFolder) {
        String path = rootPath + "/" + pieceFolder(type, color) + "/states/" + stateFolder + "/sprites";
        List<Img> loadedFrames = new ArrayList<>();

        for (int i = 1; ; i++) {
            File file = new File(path + "/" + i + ".png");
            if (!file.exists()) break;
            loadedFrames.add(new Img().read(file.getPath(), new Dimension(cellSize, cellSize), true, null));
        }

        if (!loadedFrames.isEmpty()) {
            frames.put(new ImageKey(type, color, stateFolder), loadedFrames);
        }
    }
}