package com.sunflow.simpleneuralnetwork.convolutional;

public class NNError extends Exception {
	private static final long serialVersionUID = -1042271036335736550L;

	public NNError() {
		super();
	}

	public NNError(String message) {
		super(message);
	}

	public NNError(String message, Throwable cause) {
		super(message, cause);
	}

	public NNError(Throwable cause) {
		super(cause);
	}

	protected NNError(String message, Throwable cause,
			boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
