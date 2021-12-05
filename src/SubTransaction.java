public class SubTransaction {

    public int transactionId;
    public int siteId;
    public int requestDataIndex;
    public int requestDataValueChangeTo;
    public int timeStamp;
    public String requestType;

    // used for begin and end
    public SubTransaction( int transactionId, String requestType, int timeStamp ) {
        this.transactionId = transactionId;
        this.requestType = requestType;
        this.siteId = -1;
        this.requestDataIndex = -1;
        this.requestDataValueChangeTo = -1;
        this.timeStamp = timeStamp;
    }

    // used for fail and recover
    public SubTransaction( int transactionId, String requestType, int siteId, int timeStamp ) {
        this.transactionId = transactionId;
        this.siteId = siteId;
        this.requestType = requestType;
        this.requestDataIndex = -1;
        this.requestDataValueChangeTo = -1;
        this.timeStamp = timeStamp;
    }

    // used for read
    public SubTransaction( int transactionId, int requestDataIndex, String requestType, int timeStamp ) {
        this.transactionId = transactionId;
        this.siteId = -1;
        this.requestDataIndex = requestDataIndex;
        this.requestType = requestType;
        this.requestDataValueChangeTo = -1;
        this.timeStamp = timeStamp;
    }

    // used for write
    public SubTransaction( int transactionId, int requestDataIndex, int requestDataValueChangeTo, String requestType, int timeStamp ) {
        this.transactionId = transactionId;
        this.siteId = -1;
        this.requestDataIndex = requestDataIndex;
        this.requestType = requestType;
        this.requestDataValueChangeTo = requestDataValueChangeTo;
        this.timeStamp = timeStamp;
    }
}
