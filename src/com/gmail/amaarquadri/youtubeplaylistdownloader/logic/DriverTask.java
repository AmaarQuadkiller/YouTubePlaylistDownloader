package com.gmail.amaarquadri.youtubeplaylistdownloader.logic;

/**
 * Created by Amaar on 2017-08-23.
 */
public abstract class DriverTask {
    protected final Data data; //TODO: address repetitive calls to data object
    protected MessageAcceptor messageAcceptor;
    protected boolean cancel;

    public DriverTask(Data data, MessageAcceptor messageAcceptor) {
        this.data = data;
        if (messageAcceptor == null) this.messageAcceptor = MessageAcceptor.EMPTY;
        else this.messageAcceptor = MessageAcceptor.addAcceptCondition(messageAcceptor, () -> !cancel);
    }

    //@CallSuper
    public void cancel() {
        cancel = true;
    }

    /**
     * @return Whether or not the method call was successful.
     */
    protected final boolean waitForDriver() {
        while (true) {
            if (DriverUtils.isDriverReady()) return true;
            if (cancel) return false;
            try {
                Thread.sleep(100);//TODO: configure millis to wait
            }
            catch (InterruptedException e) {
                return false;
            }
        }
    }

    public abstract void execute();
}
