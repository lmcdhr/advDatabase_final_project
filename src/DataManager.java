import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
            Site site = siteMap.get( siteId );
            for( int dataId: siteMap.get( siteId ).lockMap.keySet() ) {
                Map<Integer, Set<Integer>> dataLockMap =  site.lockMap.get( dataId );
                for( int lockType: dataLockMap.keySet() ) {
                    if( dataLockMap.get( lockType ).contains( transactionId ) ) {
                        siteMap.get( siteId ).lockMap.get( dataId ).get( lockType ).remove( transactionId );
                    }
                }
            }
        }
    }

    public void implementReadLockOnSite( int siteId, int dataId, int transactionId ) {
        siteMap.get( siteId ).lockMap.get( dataId ).get( 1 ).add( transactionId );
    }

    public void implementWriteLockOnSite( int siteId, int dataId, int transactionId ) {
        siteMap.get( siteId ).lockMap.get( dataId ).get( 2 ).add( transactionId );
    }

    public void dump() {
        for( int i=1; i<=10; i++ ) {
            Site s = siteMap.get( i );
            s.print();
            System.out.println();
        }
    }

    public void printLockTable() {
        for( int siteId: siteMap.keySet() ) {
            System.out.println( "lock on site: " + siteId );
            Site site = siteMap.get( siteId );
            for( int dataId: site.lockMap.keySet() ) {
                Map<Integer, Set<Integer>> lockMap = site.lockMap.get( dataId );
                Set<Integer> readLock = lockMap.get( 1 );
                Set<Integer> writeLock = lockMap.get( 2 );
                System.out.print( "data: " + dataId + " " );
                System.out.print( "read lock: " );
                for( int transactionId: readLock ) {
                    System.out.print( transactionId + "," );
                }
                System.out.print( "write lock: " );
                for( int transactionId: writeLock ) {
                    System.out.print( transactionId + "," );
                }
                System.out.println();
            }
        }
    }
}
