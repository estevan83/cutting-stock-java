import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

public class PolygonNesting {
    public static void main(String[] args) {
        // Definizione del poligono irregolare
        Path2D.Double polygon = new Path2D.Double();
        polygon.moveTo(0, 0);
        polygon.lineTo(100, 0);
        polygon.lineTo(100, 50);
        polygon.lineTo(50, 100);
        polygon.lineTo(0, 100);
        polygon.closePath();

        Area polygonArea = new Area(polygon);
        double originalPolygonArea = computePolygonArea(polygon);
        
        // Lista di rettangoli da inserire
        List<Rectangle2D.Double> rectangles = new ArrayList<>();
        rectangles.add(new Rectangle2D.Double(0, 0, 30, 20));
        rectangles.add(new Rectangle2D.Double(0, 0, 40, 30));
        rectangles.add(new Rectangle2D.Double(0, 0, 20, 10));
        
        List<Rectangle2D.Double> placedRectangles = bottomLeftNest(polygonArea, rectangles);
        double remainingPolygonArea = computeRemainingArea(originalPolygonArea, placedRectangles);
        double[] newDimensions = computeBoundingBox(placedRectangles);
        
        System.out.println("Area originale del poligono: " + originalPolygonArea);
        System.out.println("Area rimanente del poligono: " + remainingPolygonArea);
        System.out.println("Nuove dimensioni del poligono: Larghezza = " + newDimensions[0] + ", Altezza = " + newDimensions[1]);
        System.out.println("Rettangoli posizionati:");
        for (Rectangle2D.Double rect : placedRectangles) {
            System.out.println("Posizione: (" + rect.getX() + ", " + rect.getY() + ") Dimensioni: " + rect.getWidth() + "x" + rect.getHeight());
        }
    }

    public static List<Rectangle2D.Double> bottomLeftNest(Area polygonArea, List<Rectangle2D.Double> rectangles) {
        List<Rectangle2D.Double> placedRectangles = new ArrayList<>();
        rectangles.sort(Comparator.comparingDouble(Rectangle2D.Double::getHeight).reversed()); // Ordina per altezza decrescente
        
        double x = 0, y = 0, maxWidth = 0;
        for (Rectangle2D.Double rect : rectangles) {
            if (x + rect.getWidth() > 100) { // Se supera il bordo, va a capo
                x = 0;
                y += maxWidth;
                maxWidth = 0;
            }
            if (y + rect.getHeight() <= 100) { // Verifica che non superi il limite del poligono
                rect.setRect(x, y, rect.getWidth(), rect.getHeight());
                placedRectangles.add(rect);
                x += rect.getWidth();
                maxWidth = Math.max(maxWidth, rect.getHeight());
            }
        }
        return placedRectangles;
    }

    private static double computePolygonArea(Path2D.Double polygon) {
        double area = 0;
        double[] xPoints = new double[5];
        double[] yPoints = new double[5];
        int nPoints = 0;
        
        for (var it = polygon.getPathIterator(null); !it.isDone(); it.next()) {
            double[] coords = new double[2];
            it.currentSegment(coords);
            xPoints[nPoints] = coords[0];
            yPoints[nPoints] = coords[1];
            nPoints++;
        }
        
        for (int i = 0; i < nPoints - 1; i++) {
            area += (xPoints[i] * yPoints[i + 1]) - (yPoints[i] * xPoints[i + 1]);
        }
        return Math.abs(area / 2.0);
    }
    
    private static double computeRemainingArea(double originalPolygonArea, List<Rectangle2D.Double> placedRectangles) {
        double usedArea = 0;
        for (Rectangle2D.Double rect : placedRectangles) {
            usedArea += rect.getWidth() * rect.getHeight();
        }
        return Math.max(originalPolygonArea - usedArea, 0);
    }
    
    private static double[] computeBoundingBox(List<Rectangle2D.Double> placedRectangles) {
        double maxX = 0, maxY = 0;
        for (Rectangle2D.Double rect : placedRectangles) {
            maxX = Math.max(maxX, rect.getX() + rect.getWidth());
            maxY = Math.max(maxY, rect.getY() + rect.getHeight());
        }
        return new double[]{maxX, maxY};
    }
}
