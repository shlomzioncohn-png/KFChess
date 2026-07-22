package view;

/**
 * שורה מוכנה-לציור בטבלת המהלכים - זמן מפורמט (M:SS) + תיאור המהלך.
 * לא models.GameState.MoveLogEntry ישירות, כדי שסוג-מודל לא ידלוף לשכבת התצוגה.
 */
public record MoveLogRow(String time, String description) {}
