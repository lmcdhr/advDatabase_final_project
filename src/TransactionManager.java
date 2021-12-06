import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class TransactionManager {

    public class Pair {

        boolean hasLockReleased;
        List<Integer> abortedTransactionId;

        public Pair( boolean hasLockReleased, List<Integer> abortedTransactionId ) {
            this.hasLockReleased = hasLockReleased;
            this.abortedTransactionId = abortedTransactionId;
        }
    }


    List<SubTransaction> activeTransactions;
    List<SubTransaction> waitingTransactions;
    List<SubTransaction> waitingReadingTransactionsDueToUnavailableSite;
    Map<Integer, Transaction> transactionMap;
    DataManager dm;

    public TransactionManager() {
        this.activeTransactions = new ArrayList<SubTransaction>();
        this.waitingTransactions = new ArrayList<SubTransaction>();
        this.waitingReadingTransactionsDueToUnavailableSite = new ArrayList<SubTransaction>();
        this.transactionMap = new HashMap<Integer, Transaction>();
        this.dm = new DataManager();
    }

    // read an input text file
    public void readInputFile(String filePath) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String query; // query read from input file
            int timestamp = 0; // line number for query in input file
            while (true) {
                // if exists, idx 0 represents request type or dump, idx 1 represents transaction ID or site ID,
                // idx 2 represents data ID, idx 3 represents data value
                String[] tokens = null;
                SubTransaction subtransaction = null;
                // firstly we check if there is remaining sub-transactions in activeTransactions
                if (!activeTransactions.isEmpty()){
                    subtransaction = activeTransactions.get(0);

                } else {
                    // read from file
                    if ((query = br.readLine()) != null){
                        timestamp++;
                        System.out.println("instruction is: " + query + " at time: " + timestamp);
                        Parser parser = new Parser();
                        tokens = parser.parse(query);
                        if ("begin".equals(tokens[0]) || "end".equals(tokens[0]) || "beginRO".equals(tokens[0]) ){
                            subtransaction = new SubTransaction(Integer.parseInt(tokens[1]), tokens[0], timestamp);
                        }
                        else if ("fail".equals(tokens[0]) || "recover".equals(tokens[0])){
                            subtransaction = new SubTransaction(tokens[0], Integer.parseInt(tokens[1]), timestamp);
                        }
                        else if ("R".equals(tokens[0])){
                            subtransaction = new SubTransaction(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), tokens[0], timestamp);
                        }
                        else if ("W".equals(tokens[0])){
                            subtransaction = new SubTransaction(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3]), tokens[0], timestamp);
                        }
                    }
                    else {
                        System.out.println("Finish reading input file");
                        break;
                    }
                }
                assert subtransaction != null;
                // if there is release of lock, move appropriate sub-transactions from waitQ to activeQ
                Pair executedPair = executeSubTransaction(subtransaction);
                if (executedPair.hasLockReleased){
                    Set<SubTransaction> tempRes = new HashSet<>();
                    Set<Integer> dataIdxAffected = new HashSet<Integer>();
                    // int transactionID = subtransaction.transactionId;
                    List<Integer> abortedTransactionIDs = executedPair.abortedTransactionId;

                    for(int i=1; i<=10; i++){ // site id
                        Site site = this.dm.siteMap.get(i);
                        for (int j=1; j<=20; j++){ // data idx
                            if( site.dataMap.containsKey( j ) ) {
                                Map<Integer, Set<Integer>> dataLockMap = site.lockMap.get(j);
                                for (int k=1; k<=2; k++){ // 1 for read, 2 for write
                                    Set<Integer> transactionSet = dataLockMap.get(k);
                                    for (int tid: transactionSet){
                                        for (int transactionID: abortedTransactionIDs){
                                            if (tid == transactionID){
                                                dataIdxAffected.add(j);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // find first transaction that is waiting for the released data
                    for (int dataID: dataIdxAffected){
                        Iterator<SubTransaction> listIterator = waitingTransactions.iterator();
                        while (listIterator.hasNext()){
                            SubTransaction st = listIterator.next();
                            if (st.requestDataIndex == dataID){
                                tempRes.add(st);
                                break;
                            }
                        }
                    }
                    // move sub-transactions in tempRes from waitQ to activeQ
                    for (SubTransaction sub: tempRes){
                        for (SubTransaction wSub: waitingTransactions){
                            if (sub.equals(wSub)){
                                waitingTransactions.remove(wSub);
                                activeTransactions.add(wSub);
                            }
                        }
                    }
                }

                // finally, add the possible subtransactions in read waiting queue to active queue
                if( subtransaction.requestType.equals( "W" ) || subtransaction.requestType.equals( "recover" ) ) {
                    Iterator<SubTransaction> iter = waitingReadingTransactionsDueToUnavailableSite.iterator();
                    while( iter.hasNext() ) {
                        SubTransaction waitingReadSubTransaction = iter.next();
                        if( transactionMap.get( waitingReadSubTransaction.transactionId ).isRO ) {
                            for( int siteId: dm.siteMap.keySet() ) {
                                Site site = dm.siteMap.get( siteId );
                                if( site.dataMap.containsKey( waitingReadSubTransaction.requestDataIndex ) &&
                                        site.checkAvailableTimeForRO( waitingReadSubTransaction.requestDataIndex, timestamp ) ) {
                                    activeTransactions.add( waitingReadSubTransaction );
                                    System.out.println( "added: " + waitingReadSubTransaction.transactionId + " " + waitingReadSubTransaction.requestDataIndex );
                                    iter.remove();
                                    break;
                                }
                            }
                        }
                        else {
                            for( int siteId: dm.siteMap.keySet() ) {
                                Site site = dm.siteMap.get( siteId );
                                if( site.dataMap.containsKey( waitingReadSubTransaction.requestDataIndex ) &&
                                        site.canAcquireReadLock( waitingReadSubTransaction.requestDataIndex, waitingReadSubTransaction.transactionId ) ) {
                                    activeTransactions.add( waitingReadSubTransaction );
                                    System.out.println( "added: " + waitingReadSubTransaction.transactionId + " " + waitingReadSubTransaction.requestDataIndex );
                                    iter.remove();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Problem reading the input file");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // here we get the next sub-transaction and run it.
    public Pair executeSubTransaction(SubTransaction subtransaction) {
        if (subtransaction.requestType.equals("begin")){
            return runBeginSubTransaction(subtransaction, false);
        }
        else if( subtransaction.requestType.equals("beginRO") ) {
            return runBeginSubTransaction(subtransaction, true);
        }
        else if (subtransaction.requestType.equals("R")){
            //dm.printLockTable();
            return runReadSubTransaction(subtransaction);
        }
        else if (subtransaction.requestType.equals("W")){
            return runWriteSubTransaction(subtransaction);
        }
        else if (subtransaction.requestType.equals("end")){
            return runEndSubTransaction(subtransaction);
        }
        else if (subtransaction.requestType.equals("fail")){
            return runFailSubTransaction(subtransaction);
        }
        else {
            return runRecoverSubTransaction(subtransaction);
        }
        //return new Pair( false, new ArrayList<Integer>() );

    }

    // all the run methods will return if there is release of locks.
    // this will be used to judge if we will pick the next sub-transaction from wait queue or reading from file
    public Pair runReadSubTransaction( SubTransaction subTransaction ) {

        int targetTransactionId = subTransaction.transactionId;
        Transaction targetTransaction = transactionMap.get( targetTransactionId );

        // aborted or already blocked
        if( targetTransaction.isAborted ) {
            System.out.println( "transaction already aborted, will not read" );
            return new Pair( false, new ArrayList<Integer>() );
        }
        for( SubTransaction st: waitingTransactions ) {
            if( st.transactionId == subTransaction.transactionId ) {
                waitingTransactions.add( subTransaction );
                System.out.println( "transaction is waiting, will not read" );
                return new Pair( false, new ArrayList<Integer>() );
            }
        }

        // deal with RO
        if( targetTransaction.isRO ) {
            return runROSubTransaction( subTransaction );
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
                        transactionMap.get( targetTransactionId ).visitedSet.add( site.siteId );
                        int dataValue = site.dataMap.get( dataId ).dataValue;
                        System.out.println( "x" + dataId + ":" + dataValue );
                        return new Pair( false, new ArrayList<Integer>() );
                    }
                }
                else {
                    // here means that it needs to wait for a write lock.
                    // we will implement wait edge and check cycle to decide if we need to abort

                    // firstly we will need to add waiting edge
                    int parentTransactionId = getTransactionIdWriteLockOnData( subTransaction.requestDataIndex );
                    if( !transactionMap.get( targetTransactionId ).waitingGraphEdges.containsKey( parentTransactionId ) ) {
                        transactionMap.get( targetTransactionId ).waitingGraphEdges.put( parentTransactionId, new HashSet<Integer>() );
                    }
                    transactionMap.get( targetTransactionId ).waitingGraphEdges.get( parentTransactionId ).add( subTransaction.requestDataIndex );

                    // then check edge, if there is edge, abort the youngest then return true since there is lock release
                    if( isCycleDetected( targetTransactionId ) ) {
                        int abortedTransactionId = getYoungestTransactionIdToAbort(targetTransactionId);
                        updateCycleGraph( abortedTransactionId );
                        transactionMap.get( abortedTransactionId ).isAborted = true;
                        dm.removeLockForTransaction( abortedTransactionId );
                        removeBlockedAndActiveSubtransactionsDueToAbort( abortedTransactionId );
                        return new Pair( true, new ArrayList<Integer>( Arrays.asList( abortedTransactionId ) ) );
                    }

                    // when reaching here, it means that no deadlock but need to wait, return false
                    waitingTransactions.add( subTransaction );
                    return new Pair( false, new ArrayList<Integer>() );
                }
            }
        }

        // here means that we can not find a site to read it, need to wait
        waitingReadingTransactionsDueToUnavailableSite.add( subTransaction );
        System.out.println( "can not find a valid site, add to read waiting site queue" );
        return new Pair( false, new ArrayList<Integer>() );
    }

    public Pair runROSubTransaction( SubTransaction subTransaction ) {

        int targetTransactionId = subTransaction.transactionId;
        Transaction targetTransaction = transactionMap.get( targetTransactionId );

        // aborted or already blocked
        if( targetTransaction.isAborted ) {
            System.out.println( "transaction already aborted, will not read" );
            return new Pair( false, new ArrayList<Integer>() );
        }
        for( SubTransaction st: waitingTransactions ) {
            if( st.transactionId == subTransaction.transactionId ) {
                waitingTransactions.add( subTransaction );
                System.out.println( "transaction is waiting, will not read" );
                return new Pair( false, new ArrayList<Integer>() );
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
                return new Pair( false, new ArrayList<Integer>() );
            }
        }

        // here means that we can not find a site to read it, need to wait
        waitingReadingTransactionsDueToUnavailableSite.add( subTransaction );
        System.out.println( "can not find a valid site, add to read waiting site queue" );
        return new Pair( false, new ArrayList<Integer>() );
    }

    public Pair runWriteSubTransaction( SubTransaction subTransaction ) {
        return new Pair( false, new ArrayList<Integer>() );
    }

    public Pair runBeginSubTransaction( SubTransaction subTransaction, boolean isRO ) {
        if (!transactionMap.containsKey(subTransaction.transactionId)) {
            transactionMap.put(subTransaction.transactionId,
                    new Transaction(subTransaction.transactionId, subTransaction.timeStamp, isRO));
        }
        return new Pair( false, new ArrayList<Integer>() );
    }

    public Pair runEndSubTransaction( SubTransaction subTransaction ) {
        int targetTransactionId = subTransaction.transactionId;
        Transaction targetTransaction = transactionMap.get( targetTransactionId );

        if( targetTransaction.isAborted ) {
            System.out.println( "transaction: " + targetTransactionId + " was aborted " );
            transactionMap.remove( targetTransactionId );
            return new Pair( false, new ArrayList<Integer>() );
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
        removeBlockedAndActiveSubtransactionsDueToAbort( targetTransactionId );
        dm.removeLockForTransaction( targetTransactionId );
        transactionMap.remove( targetTransactionId );
        System.out.println( "transaction: " + targetTransactionId + " was committed " );
        return new Pair( true, new ArrayList<Integer>( subTransaction.transactionId ) );
    }

    public Pair runFailSubTransaction( SubTransaction subTransaction ) {
        int siteId = subTransaction.siteId;
        dm.siteMap.get( siteId ).fail( subTransaction.timeStamp );
        boolean haveTransactionsAborted = false;
        List<Integer> abortedTransactionId = new ArrayList<Integer>();
        for( int transactionId: transactionMap.keySet() ) {
            if( transactionMap.get( transactionId ).visitedSet.contains( siteId ) ) {
                if( transactionMap.get( transactionId ).isAborted ) {
                    continue;
                }
                System.out.println( "transaction: " + transactionId + " is aborted due to the fail of site: " + siteId );
                haveTransactionsAborted = true;
                transactionMap.get( transactionId ).isAborted = true;
                // remove all active and blocked subtransactions in two queues
                removeBlockedAndActiveSubtransactionsDueToAbort( transactionId );
                updateCycleGraph( transactionId );
                dm.removeLockForTransaction( transactionId );
                abortedTransactionId.add( transactionId );
            }
        }
        return new Pair( haveTransactionsAborted, abortedTransactionId );
    }

    public Pair runRecoverSubTransaction( SubTransaction subTransaction ) {
        int siteId = subTransaction.siteId;
        dm.siteMap.get( siteId ).recover( subTransaction.timeStamp );
        return new Pair( false, new ArrayList<Integer>() );
    }

    // this method will deal with the waiting graph when one transaction is aborted / committed
    private void updateCycleGraph( int abortedTransactionId ) {
        Transaction abortedTransaction = transactionMap.get( abortedTransactionId );

        // first, update its child transaction,
        // look for the transaction the child should be waiting after current transaction is aborted
        // if there is no such transaction, it means that the child transaction does not need to wait for the data
        for( int childTransactionId: transactionMap.keySet() ) {
            if( childTransactionId != abortedTransactionId ) {
                Transaction childTransaction = transactionMap.get( childTransactionId );
                if( childTransaction.waitingGraphEdges.containsKey( abortedTransactionId ) ) {
                    Set<Integer> waitingData = childTransaction.waitingGraphEdges.get( abortedTransactionId );
                    for( int waitingDataId: waitingData ) {
                        for( int tId: abortedTransaction.waitingGraphEdges.keySet() ) {
                            if (abortedTransaction.waitingGraphEdges.get(tId).contains(waitingDataId)) {
                                if( !childTransaction.waitingGraphEdges.containsKey( tId ) ) {
                                    transactionMap.get( childTransactionId ).waitingGraphEdges.put( tId, new HashSet<Integer>() );
                                }
                                transactionMap.get( childTransactionId ).waitingGraphEdges.get( tId ).add( waitingDataId );
                                break;
                            }
                        }
                        transactionMap.get( childTransactionId ).waitingGraphEdges.get( abortedTransactionId ).remove( waitingDataId );
                    }
                    transactionMap.get( childTransactionId ).waitingGraphEdges.remove( abortedTransactionId );
                }
            }
        }

        // clean up the abortedTransaction
        transactionMap.get( abortedTransactionId ).waitingGraphEdges.clear();
    }

    private int getTransactionIdWriteLockOnData( int dataIndex ) {
        for( int siteId: dm.siteMap.keySet() ) {
            Site site = dm.siteMap.get( siteId );
            if( !site.isStatusUp ) {
                continue;
            }
            for( int parentTransactionId: site.lockMap.get( dataIndex ).get( 2 ) ) {
                return parentTransactionId;
            }
        }

        return -1;
    }

    private boolean isCycleDetected( int startTransactionId ) {
        return false;
    }

    private int getYoungestTransactionIdToAbort( int startPointId ) {
        return -1;
    }

    private void removeBlockedAndActiveSubtransactionsDueToAbort( int abortedTransactionId ) {
        Iterator<SubTransaction> subTransactionIterator = waitingTransactions.iterator();
        while( subTransactionIterator.hasNext() ) {
            SubTransaction st = subTransactionIterator.next();
            if( st.transactionId == abortedTransactionId ) {
                subTransactionIterator.remove();
            }
        }
        subTransactionIterator = activeTransactions.iterator();
        while( subTransactionIterator.hasNext() ) {
            SubTransaction st = subTransactionIterator.next();
            if( st.transactionId == abortedTransactionId ) {
                subTransactionIterator.remove();
            }
        }
        subTransactionIterator = waitingReadingTransactionsDueToUnavailableSite.iterator();
        while( subTransactionIterator.hasNext() ) {
            SubTransaction st = subTransactionIterator.next();
            if( st.transactionId == abortedTransactionId ) {
                subTransactionIterator.remove();
            }
        }
    }

}
