package manipulator;

public class IncorrectFileException extends Exception {
	
	public IncorrectFileException(String errorMessage, Throwable err) {
		super(errorMessage, err);
	}
	
}
