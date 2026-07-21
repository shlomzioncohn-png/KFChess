package bus;

import bus.events.CaptureEvent;
import bus.events.GameOverEvent;
import bus.events.MoveEvent;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class SoundSubscriber implements EventListener {

    @Override
    public void onEvent(String topic, Object payload) {
        if (payload instanceof CaptureEvent) {
            playSound("resources/sounds/capture.wav");
        } else if (payload instanceof MoveEvent moveEvent && !moveEvent.wasCapture()) {
            playSound("resources/sounds/move.wav");
        } else if (payload instanceof GameOverEvent) {
            playSound("resources/sounds/game_over.wav");
        }
    }

    private void playSound(String filePath) {
        try {
            File soundFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            System.out.println("[SOUND] could not play " + filePath + ": " + e.getMessage());
        }
    }
}