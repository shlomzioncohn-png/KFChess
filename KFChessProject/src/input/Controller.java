package input;

import bus.events.IllegalMoveEvent;
import client.CommandBuilder;
import client.GameClient;
import engine.GameEngine;
import models.Board;
import models.Piece;
import models.Position;
import rules.RuleEngine;

import java.util.function.IntSupplier;

public class Controller {
    private final GameEngine engine;
    private final Board board;
    private final GameClient client;
    private final IntSupplier cellSizeSupplier;


    private Position selectedPosition = null;

    public Controller(GameEngine engine, Board board, client.GameClient client, IntSupplier cellSizeSupplier) {
        this.engine = engine;
        this.board = board;
        this.client = client;
        this.cellSizeSupplier = cellSizeSupplier;
    }

    /**
     * פונקציה מרכזית לטיפול בכל לחיצה על הלוח.
     */
    public void handleMouseClick(int x, int y) {
        Position pos = BoardMapper.mapPixelToPosition(x, y, board, cellSizeSupplier.getAsInt());
        if (pos == null) return; // לחיצה מחוץ ללוח

        if (selectedPosition != null && board.getPieceAt(selectedPosition) == null) {
            // הכלי שנבחר כבר לא שם (זז/נאכל בינתיים - משחק בזמן-אמת) - מבטלים בחירה ישנה
            selectedPosition = null;
        }

        if (selectedPosition == null) {
            if (board.getPieceAt(pos) != null) {
                selectedPosition = pos;
                System.out.println("Selected piece at: " + pos);
            }
        } else {
            Piece selectedPiece = board.getPieceAt(selectedPosition);
            Piece targetPiece = board.getPieceAt(pos);

            if (targetPiece != null && targetPiece.getColor() == selectedPiece.getColor()) {
                selectedPosition = pos;
            } else if (!new RuleEngine().getLegalDestinations(board, selectedPosition).contains(pos)) {
                // בדיקה מקומית בלבד (רמז ל-UI/סאונד) - השרת נשאר הסמכות היחידה שמאשרת מהלכים בפועל
                engine.getBus().publish("move.illegal", new IllegalMoveEvent(selectedPosition, pos));
                selectedPosition = null;
            } else {
                System.out.println("Attempting move from " + selectedPosition + " to " + pos);
                String command = CommandBuilder.buildMoveCommand(selectedPiece, selectedPosition, pos, board.getHeight());
                try {
                    client.send(command);
                } catch (Exception e) {
                    System.out.println("[CLIENT] could not send move - not connected to server");
                }
                selectedPosition = null;
            }
        }
    }

    /**
     * לוגיקת הקפיצה (Jump) - מוכנה להטמעה!
     */
    public void handleJumpCommand(int x, int y) {
        Position pos = BoardMapper.mapPixelToPosition(x, y, board, cellSizeSupplier.getAsInt());
        if (pos == null) return;

        Piece piece = board.getPieceAt(pos);
        if (piece == null) return;

        String command = CommandBuilder.buildJumpCommand(piece, pos, board.getHeight());
        try {
            client.send(command);
        } catch (Exception e) {
            System.out.println("[CLIENT] could not send jump - not connected to server");
        }
    }

    public void update(long deltaMs) {
        engine.waitMs(deltaMs);
    }

    public Position getSelectedPosition() {
        return selectedPosition;
    }
}
