import java.util.Set;
import java.util.HashSet;

public class Transaction {

    public int transactionId;
    public int birthTime;
    public boolean isAborted;
    public boolean isCommited;
    public Set<Integer> visitedSet;

    // this is no longer needed since we do not need to store subtransactions in transaction
    //public Set<SubTransaction> subTransactionSet;

    public Transaction( int transactionId, int birthTime ) {
        this.transactionId = transactionId;
        this.birthTime = birthTime;
        this.isAborted = false;
        this.isCommited = false;
        //this.subTransactionSet = new HashSet<SubTransaction>();
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
