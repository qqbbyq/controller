package org.cmcc.aero.impl.rpc.server;

import org.cmcc.aero.impl.rpc.message.RpcTask;
import org.cmcc.aero.impl.rpc.protocol.Event;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by zhuyuqing on 2017/8/11.
 */

public class RpcServiceState implements Serializable {

  private static final long serialVersionUID = 1L;
  private final Queue<Event> events;

  public RpcServiceState() {
    this(new LinkedList<>());
  }

  public RpcServiceState(Queue<Event> events) {
    this.events = events;
  }

  public RpcServiceState copy() {
    return new RpcServiceState(new LinkedList<>(events));
  }

  public void offer(Event task) {
    events.offer(task);
  }

  public void poll(String taskId) throws Exception {
    RpcTask event = (RpcTask) events.peek();
    if(event.getTaskId().equals(taskId))
      events.poll();
    else throw new Exception("unreasonable poll,peek=" + events.peek() + ",but poll.taskId=" + taskId);
  }

  public int size() {
    return events.size();
  }

  public Queue<Event> getEvents() {
    return this.events;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("RpcServiceState(");
    for(int i = 0; i < events.size(); ++i) {
      builder.append(events.toArray()[i].toString());
      if(i < events.size() -1 ) builder.append(",");
    }
    builder.append(")");
    return builder.toString();
  }
}
