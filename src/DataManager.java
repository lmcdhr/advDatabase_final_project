import java.util.HashMap;
import java.util.Map;

public class DataManager {

    Map<Integer, Site> siteMap;

    public DataManager() {
        this.siteMap = new HashMap<Integer, Site>();
        this.generateSites();
    }

    private void generateSites() {
        for( int i=1; i<=10; i++ ) {
            Site site = new Site( i );
            siteMap.put( i, site );
        }
    }

    public void fail( int siteId, int timeStamp ) {
        Site site = this.siteMap.get( siteId );
        site.fail( timeStamp );
    }

    public void recover( int siteId, int timeStamp ) {
        Site site = this.siteMap.get( siteId );
        site.recover( timeStamp );
    }

    public void write( int siteId, int dataId, int dataValue, int timeStamp ) {
        Site site = this.siteMap.get( siteId );
        site.writeValueToData( dataId, dataValue, timeStamp );
    }

    public void removeLockForTransaction( int transactionId ) {
        for( int siteId: siteMap.keySet() ) {
            siteMap.get( siteId ).removeLockFromTransaction( transactionId );
        }
    }

    public void dump() {
        for( int i=1; i<=10; i++ ) {
            Site s = siteMap.get( i );
            s.print();
            System.out.println();
        }
    }
}
