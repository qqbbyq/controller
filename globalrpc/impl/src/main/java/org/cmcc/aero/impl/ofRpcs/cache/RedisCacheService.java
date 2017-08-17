/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.impl.ofRpcs.cache;

import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

import org.cmcc.aero.impl.utils.ConstantsUtils;

/**
 * Created by cmcc on 2017/8/5.
 */
public class RedisCacheService implements AutoCloseable{
    private Jedis jedis;
    private RedisClient redisClient;

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

        String flowUniqKey = generateFlowUniqKey(nodeId, tableFlowId);
        Transaction tx =jedis.multi();
        tx.set(flowUniqKey.getBytes(), flowBytes);
        tx.sadd(nodeFlowKey, flowUniqKey);
        tx.exec();
    }

    private String generateNodeFlowsKey(String nodeId) {
        return nodeId.concat(ConstantsUtils.SPLITOR).concat(ConstantsUtils.OF_NODE_FLOWS);
    }

    private String generateNodeGroupsKey(String nodeId) {
        return nodeId.concat(ConstantsUtils.SPLITOR).concat(ConstantsUtils.OF_NODE_GROUPS);
    }

    private String generateNodeMetersKey(String nodeId) {
        return nodeId.concat(ConstantsUtils.SPLITOR).concat(ConstantsUtils.OF_NODE_METERS);
    }

    public void delFlowEntry(String nodeId, String tableFlowId){
        String nodeFlowKey = generateNodeFlowsKey(nodeId);
        String uniqFlowKey = generateFlowUniqKey(nodeId, tableFlowId);
        Transaction tx =jedis.multi();
        tx.del(uniqFlowKey.getBytes());
        tx.srem(nodeFlowKey, uniqFlowKey);
        tx.exec();
    }

    public void delFlowEntries(String nodeId, List<String> removeFlowIds) {
        String nodeFlowKey = generateNodeFlowsKey(nodeId);
        String[] flowIDs = new String[removeFlowIds.size()];
        byte[][] flowKeyBytes = new byte[removeFlowIds.size()][];
        for(int i = 0; i < removeFlowIds.size();i++){
            String tableFlowId = removeFlowIds.get(i);
            flowIDs[i] = generateFlowUniqKey(nodeId, tableFlowId);
            flowKeyBytes[i]= flowIDs[i].getBytes();
        }
        Transaction tx =jedis.multi();
        tx.del(flowKeyBytes);
        tx.srem(nodeFlowKey, flowIDs);
        tx.exec();
    }

    public void addGroupEntry(String nodeId, String groupId, byte[] groupBytes) {
        String nodeGroupKey = generateNodeGroupsKey(nodeId);

        String groupUniqKey = generateGroupUniqKey(nodeId, groupId);
        Transaction tx =jedis.multi();
        tx.set(groupUniqKey.getBytes(), groupBytes);
        tx.sadd(nodeGroupKey, groupUniqKey);
        tx.exec();
    }

    private String generateGroupUniqKey(String nodeId, String groupId){
        return generateUniqKey(nodeId, ConstantsUtils.OF_NODE_GROUPS, groupId);
    }

    private String generateMeterUniqKey(String nodeId, String meterId){
        return generateUniqKey(nodeId, ConstantsUtils.OF_NODE_METERS, meterId);
    }

    private String generateFlowUniqKey(String nodeId, String tableFlowId){
        return generateUniqKey(nodeId, ConstantsUtils.OF_NODE_FLOWS, tableFlowId);
    }

    private String generateUniqKey(String... keys) {
        return StringUtils.join(keys, ConstantsUtils.SPLITOR);
    }

    public void delGroupEntries(String nodeId, List<Long> removeGroupIds) {
        String nodeGroupKey = generateNodeGroupsKey(nodeId);
        String[] groupIDs = new String[removeGroupIds.size()];
        byte[][] groupKeyBytes = new byte[removeGroupIds.size()][];
        for(int i = 0; i < removeGroupIds.size();i++){
            String groupId = String.valueOf(removeGroupIds.get(i));
            groupIDs[i] = generateGroupUniqKey(nodeId, groupId);
            groupKeyBytes[i]= groupIDs[i].getBytes();
        }
        Transaction tx =jedis.multi();
        tx.del(groupKeyBytes);
        tx.srem(nodeGroupKey, groupIDs);
        tx.exec();
    }

    public void delMeterEntries(String nodeId, List<Long> removeMeterIds) {
        String nodeMeterKey = generateNodeGroupsKey(nodeId);
        String[] meterIDs = new String[removeMeterIds.size()];
        byte[][] meterKeyBytes = new byte[removeMeterIds.size()][];
        for(int i = 0; i < removeMeterIds.size();i++){
            String meterId = String.valueOf(removeMeterIds.get(i));
            meterIDs[i] = generateMeterUniqKey(nodeId, meterId);
            meterKeyBytes[i]= meterIDs[i].getBytes();
        }
        Transaction tx =jedis.multi();
        tx.del(meterKeyBytes);
        tx.srem(nodeMeterKey, meterIDs);
        tx.exec();
    }

    public void addMeterEntry(String nodeId, String meterId, byte[] meterBytes) {
        String nodeMeterKey = generateNodeMetersKey(nodeId);
        String meterUniqKey = generateMeterUniqKey(nodeId, meterId);
        Transaction tx =jedis.multi();
        tx.set(meterUniqKey.getBytes(), meterBytes);
        tx.sadd(nodeMeterKey, meterUniqKey);
        tx.exec();
    }


    public byte[] getGroupEntry(String nodeId, String groupId) {
        String uniqGroupKey = generateGroupUniqKey(nodeId, groupId);
        return jedis.get(uniqGroupKey.getBytes());
    }

    public byte[] getFlowEntry(String nodeId, String tableFlowId) {
        String uniqFlowKey = generateFlowUniqKey(nodeId, tableFlowId);
        return jedis.get(uniqFlowKey.getBytes());
    }

    public byte[] getMeterEntry(String nodeId, String meterId) {
        String uniqMeterKey = generateMeterUniqKey(nodeId, meterId);
        return jedis.get(uniqMeterKey.getBytes());
    }
}
