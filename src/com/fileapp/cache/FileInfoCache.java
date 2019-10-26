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

    private HashMap<String, ArrayList<FileInfo>> hashMap;
    private JedisPool pool;
    private String root;

    private
    JedisPoolConfig buildPoolConfig() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(5);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(2);
        return poolConfig;
    }

    private
    FileInfoCache()
    {
        JedisPoolConfig jedisPoolConfig = buildPoolConfig();
        try {
            pool = new JedisPool(jedisPoolConfig);
        } catch (Exception e) {
            LOGGER.severe("Connection to Redis failed");
        }

        hashMap = new HashMap<>();
    }

    public static
    FileInfoCache getInstance ()
    {
        return instance;
    }

    public void
    initialize (String root)
    {
        this.root = root;
    }

    public void
    set(String key, ArrayList<FileInfo> fileList)
    {
        if (root == null)
            throw new RuntimeException("FileInfoCache not initialized");

        Jedis jedis = pool.getResource();

        jedis.hset(root, key, FileInfo.arrayListToString(fileList));
        hashMap.put(key, fileList);

        LOGGER.info("SETTING key=" + key);

        jedis.close();
    }

    public
    ArrayList<FileInfo> get(String key)
    {
        if (root == null)
            throw new RuntimeException("FileInfoCache not initialized");

        Jedis jedis = pool.getResource();

        System.out.println("CHECKING CACHE: key=" + key);

        if (!hashMap.containsKey(key)) {
            String listString = jedis.hget(root, key);
            if (listString == null)
                return null;

            LOGGER.info("READING REDIS: key=" + key);
            ArrayList<FileInfo> fileList = FileInfo.stringToArrayList(listString);

            hashMap.put(key, fileList);

            return fileList;
        }

        jedis.close();

        LOGGER.info("READING HASHMAP: key=" + key);
        return hashMap.get(key);
    }

    public void clear () {
        if (root == null)
            throw new RuntimeException("FileInfoCache not initialized");

        Jedis jedis = pool.getResource();

        hashMap.clear();
        jedis.del(root);

        LOGGER.info("Clearing all cache data");

        jedis.close();
    }

    public void destroy () {
        if (root == null)
            throw new RuntimeException("FileInfoCache not initialized");

        hashMap.clear();
        pool.close();
    }
}
