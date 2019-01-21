package client.interpreter;

import geometry.Point3DH;
import geometry.Vertex3D;
import polygon.Chain;
import polygon.Polygon;
import windowing.graphics.Color;

import java.util.ArrayList;

public class Clipper {

	private Vertex3D[] vertices;
	private double front;
	private double back;
	private int screenleft;
	private int screenright;
	private int screentop;
	private int screenbottom;

	public Clipper(double front, double back, int left, int right, int top, int bottom) {
		this.front = front;
		this.back = back;
		this.screenleft = left;
		this.screenright = right;
		this.screentop = top;
		this.screenbottom = bottom;
	}

	public Chain clip_z(Chain verts) {
		Chain front = clip_front(verts);
		Chain back = clip_back(front);
		return back;
	}

	public Chain clip_xy(Chain verts) {
		Chain left = clip_left(verts);
		Chain right = clip_right(left);
		Chain top = clip_top(right);
		Chain bottom = clip_bottom(top);
		return bottom;
	}

	public Chain clip_front(Chain verts) {
		Chain vertices = new Chain();
		int n = verts.length();

		for (int i = 0; i < n; i++) {
			Vertex3D p1 = verts.get(i);
			Vertex3D p2 = verts.get((i + 1) % n);

			double dx = p2.getX() - p1.getX();
			double dy = p2.getY() - p1.getY();
			double dz = p2.getZ() - p1.getZ();

			double r = (front - p1.getZ()) / dz;
			double ix = p1.getX() + r * dx;
			double iy = p1.getY() + r * dy;
			double iz = front;

			Vertex3D pi = new Vertex3D(ix, iy, iz, p1.getColor());

			// Inside Inside
			if (p1.getZ() <= front && p2.getZ() <= front) {
				vertices.add(p2);
			}

			// Inside Outside
			if (p1.getZ() <= front && p2.getZ() > front) {
				vertices.add(pi);
			}

			// Outside Outside - do nothing

			// Outside Inside
			if (p1.getZ() > front && p2.getZ() <= front) {
				vertices.add(pi);
				vertices.add(p2);
			}
		}
		return vertices;
	}

	public Chain clip_back(Chain verts) {
		Chain vertices = new Chain();
		int n = verts.length();

		for (int i = 0; i < n; i++) {
			Vertex3D p1 = verts.get(i);
			Vertex3D p2 = verts.get((i + 1) % n);

			double dx = p2.getX() - p1.getX();
			double dy = p2.getY() - p1.getY();
			double dz = p2.getZ() - p1.getZ();

			double r = (back - p1.getZ()) / dz;
			double ix = p1.getX() + r * dx;
			double iy = p1.getY() + r * dy;
			double iz = back;

			Vertex3D pi = new Vertex3D(ix, iy, iz, p1.getColor());

			// Inside Inside
			if (p1.getZ() >= back && p2.getZ() >= back) {
				vertices.add(p2);
			}

			// Inside Outside
			if (p1.getZ() >= back && p2.getZ() < back) {
				vertices.add(pi);
			}

			// Outside Outside - do nothing

			// Outside Inside
			if (p1.getZ() < back && p2.getZ() >= back) {
				vertices.add(pi);
				vertices.add(p2);
			}
		}
		return vertices;
	}

	private double intercept_z(Vertex3D p0, Vertex3D p1, double r) {
		double z0_den = 1 / p0.getZ();
		double z1_den = 1 / p1.getZ();
		double z = 1 / (z0_den + r * (z1_den - z0_den));
		return z;
	}

	public Chain clip_left(Chain verts) 
	{
		Chain vertices = new Chain();
		int n = verts.length();

		for (int i = 0; i < n; i++) 
		{
			Vertex3D p1 = verts.get(i);
			Vertex3D p2 = verts.get((i + 1) % n);
			
			double dx = p2.getX() - p1.getX();
			double dy = p2.getY() - p1.getY();
			double dz = p2.getZ() - p1.getZ();
			
			double dcx = p2.getCameraX()/p2.getCameraZ() - p1.getCameraX()/p1.getCameraZ();
			double dcy = p2.getCameraY()/p2.getCameraZ() - p1.getCameraY()/p1.getCameraZ();
			double dcz = 1/p2.getCameraZ() - 1/p1.getCameraZ();
			double recip_dz = 1/p2.getZ() - 1/p1.getZ();

			double r = (screenleft - p1.getX()) / dx;
			double ix = screenleft;
			double iy = p1.getY() + r * dy;
			
			double icsz = 1/(1/p1.getCameraZ() + dcz*r);
			double icsx = (p1.getCameraX()/p1.getZ() + dcx*r)*icsz;
			double icsy = (p1.getCameraY()/p1.getZ() + dcy*r)*icsz;
			double iz = 1/( 1/p1.getZ() + recip_dz*r);	

			Color iColor =  p1.getColor();
			Vertex3D pi = new Vertex3D(ix, iy, iz, iColor);
			pi.setCameraPoint(new Point3DH(icsx,icsy,icsz));

			// Inside Inside
			if (p1.getX() >= screenleft && p2.getX() >= screenleft) {
				vertices.add(p2);
			}

			// Inside Outside
			if (p1.getX() >= screenleft && p2.getX() < screenleft) {
				vertices.add(pi);
			}

			// Outside Outside - do nothing

			// Outside Inside
			if (p1.getX() < screenleft && p2.getX() >= screenleft) {
				vertices.add(pi);
				vertices.add(p2);
			}
		}
		return vertices;
	}

	public Chain clip_right(Chain verts) {
		Chain vertices = new Chain();
		int n = verts.length();
		
		boolean flag = true;

		for (int i = 0; i < n; i++) 
		{
			Vertex3D p1 = verts.get(i);
			Vertex3D p2 = verts.get((i + 1) % n);
			
			double dx = p2.getX() - p1.getX();
			double dy = p2.getY() - p1.getY();
			double dz = p2.getZ() - p1.getZ();
			
			double dR = p2.getColor().getR()/p2.getCameraZ() - p1.getColor().getR()/p1.getCameraZ();
			double dG = p2.getColor().getG()/p2.getCameraZ() - p1.getColor().getG()/p1.getCameraZ();
			double dB = p2.getColor().getB()/p2.getCameraZ() - p1.getColor().getB()/p1.getCameraZ();
			
			double dcx = p2.getCameraX()/p2.getCameraZ() - p1.getCameraX()/p1.getCameraZ();
			double dcy = p2.getCameraY()/p2.getCameraZ() - p1.getCameraY()/p1.getCameraZ();
			double dcz = 1/p2.getCameraZ() - 1/p1.getCameraZ();
			double recip_dz = 1/p2.getZ() - 1/p1.getZ();

			double r = (screenright - p1.getX()) / dx;
			double ix = screenright;
			double iy = p1.getY() + r * dy;
//			double iz = intercept_z(p1, p2, r);
			
			
			double icsz = 1/(1/p1.getCameraZ() + dcz*r);
			double icsx = (p1.getCameraX()/p1.getZ() + dcx*r)*icsz;
			double icsy = (p1.getCameraY()/p1.getZ() + dcy*r)*icsz;
			double iz = 1/( 1/p1.getZ() + recip_dz*r);	
		
			
			double iR = (p1.getColor().getR()/p1.getCameraZ() + dR * r) * icsz;
			double iG = (p1.getColor().getG()/p1.getCameraZ() + dG * r) * icsz;
			double iB = (p1.getColor().getB()/p1.getCameraZ() + dB * r) * icsz;
			
			
			Color iColor =  new Color(iR, iG, iB);
			Vertex3D pi = new Vertex3D(ix, iy, iz, iColor);
			pi.setCameraPoint(new Point3DH(icsx,icsy,icsz));
			

			// Inside Inside
			if (p1.getX() <= screenright && p2.getX() <= screenright) 
			{
				vertices.add(p2);
			}

			// Inside Outside
			if (p1.getX() <= screenright && p2.getX() > screenright) 
			{
				vertices.add(pi);
			}

			// Outside Outside - do nothing

			// Outside Inside
			if (p1.getX() > screenright && p2.getX() <= screenright) 
			{
				vertices.add(pi);
				vertices.add(p2);
			}
		}
		return vertices;
	}


	public Chain clip_top(Chain verts) {
		Chain vertices = new Chain();
		int n = verts.length();

		for (int i = 0; i < n; i++) {
			Vertex3D p1 = verts.get(i);
			Vertex3D p2 = verts.get((i + 1) % n);

			double dx = p2.getX() - p1.getX();
			double dy = p2.getY() - p1.getY();
			double dz = p2.getZ() - p1.getZ();
			
			double dcx = p2.getCameraX()/p2.getCameraZ() - p1.getCameraX()/p1.getCameraZ();
			double dcy = p2.getCameraY()/p2.getCameraZ() - p1.getCameraY()/p1.getCameraZ();
			double dcz = 1/p2.getCameraZ() - 1/p1.getCameraZ();
			double recip_dz = 1/p2.getZ() - 1/p1.getZ();
			

			double r = (screentop - p1.getY()) / dy;
			double ix = p1.getX() + r * dx;
			double iy = screentop;
			// double iz = intercept_z(p1, p2, r);
			
			double icsz = 1/(1/p1.getCameraZ() + dcz*r);
			double icsx = (p1.getCameraX()/p1.getZ() + dcx*r)*icsz;
			double icsy = (p1.getCameraY()/p1.getZ() + dcy*r)*icsz;
			double iz = 1/( 1/p1.getZ() + recip_dz*r);	

			Color iColor =  p1.getColor();
			Vertex3D pi = new Vertex3D(ix, iy, iz, iColor);
			pi.setCameraPoint(new Point3DH(icsx,icsy,icsz));

			// Inside Inside
			if (p1.getY() <= screentop && p2.getY() <= screentop) {
				vertices.add(p2);
			}

			// Inside Outside
			if (p1.getY() <= screentop && p2.getY() > screentop) {
				vertices.add(pi);
			}

			// Outside Outside - do nothing

			// Outside Inside
			if (p1.getY() > screentop && p2.getY() <= screentop) {
				vertices.add(pi);
				vertices.add(p2);
			}
		}
		return vertices;
	}

	public Chain clip_bottom(Chain verts) {
		Chain vertices = new Chain();
		int n = verts.length();

		for (int i = 0; i < n; i++) {
			Vertex3D p1 = verts.get(i);
			Vertex3D p2 = verts.get((i + 1) % n);

			double dx = p2.getX() - p1.getX();
			double dy = p2.getY() - p1.getY();
			double dz = p2.getZ() - p1.getZ();
			
			double dcx = p2.getCameraX()/p2.getCameraZ() - p1.getCameraX()/p1.getCameraZ();
			double dcy = p2.getCameraY()/p2.getCameraZ() - p1.getCameraY()/p1.getCameraZ();
			double dcz = 1/p2.getCameraZ() - 1/p1.getCameraZ();
			double recip_dz = 1/p2.getZ() - 1/p1.getZ();

			double r = (screenbottom - p1.getY()) / dy;
			double ix = p1.getX() + r * dx;
			double iy = screenbottom;
//			double iz = intercept_z(p1, p2, r);
			
			double icsz = 1/(1/p1.getCameraZ() + dcz*r);
			double icsx = (p1.getCameraX()/p1.getZ() + dcx*r)*icsz;
			double icsy = (p1.getCameraY()/p1.getZ() + dcy*r)*icsz;
			double iz = 1/( 1/p1.getZ() + recip_dz*r);	
			
			

			Color iColor =  p1.getColor();
			Vertex3D pi = new Vertex3D(ix, iy, iz, iColor);
			pi.setCameraPoint(new Point3DH(icsx,icsy,icsz));
			

			// Inside Inside
			if (p1.getY() >= screenbottom && p2.getY() >= screenbottom) {
				vertices.add(p2);
			}

			// Inside Outside
			if (p1.getY() >= screenbottom && p2.getY() < screenbottom) {
				vertices.add(pi);
			}

			// Outside Outside - do nothing

			// Outside Inside
			if (p1.getY() < screenbottom && p2.getY() >= screenbottom) {
				vertices.add(pi);
				vertices.add(p2);
			}
		}
		return vertices;
	}

}
