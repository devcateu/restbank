package pl.slawek.restbank.infrastructure;

import pl.slawek.restbank.application.*;
import pl.slawek.restbank.infrastructure.db.MapRepositoriesImplementation;
import pl.slawek.restbank.infrastructure.endpoints.Endpoints;

public class ApplicationRunner {
    private MapRepositoriesImplementation repositories = new MapRepositoriesImplementation();

    private CreateAccountUseCase createAccountUseCase = new CreateAccountUseCase(repositories);
    private MakeOutgoingTransactionUseCase makeOutgoingTransactionUseCase = new MakeOutgoingTransactionUseCase(repositories, repositories);
    private ReceiveIncomingTransactionUseCase receiveIncomingTransactionUseCase = new ReceiveIncomingTransactionUseCase(repositories, repositories);
    private RejectTransactionUseCase rejectTransactionUseCase = new RejectTransactionUseCase(repositories);
    private SettleTransactionUseCase settleTransactionUseCase = new SettleTransactionUseCase(repositories);
    private Endpoints endpoints = new Endpoints(repositories,
            repositories,
            createAccountUseCase,
            makeOutgoingTransactionUseCase,
            receiveIncomingTransactionUseCase,
            rejectTransactionUseCase,
            settleTransactionUseCase);

    public static void main(String... args) {
        final ApplicationRunner applicationRunner = new ApplicationRunner();
        applicationRunner.endpoints.start();
    }
}
