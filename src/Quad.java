import java.awt.Graphics;

public class Quad {
    private int depth;  // Depth of this quad in the quad tree where root is depth 0
    private Body body;  // Body or aggregate body represented by this quad
    private Quad NW, NE, SW, SE; // Four sub quadrants: North West, North East, South West, South East
    private double xMid, yMid;   // Coordinate of center of quad
    private double length;       // Width and height of quad
    private final double THETA = 0.2;  // Parameter of the Barnes Hut algorithm.
                                       // THETA = 0 would give n^2 runtime
                                       // equivalent to the classic direct-sum
                                       // algorithm

    public Quad(double xMid, double yMid, double length, int depth) {
        this.body = null;
        this.xMid = xMid;
        this.yMid = yMid;
        this.length = length;
        this.depth = depth;
    }

    public void insert(Body b) {
        // If quad does not contain a body, put the body in it
        if (body == null) {
            body = b;
            return;
        }

        // If quad is internal...
        if (!isExternal()) {
            // Update aggregate mass and center of mass
            body = body.combine(b);
            // Recursively add body b to correct subquad
            insertToSubquadrants(b);
        }

        // Else quad is external and therefore now has 2 bodies...
        else {
            // Tree depth is limited. Arbitrarily close or numerous bodies are
            // approximated as a single body by combining them without creating subquads
            if (depth < 50) {
                // The quad is subdivided
                NW = NW();
                NE = NE();
                SW = SW();
                SE = SE();

                // Both bodies are inserted recursively to the correct sub-quad
                insertToSubquadrants(body);
                insertToSubquadrants(b);
            }

            // Update aggregate body mass and center of mass
            body = body.combine(b);
        }
    }

    public void insertToSubquadrants(Body b) {
        if (NW.contains(b))
            NW.insert(b);
        else if (NE.contains(b))
            NE.insert(b);
        else if (SW.contains(b))
            SW.insert(b);
        else if (SE.contains(b))
            SE.insert(b);
    }

    public void updateVelocityOf(Body b) {
        if (b == null || b.equals(body) || body == null) {
            return;
        }

        if (isExternal()) {
            b.updateVelocity(this.body);
        }

        else { // Quad is internal
            double dx = body.getX() - b.getX();
            double dy = body.getY() - b.getY();

            double d = Math.sqrt(dx * dx + dy * dy);
            double s = length;

            if (s / d < THETA) {
                b.updateVelocity(this.body);
            } else {
                NW.updateVelocityOf(b);
                NE.updateVelocityOf(b);
                SW.updateVelocityOf(b);
                SE.updateVelocityOf(b);
            }
        }

    }

    public boolean isExternal() {
        return (NW == null && NE == null && SW == null && SE == null);
    }

    /*
     * Returns whether body b is positioned within this quad
     */
    public boolean contains(Body b) {
        double x = b.getX();
        double y = b.getY();
        return (x >= (xMid - length / 2) && x <= (xMid + length / 2) && y >= (yMid - length / 2)
                && y <= (yMid + length / 2));
    }

    public Quad NW() {
        double x = xMid - length / 4;
        double y = yMid + length / 4;
        double len = length / 2;
        return new Quad(x, y, len, depth + 1);
    }

    public Quad NE() {
        double x = xMid + length / 4;
        double y = yMid + length / 4;
        double len = length / 2;
        return new Quad(x, y, len, depth + 1);
    }

    public Quad SW() {
        double x = xMid - length / 4;
        double y = yMid - length / 4;
        double len = length / 2;
        return new Quad(x, y, len, depth + 1);
    }

    public Quad SE() {
        double x = xMid + length / 4;
        double y = yMid - length / 4;
        double len = length / 2;
        return new Quad(x, y, len, depth + 1);
    }

    public void drawAll(Graphics g) {
        double x = xMid - length / 2;
        double y = yMid - length / 2;
        g.drawRect((int) x, (int) y, (int) length, (int) length);

        // If quad is internal, recursively draw all subquads
        if (!isExternal()) {
            NW.drawAll(g);
            NE.drawAll(g);
            SW.drawAll(g);
            SE.drawAll(g);
        }
    }
}
