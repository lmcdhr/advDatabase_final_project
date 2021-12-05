public class SubTransaction {

    public int transactionId;
    public int siteId;
    public int requestDataIndex;
    public int requestDataValueChangeTo;
    public String requestType;

    // used for begin and end
    public SubTransaction( int transactionId, String requestType ) {
        this.transactionId = transactionId;
        this.requestType = requestType;
        this.siteId = -1;
        this.requestDataIndex = -1;
        this.requestDataValueChangeTo = -1;
    }

    // used for fail and recover
    public SubTransaction( int transactionId, String requestType, int siteId ) {
        this.transactionId = transactionId;
        this.siteId = siteId;
        this.requestType = requestType;
        this.requestDataIndex = -1;
        this.requestDataValueChangeTo = -1;
    }

    // used for read
    public SubTransaction( int transactionId, int requestDataIndex, String requestType ) {
        this.transactionId = transactionId;
        this.siteId = -1;
        this.requestDataIndex = requestDataIndex;
        this.requestType = requestType;
        this.requestDataValueChangeTo = -1;
    }

    // used for write
    public SubTransaction( int transactionId, int requestDataIndex, int requestDataValueChangeTo, String requestType ) {
        this.transactionId = transactionId;
        this.siteId = -1;
        this.requestDataIndex = requestDataIndex;
        this.requestType = requestType;
        this.requestDataValueChangeTo = requestDataValueChangeTo;
    }
}
