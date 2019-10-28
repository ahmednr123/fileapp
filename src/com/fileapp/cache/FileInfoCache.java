package com.fileapp.cache;

import com.fileapp.storage.FileInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import redis.clients.jedis.*;

public class FileInfoCache {
    private static Logger LOGGER = Logger.getLogger(FileInfoCache.class.getName());
    private static FileInfoCache instance;

    static {
        instance = new FileInfoCache();
    }

    // Once data is fetched from redis
    // It can be stored in HashMap to avoid accessing redis server
    // every time to get data.
    private HashMap<String, ArrayList<FileInfo>> hashMap;
    private JedisPool pool;
    private String root;

    // isConnected variable is used to check if connection was made or not
    // This is done to avoid asking for connection from pool every time,
    // which is time consuming.
    private boolean isConnected;

    private static
    JedisPoolConfig buildPoolConfig() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(5);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(0);
        return poolConfig;
    }

    private
    FileInfoCache()
    {
        JedisPoolConfig jedisPoolConfig = buildPoolConfig();

        pool = new JedisPool(jedisPoolConfig);
        try(Jedis jedis = pool.getResource()) {
            isConnected = jedis.isConnected();
        } catch (Exception e) {
            LOGGER.info("Connection to Redis failed");
            isConnected = false;
        }

        hashMap = new HashMap<>();
    }

    /**
     * Returns the singleton instance of this class.
     *
     * It reconnects to redis server if the connection failed
     * earlier. This is done in a thread to avoid time delay in
     * trying to make a connection to redis server that's not
     * running, which crushes the whole purpose of having a cache
     * in the first place.
     *
     * @return Cache Instance
     */
    public static
    FileInfoCache getInstance ()
    {
        if (!instance.isConnected) {
            (new Thread(() -> {
                instance.pool = new JedisPool(buildPoolConfig());
                try(Jedis jedis = instance.pool.getResource()) {
                    instance.isConnected = jedis.isConnected();
                } catch (Exception e) {
                    LOGGER.info("Connection to Redis failed");
                    instance.isConnected = false;
                }
            })).start();
        }

        return instance;
    }

    public static
    void initialize (String root)
    {
        instance.root = root;
    }

    /**
     * Checks if a successful redis connection is made
     * and get data from redis or sets isConnected to false
     * suggesting connection failure.
     *
     * Saves data in redis server and hashmap
     *
     * @param key Directory Path
     * @param fileList FileInfo List to be saved
     */
    public void
    set(String key, ArrayList<FileInfo> fileList)
    {
        if (isConnected) {
            try (Jedis jedis = pool.getResource()) {
                jedis.hset(root, key, FileInfo.arrayListToString(fileList));
            } catch (Exception e) {
                LOGGER.info("Connection to Redis failed");
                isConnected = false;
            }
        }

        hashMap.put(key, fileList);
        LOGGER.info("Setting key=" + key);
    }

    /**
     * Checks if a successful redis connection is made
     * and get data from redis or sets isConnected to false
     * suggesting connection failure.
     *
     * Saves data in redis server and hashmap
     *
     * @param key Directory Path
     * @return FileInfo List
     */
    public
    ArrayList<FileInfo> get(String key)
    {
        System.out.println("Checking cache: key=" + key);

        if (!hashMap.containsKey(key)) {
            if (isConnected) {
                try (Jedis jedis = pool.getResource()) {
                    String listString = jedis.hget(root, key);
                    if (listString == null)
                        return null;

                    LOGGER.info("Reading Redis: key=" + key);
                    ArrayList<FileInfo> fileList = FileInfo.stringToArrayList(listString);

                    hashMap.put(key, fileList);

                    return fileList;
                } catch (Exception e) {
                    LOGGER.info("Connection to Redis failed");
                    isConnected = false;
                }
            } else {
                System.out.println("Cannot get resource");
                return null;
            }
        }

        LOGGER.info("Reading HashMap: key=" + key);
        return hashMap.get(key);
    }

    public void clear () {
        LOGGER.info("Clearing all cache data");
        if (isConnected) {
            try (Jedis jedis = pool.getResource()) {
                jedis.del(root);
            } catch (Exception e) {
                LOGGER.info("Connection to Redis failed");
                isConnected = false;
            }
        }

        hashMap.clear();
    }

    public void destroy () {
        hashMap.clear();
        pool.close();
    }
}
