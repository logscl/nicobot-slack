package be.zqsd.nicobot.handler.message;

import com.slack.api.model.event.MessageEvent;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class BackwardsSpeakingTest {

    @Inject
    BackwardsSpeaking backwardsSpeaking;

    @Test
    void allLowercaseMessage() {
        var message = "hello world";
        var shouldReverse = backwardsSpeaking.conditionMet(createEvent(message));
        var reversed = backwardsSpeaking.reverseMessage(message);

        assertTrue(shouldReverse);
        assertEquals("dlrow olleh", reversed);
    }

    @Test
    void startingWithAUppercaseMessage() {
        var message = "Hello world.";
        var shouldReverse = backwardsSpeaking.conditionMet(createEvent(message));
        var reversed = backwardsSpeaking.reverseMessage(message);

        assertTrue(shouldReverse);
        assertEquals(".dlrow olleH", reversed);
    }

    @Test
    void allUppercaseMessage() {
        var message = "HELLO WORLD";
        var shouldReverse = backwardsSpeaking.conditionMet(createEvent(message));
        var reversed = backwardsSpeaking.reverseMessage(message);

        assertTrue(shouldReverse);
        assertEquals("DLROW OLLEh", reversed);
    }

    @Test
    void messageWithAUrl() {
        var message = "hello world http://www.perdu.com";
        var shouldReverse = backwardsSpeaking.conditionMet(createEvent(message));

        assertFalse(shouldReverse);
    }

    @Test
    void tooLongMessage() {
        var message = "This is a very long message. This will not be reversed by the service";
        var shouldReverse = backwardsSpeaking.conditionMet(createEvent(message));

        assertFalse(shouldReverse);
    }

    @Test
    void messageWithAnEmoji() {
        var message = "Hello :happy: world";
        var shouldReverse = backwardsSpeaking.conditionMet(createEvent(message));
        var reversed = backwardsSpeaking.reverseMessage(message);

        assertTrue(shouldReverse);
        assertEquals("dlrow :happy: olleH", reversed);
    }

    @Test
    void messageWithoutEmojis() {
        var message = "Hello :not an emoji: world";
        var shouldReverse = backwardsSpeaking.conditionMet(createEvent(message));
        var reversed = backwardsSpeaking.reverseMessage(message);

        assertTrue(shouldReverse);
        assertEquals("dlrow :ijome na ton: olleH", reversed);
    }

    @Test
    void messageWithAnUsername() {
        var message = "Hello <@ABCD|michel> !";
        var shouldReverse = backwardsSpeaking.conditionMet(createEvent(message));
        var reversed = backwardsSpeaking.reverseMessage(message);

        assertTrue(shouldReverse);
        assertEquals("! <@ABCD|michel> olleH", reversed);
    }

    @Test
    void messageWithAChannel() {
        var message = "Hello <#ABCD|general> !";
        var shouldReverse = backwardsSpeaking.conditionMet(createEvent(message));
        var reversed = backwardsSpeaking.reverseMessage(message);

        assertTrue(shouldReverse);
        assertEquals("! <#ABCD|general> olleH", reversed);
    }

    private MessageEvent createEvent(String message) {
        var event = new MessageEvent();
        event.setText(message);
        return event;
    }

}