package cn.wjb114514.customProtocol.message;

public class PongMessage extends Message {
    @Override
    public int getMessageType() {
        return PongMessage;
    }
}
