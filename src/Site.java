import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Site {

    public int siteId;
    public boolean isStatusUp;
    public Map<Integer, Data> dataMap;

    // lock type: 1: read lock, 2: write lock
    // lockMap: key: dataId, value: lockMap of corresponding data(dataLockMap)
    // dataLockMap: key: lock type, value: a hashset,
    // inside the set is the id of transactions that asked(already holds) for such lock
    public Map<Integer, Map<Integer, Set<Integer>>> lockMap;

    public Site( int siteId ) {
        this.siteId = siteId;
        this.isStatusUp = true;
        this.dataMap = new HashMap<Integer, Data>();
        this.lockMap = new HashMap<>();
        this.generateData();
    }

    private void generateData() {
        for( int i=2; i<=20; i+=2 ) {
            Data data = new Data( i, i * 10 );
            dataMap.put( i, data );
            Map<Integer, Set<Integer>> dataLockMap = new HashMap<>();
            for( int j=1; j<=2; j++ ) {
                dataLockMap.put( j, new HashSet<Integer>() );
            }
            lockMap.put( i, dataLockMap ); // 0 means no lock
        }
        if( ( this.siteId - 1 ) % 2 != 0 ) {
            int index = this.siteId - 1;
            Data firstData = new Data( index, index * 10 );
            dataMap.put( index, firstData );
            Map<Integer, Set<Integer>> dataLockMap = new HashMap<>();
            for( int j=1; j<=2; j++ ) {
                dataLockMap.put( j, new HashSet<Integer>() );
            }
            lockMap.put( index, dataLockMap );
            index += 10;
            Data secondData = new Data( index, index * 10 );
            dataLockMap = new HashMap<>();
            for( int j=1; j<=2; j++ ) {
                dataLockMap.put( j, new HashSet<Integer>() );
            }
            dataMap.put( index, secondData );
            lockMap.put( index, dataLockMap );
        }
    }

    public void fail() {
        this.isStatusUp = false;
    }

    public void recover() {
        this.isStatusUp = true;
    }

    public void applyLock( int dataIndex, int lockType, int transactionId ) {
        this.lockMap.get( dataIndex ).get( lockType ).add( transactionId );
    }

    public void removeLock( int dataIndex, int lockType, int transactionId ) {
        this.lockMap.get( dataIndex ).get( lockType ).remove( transactionId );
    }

    public void printTest() {
        System.out.println( "site: " + this.siteId + " is up? " + this.isStatusUp );
        for( int i=1; i<=20; i++ ) {
            if( this.dataMap.containsKey( i ) ) {
                Data d = this.dataMap.get( i );
                System.out.println( "data: " + d.dataIndex  + " : " + d.dataValue );
            }
        }
    }
}
