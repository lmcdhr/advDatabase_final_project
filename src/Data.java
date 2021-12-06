import java.util.TreeMap;

public class Data {

    public int dataIndex;
    public int dataValue;
    public TreeMap<Integer, Integer> valueRecord;

    // this variable is used when a site is recovered
    // in a recovered site, replicated is only ready to be written, but not read
    public boolean canBeRead;

    public Data(){
        this.dataIndex = -1;
        this.dataValue = -1;
        this.canBeRead = true;
        this.valueRecord = new TreeMap<Integer, Integer>();
    }

    public Data( int dataIndex, int dataValue, int timeStamp ) {
        this.dataIndex = dataIndex;
        this.dataValue = dataValue;
        this.canBeRead = true;
        this.valueRecord = new TreeMap<Integer, Integer>();
        this.valueRecord.put( timeStamp, dataValue );
    }

    public void updateValue() {
        int latestTimeStamp = this.valueRecord.lastKey();
        this.dataValue = this.valueRecord.get( latestTimeStamp );
    }

}
