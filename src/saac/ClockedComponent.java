package saac;

interface ClockedComponent {
	void tick() throws Exception;
	void tock() throws Exception;
}
