package saac;

public class FConnection<T> {

	private T value;
	private boolean givenInputEnd;
	private boolean givenOutputEnd;
	
	Input getInputEnd() throws Exception {
		if(givenInputEnd)
			throw new Exception();
		else
			return new Input();
	}
	
	Output getOutputEnd() throws Exception {
		if(givenOutputEnd)
			throw new Exception();
		else
			return new Output();
	}
	
	class Input {
		void put(T val) throws FullChannelException {
			if(value == null)
				value = val;
			else
				throw new FullChannelException();
		}
		boolean clear() {
			return value == null;
		}
	}
	
	class Output {
		T get() throws FullChannelException {
			if(value == null) {
				throw new FullChannelException();
			} else {
				T val = value;
				value = null;
				return val;
			}
			
		}
		boolean ready() {
			return value != null;
		}
	}
	
	
}
