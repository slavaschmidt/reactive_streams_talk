package slasch.rs;

import java.util.Arrays;
import java.util.function.Supplier;

public class SupplierAlice extends SThread implements Supplier<byte[]> {

    private int count = 0;

    public SupplierAlice(String name) {
        super(name);
    }

    public SupplierAlice setCount(int count) {
        this.count = count;
        return this;
    }

    @Override
    public byte[] get() {
        byte[] buf = new byte[count];
        Arrays.fill(buf, Env.SCRUPT());
        super.swap(count);
        return buf;
    }
}
