package jackpot.orm.repository;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TransactionPoolManager {
    private static  TransactionPoolManager instance = new TransactionPoolManager();
    public static TransactionPoolManager getInstance() {
        return instance;
    };

    private ConcurrentMap<Long, Transaction> threadIDToTransactionMAP = new ConcurrentHashMap<>();

    synchronized public ConnectionManager createTransaction() {
        long threadId = Thread.currentThread().getId();

        Transaction transaction = Transaction.builder()
                .threadID(threadId)
                .connectionManager(ConnectionManager.createNew(false))
                .build();

        threadIDToTransactionMAP.put(threadId, transaction);
        return transaction.getConnectionManager();
    }

    synchronized public void closeTransaction() {
        long threadId = Thread.currentThread().getId();

        threadIDToTransactionMAP.get(threadId).getConnectionManager().forceClose();
        threadIDToTransactionMAP.remove(threadId);
    }

    public static ConnectionManager getConnection() {
        return instance.createOrGetConnection();
    }

    private ConnectionManager createOrGetConnection() {
        long threadId = Thread.currentThread().getId();
        Transaction transaction = threadIDToTransactionMAP.get(threadId);
        if(transaction != null) {
            System.out.println("JACKPOT !!!!!!!!!!!!!!!!!!!!!!!!!");
        }
        if(transaction == null) {
            System.out.println("threadId == " + threadId);
           return ConnectionManager.createNew();
        }
        return transaction.getConnectionManager();
    }

}
