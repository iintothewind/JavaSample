package sample.foundation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

public class Loop {

    @Test(expected = ConcurrentModificationException.class)
    public void removeElementsInList() {
        List<String> list = Lists.newArrayList("a", "b", "c", "d", "e");
        for (String s : list) {
            list.remove(s);
        }
    }

    @Test
    public void removeElementsInIteration() {
        List<String> list = Lists.newArrayList("a", "b", "c", "d", "e");
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String s = iterator.next();
            if (s.equals("c")) {
                iterator.remove();
            }
        }
        System.out.println(list);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void removeElementsInQueue() {
        Queue<String> queue = Lists.newLinkedList(ImmutableList.of("a", "b", "c", "d", "e"));
        for (String s : queue) {
            if (s.equals("c")) {
                queue.remove();
            }
        }

    }


}
