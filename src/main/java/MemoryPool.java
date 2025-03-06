import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
public class MemoryPool {

    int memorySize = 16;

    int pageSize = 2;

    int[] flag = new int[memorySize / pageSize];

    byte[] memory = new byte[memorySize];

    ByteBuffer[] byteBuffers = new ByteBuffer[memorySize / pageSize];

    HashMap<Integer, int[]> map = new HashMap<>();

    MemoryPool() {
        for (int i = 0; i < byteBuffers.length; i++) {
            byteBuffers[i] = ByteBuffer.wrap(memory, i * pageSize, pageSize);
        }
    }

    public ByteBuffer allocate(int pages) {
        if (pages == 0) throw new OutOfMemoryError("申请的空间不能是" + pages + "页！");
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
        BufferedReader br = new BufferedReader(new FileReader("test.txt"));
        StreamTokenizer in = new StreamTokenizer(br);
        PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out));

        in.nextToken();
        int T = (int) in.nval;
        for (int i = 0; i < T; i++) {
            in.nextToken();
            int pages = (int)in.nval;
            MemoryPool memoryPool = new MemoryPool();
            // 申请两页内存
            ByteBuffer buffer = memoryPool.allocate(pages);
            in.nextToken();
            buffer.put((byte) (int)in.nval);
            in.nextToken();
            buffer.put((byte) (int)in.nval);
            in.nextToken();
            buffer.put((byte) (int)in.nval);
            memoryPool.print(buffer);
            in.nextToken();
            buffer.put((byte) (int)in.nval);
            memoryPool.print(buffer);
            // 再申请一页内存
            in.nextToken();
            ByteBuffer buffer2 = memoryPool.allocate((int)in.nval);
            in.nextToken();
            buffer2.put((byte) (int)in.nval);
            in.nextToken();
            buffer2.put((byte) (int)in.nval);
            memoryPool.print(buffer2);

            System.out.println("释放内存前内存池中的情况：" + Arrays.toString(memoryPool.memory));
            memoryPool.deallocate(buffer);
            System.out.println("释放内存后内存池中的情况：" + Arrays.toString(memoryPool.memory));

            // 申请maxPage + 1的内存
            in.nextToken();
            ByteBuffer buffer3 = memoryPool.allocate((int)in.nval);


            System.out.println("结束了");
        }


    }
}
