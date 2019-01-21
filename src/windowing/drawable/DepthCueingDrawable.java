package windowing.drawable;

import geometry.Vertex3D;
import polygon.Polygon;
import windowing.graphics.Color;

public class DepthCueingDrawable extends DrawableDecorator {

	private int row;
	private int col;
	private double[][] zbuffer;
	private double near;
	private double far;
	private Color color;

	public DepthCueingDrawable(Drawable delegate, int near, int far, Color color) 
	{
		super(delegate);
		this.color = color;
		this.row = delegate.getHeight();
		this.col = delegate.getWidth();
		this.zbuffer = new double[row][col];

		this.near = near;
		this.far = far;
		reset_z();
	}

	@Override
	public void clear() 
	{
		fill(ARGB_BLACK, Double.MAX_VALUE);
		reset_z();
	}

	@Override
	public void setPixel(int x, int y, double z, int argbColor) 
	{
		Color depth_color = color;
		Color lighting_calculation = Color.fromARGB(argbColor);

		if (z >= near) 
		{
			delegate.setPixel(x, y, z, lighting_calculation.asARGB());
		}

		if (z <= far) 
		{
			delegate.setPixel(x, y, z, depth_color.asARGB());
		}

		if (near >= z && z >= far) 
		{
			double r = (z - far) / (near - far);
			lighting_calculation = lighting_calculation.scale(r).add(depth_color.scale(1 - r));

			if ((x > 0 && x < col) && (y > 0 && y < row)) 
			{
				if (z <= near && z >= zbuffer[y][x]) 
				{
					delegate.setPixel(x, y, z, lighting_calculation.asARGB());
					zbuffer[y][x] = z;
				}
			}
		}

	}

	private void reset_z() 
	{
		for (int y = 0; y < row; y++) {
			for (int x = 0; x < col; x++) {
				zbuffer[y][x] = far;
			}
		}
	}
	
}
