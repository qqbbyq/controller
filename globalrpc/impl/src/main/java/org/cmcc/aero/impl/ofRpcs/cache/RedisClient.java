/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.impl.ofRpcs.cache;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by cmcc on 2017/8/5.
 */
public class RedisClient implements AutoCloseable{
    private String ipStr;
    private Integer portNo;
    private String password;
    private Jedis jedis;
    private JedisPool jedisPool;

    public RedisClient(String ipStr, Integer port, String password, int database){
        this.ipStr = ipStr != null ? ipStr : "localhost";
        this.portNo = port != null ? port : 6379;
        this.jedisPool = new JedisPool(this.ipStr, portNo);

        this.jedis = jedisPool.getResource();
        this.password = password;
        jedis.auth(this.password);
        jedis.select(database);
    }

    public Jedis getClient(){
        return jedis;
    }

    public void close() throws Exception {
        if(jedis != null){
            jedis.close();
        }
        jedisPool.destroy();
    }
}
