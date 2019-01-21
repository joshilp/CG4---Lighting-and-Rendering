package polygon;

import shading.FaceShader;
import shading.PixelShader;
import shading.Shader;
import shading.VertexShader;
import windowing.drawable.Drawable;

public interface PolygonRenderer {
	// assumes polygon is ccw.
//	public void drawPolygon(Polygon polygon, Drawable drawable, Shader vertexShader);
	public void drawPolygon(Polygon polygon, Drawable drawable, FaceShader faceShader, VertexShader vertexShader, PixelShader pixelShader);

//	default public void drawPolygon(Polygon polygon, Drawable panel) {
//		drawPolygon(polygon, panel, c -> c);
//	};
}
