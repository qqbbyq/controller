/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.impl.ofRpcs.cache;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

/**
 * Created by cmcc on 2017/8/5.
 */
public class RedisCacheService implements AutoCloseable{

    private static final String SPLITOR = "&";
    private Jedis jedis;
    private RedisClient redisClient;
    private static final String OF_NODES = "openflowNodes";
    private static final String OF_NODE_FLOWS = "flows";
    private static final String OF_NODE_GROUPS = "groups";
    private static final String OF_NODE_METERS = "meters";

    private RedisCacheService(String ipStr, Integer port, String password, Integer database){
        redisClient = new RedisClient(ipStr, port, password, database);
        jedis = redisClient.getClient();
    }

    public static RedisCacheService createInstance(String ipStr, Integer port, String password, Integer database){
        return new RedisCacheService(ipStr, port, password, database);
    }

    public static RedisCacheService createInstance(String ipStr, String password, Integer port){
        return new RedisCacheService(ipStr, port, password, 0);
    }


    @Override
    public void close() throws Exception {
        redisClient.close();
    }

    public void addFlowEntry(String nodeId, String tableFlowId, byte[] flowBytes) {
        String nodeFlowKey = generateNodeFlowsKey(nodeId);

        Boolean nodeExists = jedis.exists(nodeId);
        if(!nodeExists){
            jedis.lpush(OF_NODES, nodeId);
        }

        Boolean nodeflowExists = jedis.hexists(nodeId, OF_NODE_FLOWS);
        if(!nodeflowExists){
            jedis.lpush(nodeId, OF_NODE_FLOWS, nodeFlowKey);
        }

        Transaction tx =jedis.multi();
        tx.set(tableFlowId.getBytes(), flowBytes);
        tx.sadd(nodeFlowKey, tableFlowId);
        tx.exec();
    }

    private String generateNodeFlowsKey(String nodeId) {
        return nodeId.concat(SPLITOR).concat(OF_NODE_FLOWS);
    }

    private String generateNodeGroupsKey(String nodeId) {
        return nodeId.concat(SPLITOR).concat(OF_NODE_GROUPS);
    }

    private String generateNodeMetersKey(String nodeId) {
        return nodeId.concat(SPLITOR).concat(OF_NODE_METERS);
    }

    public void delFlowEntry(String nodeId, String tableFlowId){
        if(!jedis.exists(nodeId))
            return;
        if(!jedis.hexists(nodeId, OF_NODE_FLOWS))
            return;
        String nodeFlowKey = generateNodeFlowsKey(nodeId);
        Transaction tx =jedis.multi();
        tx.del(tableFlowId.getBytes());
        tx.srem(nodeFlowKey, tableFlowId);
        tx.exec();
    }

    public void delFlowEntries(String nodeId, List<String> removeFlowIds) {
        if(!jedis.exists(nodeId))
            return;
        if(!jedis.hexists(nodeId, OF_NODE_FLOWS))
            return;
        String nodeFlowKey = generateNodeFlowsKey(nodeId);
        String[] flowIDs = new String[removeFlowIds.size()];
        byte[][] flowKeyBytes = new byte[removeFlowIds.size()][];
        for(int i = 0; i < removeFlowIds.size();i++){
            String flowId = removeFlowIds.get(i);
            flowIDs[i] = flowId;
            flowKeyBytes[i]= flowId.getBytes();
        }
        Transaction tx =jedis.multi();
        tx.del(flowKeyBytes);
        tx.srem(nodeFlowKey, flowIDs);
        tx.exec();
    }

    public void addGroupEntry(String nodeId, String groupId, byte[] groupBytes) {
        String nodeGroupKey = generateNodeGroupsKey(nodeId);

        Boolean nodeExists = jedis.exists(nodeId);
        if(!nodeExists){
            jedis.lpush(OF_NODES, nodeId);
        }

        Boolean nodeflowExists = jedis.hexists(nodeId, OF_NODE_GROUPS);
        if(!nodeflowExists){
            jedis.lpush(nodeId, OF_NODE_GROUPS, nodeGroupKey);
        }

        Transaction tx =jedis.multi();
        tx.set(groupId.getBytes(), groupBytes);
        tx.sadd(nodeGroupKey, groupId);
        tx.exec();
    }

    public void delGroupEntries(String nodeId, List<Long> removeGroupIds) {
        if(!jedis.exists(nodeId))
            return;
        if(!jedis.hexists(nodeId, OF_NODE_GROUPS))
            return;
        String nodeGroupKey = generateNodeGroupsKey(nodeId);
        String[] groupIDs = new String[removeGroupIds.size()];
        byte[][] groupKeyBytes = new byte[removeGroupIds.size()][];
        for(int i = 0; i < removeGroupIds.size();i++){
            String groupId = String.valueOf(removeGroupIds.get(i));
            groupIDs[i] = groupId;
            groupKeyBytes[i]= groupId.getBytes();
        }
        Transaction tx =jedis.multi();
        tx.del(groupKeyBytes);
        tx.srem(nodeGroupKey, groupIDs);
        tx.exec();
    }

    public void delMeterEntries(String nodeId, List<Long> removeMeterIds) {
        if(!jedis.exists(nodeId))
            return;
        if(!jedis.hexists(nodeId, OF_NODE_METERS))
            return;

        String nodeMeterKey = generateNodeGroupsKey(nodeId);
        String[] meterIDs = new String[removeMeterIds.size()];
        byte[][] meterKeyBytes = new byte[removeMeterIds.size()][];
        for(int i = 0; i < removeMeterIds.size();i++){
            String meterId = String.valueOf(removeMeterIds.get(i));
            meterIDs[i] = meterId;
            meterKeyBytes[i]= meterId.getBytes();
        }
        Transaction tx =jedis.multi();
        tx.del(meterKeyBytes);
        tx.srem(nodeMeterKey, meterIDs);
        tx.exec();
    }

    public void addMeterEntry(String nodeId, String meterId, byte[] meterBytes) {
        String nodeMeterKey = generateNodeMetersKey(nodeId);

        Boolean nodeExists = jedis.exists(nodeId);
        if(!nodeExists){
            jedis.lpush(OF_NODES, nodeId);
        }

        Boolean nodeflowExists = jedis.hexists(nodeId, OF_NODE_METERS);
        if(!nodeflowExists){
            jedis.lpush(nodeId, OF_NODE_METERS, nodeMeterKey);
        }

        Transaction tx =jedis.multi();
        tx.set(meterId.getBytes(), meterBytes);
        tx.sadd(nodeMeterKey, meterId);
        tx.exec();
    }


}
