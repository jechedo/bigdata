package cn.skyeye.rpc.netty.transfers.messages;

import cn.skyeye.rpc.netty.protocol.Encoders;
import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/31 17:03
 */
public class JsonMessage extends TransferMessage{

    public final String appId;
    public final String execId;
    public final String jsonStr;


    public JsonMessage(
            String appId,
            String execId,
            String jsonStr) {
        this.appId = appId;
        this.execId = execId;
        this.jsonStr = jsonStr;
    }

    @Override
    protected TransferMessage.Type type() { return Type.JSON; }

    @Override
    public int hashCode() {
        return Objects.hashCode(appId, execId, jsonStr);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("appId", appId)
                .add("execId", execId)
                .add("jsonStr", jsonStr)
                .toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other != null && other instanceof JsonMessage) {
            JsonMessage o = (JsonMessage) other;
            return Objects.equal(appId, o.appId)
                    && Objects.equal(execId, o.execId)
                    && Objects.equal(jsonStr, o.jsonStr);
        }
        return false;
    }

    @Override
    public int encodedLength() {
        return Encoders.Strings.encodedLength(appId)
                + Encoders.Strings.encodedLength(execId)
                + Encoders.Strings.encodedLength(jsonStr);
    }

    @Override
    public void encode(ByteBuf buf) {
        Encoders.Strings.encode(buf, appId);
        Encoders.Strings.encode(buf, execId);
        Encoders.Strings.encode(buf, jsonStr);
    }

    public static JsonMessage decode(ByteBuf buf) {
        String appId = Encoders.Strings.decode(buf);
        String execId = Encoders.Strings.decode(buf);
        String jsonStr = Encoders.Strings.decode(buf);
        return new JsonMessage(appId, execId, jsonStr);
    }
}
