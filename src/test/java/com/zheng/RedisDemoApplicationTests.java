package com.zheng;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;
import redis.clients.jedis.*;

import java.util.*;

@SpringBootTest
class RedisDemoApplicationTests {

    // 此对象为spring提供的一个用于操作redis数据库中的字符串的一个对象
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    public void testJedis() {
        Jedis jedis = new Jedis("192.168.126.128", 6379);
        String get = jedis.get("age");
        Long num = jedis.incrBy("num", 1);
        System.out.println(num);
    }

    @Test
    public void testJedisPool() {
        // 构建连接池配置对象，进行初始化
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(100);
        // 构建连接池
        JedisPool jedisPool = new JedisPool(config, "192.168.126.128", 6379);
        // 从池中获取连接
        Jedis jedis = jedisPool.getResource();
        // 读写redis数据
        jedis.set("hi", "hello");
        System.out.println(jedis.strlen("hi"));
        Map<String, String> map = new HashMap<>();
        map.put("name", "zhengc");
        map.put("ddd", "zhessssngc");
        jedis.hmset("address", map);
        Map<String, String> address = jedis.hgetAll("address");
        // 关闭资源
        jedisPool.close();
    }

    @Test
    public void testOpsForValueSet() {
        stringRedisTemplate.opsForValue().set("age", "25");
    }

    @Test
    public void testMulti() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(20);
        JedisPool jedisPool = new JedisPool(config, "192.168.126.128", 6379);
        Jedis jedis = jedisPool.getResource();
        jedis.set("z", "200");
        jedis.set("w", "700");
        Transaction transaction = jedis.multi();
        transaction.decrBy("w", 500);
        transaction.incrBy("z", 500);
        transaction.exec();
        List<String> mget1 = jedis.mget("w", "z");
        System.out.println(mget1);
        jedisPool.close();
    }

    @Test
    public void testSetData() {
        SetOperations setOperations = redisTemplate.opsForSet();
        setOperations.add("zc", "a", "b", "c", "d", "e");
        Set<Object> zc = setOperations.members("zc");
        System.out.println(zc);

        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        opsForValue.set("", "");
        String id = opsForValue.get("id");
        System.out.println(id);
    }

    @Test
    public void testListData() {
        ListOperations list = redisTemplate.opsForList();
        list.leftPush("d", 100);
        list.leftPush("d", 200);
        list.leftPush("d", 300);
        Object a = list.rightPop("d");
        System.out.println(a);
        List a1 = list.range("d", 0, -1);
        System.out.println(a1);
    }

    @Test
    public void testHashData() {
        HashOperations hash = redisTemplate.opsForHash();
        hash.put("m", "id", 101);
        hash.put("m", "name", "zhengc");
        Object o = hash.get("m", "name");
        Object o1 = hash.get("m", "id");
        System.out.println(o);
        System.out.println(o1);
        hash.putIfAbsent("m", "sex", "man");
        hash.putIfAbsent("m", "sex", "girl");
        Object o2 = hash.get("m", "sex");
        System.out.println(o2);
    }

    @Test
    public void testClear() {
        redisTemplate.execute((RedisCallback) connection -> {
            connection.flushAll();
            return null;
        });
    }

    @Test
    public void testRedisShard(){
        JedisShardInfo info = new JedisShardInfo("192.168.126.128", 6379);
        JedisShardInfo info1 = new JedisShardInfo("192.168.126.128", 6380);
        JedisShardInfo info2 = new JedisShardInfo("192.168.126.128", 6381);

        List<JedisShardInfo> list = new ArrayList<>();
        list.add(info);
        list.add(info1);
        list.add(info2);

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(100);

        ShardedJedisPool pool = new ShardedJedisPool(config, list);
        ShardedJedis resource = pool.getResource();
        for (int i = 0; i < 10; i++) {
            // k/v 是如何存储到不同的redis数据库中的？（一致性hash算法）
            resource.set("key-" + i, String.valueOf(i));
        }
        pool.close();
    }

}
