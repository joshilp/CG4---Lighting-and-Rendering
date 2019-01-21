package geometry;

import windowing.graphics.Color;

public class Vertex3D implements Vertex {
	protected Point3DH point;
	protected Color color;
	
	protected Point3DH cameraPoint;
//	InterpolantList interpolants;
	private boolean hasNormal;
	protected Point3DH normal;
	private boolean renderPhong;
	private boolean fromObject;
	
	private boolean interpolateColor;
	private boolean interpolateNormals;
	private boolean interpolateCameraSpacePoint;
	
	public enum ShaderStyle {
		FLAT, GOURAUD, PHONG;
	}
	public ShaderStyle shaderStyle;

	private Color shaderColor;
	
	public Vertex3D(Point3DH point, Color color) {
		super();
		this.point = point;
		this.color = color;
		
		this.hasNormal = false;
		this.interpolateColor = false;
		this.interpolateNormals = false;
		this.interpolateCameraSpacePoint = false;
		this.shaderStyle = ShaderStyle.PHONG;
		this.cameraPoint = point;
		this.renderPhong = false;
		this.shaderColor = color;
		this.fromObject = false;
	}

	public Vertex3D(double x, double y, double z, Color color) {

		this(new Point3DH(x, y, z), color);
		if (color == null)
		{
			System.out.println("You didnt give me a color!");
		}
	}

	public Vertex3D() {
	}

	public double getX() {
		return point.getX();
	}

	public double getY() {
		return point.getY();
	}

	public double getZ() {
		return point.getZ();
	}

	public double getCameraSpaceZ() {
		return getZ();
	}

	public Point getPoint() {
		return point;
	}

	public Point3DH getPoint3D() {
		return point;
	}

	public int getIntX() {
		return (int) Math.round(getX());
	}

	public int getIntY() {
		return (int) Math.round(getY());
	}

	public int getIntZ() {
		return (int) Math.round(getZ());
	}

	public Color getColor() {
		return color;
	}

	public Vertex3D rounded() {
		return new Vertex3D(point.round(), color);
	}

	public Vertex3D add(Vertex other) {
		Vertex3D other3D = (Vertex3D) other;
		return new Vertex3D(point.add(other3D.getPoint()), color.add(other3D.getColor()));
	}

	public Vertex3D subtract(Vertex other) {
		Vertex3D other3D = (Vertex3D) other;
		return new Vertex3D(point.subtract(other3D.getPoint()), color.subtract(other3D.getColor()));
	}

	public Vertex3D scale(double scalar) {
		return new Vertex3D(point.scale(scalar), color.scale(scalar));
	}

	public Vertex3D replacePoint(Point3DH newPoint) {
		return new Vertex3D(newPoint, color);
	}

	public Vertex3D replaceColor(Color newColor) {
		return new Vertex3D(point, newColor);
	}

	public Vertex3D euclidean() {
		Point3DH euclidean = getPoint3D().euclidean();
		return replacePoint(euclidean);
	}

	public String toString() {
		return "(" + getX() + ", " + getY() + ", " + getZ() + ", " + getColor().toIntString() + ")";
	}

	public String toIntString() {
		return "(" + getIntX() + ", " + getIntY() + getIntZ() + ", " + ", " + getColor().toIntString() + ")";
	}

	public void print() {
		double x = point.getX();
		double y = point.getY();
		double z = point.getZ();
		System.out.println(x + ", " + y + ", " + z);
	}
	
	public void setNormal(Point3DH normal)
	{
		this.normal = normal;
		this.hasNormal = true;
	}
	
	public boolean hasNormal()
	{
		return hasNormal;
	}
	
	public Point3DH getNormal()
	{
//		if(hasNormal)
//			return normal;
//		else
//			return new Point3DH(0, 0, 0);
		return normal;
	}
	
	public void setCameraPoint(Point3DH cameraSpacePoint)
	{
		this.cameraPoint = cameraSpacePoint;
	}
	
	public void setCameraPoint(Vertex3D cameraSpacePoint)
	{
		this.cameraPoint = cameraSpacePoint.getPoint3D();
	}
	
	public double getCameraX()
	{
		return cameraPoint.getX();
	}
	
	public double getCameraY()
	{
		return cameraPoint.getY();
	}
	
	public double getCameraZ()
	{
		return cameraPoint.getZ();
	}
	
	public Point3DH getCameraPoint()
	{
		return cameraPoint;
	}
	
	
	public Vertex3D setColor(Color color)
	{
		return new Vertex3D(this.getX(), this.getY(), this.getZ(), color);
	}
	
	public void interpolateColor(boolean value)
	{
		this.interpolateColor = value;
	}

	public void interpolateNormals(boolean value) 
	{
		this.interpolateNormals = value;		
	}

	public void interpolateCameraSpacePoint(boolean value) 
	{
		this.interpolateCameraSpacePoint = value;		
	}
	
	public boolean checkInterpolateColor()
	{
		return interpolateColor;
	}

	public boolean checkInterpolateNormals() 
	{
		return interpolateNormals;		
	}

	public boolean checkInterpolateCameraSpacePoint() 
	{
		return interpolateCameraSpacePoint;		
	}
	
	public void printColor(String message)
	{
		if (this.getColor() == null)
		{
			System.out.println("There is no color for this Vertex3D");
		}
		
		else
		{
			Color c = this.getColor();
			int r = c.getIntR();
			int g = c.getIntG();
			int b = c.getIntB();
			System.out.println(message +": r:" +r +" g:" +g +" b:" +b);
		}
	}
	
	public void printInfo()
	{
		System.out.println("Vertex3D: " +this.getX() +", " +this.getY() +", " +this.getZ());
//		printColor();
//		System.out.println("hasNormal: " +this.hasNormal);
//		System.out.println("interpolateColor: " +this.interpolateColor);
//		System.out.println("interpolateNormals: " +this.interpolateNormals);
//		System.out.println("interpolateCameraSpacePoint: " +this.interpolateCameraSpacePoint);
		System.out.println("Camera Spaces: " +this.getCameraX() +", " +this.getCameraY() +", " +this.getCameraZ());
		this.getNormal().printNormal();
		System.out.println(" ");
	}
	
	public void renderPhong() 
	{
		this.renderPhong = true;
	}
	
	public boolean isPhong() 
	{
		return this.renderPhong;
	}
	
	public Color getShaderColor()
	{
		return this.shaderColor;
	}

	public void setShaderColor(Color lighting_calculation) 
	{
		this.shaderColor = lighting_calculation;
	}

	public boolean fromObject() 
	{
		return this.fromObject;
	}
	
	public void isObject() {
		this.fromObject = true;
	}

	public void notObject() {
		this.fromObject = false;
	}
	
	

}
