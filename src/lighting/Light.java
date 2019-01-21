package lighting;


import geometry.Point3DH;
import geometry.Vertex3D;
import windowing.graphics.Color;


public class Light {
	Color intensity;
	Point3DH cameraSpaceLocation;
	private double fattA;
	private double fattB;
	
	public Light(double red, double green, double blue, double fattA, double fattB, Point3DH cameraSpaceLocation) 
	{
		this.intensity = new Color(red, green, blue);
		this.fattA = fattA;
		this.fattB = fattB;
		this.cameraSpaceLocation = cameraSpaceLocation;
	}
	
	public Color getIntensity()
	{
		return intensity;
	}

	public double getfattA() 
	{
		return fattA;
	}
	
	public double getfattB() 
	{
		return fattB;
	}
	
	public double getR()
	{
		return intensity.getR();
	}
	
	public double getG()
	{
		return intensity.getG();
	}
	
	public double getB()
	{
		return intensity.getB();
	}
	
	public double getCameraX()
	{
		return cameraSpaceLocation.getX();
	}
	
	public double getCameraY()
	{
		return cameraSpaceLocation.getY();
	}
	
	public double getCameraZ()
	{
		return cameraSpaceLocation.getZ();
	}
	
	public Point3DH getCameraLocation()
	{
		return cameraSpaceLocation;
	}


	
	
}
