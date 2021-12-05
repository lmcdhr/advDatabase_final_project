import java.util.*;

public class Transaction {

    public int transactionId;
    public int birthTime;
    public boolean isAborted;
    public Set<Integer> visitedSet;
    // store write result, key: site id
    // value: List of int[] where int[0] is data id, int[1] is new value
    Map<Integer, List<int[]>> writeCache;
    // store read result, key: data id, value: data value
    Map<Integer, Integer> readCache;

    // this is no longer needed since we do not need to store subtransactions in transaction
    //public Set<SubTransaction> subTransactionSet;

    public Transaction( int transactionId, int birthTime ) {
        this.transactionId = transactionId;
        this.birthTime = birthTime;
        this.isAborted = false;
        //this.subTransactionSet = new HashSet<SubTransaction>();
        this.visitedSet = new HashSet<Integer>();
        this.writeCache = new HashMap<Integer, List<int[]>>();
        this.readCache = new HashMap<Integer, Integer>();
    }

//    public void addReadSubTransaction( int transactionId, int requestDataIndex, String requestType ) {
//        SubTransaction subTransaction = new SubTransaction( transactionId, requestDataIndex, requestType );
//        this.subTransactionSet.add( subTransaction );
//    }
//
//    public void addWriteSubTransaction( int transactionId, int requestDataIndex, int requestDataValueChangeTo, String requestType ) {
//        SubTransaction subTransaction = new SubTransaction( transactionId, requestDataIndex, requestDataValueChangeTo, requestType );
//        this.subTransactionSet.add( subTransaction );
//    }

}
