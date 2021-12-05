import java.util.*;

public class Site {

    public int siteId;
    public boolean isStatusUp;
    public Map<Integer, Data> dataMap;

    // lock type: 1: read lock, 2: write lock
    // lockMap: key: dataId, value: lockMap of corresponding data(dataLockMap)
    // dataLockMap: key: lock type, value: a hashset,
    // inside the set is the id of transactions that asked(already holds) for such lock
    public Map<Integer, Map<Integer, Set<Integer>>> lockMap;

    // this map will save the time interval when site is up
    // key is the start time, value is the end time
    public TreeMap<Integer, Integer> statusUpTime;

    public Site( int siteId ) {
        this.siteId = siteId;
        this.isStatusUp = true;
        this.dataMap = new HashMap<Integer, Data>();
        this.lockMap = new HashMap<>();
        this.generateData();
        this.statusUpTime.put( 0, Integer.MAX_VALUE );
    }

    private void generateData() {
        for( int i=2; i<=20; i+=2 ) {
            Data data = new Data( i, i * 10, 0 );
            dataMap.put( i, data );
            Map<Integer, Set<Integer>> dataLockMap = new HashMap<>();
            for( int j=1; j<=2; j++ ) {
                dataLockMap.put( j, new HashSet<Integer>() );
            }
            lockMap.put( i, dataLockMap ); // 0 means no lock
        }
        if( ( this.siteId - 1 ) % 2 != 0 ) {
            int index = this.siteId - 1;
            Data firstData = new Data( index, index * 10, 0 );
            dataMap.put( index, firstData );
            Map<Integer, Set<Integer>> dataLockMap = new HashMap<>();
            for( int j=1; j<=2; j++ ) {
                dataLockMap.put( j, new HashSet<Integer>() );
            }
            lockMap.put( index, dataLockMap );
            index += 10;
            Data secondData = new Data( index, index * 10, 0 );
            dataLockMap = new HashMap<>();
            for( int j=1; j<=2; j++ ) {
                dataLockMap.put( j, new HashSet<Integer>() );
            }
            dataMap.put( index, secondData );
            lockMap.put( index, dataLockMap );
        }
    }

    public void fail( int timeStamp ) {
        this.isStatusUp = false;
        this.eraseAllLock();
        int prevUpTime = statusUpTime.floorKey( timeStamp );
        statusUpTime.put( prevUpTime, timeStamp );
    }

    public void recover( int timeStamp ) {
        this.isStatusUp = true;
        this.setDataStatusAfterRecover();
        statusUpTime.put( timeStamp, Integer.MAX_VALUE );
    }

    public void applyLock( int dataIndex, int lockType, int transactionId ) {
        this.lockMap.get( dataIndex ).get( lockType ).add( transactionId );
    }

    public void removeLock( int dataIndex, int lockType, int transactionId ) {
        this.lockMap.get( dataIndex ).get( lockType ).remove( transactionId );
    }

    public void eraseAllLock() {
        for( int i=2; i<=20; i+=2 ) {
            Map<Integer, Set<Integer>> dataLockMap = new HashMap<>();
            for( int j=1; j<=2; j++ ) {
                dataLockMap.put( j, new HashSet<Integer>() );
            }
            lockMap.put( i, dataLockMap ); // 0 means no lock
        }
        if( ( this.siteId - 1 ) % 2 != 0 ) {
            int index = this.siteId - 1;
            Map<Integer, Set<Integer>> dataLockMap = new HashMap<>();
            for( int j=1; j<=2; j++ ) {
                dataLockMap.put( j, new HashSet<Integer>() );
            }
            lockMap.put( index, dataLockMap );
            index += 10;
            dataLockMap = new HashMap<>();
            for( int j=1; j<=2; j++ ) {
                dataLockMap.put( j, new HashSet<Integer>() );
            }
            lockMap.put( index, dataLockMap );
        }
    }

    public void setDataStatusAfterRecover() {
        for( int i=2; i<=20; i+=2 ) {
            Data prevData = dataMap.get( i );
            prevData.canBeRead = false;
            dataMap.put( i, prevData );
        }
    }

    public void writeValueToData( int dataId, int dataValue, int timeStamp ) {
        dataMap.get( dataId ).valueRecord.put( timeStamp, dataValue );
        dataMap.get( dataId ).updateValue();
        System.out.println( "write to site: " + siteId + " data: " + dataId + " with value: " + dataValue + " at time: " + timeStamp );
    }

    public void removeLockFromTransaction( int transactionId ) {
        for( int dataId: lockMap.keySet() ) {
            Map<Integer, Set<Integer>> dataLocks = lockMap.get( dataId );
            for( int lockType: dataLocks.keySet() ) {
                if( dataLocks.get( lockType ).contains( transactionId ) ) {
                    dataLocks.get( lockType ).remove( transactionId );
                }
            }
            lockMap.put( dataId, dataLocks );
        }
    }

    public boolean checkAvailableTimeForRO( int ROTime ) {
        int prevUpTime = statusUpTime.floorKey( ROTime );
        int prevUpEndTime = statusUpTime.get( prevUpTime );
        if( prevUpEndTime < ROTime ) {
            return false;
        }
        else {
            return true;
        }
    }

    public void print() {
        System.out.print( "site" + this.siteId + " : "  );
        for( int i=1; i<=20; i++ ) {
            if( this.dataMap.containsKey( i ) ) {
                Data d = this.dataMap.get( i );
                System.out.print( "x" + d.dataIndex  + ":" + d.dataValue + " " );
            }
        }
    }
}
