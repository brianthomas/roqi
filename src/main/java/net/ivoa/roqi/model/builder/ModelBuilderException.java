package net.ivoa.roqi.model.builder;

public class ModelBuilderException 
extends Exception 
{

	private static final long serialVersionUID = -2351347754234001838L;

	public ModelBuilderException() {
	}

	public ModelBuilderException(String message) {
		super(message);
	}

	public ModelBuilderException(Throwable cause) {
		super(cause);
	}

	public ModelBuilderException(String message, Throwable cause) {
		super(message, cause);
	}

}
