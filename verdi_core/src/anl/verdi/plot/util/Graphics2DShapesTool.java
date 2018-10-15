package anl.verdi.plot.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;

public class Graphics2DShapesTool {
	private static final long serialVersionUID = 1168616356843100388L;

	public Graphics2DShapesTool() {
		//Note: no-op
	}
	
	public void draw4PointStar(int X, int Y, int size, Graphics2D g2d, Color color) {
		int x1 = (X - size) < 0 ? 0 : X - size;
		int x2 = X;
		int x3 = X + size;
		int x4 = x2;
		int y1 = Y;
		int y2 = (Y - size) < 0 ? 0 : Y - size;
		int y3 = y1;
		int y4 = Y + size;

		GeneralPath star = new GeneralPath(Path2D.WIND_EVEN_ODD);

		star.moveTo(x1, y1);
		star.quadTo(X, Y, x2, y2) ;
		star.quadTo(X, Y, x3, y3);
		star.quadTo(X, Y, x4, y4);
		star.quadTo(X, Y, x1, y1);
		star.closePath();

		g2d.setColor(color);
		g2d.fill(star);
		g2d.setColor(Color.BLACK);
		g2d.draw(star);
	}
	
	public void draw8PointStar(int X, int Y, int size, Graphics2D g2d, Color color) {
		int[] x = {X - size/2, 
				X - size/4, 
				X - size/4 - 1, 
				X - size/4 + 1, 
				X, 
				X + size/4 - 1, 
				X + size/4 + 1, 
				X + size/4, 
				X + size/2, 
				X + size/4,
				X + size/4 + 1, 
				X + size/4 - 1, 
				X, 
				X - size/4 + 1, 
				X - size/4 - 1,  
				X - size/4};
		int[] y = {Y, 
				Y - size/4 + 1, 
				Y - size/4 - 1, 
                Y - size/4,  
                Y - size/2, 
                Y - size/4,  
                Y - size/4 - 1, 
                Y - size/4 + 1, 
                Y, 
                Y + size/4 - 1, 
                Y + size/4 + 1, 
                Y + size/4, 
                Y + size/2, 
                Y + size/4, 
                Y + size/4 + 1, 
                Y + size/4 - 1};
		
		GeneralPath star = new GeneralPath(Path2D.WIND_EVEN_ODD); // create GeneralPath object
		star.moveTo(x[0], y[0]);
		
		for (int count = 1; count < x.length; count++)
			star.lineTo(x[count], y[count]);
		
		star.closePath();
		
		g2d.setColor(color);
		g2d.fill(star);
		g2d.setColor(Color.BLACK);
		g2d.draw(star);
	}
	
	public void drawTriangle(int X, int Y, int size, Graphics2D g2d, Color color) {
		int x1 = (X - size/2) < 0 ? 0 : X - size/2;
		int x2 = X;
		int x3 = X + size/2;
		int y1 = Y + size/2;
		int y2 = (Y - size/2) < 0 ? 0 : Y - size/2;
		int y3 = y1;
		
		int[] xPoints = new int[]{x1, x2, x3};
		int[] yPoints = new int[]{y1, y2, y3};
		
		g2d.setColor(color);
		g2d.fillPolygon(xPoints, yPoints, 3);
		g2d.setColor(Color.BLACK);
		g2d.drawPolygon(xPoints, yPoints, 3);
	}
	
	public void drawSquare(int X, int Y, int size, Graphics2D g2d, Color color) {
		int x1 = (X - size/2) < 0 ? 0 : X - size/2;
		int x2 = X + size/2;
		int x3 = x2;
		int x4 = x1;
		int y1 = (Y - size/2) < 0 ? 0 : Y -size/2;
		int y2 = y1;
		int y3 = Y + size/2;
		int y4 = y3;
		
		int[] xPoints = new int[]{x1, x2, x3, x4};
		int[] yPoints = new int[]{y1, y2, y3, y4};
		
		g2d.setColor(color);
		g2d.fillPolygon(xPoints, yPoints, 4);
		g2d.setColor(Color.BLACK);
		g2d.drawPolygon(xPoints, yPoints, 4);
	}
	
	public void drawDiamond(int X, int Y, int size, Graphics2D g2d, Color color) {
		int x1 = (X - size/2) < 0 ? 0 : X - size/2;
		int x2 = X;
		int x3 = X + size/2;
		int x4 = X;
		int y1 = Y;
		int y2 = (Y - size/2) < 0 ? 0 : Y - size/2;
		int y3 = Y;
		int y4 = Y + size/2;
		
		int[] xPoints = new int[]{x1, x2, x3, x4};
		int[] yPoints = new int[]{y1, y2, y3, y4};
		
		g2d.setColor(color);
		g2d.fillPolygon(xPoints, yPoints, 4);
		g2d.setColor(Color.BLACK);
		g2d.drawPolygon(xPoints, yPoints, 4);
	}
	
} 
