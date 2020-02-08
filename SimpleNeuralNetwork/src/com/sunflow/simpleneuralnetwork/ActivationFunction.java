package com.sunflow.simpleneuralnetwork;

import java.io.Serializable;

import com.sunflow.util.SimpleMapper;

public class ActivationFunction implements Cloneable, Serializable {
	public SimpleMapper func;
	public SimpleMapper dfunc;

	public ActivationFunction(SimpleMapper func, SimpleMapper dfunc) {
		this.func = func;
		this.dfunc = dfunc;
	}

	@Override
	public ActivationFunction clone() {
		return new ActivationFunction(func, dfunc);
	}

	public static class Double {
		public SimpleMapper.Double func;
		public SimpleMapper.Double dfunc;

		public Double(SimpleMapper.Double func, SimpleMapper.Double dfunc) {
			this.func = func;
			this.dfunc = dfunc;
		}
	}

	public static class Generic<T> {
		public SimpleMapper.Generic<T> func;
		public SimpleMapper.Generic<T> dfunc;

		public Generic(SimpleMapper.Generic<T> func, SimpleMapper.Generic<T> dfunc) {
			this.func = func;
			this.dfunc = dfunc;
		}
	}
}