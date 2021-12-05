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

    public void fail( int siteId ) {
        Site site = this.siteMap.get( siteId );
        site.fail();
    }

    public void recover( int siteId ) {
        Site site = this.siteMap.get( siteId );
        site.recover();
    }

    public void dump() {
        for( int i=1; i<=10; i++ ) {
            Site s = siteMap.get( i );
            s.print();
            System.out.println();
        }
    }
}
