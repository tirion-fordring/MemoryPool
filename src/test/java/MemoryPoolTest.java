import org.junit.jupiter.api.*;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

public class MemoryPoolTest {

    private MemoryPool memoryPool;

    @BeforeEach
    public void setUp() {
        memoryPool = new MemoryPool();
    }

    @AfterEach
    public void tearDown() {
        memoryPool = null;
    }

    @Test
    public void testAllocate() {
        // 测试分配内存的合法情况
        ByteBuffer buffer = memoryPool.allocate(2);
        assertNotNull(buffer);
        assertEquals(4, buffer.remaining());
        try {
            // 测试申请空间超过总空间的情况
            ByteBuffer buffer2 = memoryPool.allocate(10);
        } catch (OutOfMemoryError e) {
            assertEquals("已经没有空闲的连续内存容纳10页！", e.getMessage());
        }
    }

    // 用于测试回收指定空间的方法deallocate(ByteBuffer byteBuffer)
    @Test
    public void testDeallocate() {
        ByteBuffer buffer = memoryPool.allocate(2);
        buffer.put((byte) 1);
        buffer.put((byte) 2);
        memoryPool.deallocate(buffer);
        byte[] memory = memoryPool.memory;
        for (int i = 0; i < 4; i++) {
            assertEquals(0, memory[i]);
        }
    }

    // 用于测试回收所有空间的方法deallocate
    @Test
    public void testDeallocateAll() {
        ByteBuffer buffer1 = memoryPool.allocate(2);
        ByteBuffer buffer2 = memoryPool.allocate(1);
        memoryPool.deallocate();
        byte[] memory = memoryPool.memory;
        for (int i = 0; i < memory.length; i++) {
            assertEquals(0, memory[i]);
        }
    }

}