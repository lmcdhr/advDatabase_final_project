import java.util.*;

public class TransactionManager {

    List<SubTransaction> activeTransactions;
    List<SubTransaction> waitingTransactions;
    Map<Integer, Transaction> transactionMap;
    DataManager dm;

    public TransactionManager() {
        this.activeTransactions = new ArrayList<SubTransaction>();
        this.waitingTransactions = new ArrayList<SubTransaction>();
        this.transactionMap = new HashMap<Integer, Transaction>();
        this.dm = new DataManager();
    }

    // here we get the next subtransaction and run it.
    public void executeNextSubTransaction() {

    }

    // all the run methods will return if there is release of locks.
    // this will be used to judge if we will pick the next subtransaction from wait queue or reading from file
    public boolean runReadSubTransaction( SubTransaction subTransaction ) {
        return false;
    }

    public boolean runROSubTransaction( SubTransaction subTransaction ) {
        return false;
    }

    public boolean runWriteSubTransaction( SubTransaction subTransaction ) {
        return false;
    }

    public boolean runBeginSubTransaction( SubTransaction subTransaction ) {
        if (!transactionMap.containsKey(subTransaction.transactionId)) {
            transactionMap.put(subTransaction.transactionId,
                    new Transaction(subTransaction.transactionId, subTransaction.timeStamp));
        }
        return false;
    }

    public boolean runEndSubTransaction( SubTransaction subTransaction ) {
        int targetTransactionId = subTransaction.transactionId;
        Transaction targetTransaction = transactionMap.get( targetTransactionId );

        if( targetTransaction.isAborted ) {
            System.out.println( "transaction: " + targetTransactionId + " was aborted " );
            transactionMap.remove( targetTransactionId );
            return false;
        }

        // commit write changes and output write result
        for( int siteId: targetTransaction.writeCache.keySet() ) {
            Site site = dm.siteMap.get( siteId );
            for( int[] data: targetTransaction.writeCache.get( siteId ) ) {
                int dataId = data[0];
                int value = data[1];
                dm.write( siteId, dataId, value, subTransaction.timeStamp );
            }
        }
        // remove all blocked subtransactions in waiting queue
        Iterator<SubTransaction> subTransactionIterator = waitingTransactions.iterator();
        while( subTransactionIterator.hasNext() ) {
            SubTransaction st = subTransactionIterator.next();
            if( st.transactionId == targetTransactionId ) {
                subTransactionIterator.remove();
            }
        }

        // output read result
        for( int dataId: targetTransaction.readCache.keySet() ) {
            System.out.println( "read x" + dataId + ":" + targetTransaction.readCache.get( dataId ) );
        }

        dm.removeLockForTransaction( targetTransactionId );
        transactionMap.remove( targetTransactionId );
        return false;
    }

    public boolean runFailSubTransaction( SubTransaction subTransaction ) {
        int siteId = subTransaction.siteId;
        dm.siteMap.get( siteId ).fail( subTransaction.timeStamp );
        for( int transactionId: transactionMap.keySet() ) {
            if( transactionMap.get( transactionId ).visitedSet.contains( siteId ) ) {
                transactionMap.get( transactionId ).isAborted = true;
                // remove all active and blocked subtransactions in waiting queue
                Iterator<SubTransaction> subTransactionIterator = waitingTransactions.iterator();
                while( subTransactionIterator.hasNext() ) {
                    SubTransaction st = subTransactionIterator.next();
                    if( st.transactionId == transactionId ) {
                        subTransactionIterator.remove();
                    }
                }
                subTransactionIterator = activeTransactions.iterator();
                while( subTransactionIterator.hasNext() ) {
                    SubTransaction st = subTransactionIterator.next();
                    if( st.transactionId == transactionId ) {
                        subTransactionIterator.remove();
                    }
                }
                dm.removeLockForTransaction( transactionId );
            }
        }
        return false;
    }

    public boolean runRecoverSubTransaction( SubTransaction subTransaction ) {
        int siteId = subTransaction.siteId;
        dm.siteMap.get( siteId ).recover( subTransaction.timeStamp );
        return false;
    }

}
