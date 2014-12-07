package slasch.rs;

import java.util.function.Consumer;

/**
 * @author slasch
 * @since 09.11.2014.
 */
public class ConsumerBorice extends SThread implements Consumer<byte[]> {

    public ConsumerBorice(String name) {
        super(name);
    }

    @Override
    public Consumer andThen(Consumer after) {
        return after;
    }

    @Override
    public void accept(byte[] bytes) {
        super.swap(bytes.length);
    }
}

