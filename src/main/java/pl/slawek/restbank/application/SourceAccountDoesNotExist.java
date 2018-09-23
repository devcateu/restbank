package pl.slawek.restbank.application;

import pl.slawek.restbank.common.BusinessException;

public class SourceAccountDoesNotExist extends BusinessException {
    public SourceAccountDoesNotExist() {
        super("Source account does not exist");
    }
}
