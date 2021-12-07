import java.util.*;

public class Site {

    public int siteId;
    public boolean isStatusUp;
    public Map<Integer, Data> dataMap;

    // lock type: 1: read lock, 2: write lock
    // lockMap: key: dataId, value: lockMap of corresponding data(dataLockMap)
    // dataLockMap: key: lock type, value: a hashset,
    // inside the set is the id of transactions that asked(already holds) for such lock
    // ( even though it is a set, but actually there will be only on element inside )
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
        this.statusUpTime = new TreeMap<Integer, Integer>();
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

    public boolean canAcquireReadLock( int dataIndex, int transactionId ) {
        Map<Integer, Set<Integer>> dataLockMap = lockMap.get( dataIndex );
        Set<Integer> writeLock = dataLockMap.get( 2 );
        for( int tId: writeLock ) {
            if( tId != transactionId ) {
                return false;
            }
        }
        return true;
    }

    public boolean canAcquireWriteLock( int dataIndex, int transactionId ) {
        Map<Integer, Set<Integer>> dataLockMap = lockMap.get( dataIndex );
        Set<Integer> readLock = dataLockMap.get( 1 );
        Set<Integer> writeLock = dataLockMap.get( 2 );
        for( int tId: readLock ) {
            if( tId != transactionId ) {
                return false;
            }
        }
        for( int tId: writeLock ) {
            if( tId != transactionId ) {
                return false;
            }
        }
        return true;
    }

    public Set<Integer> transactionsHoldLocksOnData( int dataIndex, int transactionId ) {
        Set<Integer> res = new HashSet<Integer>();
        Map<Integer, Set<Integer>> dataLockMap = lockMap.get( dataIndex );
        Set<Integer> readLock = dataLockMap.get( 1 );
        Set<Integer> writeLock = dataLockMap.get( 2 );
        for( int tId: readLock ) {
            if( tId != transactionId ) {
                res.add( tId );
            }
        }
        for( int tId: writeLock ) {
            if( tId != transactionId ) {
                res.add( tId );
            }
        }
        return res;
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
        //System.out.println( "write to site: " + siteId + " data: " + dataId + " with value: " + dataValue + " at time: " + timeStamp );
    }

    public boolean checkAvailableTimeForRO( int dataId, int ROTime ) {
        int prevCommitTime = dataMap.get( dataId ).valueRecord.floorKey( ROTime );
        int prevUpTime = statusUpTime.floorKey( prevCommitTime );
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
