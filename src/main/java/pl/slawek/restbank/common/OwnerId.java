package pl.slawek.restbank.common;

import java.util.Objects;

public class OwnerId {
    private final String id;

    private OwnerId(String id) {
        this.id = Validations.requireNotNull(id, "owner");
    }

    public static OwnerId of(String ownerId) {
        return new OwnerId(ownerId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OwnerId ownerId = (OwnerId) o;
        return Objects.equals(id, ownerId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id;
    }
}
