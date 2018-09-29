package pl.slawek.restbank.common;

import java.util.List;

public class Lists {
    public static <T> T getLastElement(List<T> list) {
        return list.get(list.size() - 1);
    }
}
