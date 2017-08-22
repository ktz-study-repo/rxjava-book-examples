package com.oreilly.rxjava.ch6;

import java.util.List;

interface Repository {
    void store(Record record);
    void storeAll(List<Record> records);
}

class SomeRepository implements Repository {
    @Override
    public void store(Record record) {
        System.out.println("store: " + record);
    }

    @Override
    public void storeAll(List<Record> records) {
        System.out.println("storeAll - nOf records " + records.size());
    }
}
