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

    public void testPreSearch() {
        String query = "This is a test";
        int n = SearchEngineService.preSearch(query);
        assertTrue(n > 0);
        assertEquals(n, SearchEngineService.preSearch(query));
    }

    public void testSearch() {
        String query = "This is a test";
        int n = SearchEngineService.preSearch(query);
        int i = 0;
        double last = Integer.MAX_VALUE;
        while (true) {
            ResultEntry[] result = SearchEngineService.search(query, i, 10);
            if (result == null) {
                break;
            }
            for (int j = 0; j < result.length; j++) {
                assertTrue(result[j].getScore() <= last);
                last = result[j].getScore();
            }
            i += 10;
        }
    }

    public void testQuickSelectK() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ResultEntry[] entries = new ResultEntry[10];
        for (int i = 0 ; i < 10; i++) {
            entries[i] = new ResultEntry("0");
            entries[i].score = i;
        }
        Method findKthLargest = SearchEngineService.class.getDeclaredMethod("findKthLargest", ResultEntry[].class, int.class);
        findKthLargest.setAccessible(true);
        for (int i = 0; i < 10; i++) {
            assertEquals(9.0 - i, ((ResultEntry) findKthLargest.invoke(null, entries, i)).getScore());
        }
    }

}
