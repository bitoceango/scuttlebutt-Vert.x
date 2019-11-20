package org.sword.scuttlebutt;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @author tiyxing
 * @date 2019-11-10
 * @since 1.0.0
 */
public class EventEmitter {
    private Map<String, Set<Function>> listeners=new ConcurrentHashMap<>();

    public void onSubscribe(String event,Function function){
        listeners.computeIfAbsent(event, s -> new HashSet<>()).add(function);

    }

    public void emitListeners(String event,Object value){
        listeners.computeIfAbsent(event, s -> new HashSet<>()).forEach(function -> function.apply(value));

    }
}
