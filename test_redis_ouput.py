# test.py
import redis

# 配置 Redis 连接（本地 Redis 默认配置）
# host: Redis 服务器地址（本地用 "localhost" 或 "127.0.0.1"）
# port: Redis 端口（默认 6379）
# db: 使用的数据库编号（默认 0）
r = redis.Redis(
    host="localhost",
    port=6379,
    db=0,
    decode_responses=False  # 默认为 False，返回 bytes；设为 True 直接返回字符串
)

# 先往 Redis 写入测试数据（确保 BOARD_STATE 有值）
r.hset("BOARD_STATE", "10:20", "#FF0000")

# 获取 HGETALL 返回的结果
hash_data = r.hgetall("BOARD_STATE")
print("原始返回结果（bytes 类型）：", hash_data)  # {b'10:20': b'#FF0000'}

# 可选：将 bytes 转为字符串（更易读）
hash_data_str = {k.decode(): v.decode() for k, v in hash_data.items()}
print("转为字符串后的结果：", hash_data_str)  # {'10:20': '#FF0000'}