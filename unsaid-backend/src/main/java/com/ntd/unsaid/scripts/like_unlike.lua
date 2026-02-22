if redis.call("SISMEMBER", KEYS[1], ARGV[1]) == 0 then
    redis.call("SADD", KEYS[1], ARGV[1])
    redis.call("INCR", KEYS[2])
    redis.call("SADD", KEYS[3], ARGV[2])
    return 1
else
    redis.call("SREM", KEYS[1], ARGV[1])
    redis.call("DECR", KEYS[2])
    redis.call("SADD", KEYS[3], ARGV[2])
    return 0
end