package service;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class RedisClient {
    public static RedissonClient client;
    static {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://localhost:6379");
        client = Redisson.create(config);
    }
}
