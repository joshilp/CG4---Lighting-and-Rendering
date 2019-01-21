package line;

import geometry.Point3DH;
import geometry.Vertex3D;
import polygon.Polygon;
import shading.PixelShader;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

public class DDALineRenderer implements LineRenderer {

	private DDALineRenderer() {
	}

	@Override
	public void drawLine(Vertex3D p1, Vertex3D p2, Drawable drawable, Polygon polygon, PixelShader pixelShader) {

		double deltaX = p2.getIntX() - p1.getIntX();
		double deltaY = p2.getIntY() - p1.getIntY();
		double deltaZ = 1 / p2.getZ() - 1 / p1.getZ();

		double slopeY = deltaY / deltaX;
		double slopeZ = deltaZ / deltaX;
		
		
		double deltaR = p2.getColor().getR() / p2.getZ() - p1.getColor().getR() / p1.getZ();
		double deltaG = p2.getColor().getG() / p2.getZ() - p1.getColor().getG() / p1.getZ();
		double deltaB = p2.getColor().getB() / p2.getZ() - p1.getColor().getB() / p1.getZ();

		double slopeR = deltaR / deltaX;
		double slopeG = deltaG / deltaX;
		double slopeB = deltaB / deltaX;
		
		
		double deltaCX =  p2.getCameraX() / p2.getZ() - p1.getCameraX() / p1.getZ();
		double deltaCY =  p2.getCameraY() / p2.getZ() - p1.getCameraY() / p1.getZ();
		double deltaCZ =  1 / p2.getCameraZ() - 1 / p1.getCameraZ();
		
		double slopeCX = deltaCX / deltaX;
		double slopeCY = deltaCY / deltaX;
		double slopeCZ = deltaCZ / deltaX;
		

		double deltaNX = p2.getNormal().getX() / p2.getZ() - p1.getNormal().getX() / p1.getZ();
		double deltaNY = p2.getNormal().getY() / p2.getZ() - p1.getNormal().getY() / p1.getZ();
		double deltaNZ = p2.getNormal().getZ() / p2.getZ() - p1.getNormal().getZ() / p1.getZ();
		
//		System.out.println(deltaNX);
		
		double slopeNX = deltaNX / deltaX;
		double slopeNY = deltaNY / deltaX;
		double slopeNZ = deltaNZ / deltaX;

		
		
		
		double y = p1.getIntY();
		double z = 1 / p1.getZ();

		double r = p1.getColor().getR() / p1.getZ();
		double g = p1.getColor().getG() / p1.getZ();
		double b = p1.getColor().getB() / p1.getZ();
		
		double cx = p1.getCameraX()/p1.getZ();
		double cy = p1.getCameraY()/p1.getZ();
		double cz = 1 / p1.getCameraZ();		
		
		double nx = p1.getNormal().getX() / p1.getZ();
		double ny = p1.getNormal().getY() / p1.getZ();
		double nz = p1.getNormal().getZ() / p1.getZ();


		for (int x = p1.getIntX(); x <= p2.getIntX(); x++) 
		{
			Color rgb = new Color(r/z, g/z, b/z);
			
			Vertex3D cameraPoint =  new Vertex3D(cx/z, cy/z, 1.0/z, p1.getColor());
			Point3DH normal =  new Point3DH(nx/z, ny/z, nz/z);
			
			cameraPoint.setNormal(normal);
			
			if (p1.fromObject())
			{
				cameraPoint.isObject();
			}
			
			if (p1.isPhong()) 
			{
				rgb = pixelShader.shade(polygon, cameraPoint);
			}

			
			drawable.setPixel(x, (int) Math.round(y), 1.0/z, rgb.asARGB());
			
			y = y + slopeY;
			z = z + slopeZ;
			
			r = r + slopeR;
			g = g + slopeG;
			b = b + slopeB;
			
			cx = cx + slopeCX;
			cy = cy + slopeCY;
			cz = cz + slopeCZ;
			
			nx = nx + slopeNX;
			ny = ny + slopeNY;
			nz = nz + slopeNZ;
			
		}
	}

	public static LineRenderer make() {
		return new AnyOctantLineRenderer(new DDALineRenderer());
	}
}
