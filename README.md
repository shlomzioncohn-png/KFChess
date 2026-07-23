# KFChess# KF Chess — Kung Fu Chess

משחק שחמט בזמן-אמת (ללא תורות) עם ארכיטקטורת client-server מלאה: matchmaking
לפי דירוג ELO, חדרים פרטיים (Create/Join), ניתוק/חיבור-מחדש עם resign
אוטומטי, ו-rematch אוטומטי חוזר.

## תוכן עניינים

- [איך המשחק עובד](#איך-המשחק-עובד)
- [ארכיטקטורה](#ארכיטקטורה)
- [דרישות מוקדמות](#דרישות-מוקדמות)
- [הרצה](#הרצה)
- [פרוטוקול תקשורת](#פרוטוקול-תקשורת)
- [מבנה הפרויקט](#מבנה-הפרויקט)
- [תכונות עיקריות](#תכונות-עיקריות)
- [מגבלות ופערים ידועים](#מגבלות-ופערים-ידועים)

## איך המשחק עובד

בניגוד לשחמט רגיל, אין תורות - כל שחקן יכול להזיז כלי בכל רגע. לכל מהלך יש
"זמן טיסה" (הכלי "באוויר" עד שהוא מגיע ליעד), ואחרי מהלך יש קירור (rest) לפני
שאפשר להזיז את הכלי שוב. יש גם מנגנון קפיצה (Jump) שנותן הגנה זמנית לכלי.

## ארכיטקטורה

```
┌─────────────┐         WebSocket          ┌──────────────────┐
│   Client 1   │ ◄────────────────────────► │                  │
│  (Main.java) │                            │   GameServer     │
└─────────────┘                            │  (ServerMain)     │
┌─────────────┐         WebSocket          │                  │
│   Client 2   │ ◄────────────────────────► │   RoomManager    │
│  (Main.java) │                            │  ┌─────────────┐ │
└─────────────┘                            │  │ GameSession │ │
                                             │  │  (room A)   │ │
                                             │  └─────────────┘ │
                                             │  ┌─────────────┐ │
                                             │  │ GameSession │ │
                                             │  │  (room B)   │ │
                                             │  └─────────────┘ │
                                             └──────────────────┘
```

- **שרת (`server/`)**: תהליך יחיד, מאזין לחיבורי WebSocket. `RoomManager`
  מנהל מספר משחקים (`GameSession`) בו-זמנית, כל אחד עם `board`/`GameEngine`
  משלו - משחקים לא "מדליפים" מהלכים אחד לשני.
- **לקוח (`Main.java` + `client/`)**: חלון Swing גרפי, מתחבר לשרת, שולח
  מהלכים, ומעדכן את התצוגה המקומית שלו לפי מה שהשרת משדר.
- **Bus (`bus/`)**: תשתית Pub/Sub פנימית - `GameEngine` מפרסם אירועים
  (`piece.captured`, `move.completed`, `game.over`, `piece.jumped`), ומאזינים
  שונים (ניקוד, לוג, סאונד, שידור-רשת, עדכון ELO) מגיבים אליהם בלי תלות
  ישירה זה בזה.

## דרישות מוקדמות

- **JDK 21** ומעלה
- **IntelliJ IDEA**
- הפרויקט **אינו** משתמש ב-Maven - ספריות חיצוניות מותקנות כ-JAR ידני:
  - [`Java-WebSocket`](https://repo1.maven.org/maven2/org/java-websocket/Java-WebSocket/1.6.0/Java-WebSocket-1.6.0.jar)
  - [`slf4j-api`](https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.13/slf4j-api-2.0.13.jar)
    (תלות של הספרייה למעלה)
  - [`sqlite-jdbc`](https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.46.1.3/sqlite-jdbc-3.46.1.3.jar)

  להוסיף: `File → Project Structure → Libraries → + → Java`, ולבחור את שלושת
  קבצי ה-JAR (מומלץ לשמור אותם בתיקיית `lib/` בשורש הפרויקט).

## הרצה

### 1. הפעלת השרת

מריצים את `ServerMain.java`. השרת מאזין כברירת מחדל על פורט `8887`, ויוצר
אוטומטית קובץ `chess.db` (SQLite) בתיקיית העבודה בהרצה הראשונה.

```
[SERVER] listening on port 8887
[SERVER] started successfully
```

### 2. הפעלת לקוח (לכל שחקן)

מריצים את `Main.java`. כדי להריץ כמה חלונות לקוח במקביל מאותה קונפיגורציית
Run, יש להפעיל **Allow multiple instances** ב-`Edit Configurations → Modify
options`.

הזרימה בצד הלקוח:

1. חלון Login (username + password) - הרשמה אוטומטית בכניסה ראשונה.
2. מסך בית: **Quick Play** (matchmaking אוטומטי) או **Room** (Create/Join
   עם קוד ל-6 תווים).
3. משחק בזמן-אמת עם לוח שמתאים את עצמו לגודל החלון (resize).

## פרוטוקול תקשורת

תקשורת client↔server היא הודעות טקסט על גבי WebSocket.

| כיוון | הודעה | משמעות |
|---|---|---|
| C→S | `LOGIN <user> <pass>` | הרשמה/כניסה |
| S→C | `LOGIN_OK <rating>` / `LOGIN_OK <rating> RECONNECTED <role>` | תוצאת login, כולל חזרה למשחק פעיל |
| C→S | `PLAY` | הצטרפות לתור matchmaking |
| S→C | `ROLE <WHITE\|BLACK>` / `NO_MATCH` | תוצאת matchmaking |
| C→S | `CREATE_ROOM` / `JOIN_ROOM <id>` / `CANCEL` | ניהול חדרים ידניים |
| S→C | `ROOM_CREATED <id>` / `ROOM_JOINED <id>` / `JOIN_FAILED ...` | תוצאת בקשת חדר |
| S→C | `BOARD_STATE\n<תיאור לוח מלא>` | מצב הלוח הנוכחי (בהצטרפות לחדר קיים) |
| C→S | `W<piece><src><dst>` / `B<piece><src><dst>` (למשל `WPe2e4`) | מהלך |
| C→S | `<color>J<pos>` (למשל `WJe2`) | קפיצה |
| S→C | `MOVE r,c r,c` / `CAPTURE r,c` / `JUMP r,c` / `GAMEOVER <color>` | עדכוני מצב משחק |
| S→C | `OPPONENT_DISCONNECTED <sec>` / `OPPONENT_RECONNECTED` | ניתוק/חזרה של היריב |
| S→C | `RETURN_TO_QUEUE_COUNTDOWN <sec>` / `RETURN_TO_QUEUE` | rematch אוטומטי |

## מבנה הפרויקט

```
src/
├── Main.java, ServerMain.java     # נקודות כניסה (לקוח, שרת)
├── engine/                        # GameEngine - לוגיקת המשחק
├── models/, rules/, realtime/     # מודל הלוח, חוקי תזוזה, timing
├── io/                            # פרסור/הדפסת לוח
├── bus/                           # EventBus + subscribers (ניקוד, לוג, סאונד)
│   └── events/                    # מחלקות payload (CaptureEvent, MoveEvent...)
├── server/                        # שרת: GameServer, RoomManager, GameSession,
│                                   # MatchmakingService, ReconnectManager,
│                                   # RematchService, DatabaseManager
├── client/                        # לקוח: GameClient (WebSocket), פרסור פרוטוקול
├── input/                         # Controller, מיפוי קליק→לוח
└── view/                          # Renderer, דיאלוגים (Login/Home/Room/Searching)
```

## תכונות עיקריות

- **ריבוי חדרים בו-זמנית**, מבודדים לגמרי זה מזה (כל `GameSession` עם
  `board`/`GameEngine`/players נפרדים).
- **Matchmaking** לפי טווח ELO (±100), עם timeout ו-retry שקט.
- **Login מאובטח** - סיסמאות נשמרות כ-hash, לא כטקסט גלוי.
- **דירוג ELO** מתעדכן אוטומטית בסיום כל משחק (K=32).
- **Auto-resign** אחרי 20 שניות ניתוק, עם ספירה לאחור גרפית על הלוח - וגם
  אפשרות **reconnect** תוך אותו חלון-זמן, שמחזירה את השחקן למשחק בדיוק
  מהמקום שהפסיק.
- **Rematch אוטומטי**: אחרי ניצחון במשחקי Quick Play, שני השחקנים חוזרים
  אוטומטית לתור matchmaking (לא בהכרח שוב ביחד).
- **סנכרון מצב מלא** - מי שמצטרף לחדר קיים (למשל צופה) מקבל את מצב הלוח
  האמיתי, לא לוח פתיחה ריק.
- **לוג פעילות** בצד שרת (טבלת `activity_log` ב-SQLite) ובצד לקוח
  (`client_log.txt`).
- **לוח שמתאים גודל אוטומטית** (resize), תוך שמירה על יחס-רוחב/גובה מרובע.

## מגבלות ופערים ידועים

- אחרי **reconnect לחדר ידני**, ה-banner "Room: XXXXX" לא מוצג שוב (roomId
  לא נשלח מחדש בהודעת ה-reconnect כרגע).
- **בחדר ידני** (Create/Join), אחרי ניצחון אין חזרה אוטומטית למסך הבית -
  יש לסגור ולפתוח מחדש את הלקוח כדי לשחק שוב.
- אין כרגע test coverage אוטומטי לרכיבי השרת החדשים (matchmaking, rooms,
  reconnect) - הבדיקות שבוצעו היו ידניות/אינטגרציה.
