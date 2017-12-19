package cn.skyeye.cascade.rpc;

import cn.skyeye.cascade.CascadeContext;
import cn.skyeye.cascade.rpc.managers.handlers.MessageHandler;
import cn.skyeye.cascade.rpc.managers.handlers.RegistMessageHandler;

public enum MessageType {
    register{
        private volatile MessageHandler messageHandler;
        @Override
        public synchronized MessageHandler getHandler() {
            if(messageHandler == null)
                messageHandler = new RegistMessageHandler(CascadeContext.get());
            return messageHandler;
        }
    },
    heartbeats{
        @Override
        public synchronized MessageHandler getHandler() {
            return CascadeContext.get().getHeartbeatManager().getReceiver();
        }
    },
    esdata{
        @Override
        public synchronized MessageHandler getHandler() {
            return null;
        }
    },
    dbdata{
        @Override
        public synchronized MessageHandler getHandler() {
            return null;
        }
    },
    order{
        @Override
        public synchronized MessageHandler getHandler() {
            return null;
        }
    };

    public abstract MessageHandler getHandler();

    public static MessageType get(String type) {
        if(type != null) {
            switch (type.toLowerCase()) {
                case "register":
                    return register;
                case "heartbeats":
                    return heartbeats;
                case "esdata":
                    return esdata;
                case "dbdata":
                    return dbdata;
                case "order":
                    return order;
            }
        }
        return null;
    }
}