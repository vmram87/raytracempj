package xdev.niodev;

public interface ICheckpoint {
	public void preProcess();
	public void processRestart();
	public void processContinue();
}
