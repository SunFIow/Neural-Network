package com.sunflow.simpleneuralnetwork.convolutional;

public class PredictionError extends NNError {
	private static final long serialVersionUID = 7223724220841010150L;

	public PredictionError() {
		super();
	}

	public PredictionError(String message) {
		super(message);
	}

	public PredictionError(String message, Throwable cause) {
		super(message, cause);
	}

	public PredictionError(Throwable cause) {
		super(cause);
	}

	protected PredictionError(String message, Throwable cause,
			boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
