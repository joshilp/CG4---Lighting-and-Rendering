package shading;

import windowing.graphics.Color;

@FunctionalInterface
public interface Shader {
	public Color shade(Color c);
}
