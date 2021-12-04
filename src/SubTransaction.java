public class SubTransaction {

    public int transactionId;
    public int requestDataIndex;
    public int requestDataValueChangeTo;
    public String requestType;

    public SubTransaction( int transactionId, int requestDataIndex, String requestType ) {
        this.transactionId = transactionId;
        this.requestDataIndex = requestDataIndex;
        this.requestType = requestType;
        this.requestDataValueChangeTo = -1;
    }

    public SubTransaction( int transactionId, int requestDataIndex, int requestDataValueChangeTo, String requestType ) {
        this.transactionId = transactionId;
        this.requestDataIndex = requestDataIndex;
        this.requestType = requestType;
        this.requestDataValueChangeTo = requestDataValueChangeTo;
    }
}
