package polygon;

import java.awt.Panel;

import client.interpreter.SimpInterpreter.ShaderStyle;
import geometry.Point3DH;
import geometry.Vertex3D;
import line.DDALineRenderer;
import line.LineRenderer;
import shading.FaceShader;
import shading.PixelShader;
import shading.Shader;
import shading.VertexShader;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

public class ColorPolygonRenderer implements PolygonRenderer {
	
	
	// for XYX
	private static int X = 0;
	private static int Y = 1;
	private static int Z = 2;

	private static int DX = 0;
	private static int DY = 1;
	private static int DZ = 2;
	
	private static int CX = 0;
	private static int CY = 1;
	private static int CZ = 2;
	
	private static int NX = 0;
	private static int NY = 1;
	private static int NZ = 2;
	
	private static int MX = 0;
	private static int MZ = 1;
	
	
	// for RGB
	private static int R = 0;
	private static int G = 1;
	private static int B = 2;

	private static int DR = 0;
	private static int DG = 1;
	private static int DB = 2;

	private static int MR = 0;
	private static int MG = 1;
	private static int MB = 2;
	
	private static int MCX = 0;
	private static int MCY = 1;
	private static int MCZ = 2;
	
	private static int MNX = 0;
	private static int MNY = 1;
	private static int MNZ = 2;
	

	Polygon polygon;
	FaceShader faceShader;
	VertexShader vertexShader;
	PixelShader pixelShader;
	
	
	private ColorPolygonRenderer() {
	}

	@Override
	public void drawPolygon(Polygon poly, Drawable drawable, FaceShader faceShader, VertexShader vertexShader, PixelShader pixelShader) {
		
		this.polygon = poly;
		this.faceShader = faceShader;
		this.vertexShader = vertexShader;
		this.pixelShader = pixelShader;
		
		
		
		LineRenderer DDAdrawer = DDALineRenderer.make();
		
		
		polygon = faceShader.shade(polygon);
		
		
		
		
		
		Chain LChain = polygon.leftChain();
		Chain RChain = polygon.rightChain();

		int lengthL = LChain.numVertices;
		int lengthR = RChain.numVertices;
		
		if ((lengthL + lengthR) >= 3) {

			Vertex3D p0 = vertexShader.shade(polygon, RChain.vertices.get(0));
			Vertex3D p1 = vertexShader.shade(polygon, LChain.vertices.get(1));
			Vertex3D p2 = vertexShader.shade(polygon, RChain.vertices.get(1));
			
			
			
//			Vertex3D p0 = RChain.vertices.get(0);
//			Vertex3D p1 = LChain.vertices.get(1);
//			Vertex3D p2 = RChain.vertices.get(1);
			
			//Setup for XYZ interpolation
			double[] p0_xyz = get_xyz(p0);
			double[] p1_xyz = get_xyz(p1);
			double[] p2_xyz = get_xyz(p2);

			double[] d_xyz_left = get_d_xyz(p0, p1);
			double[] d_xyz_right = get_d_xyz(p2, p0);
			double[] d_xyz_low = get_d_xyz(p1, p2);
			
			double[] m_xyz_left = get_m_xyz(p0, p1);
			double[] m_xyz_right = get_m_xyz(p2, p0);
			double[] m_xyz_low = get_m_xyz(p1, p2);
			
			double y_middle = Math.max(p1_xyz[Y], p2_xyz[Y]);
			double y_bottom = Math.min(p1_xyz[Y], p2_xyz[Y]);
			
			double fx_left = p0_xyz[X];
			double fx_right = p0_xyz[X];
			double fz_left = p0_xyz[Z];
			double fz_right = p0_xyz[Z];
			
			int xleft = (int) p0_xyz[X];
			int xright = (int) p0_xyz[X];
			
			

			//Setup for RGB interpolation
			double[] p0_rgb = get_rgb(p0);
			double[] p1_rgb = get_rgb(p1);
			double[] p2_rgb = get_rgb(p2);
			
			double[] d_rgb_left = get_d_rgb(p0, p1);
			double[] d_rgb_right = get_d_rgb(p2, p0);
			double[] d_rgb_low = get_d_rgb(p1, p2);
			
			double[] m_rgb_left = get_m_rgb(p0, p1);
			double[] m_rgb_right = get_m_rgb(p2, p0);
			double[] m_rgb_low = get_m_rgb(p1, p2);

			double[] rgb_left = get_rgb(p0);
			double[] rgb_right = get_rgb(p0);
			
			
			
			//Setup for Camera XYZ interpolation
			double[] p0_camera_xyz = get_camera(p0);
			double[] p1_camera_xyz = get_camera(p1);
			double[] p2_camera_xyz = get_camera(p2);

			double[] d_camera_left = get_d_camera(p0, p1);
			double[] d_camera_right = get_d_camera(p2, p0);
			double[] d_camera_low = get_d_camera(p1, p2);
			
			double[] m_camera_left = get_m_camera(p0, p1);
			double[] m_camera_right = get_m_camera(p2, p0);
			double[] m_camera_low = get_m_camera(p1, p2);
			
			double[] camera_left = get_camera(p0);
			double[] camera_right = get_camera(p0);
			
			
			
			//Setup for Normal interpolation
			double[] p0_normal_xyz = get_normal(p0);
			double[] p1_normal_xyz = get_normal(p1);
			double[] p2_normal_xyz = get_normal(p2);
			
			double[] d_normal_left = get_d_normal(p0, p1);
			double[] d_normal_right = get_d_normal(p2, p0);
			double[] d_normal_low = get_d_normal(p1, p2);
			
			double[] m_normal_left = get_m_normal(p0, p1);
			double[] m_normal_right = get_m_normal(p2, p0);
			double[] m_normal_low = get_m_normal(p1, p2);

			double[] normal_left = get_normal(p0);
			double[] normal_right = get_normal(p0);
			
			
			
			
			
			if (d_xyz_left[DY] == 0) 
			{
				
				fx_left = p1_xyz[X];
				fz_left = p1_xyz[Z];
				
				rgb_left[R] = p1_rgb[R];
				rgb_left[G] = p1_rgb[G];
				rgb_left[B] = p1_rgb[B];
				
				camera_left[X] = p1_camera_xyz[X];
				camera_left[Y] = p1_camera_xyz[Y];
				camera_left[Z] = p1_camera_xyz[Z];
				
				normal_left[X] = p1_normal_xyz[X];
				normal_left[Y] = p1_normal_xyz[Y];
				normal_left[Z] = p1_normal_xyz[Z];
			}

			
			if (d_xyz_right[DY] == 0) 
			{
				fx_right = p2_xyz[X];
				fz_right = p2_xyz[Z];
				
				rgb_right[R] = p2_rgb[R];
				rgb_right[G] = p2_rgb[G];
				rgb_right[B] = p2_rgb[B];
				
				camera_right[X] = p2_camera_xyz[X];
				camera_right[Y] = p2_camera_xyz[Y];
				camera_right[Z] = p2_camera_xyz[Z];
				
				normal_right[X] = p2_normal_xyz[X];
				normal_right[Y] = p2_normal_xyz[Y];
				normal_right[Z] = p2_normal_xyz[Z];
			}
			
			

			for (int y = (int) p0_xyz[Y]; y > y_bottom; y--) 
			{
				
				xleft = (int) Math.round(fx_left);
				xright = (int) Math.round(fx_right);
				
				if (xleft <= xright - 1)
				{
					
					Color color_left = new Color(	rgb_left[R] / fz_left,
													rgb_left[G] / fz_left, 
													rgb_left[B] / fz_left	);
					
					Color color_right = new Color(	rgb_right[R] / fz_right,
													rgb_right[G] / fz_right,
													rgb_right[B] / fz_right	);
	
					Vertex3D vertex_left = new Vertex3D(xleft, y, 1.0/fz_left, color_left);
					Vertex3D vertex_right = new Vertex3D(xright-1, y, 1.0/fz_right, color_right);
					
					
					
					Point3DH camerapoint_left = new Point3DH(	camera_left[X] / fz_left,
																camera_left[Y] / fz_left, 
																camera_left[Z] / fz_left);
					
					Point3DH camerapoint_right = new Point3DH(	camera_right[X] / fz_right,
																camera_right[Y] / fz_right, 
																camera_right[Z] / fz_right);
					
					Point3DH normalpoint_left = new Point3DH(	normal_left[X],
																normal_left[Y], 
																normal_left[Z]);

					Point3DH normalpoint_right = new Point3DH(	normal_right[X],
																normal_right[Y],
																normal_right[Z]);
					
					
					vertex_left.setCameraPoint(camerapoint_left);
					vertex_right.setCameraPoint(camerapoint_right);
					
					vertex_left.setNormal(normalpoint_left);
					vertex_right.setNormal(normalpoint_right);
					
					
					if (p0.isPhong()) 
					{
						vertex_left.renderPhong();
						vertex_right.renderPhong();					
					}
					
					if (p1.fromObject())
					{
						vertex_left.isObject();
						vertex_right.isObject();
					}
					
					DDAdrawer.drawLine(vertex_left, vertex_right, drawable, polygon, pixelShader);
				}

				if (y > y_middle) 
				{
					fx_left -= m_xyz_left[MX];
					fz_left -= m_xyz_left[MZ];
					fx_right -= m_xyz_right[MX];
					fz_right -= m_xyz_right[MZ];

					rgb_left[R] -= m_rgb_left[MR];
					rgb_left[G] -= m_rgb_left[MG];
					rgb_left[B] -= m_rgb_left[MB];

					rgb_right[R] -= m_rgb_right[MR];
					rgb_right[G] -= m_rgb_right[MG];
					rgb_right[B] -= m_rgb_right[MB];
					
					camera_left[X] -= m_camera_left[MCX];
					camera_left[Y] -= m_camera_left[MCY];
					camera_left[Z] -= m_camera_left[MCZ];
					
					camera_right[X] -= m_camera_right[MCX];
					camera_right[Y] -= m_camera_right[MCY];
					camera_right[Z] -= m_camera_right[MCZ];
					
					normal_left[X] -= m_normal_left[MNX];
					normal_left[Y] -= m_normal_left[MNY];
					normal_left[Z] -= m_normal_left[MNZ];

					normal_right[X] -= m_normal_right[MNX];
					normal_right[Y] -= m_normal_right[MNY];
					normal_right[Z] -= m_normal_right[MNZ];

				}

				if (y <= y_middle && p1_xyz[Y] > p2_xyz[Y]) 
				{
					fx_left -= m_xyz_low[MX];
					fz_left -= m_xyz_low[MZ];
					fx_right -= m_xyz_right[MX];
					fz_right -= m_xyz_right[MZ];

					rgb_left[R] -= m_rgb_low[MR];
					rgb_left[G] -= m_rgb_low[MG];
					rgb_left[B] -= m_rgb_low[MB];

					rgb_right[R] -= m_rgb_right[MR];
					rgb_right[G] -= m_rgb_right[MG];
					rgb_right[B] -= m_rgb_right[MB];
					
					camera_left[CX] -= m_camera_low[MCX];
					camera_left[CY] -= m_camera_low[MCY];
					camera_left[CZ] -= m_camera_low[MCZ];

					camera_right[CX] -= m_camera_right[MCX];
					camera_right[CY] -= m_camera_right[MCY];
					camera_right[CZ] -= m_camera_right[MCZ];
					
					normal_left[NX] -= m_normal_low[MNX];
					normal_left[NY] -= m_normal_low[MNY];
					normal_left[NZ] -= m_normal_low[MNZ];

					normal_right[NX] -= m_normal_right[MNX];
					normal_right[NY] -= m_normal_right[MNY];
					normal_right[NZ] -= m_normal_right[MNZ];

				}

				if (y <= y_middle && p1_xyz[Y] < p2_xyz[Y]) 
				{
					fx_left -= m_xyz_left[MX];
					fz_left -= m_xyz_left[MZ];
					fx_right -= m_xyz_low[MX];
					fz_right -= m_xyz_low[MZ];
					
					rgb_left[R] -= m_rgb_left[MR];
					rgb_left[G] -= m_rgb_left[MG];
					rgb_left[B] -= m_rgb_left[MB];

					rgb_right[R] -= m_rgb_low[MR];
					rgb_right[G] -= m_rgb_low[MG];
					rgb_right[B] -= m_rgb_low[MB];
					
					camera_left[CX] -= m_camera_left[MCX];
					camera_left[CY] -= m_camera_left[MCY];
					camera_left[CZ] -= m_camera_left[MCZ];

					camera_right[CX] -= m_camera_low[MCX];
					camera_right[CY] -= m_camera_low[MCY];
					camera_right[CZ] -= m_camera_low[MCZ];
					
					normal_left[NX] -= m_normal_left[MNX];
					normal_left[NY] -= m_normal_left[MNY];
					normal_left[NZ] -= m_normal_left[MNZ];

					normal_right[NX] -= m_normal_low[MNX];
					normal_right[NY] -= m_normal_low[MNY];
					normal_right[NZ] -= m_normal_low[MNZ];

				}

			}

		}
	}
	
	
	


	public static PolygonRenderer make() 
	{
		return new ColorPolygonRenderer();
	}

	
	
	
	
	private double[] get_xyz(Vertex3D point) 
	{
		double[] xyz = new double[3];

		xyz[X] = point.getIntX();
		xyz[Y] = point.getIntY();
		xyz[Z] = 1.0 / point.getZ();

		return xyz;
	}
	
	private double[] get_rgb(Vertex3D point) 
	{
		double[] rgb = new double[3];
		double[] xyz = get_xyz(point);

//		Color c = pixelShader.shade(polygon, point);	//replaces bottom comments
	
		Color c;
		if (!point.isPhong()) 
		{	
			c = pixelShader.shade(polygon, point);			
		}
		else 
		{
			c = point.getColor();
		}
		
		rgb[R] = c.getR() * xyz[Z];
		rgb[G] = c.getG() * xyz[Z];
		rgb[B] = c.getB() * xyz[Z];
		
		return rgb;
	}
	
	private double[] get_camera(Vertex3D point) 
	{
		double[] cam_xyz = new double[3];
		double[] xyz = get_xyz(point);

		cam_xyz[X] = point.getCameraX() * xyz[Z];
		cam_xyz[Y] = point.getCameraY() * xyz[Z];
		cam_xyz[Z] = point.getCameraZ() * xyz[Z];
//		cam_xyz[Z] = 1.0 / point.getCameraZ();
		return cam_xyz;
	}
	
	private double[] get_normal(Vertex3D point) 
	{
		double[] xyz = new double[3];
		Point3DH normal = new Point3DH(0,0,0);
		
		if (point.hasNormal()) 
		{			
			normal = point.getNormal();
		}
		
		xyz[X] = normal.getX();
		xyz[Y] = normal.getY();
		xyz[Z] = normal.getZ();

		return xyz;
	}
	
	
	
	
	
	private double[] get_d_xyz(Vertex3D point1, Vertex3D point2) 
	{
		
		double[] p1_xyz = get_xyz(point1);
		double[] p2_xyz = get_xyz(point2);
		double[] d = new double[3];

		d[DX] = p1_xyz[X] - p2_xyz[X];
		d[DY] = p1_xyz[Y] - p2_xyz[Y];
		d[DZ] = p1_xyz[Z] - p2_xyz[Z];
		return d;
	}
	
	private double[] get_d_rgb(Vertex3D point1, Vertex3D point2) 
	{
		
		double[] p1_rgb = get_rgb(point1);
		double[] p2_rgb = get_rgb(point2);
		double[] d = new double[3];

		d[DR] = p1_rgb[R] - p2_rgb[R];
		d[DG] = p1_rgb[G] - p2_rgb[G];
		d[DB] = p1_rgb[B] - p2_rgb[B];

		return d;
	}
	
	private double[] get_d_camera(Vertex3D point1, Vertex3D point2) 
	{
		
		double[] p1_xyz = get_camera(point1);
		double[] p2_xyz = get_camera(point2);
		double[] d = new double[3];

		d[DX] = p1_xyz[X] - p2_xyz[X];
		d[DY] = p1_xyz[Y] - p2_xyz[Y];
		d[DZ] = p1_xyz[Z] - p2_xyz[Z];

		return d;
	}
	
	private double[] get_d_normal(Vertex3D point1, Vertex3D point2) 
	{
		double[] p1_normals = get_normal(point1);
		double[] p2_normals = get_normal(point2);
		
		double[] p1_cameras = get_camera(point1);
		double[] p2_cameras = get_camera(point2);
		
		double[] d = new double[3];

		d[DX] = p1_normals[X]/p1_cameras[CZ] - p2_normals[X]/p2_cameras[CZ];
		d[DY] = p1_normals[Y]/p1_cameras[CZ] - p2_normals[Y]/p2_cameras[CZ];
		d[DZ] = p1_normals[Z]/p1_cameras[CZ] - p2_normals[Z]/p2_cameras[CZ];

		return d;
	}
	
	
	
	
	private double[] get_m_xyz(Vertex3D point1, Vertex3D point2) 
	{
		
		double[] d = get_d_xyz(point1, point2);
		double[] m = new double[2];

		m[MX] = d[DX] / d[DY];
		m[MZ] = d[DZ] / d[DY];

		return m;
	}
	
	private double[] get_m_rgb(Vertex3D point1, Vertex3D point2) 
	{
		
		double[] d_xyz = get_d_xyz(point1, point2);
		double[] d_rgb = get_d_rgb(point1, point2);
		double[] m = new double[3];

		m[MR] = d_rgb[DR] / d_xyz[DY];
		m[MG] = d_rgb[DG] / d_xyz[DY];
		m[MB] = d_rgb[DB] / d_xyz[DY];

		return m;
	}
	
	private double[] get_m_camera(Vertex3D point1, Vertex3D point2) 
	{
		
		double[] d_cam = get_d_camera(point1, point2);
		double[] d_xyz = get_d_xyz(point1, point2);
		double[] m = new double[3];

		m[MCX] = d_cam[DX] / d_xyz[DY];
		m[MCY] = d_cam[DY] / d_xyz[DY];
		m[MCZ] = d_cam[DZ] / d_xyz[DY];

		return m;
	}
	
	private double[] get_m_normal(Vertex3D point1, Vertex3D point2) 
	{
		
		double[] d_normal = get_d_normal(point1, point2);
		double[] d_xyz = get_d_xyz(point1, point2);
		double[] m = new double[3];

		m[MNX] = d_normal[DX] / d_xyz[DY];
		m[MNY] = d_normal[DY] / d_xyz[DY];
		m[MNZ] = d_normal[DZ] / d_xyz[DY];

		return m;
	}
	
	private void printRGB(double[] value)
	{
		System.out.println("R:" +value[R] +" G:" +value[G] +" B:" +value[B]);
	}

	private void printXYZ(double[] value)
	{
		System.out.println("X:" +value[X] +" Y:" +value[Y] +" Z:" +value[Z]);
		System.out.println("");
	}
	
	

}
