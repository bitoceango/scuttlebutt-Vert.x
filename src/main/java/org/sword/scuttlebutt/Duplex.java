package org.sword.scuttlebutt;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * @author tiyxing
 * @date 2019-11-10
 * @since 1.0.0
 */
@Slf4j
public class Duplex {

    private Scuttlebutt sb;

    private String peerId;

    public Map<String,Long> peerClock;

    private LinkedBlockingQueue buffer=new LinkedBlockingQueue(100);

    private ArrayBlockingQueue<Supplier> suppliers=new ArrayBlockingQueue<>(100);

    private static final String SYNC_CMD="sync";

    private boolean syncSent;

    private boolean syncRecv;

    public Duplex(Scuttlebutt scuttlebutt){
        this.sb=scuttlebutt;
        push(new Outgoing(sb.sourceId,sb.source));
    }


    public void onData(Object value){
        if (value instanceof Update){
            log.info("{} receive update:{}",sb.sourceId,value);
            sb.applyUpdates((Update) value);

        }else if(value instanceof String){
            if (SYNC_CMD.equals(value)){
                syncRecv=true;
                log.info("{},receive sync",sb.sourceId);
            }
        }else if (value instanceof Outgoing){
            log.info("{},receive incoming:{}",sb.sourceId,value);
            sync((Outgoing) value);

        }

    }


    public void source(boolean end,BiConsumer<Boolean,Object> cb){
        if (end){
            cb.accept(end,null);
        }
        if (buffer.isEmpty()){
            Supplier<Object> promise = () -> {
                Object poll = buffer.poll();
                cb.accept(false,poll);
                return poll;
            };
            suppliers.offer(promise);
            log.info("{} record promise",sb.sourceId);
            return;
        }
        //todo 什么是结束？
        cb.accept(false,buffer.poll());

    }


    public  static class Recursion<T>{
        public T function;
    }

    public void pullSource(BiConsumer<Boolean,BiConsumer<Boolean,Object>> consumer){
        Recursion<BiConsumer<Boolean, Object>> recursion=new Recursion<>();
        recursion.function= (end, o) -> {
            if (end){
                return;
            }
            onData(o);
            consumer.accept(false,recursion.function);
        };
        consumer.accept(false,recursion.function);

    }

    public void sync(Outgoing incoming){
        peerClock=incoming.getSource();
        peerId=incoming.getSourceId();
        Update[] history = sb.history(peerClock);
        sb.onSubscribe("update",onUpdate);
        Stream.of(history).forEach(update -> {
            update.setSourceId(sb.sourceId);
            push(update);
        });
        push(SYNC_CMD);
    }

    UnaryOperator<Update> onUpdate=new UnaryOperator<Update>() {
        @Override
        public Update apply(Update update) {
            if (peerId.equals(update.getFrom())){
                return update;
            }
            //支持双向通信
            if (peerClock.get(peerId)>update.getTimestamp()){
                return update;
            }
            update.setFrom(sb.sourceId);
            push(update);
            peerClock.putIfAbsent(update.getSourceId(),update.getTimestamp());

            return update;
        }
    };

    public void push(Object update){
        buffer.offer(update);

        if (suppliers.size()>0){
            while (suppliers.size()>0&&buffer.size()>0){
                suppliers.poll().get();
            }

            log.info("{} resolve promise",sb.sourceId);
        }

    }


}
