public class Main {

    public static void main( String[] args ) {
        System.out.println( "this is a test" );
        String filePath = "C:/yjjw/NYU_Courses/Fall 2021/Advanced_DB/project_tests/test1.txt";
        TransactionManager tm = new TransactionManager();
        tm.readInputFile(filePath);
    }
}
