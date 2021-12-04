import java.util.Set;
import java.util.HashSet;

public class Transaction {

    public Set<SubTransaction> subTransactionSet;

    public Transaction() {
        this.subTransactionSet = new HashSet<SubTransaction>();
    }

    public void addReadSubTransaction( int transactionId, int requestDataIndex, String requestType ) {
        SubTransaction subTransaction = new SubTransaction( transactionId, requestDataIndex, requestType );
        this.subTransactionSet.add( subTransaction );
    }

    public void addWriteSubTransaction( int transactionId, int requestDataIndex, int requestDataValueChangeTo, String requestType ) {
        SubTransaction subTransaction = new SubTransaction( transactionId, requestDataIndex, requestDataValueChangeTo, requestType );
        this.subTransactionSet.add( subTransaction );
    }
}
