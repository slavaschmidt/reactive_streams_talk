package slasch.rs;

import akka.japi.Pair;

/**
 * @author slasch
 * @since 09.11.2014.
 */
public class J6Local {

    private static final SThread borice =
            new SThread("RS-Borice") {
        @Override public void run() {
            while (!stopped()) { Env.work(); }
        }
        @Override public void swap(int count) {
            super.swap(count);
        }
    };

    private static final SThread alice =
            new SThread("RS-Alice") {
        @Override public void swap(int count) {
            super.swap(count);
            borice.swap(count);
        }
    };

    public static Pair<SThread, SThread> create() {
        return new Pair(alice, borice);
    }
}
