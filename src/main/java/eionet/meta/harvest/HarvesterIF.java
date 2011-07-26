
package eionet.meta.harvest;


public interface HarvesterIF {

    /**
     *
     */
    public abstract void harvest() throws Exception;

    /**
     *
     */
    public abstract void cleanup();
}
