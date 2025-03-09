import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
public class MemoryPool {

    int memorySize;
    int freeSize;
    int pageSize;
    int freePage;
    int totalPage;
    int preAssignPage;
    Deque<ByteBuffer> assignButNotUsed;

    static int DEFAULT_MEMORY_SIZE = 8;
    static int DEFAULT_PAGE_SIZE = 2;
    static int DEFAULT_PRE_ASSIGN_PAGES = 1;

    MemoryPool() {
        this(DEFAULT_MEMORY_SIZE, DEFAULT_PAGE_SIZE, DEFAULT_PRE_ASSIGN_PAGES);
    }
    MemoryPool(int capacity, int pagesize, int preassignpage) {
        memorySize = capacity;
        pageSize = pagesize;
        assignButNotUsed = new ArrayDeque<>();
        totalPage = preassignpage;
        freePage = preassignpage;
        freeSize = memorySize;
        preAssignPage = preassignpage;
        if (preAssignPage * pageSize > freeSize) throw new IllegalArgumentException("预分配的页大小超过可用页的数量！");
        for (int i = 0; i < preAssignPage; i++) {
            assignButNotUsed.add(ByteBuffer.allocate(pageSize));
            totalPage++;
            freePage++;
            freeSize -= pageSize;
        }
    }

    public ByteBuffer allocate(int pages) {
        if (pages <= 0) throw new IllegalArgumentException("申请的空间不能是" + pages + "页！");
        if (pages * pageSize > freeSize) throw new IllegalArgumentException("已经没有空余的数据页进行分配了！");
        if (pages == 1) {
            freeSize -= pageSize;
            if (freePage > 0) {
                freePage -= 1;
                return assignButNotUsed.pollFirst();
            }
            // 没有空闲页，新增一个
            return ByteBuffer.allocate(pages);
        }
        // 申请页数大于一，并且还有空闲内存可以分配
        freeSize -= pageSize * pages;
        return ByteBuffer.allocate(pageSize * pages);
    }

    void deallocate(ByteBuffer byteBuffer) {
        int n = byteBuffer.capacity() / pageSize;
        if (n == 1) {
            byteBuffer.clear();
            assignButNotUsed.add(byteBuffer);
            freePage++;
        } else {
            freeSize += byteBuffer.capacity();
        }
    }
}
