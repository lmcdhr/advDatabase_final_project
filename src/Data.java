public class Data {

    public int dataIndex;
    public int dataValue;

    // this variable is used when a site is recovered
    // in a recovered site, replicated is only ready to be written, but not read
    public boolean canBeRead;

    public Data(){
        this.dataIndex = -1;
        this.dataValue = -1;
        this.canBeRead = true;
    }

    public Data( int dataIndex, int dataValue ) {
        this.dataIndex = dataIndex;
        this.dataValue = dataValue;
        this.canBeRead = true;
    }

}
