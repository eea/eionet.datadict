package eionet.meta.exports.xforms;

public class InstanceDOMException extends Throwable {

    public InstanceDOMException(String msg) {
        super(msg);
    }

    public InstanceDOMException(Throwable t) {
        super(t.toString());
    }
}
