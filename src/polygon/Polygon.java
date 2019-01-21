package polygon;

import java.util.ArrayList;
import java.util.List;

import geometry.Point3DH;
import geometry.Vertex;
import geometry.Vertex3D;
import windowing.graphics.Color;

public class Polygon extends Chain {
	private static final int INDEX_STEP_FOR_CLOCKWISE = -1;
	private static final int INDEX_STEP_FOR_COUNTERCLOCKWISE = 1;
	
	public boolean hasNormalatVertices = false;
	private Color color = Color.WHITE;
	private double kSpecular = 0.3;
	private double specularExponent = 8.0;

	private Polygon(Vertex3D... initialVertices) {
		super(initialVertices);
		if (length() < 3) {
			throw new IllegalArgumentException("Not enough vertices to construct a polygon");
		}
	}

	// the EmptyMarker is to distinguish this constructor from the one above (when
	// there are no initial vertices).
	private enum EmptyMarker {
		MARKER;
	};

	private Polygon(EmptyMarker ignored) {
		super();
	}

	public static Polygon makeEmpty() {
		return new Polygon(EmptyMarker.MARKER);
	}

	public static Polygon make(Vertex3D... initialVertices) {
		return new Polygon(initialVertices);
	}

	public static Polygon makeEnsuringClockwise(Vertex3D... initialVertices) {
		if (isClockwise(initialVertices[0], initialVertices[1], initialVertices[2])) {
			return new Polygon(reverseArray(initialVertices));
		}
		return new Polygon(initialVertices);
	}

	public static <V extends Vertex> boolean isClockwise(Vertex3D a, Vertex3D b, Vertex3D c) {
		Vertex3D vector1 = b.subtract(a);
		Vertex3D vector2 = c.subtract(a);

		double term1 = vector1.getX() * vector2.getY();
		double term2 = vector2.getX() * vector1.getY();
		double cross = term1 - term2;

		return cross < 0;
	}

	private static <V extends Vertex> V[] reverseArray(V[] initialVertices) {
		int length = initialVertices.length;
		List<V> newVertices = new ArrayList<V>();

		for (int index = 0; index < length; index++) {
			newVertices.add(initialVertices[index]);
		}
		for (int index = 0; index < length; index++) {
			initialVertices[index] = newVertices.get(length - 1 - index);
		}
		return initialVertices;
	}

	/**
	 * The Polygon is a circular Chain and the index given will be taken modulo the
	 * number of vertices in the Chain.
	 * 
	 * @param index
	 * @return
	 */
	public Vertex3D get(int index) {
		int realIndex = wrapIndex(index);
		return vertices.get(realIndex);
	}

	/**
	 * Wrap the indices for the list vertices.
	 * 
	 * @param index
	 *            any integer
	 * @return the number n such that n is equivalent to the given index modulo the
	 *         number of vertices.
	 */
	private int wrapIndex(int index) {
		return ((index % numVertices) + numVertices) % numVertices;
	}

	/////////////////////////////////////////////////////////////////////////////////
	//
	// methods for dividing a y-monotone polygon into a left chain and a right
	///////////////////////////////////////////////////////////////////////////////// chain.

	/**
	 * returns the left-hand chain of the polygon, ordered from top to bottom.
	 */
	public Chain leftChain() {
		return sideChain(INDEX_STEP_FOR_COUNTERCLOCKWISE);
	}

	/**
	 * returns the right-hand chain of the polygon, ordered from top to bottom.
	 */
	public Chain rightChain() {
		return sideChain(INDEX_STEP_FOR_CLOCKWISE);
	}

	private Chain sideChain(int indexStep) {
		int topIndex = topIndex();
		int bottomIndex = bottomIndex();

		Chain chain = new Chain();
		for (int index = topIndex; wrapIndex(index) != bottomIndex; index += indexStep) {
			chain.add(get(index));
		}
		chain.add(get(bottomIndex));

		return chain;
	}

	private int topIndex() {
		int maxIndex = 0;
		double maxY = get(0).getY();

		for (int index = 1; index < vertices.size(); index++) {
			if (get(index).getY() > maxY) {
				maxY = get(index).getY();
				maxIndex = index;
			}
		}
		return maxIndex;
	}

	private int bottomIndex() {
		int minIndex = 0;
		double minY = get(0).getY();

		for (int index = 1; index < vertices.size(); index++) {
			if (get(index).getY() < minY) {
				minY = get(index).getY();
				minIndex = index;
			}
		}
		return minIndex;
	}

	public String toString() {
		return "Polygon[" + super.toString() + "]";
	}

	public static Polygon chaintopolygon(Chain c) {
		int n = c.length();
		Vertex3D[] verts = new Vertex3D[n];
		for (int i = 0; i < n; i++) {
			verts[i] = c.get(i);
		}

		Polygon polygon = Polygon.make(verts);
		return polygon;
	}
	
	public Vertex3D getCenter()
	{
		Vertex3D p0 = this.get(0);
		Vertex3D p1 = this.get(1);
		Vertex3D p2 = this.get(2);
		
		double sum_x = (p0.getX() + p1.getX() + p2.getX())/3.0;
		double sum_y = (p0.getY() + p1.getY() + p2.getY())/3.0;
		double sum_z = (p0.getZ() + p1.getZ() + p2.getZ())/3.0;
		
		double sum_cx = (p0.getCameraX() + p1.getCameraX() + p2.getCameraX())/3.0;
		double sum_cy = (p0.getCameraY() + p1.getCameraY() + p2.getCameraY())/3.0;
		double sum_cz = (p0.getCameraZ() + p1.getCameraZ() + p2.getCameraZ())/3.0;
		
		Vertex3D center = new Vertex3D(sum_x, sum_y, sum_z, this.getColor());
		center.setCameraPoint(new Vertex3D(sum_cx, sum_cy, sum_cz, this.getColor()));
		
		return center;
	}

	public boolean hasNormalatVertices()
	{
		if (this.get(0).hasNormal())
		{
			hasNormalatVertices = true;
		}
		return hasNormalatVertices;
	}
	
	public Point3DH getNormal()
	{
		Vertex3D v1 = this.vertices.get(0);
		Vertex3D v2 = this.vertices.get(1);
		Vertex3D v3 = this.vertices.get(2);
		
		Point3DH v2_v1 = new Point3DH(v2.getCameraX()-v1.getCameraX(), v2.getCameraY()-v1.getCameraY(), v2.getCameraZ()-v1.getCameraZ());
		Point3DH v3_v1 = new Point3DH(v3.getCameraX()-v1.getCameraX(), v3.getCameraY()-v1.getCameraY(), v3.getCameraZ()-v1.getCameraZ());
		
		Point3DH N = v2_v1.crossProduct(v3_v1);
		N = N.normalize();
		return N;
		
	}
	
//	public Point3DH getNormalizedNormal()
//	{
//		Point3DH nN = this.getNormal();
//		return normalize(nN);
//	}
	
//	private Point3DH normalize(Point3DH vector)
//	{
//		double x = vector.getX();
//		double y = vector.getY();
//		double z = vector.getZ();
//		double magnitude = Math.sqrt(x*x +y*y + z*z);
//		
//		if (magnitude == 0)
//			return new Point3DH(0, 0, 0);
//		
//		else
//			return new Point3DH(x/magnitude, y/magnitude, z/magnitude);
//	}
	
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
	
	public void setkSpecular(double value)
	{
		this.kSpecular = value;
	}
	
	public void setspecularExponent(double value)
	{
		this.specularExponent = value;
	}

	public double getkSpecular()
	{
		return this.kSpecular;
	}
	
	public double getspecularExponent()
	{
		return this.specularExponent;
	}
	
	public void printColor()
	{
		int r = color.getIntR();
		int g = color.getIntG();
		int b = color.getIntB();
		System.out.println("r:" +r +" g:" +g +" b:" +b);
	}
		
}
