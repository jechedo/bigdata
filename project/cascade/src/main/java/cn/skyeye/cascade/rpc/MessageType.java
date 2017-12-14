package cn.skyeye.cascade.rpc;

public enum MessageType {

    register,heartbeats, esdata, dbdata, order;

    public static MessageType get(String type) {
        switch (type) {
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
        return null;
    }
}