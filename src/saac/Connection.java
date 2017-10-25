package saac;

public class Connection<T> {
	
	private T value;
	
	Input getInputEnd() throws Exception {
		return new Input();
	}
	
	Output getOutputEnd() throws Exception {
		return new Output();
	}
	
	class Input {
		void put(T val) {
			value = val;
		}
	}
	
	class Output {
		T get() {
			return value;
		}
	}
	
}
