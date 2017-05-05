package test;

import junit.framework.TestCase;
import searchengine.ResultEntry;
import searchengine.SearchEngineService;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by QingxiaoDong on 5/4/17.
 */
public class SearchEngineServiceTest extends TestCase {

    public void testQuickSelectK() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ResultEntry[] entries = new ResultEntry[10];
        for (int i = 0 ; i < 10; i++) {
            entries[i] = new ResultEntry();
            entries[i].score = i;
        }
        Method findKthLargest = SearchEngineService.class.getDeclaredMethod("findKthLargest", ResultEntry[].class, int.class);
        findKthLargest.setAccessible(true);
        for (int i = 0; i < 10; i++) {
            assertEquals(9.0 - i, ((ResultEntry) findKthLargest.invoke(null, entries, i)).getScore());
        }
    }

}
