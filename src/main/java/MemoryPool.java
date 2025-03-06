import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
public class MemoryPool {

    int memorySize;

    static int DEFAULT_MEMORY_SIZE = 8;
    static int DEFAULT_PAGE_SIZE = 2;
    int pageSize;

    int[] flag;

    byte[] memory;

    ByteBuffer[] byteBuffers;

    HashMap<Integer, int[]> map = new HashMap<>();

    MemoryPool() {
        memorySize = DEFAULT_MEMORY_SIZE;
        pageSize = DEFAULT_PAGE_SIZE;
        flag = new int[memorySize / pageSize];
        byteBuffers = new ByteBuffer[memorySize / pageSize];

        memory = new byte[memorySize];
        for (int i = 0; i < byteBuffers.length; i++) {
            byteBuffers[i] = ByteBuffer.wrap(memory, i * pageSize, pageSize);
        }
    }
    MemoryPool(int capacity, int ps) {
        if (capacity % ps != 0) throw new IllegalArgumentException("内存容量必须是页容量的整数倍！");
        memorySize = capacity;
        pageSize = ps;
        flag = new int[memorySize / pageSize];
        byteBuffers = new ByteBuffer[memorySize / pageSize];
        memory = new byte[memorySize];
        for (int i = 0; i < byteBuffers.length; i++) {
            byteBuffers[i] = ByteBuffer.wrap(memory, i * pageSize, pageSize);
        }
    }

    public ByteBuffer allocate(int pages) {
        if (pages <= 0) throw new IllegalArgumentException("申请的空间不能是" + pages + "页！");
        int freeIndex = checkFree(pages);
        if (freeIndex == -1) throw new OutOfMemoryError("已经没有空闲的连续内存容纳" + pages + "页！");
        ByteBuffer merge = null;
        for (int i = 0; i < pages; i++) {
            flag[freeIndex + i] = 1;
            if (merge == null)
                merge = byteBuffers[freeIndex + i];
            else {
                merge = ByteBuffer.wrap(memory, merge.position(), merge.remaining() + byteBuffers[freeIndex + i].remaining());
            }
        }
        map.put(merge.limit(), new int[]{freeIndex * pageSize, freeIndex * pageSize + pages * pageSize});
        return merge;
    }

    void deallocate() {
        int index = 0;
        for (ByteBuffer byteBuffer : byteBuffers) {
            if (map.containsKey(byteBuffer.limit())) {
                map.remove(byteBuffer.limit());
                flag[index++] = 0;
            }
        }
        Arrays.fill(memory, (byte) 0);
    }

    void deallocate(ByteBuffer byteBuffer) {
        int[] section = map.get(byteBuffer.limit());
        for (int i = section[0]; i < section[1]; i++) {
            memory[i] = 0;
        }
    }

    int checkFree(int pages) {
        int slow = 0, fast = 0;
        while (slow < flag.length) {
            if (flag[slow] == 0) {
                for (; fast < flag.length; ) {
                    int length = fast - slow + 1;
                    if (length > pages) {
                        slow = fast;
                    } else if (length == pages && flag[slow] == 0) return slow;
                    else fast++;
                }
                if (fast - slow + 1 < pages) return -1;
            } else {
                slow++;
                fast = slow;
            }


        }
        return -1;
    }

    void print(ByteBuffer byteBuffer) {
        int position = byteBuffer.position();
        int limit = byteBuffer.limit();
        int[] p = map.get(limit);

        byteBuffer.position(p[0]);
        byteBuffer.limit(position);
        while (byteBuffer.hasRemaining()) {
            System.out.print(byteBuffer.get() + " ");
        }
        System.out.println();
        byteBuffer.position(position);
        byteBuffer.limit(limit);
    }


    public static void main(String[] args) throws Exception {
        MemoryPool memoryPool = new MemoryPool(5,2);
        memoryPool.allocate(2);



    }
}
