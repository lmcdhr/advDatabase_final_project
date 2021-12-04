import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class TransactionManager {

    Queue<SubTransaction> activeTransactions;
    Queue<SubTransaction> waitingTransactions;
    Map<Integer, Transaction> transactionMap;
    Map<Integer, Integer> transactionBirthTime;

    public TransactionManager() {
        this.activeTransactions = new LinkedList<SubTransaction>();
        this.waitingTransactions = new LinkedList<SubTransaction>();
        this.transactionMap = new HashMap<Integer, Transaction>();
        this.transactionBirthTime = new HashMap<Integer, Integer>();
    }

    public void createTransaction( int transactionId, int timeStamp ) {
        this.transactionMap.put( transactionId, new Transaction( transactionId ) );
        this.transactionBirthTime.put( transactionId, timeStamp );
    }

    public void addSubTransaction( int transactionId, int dataIndex, int requestDataValueChangeTo, String requestType, int timeStamp ) {
        if( requestType.equals( "W" ) ) {
            transactionMap.get( transactionId ).addWriteSubTransaction( transactionId, dataIndex, requestDataValueChangeTo, requestType );
        }
        // Read or Read Only
        else {
            transactionMap.get( transactionId ).addReadSubTransaction( transactionId, dataIndex, requestType );
        }

        // to do: add to active queue or waiting queue
    }

}
