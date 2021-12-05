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

    public void createTransaction( int transactionId, int timeStamp ) {
        this.transactionMap.put( transactionId, new Transaction( transactionId, timeStamp ) );
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
        return false;
    }

    public boolean runEndSubTransaction( SubTransaction subTransaction ) {
        return false;
    }

    public boolean runFailSubTransaction( SubTransaction subTransaction ) {
        return false;
    }

    public boolean runRecoverSubTransaction( SubTransaction subTransaction ) {
        return false;
    }

}
