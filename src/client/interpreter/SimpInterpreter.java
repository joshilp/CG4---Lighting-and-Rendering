package client.interpreter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import client.interpreter.LineBasedReader;
import client.interpreter.Clipper;
import geometry.Point3DH;
import geometry.Rectangle;
import geometry.Vertex3D;
import lighting.Light;
import lighting.Lighting;
import line.LineRenderer;
import client.interpreter.RendererTrio;
import client.interpreter.SimpInterpreter.RenderStyle;
import geometry.Transformation;
import polygon.Polygon;
import polygon.PolygonRenderer;
import shading.FaceShader;
import shading.PixelShader;
import shading.Shader;
import shading.VertexShader;
import polygon.Chain;
import polygon.ColorPolygonRenderer;
import windowing.drawable.DepthCueingDrawable;
import windowing.drawable.Drawable;
import windowing.graphics.Color;
import windowing.graphics.Dimensions;

public class SimpInterpreter {
	private static final int NUM_TOKENS_FOR_POINT = 3;
	private static final int NUM_TOKENS_FOR_COMMAND = 1;
	private static final int NUM_TOKENS_FOR_COLORED_VERTEX = 6;
	private static final int NUM_TOKENS_FOR_UNCOLORED_VERTEX = 3;
	private static final char COMMENT_CHAR = '#';
	public RenderStyle renderStyle;

	private static Transformation CTM;
	private Transformation worldToScreen;
	private static Transformation worldToCamera;
	private Transformation cameraToScreen;
	public Transformation projectToScreenCenter;

	private static int WORLD_LOW_X = -100;
	private static int WORLD_HIGH_X = 100;
	private static int WORLD_LOW_Y = -100;
	private static int WORLD_HIGH_Y = 100;

	private LineBasedReader reader;
	private Stack<LineBasedReader> readerStack;
	private Stack<Transformation> CTMStack;

	
	public Color kDiffuse;
	public Color ambientLight;
	private Light defaultLight;
	public Lighting lighting;
	private List<Light> lights;
	
//	private Color defaultColor = Color.WHITE;
	private Color defaultColor;

	private double kSpecular;
	private double specularExponent;
	
	public Shader ambientShader = lightingcalc -> ambientLight.multiply(lightingcalc);
	public FaceShader faceShader = polygon -> faceShader(polygon);
	public PixelShader pixelShader = (polygon, vertex) -> pixelShader(polygon, vertex);
	public VertexShader vertexShader = (polygon, vertex) -> vertexShader(polygon, vertex);

	
	public enum ShaderStyle {
		FLAT, GOURAUD, PHONG;
	}
	private ShaderStyle shaderStyle;
	

	public Drawable drawable;
	public Drawable depthCueingDrawable;

	private LineRenderer lineRenderer;
	public PolygonRenderer filledRenderer;
	public PolygonRenderer wireframeRenderer;

	public Clipper clipper;

	public enum RenderStyle {
		FILLED, WIREFRAME;
	}

	
	
	public SimpInterpreter(String filename, Drawable drawable, RendererTrio renderers) {
		this.drawable = drawable;
		this.depthCueingDrawable = drawable;
		this.lineRenderer = RendererTrio.getLineRenderer();
		this.filledRenderer = RendererTrio.getFilledRenderer();
		this.wireframeRenderer = RendererTrio.getWireframeRenderer();
		
		this.defaultColor = Color.WHITE;
		this.ambientLight = Color.BLACK;
		this.kSpecular = 0.3;
		this.specularExponent = 8.0; 

		renderStyle = RenderStyle.FILLED;
		shaderStyle = ShaderStyle.PHONG;
		
		lights = new ArrayList<Light>();
		lighting = new Lighting(ambientLight, lights);
		
		makeWorldToScreenTransform(drawable.getDimensions());

		reader = new LineBasedReader(filename);
		readerStack = new Stack<>();
		CTMStack = new Stack<>();
		
		
		CTM = Transformation.identity();
		worldToCamera = Transformation.identity();

		makeWorldToScreenTransform(drawable.getDimensions());
	}
	
	

	
	private Polygon faceShader(Polygon polygon) 
	{
		if (shaderStyle == ShaderStyle.FLAT)
		{
			polygon = FlatFaceShader(polygon);		
		}
		
		if (shaderStyle == ShaderStyle.GOURAUD)
		{
			polygon = NullFaceShader(polygon);			
		}
		
		if (shaderStyle == ShaderStyle.PHONG)
		{
			polygon = NullFaceShader(polygon);			
		}
		
		return polygon;
	}
	
	private Vertex3D vertexShader(Polygon polygon, Vertex3D vertex) 
	{
		if (shaderStyle == ShaderStyle.FLAT)
		{
			vertex = NullVertexShader(vertex);
		}
		
		if (shaderStyle == ShaderStyle.GOURAUD)
		{
			vertex = GouraudVertexShader(polygon, vertex);
		}
		
		if (shaderStyle == ShaderStyle.PHONG)
		{
			vertex = PhongVertexShader(polygon, vertex);
		}
		
		return vertex;
	}

	private Color pixelShader(Polygon polygon, Vertex3D current) 
	{
		Color c = null;
		
		if (shaderStyle == ShaderStyle.FLAT)
		{
			c = FlatPixelShader(polygon);
		}
		
		if (shaderStyle == ShaderStyle.GOURAUD)
		{
			c = GouraudPixelShader(current);
		}
		
		if (shaderStyle == ShaderStyle.PHONG)
		{
			c = PhongPixelShader(polygon, current);
		}
		return c;
	}
	



	private Polygon FlatFaceShader(Polygon polygon) 
	{
		Vertex3D v1 = polygon.get(0);
		Vertex3D v2 = polygon.get(1);
		Vertex3D v3 = polygon.get(2);
		
		Vertex3D center = polygon.getCenter();
		Point3DH N;
		
		if (v1.hasNormal())
		{
			
			N = (v1.getNormal().add(v2.getNormal().add(v3.getNormal())));
			N.scale(1.0/3.0);
			
			Transformation transN = worldToCamera.premultTransformation(CTM);
			transN.invert();
			
			N = transN.postmultNormal(N);
//			N.printNormal();
		}
		
		else
		{
			N = polygon.getNormal().normalize();
		}
		
		Color lighting_calculation = lighting.light(center, v1.getColor(), N, kSpecular, specularExponent);
		polygon.setColor(lighting_calculation);
		
		return polygon;
	}
	
	private Polygon NullFaceShader(Polygon polygon)
	{
		return polygon;
	}
	
	private Vertex3D NullVertexShader(Vertex3D vertex) 
	{
		//System.out.println("applying nullvertexshader, current color is: ");
//		vertex.printColor();
		return vertex;
	}
	
	private Vertex3D GouraudVertexShader(Polygon polygon, Vertex3D vertex) 
	{
		Point3DH N;
		if (vertex.hasNormal())
		{
			N = vertex.getNormal();
			Transformation transN = worldToCamera.premultTransformation(CTM);
			transN.invert();
			N = transN.postmultNormal(N);
		}
		
		else
		{
			N = polygon.getNormal().normalize();
		}
		
		Color kDiffuse = vertex.getColor();
		Color lighting_calculation = lighting.light(vertex, kDiffuse, N, kSpecular, specularExponent);
		
		vertex.setShaderColor(lighting_calculation);
		vertex.interpolateColor(true);
		return vertex;
	}
	
	private Vertex3D PhongVertexShader(Polygon polygon, Vertex3D vertex) 
	{
		Point3DH N;
		
		if (vertex.hasNormal())
		{
//			System.out.println("has normal!");
			N = vertex.getNormal();
			vertex.fromObject();
		}
		
		else
		{
//			System.out.println("gotta get normal!");
			N = polygon.getNormal();
			N.normalize();
			vertex.setNormal(N);
		}
		
		//Commenting this out break the interpolation or something
		vertex.renderPhong();
		
		return vertex;
	}
	
	private Color FlatPixelShader(Polygon polygon) 
	{
		Color c = polygon.getColor();
		return c;
	}
	
	private Color GouraudPixelShader(Vertex3D current) 
	{
		return current.getShaderColor();
	}
	
	private Color PhongPixelShader(Polygon polygon, Vertex3D current) 
	{
		Color kDiffuse = current.getColor();
		Point3DH N = current.getNormal();
		
		if (current.fromObject())
		{
			Transformation transN = worldToCamera.premultTransformation(CTM);
			transN.invert();
			N = transN.postmultNormal(N);	
		}
		
		Color lighting_calculation = lighting.light(current, kDiffuse, N, kSpecular, specularExponent);
		return lighting_calculation;
	}
	

	private void makeWorldToScreenTransform(Dimensions dimensions) {
		worldToScreen = Transformation.identity();
		worldToScreen.scale(dimensions.getWidth() / (WORLD_HIGH_X - WORLD_LOW_X),
				dimensions.getHeight() / (WORLD_HIGH_Y - WORLD_LOW_Y), 1, false);
		worldToScreen.translate(dimensions.getWidth() / 2.0, dimensions.getHeight() / 2.0, 0, false);
	}

	public void interpret() {
		while (reader.hasNext()) {
			String line = reader.next().trim();
			interpretLine(line);
			while (!reader.hasNext()) {
				if (readerStack.isEmpty()) {
					return;
				} else {
					reader = readerStack.pop();
				}
			}
		}
	}

	public void interpretLine(String line) {
		if (!line.isEmpty() && line.charAt(0) != COMMENT_CHAR) {
			String[] tokens = line.split("[ \t,()]+");
			if (tokens.length != 0) {
				interpretCommand(tokens);
			}
		}
	}

	private void interpretCommand(String[] tokens) {
		// //System.out.println("tokens: " +tokens[0]);
		switch (tokens[0]) {
		case "{":
			push();
			break;
		case "}":
			pop();
			break;
		case "wire":
			wire();
			break;
		case "filled":
			filled();
			break;
		case "file":
			interpretFile(tokens);
			break;
		case "scale":
			interpretScale(tokens);
			break;
		case "translate":
			interpretTranslate(tokens);
			break;
		case "rotate":
			interpretRotate(tokens);
			break;
		case "line":
			interpretLine(tokens);
			break;
		case "polygon":
			interpretPolygon(tokens);
			break;
		case "camera":
			interpretCamera(tokens);
			break;
		case "light":
			interpretLight(tokens);
			break;
		case "surface":
			interpretSurface(tokens);
			break;
		case "ambient":
			interpretAmbient(tokens);
			break;
		case "depth":
			interpretDepth(tokens);
			break;
		case "obj":
			interpretObj(tokens);
			break;
		case "flat":
			interpretFlat(tokens);
			break;
		case "gouraud":
			interpretGouraud(tokens);
			break;
		case "phong":
			interpretPhong(tokens);
			break;

		default:
			System.err.println("bad input line: " + tokens);
			break;
		}
	}
	
	private void interpretFlat(String[] tokens) 
	{
		System.out.println("setting to FLAT");
		shaderStyle = ShaderStyle.FLAT;
		lighting = new Lighting (ambientLight, lights);
	}
	
	private void interpretGouraud(String[] tokens) 
	{
		System.out.println("setting to GOURAUD");
		shaderStyle = ShaderStyle.GOURAUD;
		lighting = new Lighting (ambientLight, lights);
	}

	private void interpretPhong(String[] tokens) 
	{
		System.out.println("setting to PHONG");
		shaderStyle = ShaderStyle.PHONG;
		lighting = new Lighting (ambientLight, lights);
	}



	private void interpretObj(String[] tokens) {
		String file = tokens[1];
		file = file.replace("\"", "");
		file = file + ".obj";
		objFile(file);
	}

	private void interpretDepth(String[] tokens) {
		int nearclip = (int) cleanNumber(tokens[1]);
		int farclip = (int) cleanNumber(tokens[2]);

		double r = cleanNumber(tokens[3]);
		double g = cleanNumber(tokens[4]);
		double b = cleanNumber(tokens[5]);

		Color farColor = new Color(r, g, b);

		depthCueingDrawable = new DepthCueingDrawable(drawable, nearclip, farclip, farColor);
	}
	
	private void interpretLight(String[] tokens) {
		double red = cleanNumber(tokens[1]);
		double green = cleanNumber(tokens[2]);
		double blue = cleanNumber(tokens[3]);
		double fattA = cleanNumber(tokens[4]);
		double fattB = cleanNumber(tokens[5]);
		
		Vertex3D lightpoint = new Vertex3D(0.0, 0.0, 0.0, Color.WHITE);
		lightpoint = worldToCamera.transformV3D(CTM.transformV3D(lightpoint));
		
		Point3DH lightCSLocation = lightpoint.getPoint3D();
		
		Light light = new Light(red, green, blue, fattA, fattB, lightCSLocation);
		lights.add(light);
	}
	
	private void interpretSurface(String[] tokens) {
		double surface_r = cleanNumber(tokens[1]);
		double surface_g = cleanNumber(tokens[2]);
		double surface_b = cleanNumber(tokens[3]);
		
		kSpecular = cleanNumber(tokens[4]);
		specularExponent = cleanNumber(tokens[5]); 
		
		defaultColor = new Color(surface_r, surface_g, surface_b);
	}

	private void interpretAmbient(String[] tokens) {
		double r = cleanNumber(tokens[1]);
		double g = cleanNumber(tokens[2]);
		double b = cleanNumber(tokens[3]);
		ambientLight = new Color(r, g, b);
		lighting = new Lighting (ambientLight, lights);
	}

	private void interpretCamera(String[] tokens) {

		double xlow = cleanNumber(tokens[1]);
		double ylow = cleanNumber(tokens[2]);
		double xhigh = cleanNumber(tokens[3]);
		double yhigh = cleanNumber(tokens[4]);

		double front = cleanNumber(tokens[5]);
		double back = cleanNumber(tokens[6]);

		int left = 0;
		int right = drawable.getWidth();

		int top = drawable.getHeight();
		int bottom = 0;

		double read_width = xhigh - xlow;
		double read_height = yhigh - ylow;

		double scale = right / read_width;
		double r = read_height / read_width;

		int adjust = (int) ((top - top * r) / 2.0);

		clipper = new Clipper(front, back, left, right, top - adjust, bottom + adjust);

		worldToCamera.copy(CTM);
		worldToCamera.invert();

		projectedToScreen(drawable.getDimensions(), xhigh, xlow, yhigh, ylow);
	}

	private void projectedToScreen(Dimensions dimensions, double xhigh, double xlow, double yhigh, double ylow) {
		int height = dimensions.getHeight();
		int width = dimensions.getWidth();

		double read_width = xhigh - xlow;
		double read_height = yhigh - ylow;

		double scale = width / read_width;
		double r = read_height / read_width;

		double new_xhigh = xhigh * scale;
		double new_yhigh = yhigh * scale;
		double new_ylow = ylow * scale;

		double tx = width - new_xhigh;
		double ty = height / 2 - ((new_yhigh + new_ylow) / 2);

		projectToScreenCenter = Transformation.identity();
		projectToScreenCenter.scale(width / read_width, height / read_height * r, 1, false);
		projectToScreenCenter.translate(tx, ty, 0, false);
	}

	public Vertex3D transformToCamera(Vertex3D vertex) {
		Vertex3D t = worldToCamera.transformV3D(CTM.transformV3D(vertex));
		Point3DH cameraspacevertex = new Point3DH(t.getX(),t.getY(),t.getZ());
		t.setCameraPoint(cameraspacevertex);
		return t;
		
	}

	private void push() {
		Transformation CTM2 = Transformation.identity();
		CTM2.copy(CTM);
		CTMStack.push(CTM2);

	}

	private void pop() {
		CTM = CTMStack.pop();
	}

	private void wire() {
		// //System.out.println("wire");
		renderStyle = RenderStyle.WIREFRAME;
	}

	private void filled() {
		// //System.out.println("filled");
		renderStyle = RenderStyle.FILLED;
	}

	// this one is complete.
	private void interpretFile(String[] tokens) {
		String quotedFilename = tokens[1];
		int length = quotedFilename.length();
		assert quotedFilename.charAt(0) == '"' && quotedFilename.charAt(length - 1) == '"';
		String filename = quotedFilename.substring(1, length - 1);
		file(filename + ".simp");
	}

	private void file(String filename) {
		readerStack.push(reader);
		reader = new LineBasedReader(filename);
	}

	private void interpretScale(String[] tokens) {
		double sx = cleanNumber(tokens[1]);
		double sy = cleanNumber(tokens[2]);
		double sz = cleanNumber(tokens[3]);
		CTM.scale(sx, sy, sz, true);
	}

	private void interpretTranslate(String[] tokens) {
		double tx = cleanNumber(tokens[1]);
		double ty = cleanNumber(tokens[2]);
		double tz = cleanNumber(tokens[3]);
		CTM.translate(tx, ty, tz, true);
	}

	private void interpretRotate(String[] tokens) {
		String axisString = tokens[1];
		double angleInDegrees = cleanNumber(tokens[2]);
		double angleinRad = Math.toRadians(angleInDegrees);

		switch (axisString) {
		case ("X"):
			CTM.rotateX(angleinRad, true);
			break;

		case ("Y"):
			CTM.rotateY(angleinRad, true);
			break;

		case ("Z"):
			CTM.rotateZ(angleinRad, true);
			break;
		}
	}

	private static double cleanNumber(String string) {
		return Double.parseDouble(string);
	}

	private enum VertexColors {
		COLORED(NUM_TOKENS_FOR_COLORED_VERTEX), UNCOLORED(NUM_TOKENS_FOR_UNCOLORED_VERTEX);

		private int numTokensPerVertex;

		private VertexColors(int numTokensPerVertex) {
			this.numTokensPerVertex = numTokensPerVertex;
		}

		public int numTokensPerVertex() {
			return numTokensPerVertex;
		}
	}

	private void interpretLine(String[] tokens) {
		Vertex3D[] vertices = interpretVertices(tokens, 2, 1);

		Vertex3D p0 = vertices[0];
		Vertex3D p1 = vertices[1];
		line(p0, p1);
	}

	
	
	
	
	
	
	
	
	private void interpretPolygon(String[] tokens) {
		Vertex3D[] vertices = interpretVertices(tokens, 3, 1);
		Chain c = new Chain(vertices[0], vertices[1], vertices[2]);
		c = clipper.clip_z(c);

		Chain pv = new Chain();
		int n = c.length();

		for (int j = 0; j < n; j++) 
		{
			Vertex3D vertex = c.get(j);
			Vertex3D projectedtoscreen = projectToScreen(vertex);
			
			double csx = projectedtoscreen.getCameraX();
			double csy = projectedtoscreen.getCameraY();
			double csz = projectedtoscreen.getCameraZ();
			
			projectedtoscreen = projectToScreenCenter.transformV3D(projectedtoscreen);
			projectedtoscreen.setCameraPoint(new Point3DH(csx, csy, csz));
			
			if (vertex.hasNormal())
			{
				Point3DH normal = projectedtoscreen.getNormal();
				projectedtoscreen.setNormal(normal);
			}
			
			pv.add(projectedtoscreen);
		}
		
		pv = clipper.clip_xy(pv);

		n = pv.length();

		if (renderStyle == RenderStyle.FILLED) 
		{
			for (int k = 1; k < n - 1; k++) 
			{
				Vertex3D p0 = pv.get(0);
				Vertex3D p1 = pv.get(k);
				Vertex3D p2 = pv.get(k + 1);
				
				
				
				Polygon filled_polygon = Polygon.make(p0, p1, p2);
				
				filled_polygon.setkSpecular(kSpecular);
				filled_polygon.setspecularExponent(specularExponent);
				
//				filled_polygon.getColor().printColor("");
				
				filledRenderer.drawPolygon(filled_polygon, depthCueingDrawable, faceShader, vertexShader, pixelShader);
			}

		}
	}

	
	
	
	
	
	
	public Vertex3D[] interpretVertices(String[] tokens, int numVertices, int startingIndex) {
		VertexColors vertexColors = verticesAreColored(tokens, numVertices);
		Vertex3D vertices[] = new Vertex3D[numVertices];

		for (int index = 0; index < numVertices; index++) {
			vertices[index] = interpretVertex(tokens, startingIndex + index * vertexColors.numTokensPerVertex(),
					vertexColors);
		}
		return vertices;
	}

	public VertexColors verticesAreColored(String[] tokens, int numVertices) {
		return hasColoredVertices(tokens, numVertices) ? VertexColors.COLORED : VertexColors.UNCOLORED;
	}

	public boolean hasColoredVertices(String[] tokens, int numVertices) {
		return tokens.length == numTokensForCommandWithNVertices(numVertices);
	}

	public int numTokensForCommandWithNVertices(int numVertices) {
		return NUM_TOKENS_FOR_COMMAND + numVertices * (NUM_TOKENS_FOR_COLORED_VERTEX);
	}

	public Vertex3D projectToScreen(Vertex3D vertex) {
		// int d = -1;
		double i = -1 / vertex.getZ();
		double x = vertex.getX();
		double y = vertex.getY();
		double z = vertex.getZ();
		
		Vertex3D v = new Vertex3D((x * i), (y * i), z, vertex.getColor());
		
		v.setCameraPoint(vertex);
		
		if (vertex.hasNormal())
		{
			v.setNormal(vertex.getNormal());
		}
		if (vertex.isPhong())
		{
			v.renderPhong();
		}
		
		return v;
	}
	
	public Point3DH projectNormalToScreen(Point3DH vertex) {
		// int d = -1;
		double i = -1 / vertex.getZ();
		double x = vertex.getX();
		double y = vertex.getY();
		double z = vertex.getZ();
		
		Point3DH v = new Point3DH((x * i), (y * i), z);
		
		Vertex3D v_vertex = v.toVertex3D();
		
		v_vertex.setCameraPoint(v.toVertex3D());
		
		Point3DH v2 = v_vertex.getPoint3D();
		
		return v2;
	}

	private Vertex3D interpretVertex(String[] tokens, int startingIndex, VertexColors colored) {
		Point3DH point = interpretPoint(tokens, startingIndex);

		Color color = defaultColor;
		if (colored == VertexColors.COLORED) {
			color = interpretColor(tokens, startingIndex + NUM_TOKENS_FOR_POINT);
		}

		Vertex3D v = new Vertex3D(point, color);

		v = transformToCamera(v);

		return v;
	}

	public static Point3DH interpretPoint(String[] tokens, int startingIndex) {
		double x = cleanNumber(tokens[startingIndex]);
		double y = cleanNumber(tokens[startingIndex + 1]);
		double z = cleanNumber(tokens[startingIndex + 2]);

		return new Point3DH(x, y, z);
	}

	public static Color interpretColor(String[] tokens, int startingIndex) {
		double r = cleanNumber(tokens[startingIndex]);
		double g = cleanNumber(tokens[startingIndex + 1]);
		double b = cleanNumber(tokens[startingIndex + 2]);

		return new Color(r, g, b);
	}

	private void line(Vertex3D p1, Vertex3D p2) {
		Vertex3D screenP1 = transformToCamera(p1);
		Vertex3D screenP2 = transformToCamera(p2);
	}

	private void polygon(Vertex3D p1, Vertex3D p2, Vertex3D p3) {
		Vertex3D screenP1 = transformToCamera(p1);
		Vertex3D screenP2 = transformToCamera(p2);
		Vertex3D screenP3 = transformToCamera(p3);
	}

	public static Point3DH interpretPointWithW(String[] tokens, int startingIndex) {
		double x = cleanNumber(tokens[startingIndex]);
		double y = cleanNumber(tokens[startingIndex + 1]);
		double z = cleanNumber(tokens[startingIndex + 2]);
		double w = cleanNumber(tokens[startingIndex + 3]);
		Point3DH point = new Point3DH(x, y, z, w);
		return point;
	}

	private void objFile(String filename) {
		ObjReader objReader = new ObjReader(filename, defaultColor);
		objReader.read();
		objReader.render(this);
	}

}
