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
    public void testConstructor() {
        // 测试有参构造情况下容量和页数不是整数倍的关系
        assertThrows(IllegalArgumentException.class, () -> {
            new MemoryPool(7,2);
        },"测试没有通过！参数是非法的");
    }

    @Test
    public void testAllocate() {
        // 测试页数是非法状态的情况
        assertThrows(IllegalArgumentException.class, () -> {
            ByteBuffer buffer = memoryPool.allocate(-1);
        },"测试为通过！参数是非法的");
        // 正常申请的状态
        ByteBuffer buffer1 = memoryPool.allocate(2);
        assertEquals(4, buffer1.remaining());
        assertThrows(OutOfMemoryError.class, () -> {
            ByteBuffer buffer2 = memoryPool.allocate(10);
        },"测试没有通过！申请的内存已经大于可用空间");
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