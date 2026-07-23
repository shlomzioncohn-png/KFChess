package  tests;

import bus.EventBus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EventBusTest {

    @Test
    void multipleListenersOnSameTopicAllReceiveTheExactPayload() {
        EventBus bus = new EventBus();
        Object payload = new Object();
        List<Object> received1 = new ArrayList<>();
        List<Object> received2 = new ArrayList<>();
        bus.subscribe("topic.a", (topic, p) -> received1.add(p));
        bus.subscribe("topic.a", (topic, p) -> received2.add(p));

        bus.publish("topic.a", payload);

        assertEquals(1, received1.size());
        assertEquals(1, received2.size());
        assertSame(payload, received1.get(0), "כל מאזין חייב לקבל בדיוק את אותו אובייקט payload");
        assertSame(payload, received2.get(0));
    }

    @Test
    void publishToTopicWithNoListenersDoesNothingSilently() {
        EventBus bus = new EventBus();

        assertDoesNotThrow(() -> bus.publish("nobody.listening", "payload"),
                "פרסום לטופיק בלי מאזינים אסור לזרוק שגיאה");
    }

    @Test
    void listenersOnDifferentTopicsAreIsolatedFromEachOther() {
        EventBus bus = new EventBus();
        List<String> topicAInvocations = new ArrayList<>();
        List<String> topicBInvocations = new ArrayList<>();
        bus.subscribe("topic.a", (topic, p) -> topicAInvocations.add(topic));
        bus.subscribe("topic.b", (topic, p) -> topicBInvocations.add(topic));

        bus.publish("topic.a", "x");

        assertEquals(1, topicAInvocations.size(), "מאזין של topic.a חייב להיקרא");
        assertTrue(topicBInvocations.isEmpty(), "מאזין של topic.b אסור להיקרא כשמפרסמים ל-topic.a");
    }

    @Test
    void listenersAreInvokedInSubscriptionOrder() {
        EventBus bus = new EventBus();
        List<Integer> callOrder = new ArrayList<>();
        bus.subscribe("topic.a", (topic, p) -> callOrder.add(1));
        bus.subscribe("topic.a", (topic, p) -> callOrder.add(2));
        bus.subscribe("topic.a", (topic, p) -> callOrder.add(3));

        bus.publish("topic.a", null);

        assertEquals(List.of(1, 2, 3), callOrder, "מאזינים חייבים להיקרא בסדר שבו נרשמו");
    }

    @Test
    void sameListenerSubscribedTwiceIsInvokedTwice() {
        EventBus bus = new EventBus();
        List<Object> invocations = new ArrayList<>();
        var listener = new bus.EventListener() {
            @Override
            public void onEvent(String topic, Object payload) {
                invocations.add(payload);
            }
        };
        bus.subscribe("topic.a", listener);
        bus.subscribe("topic.a", listener);

        bus.publish("topic.a", "x");

        assertEquals(2, invocations.size(),
                "אין דה-דופליקציה ב-EventBus - רישום כפול לאותו מאזין חייב לגרום לקריאה כפולה");
    }

    /**
     * מתעד התנהגות קיימת שהיא כשל פוטנציאלי: publish() לא עוטף קריאות מאזינים ב-try/catch,
     * אז מאזין שזורק חריגה עוצר את כל שרשרת הפרסום - מאזינים שנרשמו אחריו על אותו topic
     * לא נקראים כלל. ב-GameSession יש כמה מאזינים בלתי-תלויים על אותו topic (למשל
     * NetworkBroadcastSubscriber, ActivityLogSubscriber, RatingUpdateSubscriber על "game.over") -
     * חריגה באחד מהם עלולה למנוע עדכון דירוג/לוג מבלי שאף אחד ידע.
     */
    @Test
    void listenerThrowingExceptionAbortsPublishAndBlocksLaterListenersOnSameTopic() {
        EventBus bus = new EventBus();
        List<Object> laterListenerInvocations = new ArrayList<>();
        bus.subscribe("topic.a", (topic, p) -> { throw new RuntimeException("boom"); });
        bus.subscribe("topic.a", (topic, p) -> laterListenerInvocations.add(p));

        assertThrows(RuntimeException.class, () -> bus.publish("topic.a", "x"));
        assertTrue(laterListenerInvocations.isEmpty(),
                "כשל פוטנציאלי מתועד: מאזין שנרשם אחרי מאזין שזורק חריגה לא נקרא בכלל");
    }
}
