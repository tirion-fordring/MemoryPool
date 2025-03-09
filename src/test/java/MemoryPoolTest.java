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

//    @Test
//    public void testConstructor() {
//        // 测试有参构造情况下容量和页数不是整数倍的关系
//        assertThrows(IllegalArgumentException.class, () -> {
//            new MemoryPool(7,2, 1);
//        },"测试没有通过！这个情况应该抛出异常！");
//    }

    @Test
    public void testAllocate() {
        // 测试页数是非法状态的情况
        assertThrows(IllegalArgumentException.class, () -> {
            ByteBuffer buffer = memoryPool.allocate(-1);
        },"测试为通过！这个情况应该抛出异常！");

        // 正常申请的状态
        ByteBuffer buffer1 = memoryPool.allocate(2);
        // 检查内存情况
        assertEquals(memoryPool.pageSize * 2, buffer1.capacity());
        // 测试申请页数大于最大页数情况
        assertThrows(IllegalArgumentException.class, () -> {
            ByteBuffer buffer = memoryPool.allocate(5);
        },"测试没有通过！这个情况应该抛出异常！");
    }

    // 用于测试回收指定空间的方法deallocate(ByteBuffer byteBuffer)
    @Test
    public void testDeallocate() {
        ByteBuffer buffer = memoryPool.allocate(2);
        int freeSize = memoryPool.freeSize;
        memoryPool.deallocate(buffer);
        // 测试释放内存后内存池的空闲内存
        assertEquals(memoryPool.freeSize, freeSize + memoryPool.pageSize * 2);

        ByteBuffer buffer1 = memoryPool.allocate(1);
        int freePage = memoryPool.freePage;
        memoryPool.deallocate(buffer1);
        // 测试释放内存后内存池的空闲页
        assertEquals(memoryPool.freePage, freePage + 1);
    }


}