package lighting;

import java.util.ArrayList;
import java.util.List;

import geometry.Point3DH;
import geometry.Vertex3D;
import lighting.Light;
import windowing.graphics.Color;

public class Lighting {

	private Color ambient;
	private List<Light> lights = new ArrayList<Light>();
	
	public Lighting(Color ambient, List<Light> lights)
	{
		this.ambient = ambient;
		this.lights = lights;
	}
	
	public Color light(Vertex3D cameraSpacePoint, Color kDiffuse, Point3DH normal, double kSpecular, double specularExponent)
	{
		//INCORRECT NORMAL AT THIS POINT
		
		
		Color kI = kDiffuse.multiply(ambient);
		Color diffuse_intensity = Color.BLACK;
		for (Light i : lights)
		{	
			
			Point3DH V_hat = calcV(cameraSpacePoint).normalize();
			Point3DH N_hat = normal.normalize();
			Point3DH L_hat = calcL(i, cameraSpacePoint).normalize();
			Point3DH R_hat = calcR(N_hat, L_hat).normalize();

			Color Ii = i.getIntensity();
			double fatti = fatti(i, cameraSpacePoint);
			Color Ifatti = Ii.scale(fatti);
			
			double NL = N_hat.dotProduct(L_hat);
			if (NL < 0)
			{
				NL = 0;
			}
			Color kdNL = kDiffuse.scale(NL);
			Color If_kdNL = Ifatti.multiply(kdNL);
			
			double VR = V_hat.dotProduct(R_hat);
			if (VR< 0)
			{
				VR = 0;
			}
			double ksVR = kSpecular * Math.pow(VR, specularExponent);
			Color If_ksVR = Ifatti.scale(ksVR);
			
			Color light_intensity = If_kdNL.add(If_ksVR);
			diffuse_intensity = diffuse_intensity.add(light_intensity);
		}
			
		Color intensity = kI.add(diffuse_intensity);
		
		return intensity;
	}
	
	public void addLight(Light light)
	{
		lights.add(light);
	}
	
	private double fatti(Light light, Vertex3D point)
	{
		double a = light.getfattA();
		double b = light.getfattB();
		double d = distance(light, point);
		
		double fatti = 1/(a + b*d);
		
		return fatti;
	}
	
	private double distance(Light light, Vertex3D point)
	{
		double dx = light.getCameraX() - point.getCameraX(); 
		double dy = light.getCameraY() - point.getCameraY(); 
		double dz = light.getCameraZ() - point.getCameraZ();
		
		double d = Math.sqrt(dx*dx + dy*dy + dz*dz);
		
		return d;
	}
	
	private Point3DH calcV(Vertex3D poly)
	{
		double x = 0.0 - poly.getCameraX(); 
		double y = 0.0 - poly.getCameraY(); 
		double z = 0.0 - poly.getCameraZ();
		
		Point3DH v = new Point3DH(x,y,z);
		return v;
	}
	
	private Point3DH calcL(Light light, Vertex3D point)
	{
		double x = light.getCameraX() - point.getCameraX(); 
		double y = light.getCameraY() - point.getCameraY(); 
		double z = light.getCameraZ() - point.getCameraZ();

		Point3DH v = new Point3DH(x,y,z);
		return v;
	}
	
	private Point3DH calcR(Point3DH N, Point3DH L)
	{
		double NL2 = N.dotProduct(L) * 2;
		Point3DH R = N.scale(NL2);
		Point3DH RR = R.subtract(L);
		return RR;
	}
}
