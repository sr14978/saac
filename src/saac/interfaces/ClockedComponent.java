package saac.interfaces;

public interface ClockedComponent {
	void tick() throws Exception;
	void tock() throws Exception;
}
