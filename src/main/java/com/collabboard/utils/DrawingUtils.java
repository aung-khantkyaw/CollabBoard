package com.collabboard.utils;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import com.collabboard.models.DrawingAction;

/**
 * Utility class for drawing operations on the whiteboard
 */
public class DrawingUtils {
    
    /**
     * Draw a line on the graphics context
     * @param g2d Graphics2D context
     * @param points List of points for the line
     * @param color Color of the line
     * @param strokeWidth Width of the stroke
     */
    public static void drawLine(Graphics2D g2d, List<Point> points, Color color, int strokeWidth) {
        if (points.size() < 2) return;
        
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            g2d.draw(new Line2D.Double(p1.x, p1.y, p2.x, p2.y));
        }
    }
    
    /**
     * Draw a rectangle on the graphics context
     * @param g2d Graphics2D context
     * @param points List of points (should contain start and end points)
     * @param color Color of the rectangle
     * @param strokeWidth Width of the stroke
     */
    public static void drawRectangle(Graphics2D g2d, List<Point> points, Color color, int strokeWidth) {
        if (points.size() < 2) return;
        
        Point start = points.get(0);
        Point end = points.get(points.size() - 1);
        
        int x = Math.min(start.x, end.x);
        int y = Math.min(start.y, end.y);
        int width = Math.abs(end.x - start.x);
        int height = Math.abs(end.y - start.y);
        
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(strokeWidth));
        g2d.draw(new Rectangle2D.Double(x, y, width, height));
    }
    
    /**
     * Draw a circle on the graphics context
     * @param g2d Graphics2D context
     * @param points List of points (should contain center and edge points)
     * @param color Color of the circle
     * @param strokeWidth Width of the stroke
     */
    public static void drawCircle(Graphics2D g2d, List<Point> points, Color color, int strokeWidth) {
        if (points.size() < 2) return;
        
        Point center = points.get(0);
        Point edge = points.get(points.size() - 1);
        
        int radius = (int) Math.sqrt(Math.pow(edge.x - center.x, 2) + Math.pow(edge.y - center.y, 2));
        
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(strokeWidth));
        g2d.draw(new Ellipse2D.Double(center.x - radius, center.y - radius, radius * 2, radius * 2));
    }
    
    /**
     * Draw text on the graphics context
     * @param g2d Graphics2D context
     * @param text Text to draw
     * @param position Position to draw the text
     * @param color Color of the text
     */
    public static void drawText(Graphics2D g2d, String text, Point position, Color color) {
        g2d.setColor(color);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(text, position.x, position.y + fm.getAscent());
    }
    
    /**
     * Render a drawing action on the graphics context
     * @param g2d Graphics2D context
     * @param action Drawing action to render
     */
    public static void renderDrawingAction(Graphics2D g2d, DrawingAction action) {
        switch (action.getActionType()) {
            case DRAW_LINE:
                drawLine(g2d, action.getPoints(), action.getColor(), action.getStrokeWidth());
                break;
            case DRAW_RECTANGLE:
                drawRectangle(g2d, action.getPoints(), action.getColor(), action.getStrokeWidth());
                break;
            case DRAW_CIRCLE:
                drawCircle(g2d, action.getPoints(), action.getColor(), action.getStrokeWidth());
                break;
            case DRAW_TEXT:
                if (action.getPoints().size() > 0) {
                    drawText(g2d, action.getText(), action.getPoints().get(0), action.getColor());
                }
                break;
            default:
                // Handle other action types if needed
                break;
        }
    }
    
    /**
     * Get color from RGB values
     * @param r Red component (0-255)
     * @param g Green component (0-255)
     * @param b Blue component (0-255)
     * @return Color object
     */
    public static Color getColor(int r, int g, int b) {
        return new Color(Math.max(0, Math.min(255, r)), 
                        Math.max(0, Math.min(255, g)), 
                        Math.max(0, Math.min(255, b)));
    }
    
    /**
     * Get predefined colors for the color palette
     * @return Array of predefined colors
     */
    public static Color[] getPredefinedColors() {
        return new Color[] {
            Color.BLACK, Color.RED, Color.GREEN, Color.BLUE,
            Color.YELLOW, Color.ORANGE, Color.PINK, Color.MAGENTA,
            Color.CYAN, Color.GRAY, Color.DARK_GRAY, Color.LIGHT_GRAY
        };
    }
    
    /**
     * Get predefined stroke widths
     * @return Array of predefined stroke widths
     */
    public static int[] getPredefinedStrokeWidths() {
        return new int[] { 1, 2, 3, 5, 8, 12, 16, 20 };
    }
    
    /**
     * Calculate distance between two points
     * @param p1 First point
     * @param p2 Second point
     * @return Distance between the points
     */
    public static double calculateDistance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
    }
    
    /**
     * Check if a point is near a line (for selection purposes)
     * @param point Point to check
     * @param linePoints Points that make up the line
     * @param tolerance Tolerance distance
     * @return true if point is near the line
     */
    public static boolean isPointNearLine(Point point, List<Point> linePoints, double tolerance) {
        if (linePoints.size() < 2) return false;
        
        for (int i = 0; i < linePoints.size() - 1; i++) {
            Point p1 = linePoints.get(i);
            Point p2 = linePoints.get(i + 1);
            
            double distance = distancePointToLine(point, p1, p2);
            if (distance <= tolerance) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Calculate distance from a point to a line segment
     * @param point The point
     * @param lineStart Start of the line segment
     * @param lineEnd End of the line segment
     * @return Distance from point to line segment
     */
    private static double distancePointToLine(Point point, Point lineStart, Point lineEnd) {
        double A = point.x - lineStart.x;
        double B = point.y - lineStart.y;
        double C = lineEnd.x - lineStart.x;
        double D = lineEnd.y - lineStart.y;
        
        double dot = A * C + B * D;
        double lenSq = C * C + D * D;
        double param = (lenSq != 0) ? dot / lenSq : -1;
        
        double xx, yy;
        
        if (param < 0) {
            xx = lineStart.x;
            yy = lineStart.y;
        } else if (param > 1) {
            xx = lineEnd.x;
            yy = lineEnd.y;
        } else {
            xx = lineStart.x + param * C;
            yy = lineStart.y + param * D;
        }
        
        double dx = point.x - xx;
        double dy = point.y - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
