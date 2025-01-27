package net.earthmc.emccom.combat.listener;
import java.util.List;
import java.util.Random;

public class CombatLogMessages {

    private final List<String> messages;
    private final Random random;

    public CombatLogMessages(Random random, List<String> messages) {
        if (random == null || messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("Random and message list must not be null or empty");
        }
        this.random = random;
        this.messages = messages;
    }

    public String getRandomMessage() {
        int randomIndex = random.nextInt(messages.size());
        return messages.get(randomIndex);
    }
}
