package utils.cmds.messages;

import org.reflections.Reflections;
import utils.cmds.exception.DecodeFailException;
import utils.cmds.exception.ThisCannotOccurException;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * cmds
 * Created by yeti on 16/3/5.
 */
public abstract class DefaultMessage implements Message {

    private static final HashMap<Byte, Method> MessageCreateMethodMaps = new HashMap<>();
    public static AtomicLong Counter = new AtomicLong(Long.MAX_VALUE - 1);

    static {
        new Reflections("")
            .getSubTypesOf(DefaultMessage.class)
            .stream()
            .forEach(type -> {
                try {
                    DefaultMessage message = type.newInstance();
                    MessageCreateMethodMaps.put(message.format(), type.getMethod("from", byte[].class));
                } catch (Throwable throwable) {
                    throw new ThisCannotOccurException();
                }
            });
    }

    protected long id;

    public DefaultMessage() {
        id = Counter.getAndUpdate(operand -> operand < Long.MAX_VALUE ? operand + 1 : 0);
    }

    public static DefaultMessage from(byte format, byte[] data) {
        try {
            Method method = MessageCreateMethodMaps.get(format);
            if (method == null) {
                throw new DecodeFailException("format doesn't existed.");
            }

            return (DefaultMessage) method.invoke(null, data);
        } catch (DecodeFailException ex) {
            throw ex;
        } catch (Throwable throwable) {
            throw new DecodeFailException(String.format("data can't decode by format[%d]", format));
        }
    }

    public long id() {
        return id;
    }
}