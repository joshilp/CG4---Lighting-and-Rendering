package line;

import geometry.Vertex3D;
import polygon.Polygon;
import shading.PixelShader;
import windowing.drawable.Drawable;

public interface LineRenderer {
	public void drawLine(Vertex3D p1, Vertex3D p2, Drawable panel, Polygon polygon, PixelShader pixelShader);
	
	default public void drawLine(Vertex3D p1, Vertex3D p2, Drawable panel) 
	{  
		drawLine(p1, p2, panel, null, (poly, vert) -> vert.getColor());
	};
}
