package com.jivesoftware.os.jive.utils.hwal.shared.partition;

import com.jivesoftware.os.jive.utils.hwal.shared.api.WALKey;
import java.util.Random;

/**
 *
 * @author jonathan
 */
public class RandomParitioningStrategy implements WALParitioningStrategy {

    private final Random random;

    public RandomParitioningStrategy(Random random) {
        this.random = random;
    }

    @Override
    public int parition(WALKey key, int numberOfPartitions) {
        return random.nextInt(numberOfPartitions);
    }

}
