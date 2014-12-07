package slasch.rs;

import akka.japi.Pair;

import java.util.Arrays;
import java.util.stream.Stream;

public class J8Stream {

    private static final ConsumerBorice borice = new ConsumerBorice("RS-Borice") {
        public void swap(int count) { }
    };

    private static final SThread alice  = new SThread ("RS-Alice") {
        byte[] items(int count) {
            byte[] result = new byte[count];
            Arrays.fill(result, Env.SCRUPT());
            return result;
        }
        public void swap(int count) {
            Stream.of(items(count)).sequential()
                .filter(s -> { throw new IllegalArgumentException(); })
                .forEach(borice);
            super.swap(count);
        }
    };

    public static Pair<SThread, SThread> create() {
        return new Pair<>(alice, borice);
    }
}
