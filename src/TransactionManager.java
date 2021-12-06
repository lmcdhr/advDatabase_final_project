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

        int targetTransactionId = subTransaction.transactionId;
        Transaction targetTransaction = transactionMap.get( targetTransactionId );

        // aborted or already blocked
        if( targetTransaction.isAborted ) {
            return false;
        }
        for( SubTransaction st: waitingTransactions ) {
            if( st.transactionId == subTransaction.transactionId ) {
                waitingTransactions.add( subTransaction );
                return false;
            }
        }

        // attempt to read from available site
        for( int siteId: dm.siteMap.keySet() ) {
            int dataId = subTransaction.requestDataIndex;
            Site site = dm.siteMap.get( siteId );
            // check site is up and has data we want to read
            if( site.isStatusUp && site.dataMap.containsKey( dataId ) ) {
                // check if we can acquire the read lock
                if( site.canAcquireReadLock( dataId, targetTransactionId ) ) {
                    // check if this data can be read( changed after recover )
                    if( site.dataMap.get( dataId ).canBeRead ) {
                        dm.implementReadLockOnSite( siteId, dataId, targetTransactionId );
                        int dataValue = site.dataMap.get( dataId ).dataValue;
                        System.out.println( "x" + dataId + ":" + dataValue );
                        return false;
                    }
                }
                else {
                    // to do: here means that it needs to wait for a write lock.
                    // we will implement wait edge and check cycle to decide if we need to abort
                    continue;
                }
            }
        }

        // here means that we can not find a site to read it, need to wait
        waitingTransactions.add( subTransaction );
        return false;
    }

    public boolean runROSubTransaction( SubTransaction subTransaction ) {

        int targetTransactionId = subTransaction.transactionId;
        Transaction targetTransaction = transactionMap.get( targetTransactionId );

        // aborted or already blocked
        if( targetTransaction.isAborted ) {
            return false;
        }
        for( SubTransaction st: waitingTransactions ) {
            if( st.transactionId == subTransaction.transactionId ) {
                waitingTransactions.add( subTransaction );
                return false;
            }
        }

        // attempt to read from available site
        int dataId = subTransaction.requestDataIndex;
        for( int siteId: dm.siteMap.keySet() ) {
            Site site = dm.siteMap.get( siteId );
            // if it is not even up when RO occurs, no need to continue
            if( !site.isStatusUp || !site.dataMap.containsKey( dataId ) ) {
                continue;
            }
            // for now, we already find the data in this site
            // then, check if the site is up from last commit until RO command
            if( site.checkAvailableTimeForRO( dataId, subTransaction.timeStamp ) ) {
                int dataValue = site.dataMap.get( dataId ).dataValue;
                System.out.println( "x" + dataId + ":" + dataValue );
                return false;
            }
        }

        // here means that we can not find a site to read it, need to wait
        waitingTransactions.add( subTransaction );
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

        dm.removeLockForTransaction( targetTransactionId );
        transactionMap.remove( targetTransactionId );
        return true;
    }

    public boolean runFailSubTransaction( SubTransaction subTransaction ) {
        int siteId = subTransaction.siteId;
        dm.siteMap.get( siteId ).fail( subTransaction.timeStamp );
        boolean haveTransactionsAborted = false;
        for( int transactionId: transactionMap.keySet() ) {
            if( transactionMap.get( transactionId ).visitedSet.contains( siteId ) ) {
                haveTransactionsAborted = true;
                transactionMap.get( transactionId ).isAborted = true;
                // remove all active and blocked subtransactions in two queues
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
        return haveTransactionsAborted;
    }

    public boolean runRecoverSubTransaction( SubTransaction subTransaction ) {
        int siteId = subTransaction.siteId;
        dm.siteMap.get( siteId ).recover( subTransaction.timeStamp );
        return false;
    }

}
