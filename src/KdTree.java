import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.Stack;
import edu.princeton.cs.algs4.StdDraw;

public class KdTree {
    private Node root;
    private Point2D champ;

    private static class Node {
        Node left, right;
        Point2D point;
        int level;
        int size;

        public Node(Node left, Node right, Point2D point, int level, int size) {
            this.left = left;
            this.right = right;
            this.point = point;
            this.level = level;
            this.size = size;
        }
    }

    // construct the Tree
    public KdTree() {
        root = new Node(null, null, null, 0, 0);
    }

    // is the set empty?
    public boolean isEmpty() {
        return size() == 0;
    }

    // number of points in the set
    public int size() {
        return root.size;
    }

    private Node put(Node itr, Point2D p, double px, double py, int level) {
        if (itr == null) return new Node(null, null, p, level + 1, 1);
        if (itr.level % 2 == 0) { // for the even level(vertical)
            if (px < itr.point.x()) {
                itr.left = put(itr.left, p, px, py, itr.level);
            } else {
                itr.right = put(itr.right, p, px, py, itr.level);
            }
        } else { // for the odd level(horizontal)
            if (py < itr.point.y())
                itr.left = put(itr.left, p, px, py, itr.level);
            else
                itr.right = put(itr.right, p, px, py, itr.level);
        }
        itr.size = itr.size + 1;
        return itr;
    }

    // add the point to the Tree (if it is not already in the Tree)
    public void insert(Point2D p) {
        if (p == null)
            throw new IllegalArgumentException();
        if (root.size == 0) {
            root = new Node(null, null, p, 0, 1);
            return;
        }
        if (contains(p))
            return;
        root = put(root, p, p.x(), p.y(), root.level);
    }

    // does the Tree contain point p?
    public boolean contains(Point2D p) {
        if (p == null)
            throw new IllegalArgumentException();
        double px = p.x();
        double py = p.y();
        Node itr = root;
        while (itr != null) {
            if (itr.level % 2 == 0) {
                if (p.equals(itr.point)) return true;
                else if (px < itr.point.x()) itr = itr.left;
                else itr = itr.right;
            } else {
                if (p.equals(itr.point)) return true;
                else if (py < itr.point.y()) itr = itr.left;
                else itr = itr.right;
            }
        }
        return false;
    }

    private void drawPointsRecursive(Node itr) {
        if (itr == null)
            return;
        itr.point.draw();
        drawPointsRecursive(itr.left);
        drawPointsRecursive(itr.right);
    }

    private void drawLinesRecursive(Node itr, Node itrParent, double xMin, double yMin, double xMax, double yMax) {
        if (itr == null)
            return;
        if (itr.level % 2 == 0) {
            StdDraw.setPenColor(StdDraw.RED);
            if (itrParent == null) {// for root
                StdDraw.line(itr.point.x(), 0, itr.point.x(), 1);
            } else {
                if (itr.point.y() < itrParent.point.y()) { //down
                    StdDraw.line(itr.point.x(), yMin, itr.point.x(), itrParent.point.y());
                    yMax = itrParent.point.y();
                } else {// up
                    StdDraw.line(itr.point.x(), itrParent.point.y(), itr.point.x(), yMax);
                    yMin = itrParent.point.y();
                }
            }
        } else {
            StdDraw.setPenColor(StdDraw.BLUE);
            if (itr.point.x() < itrParent.point.x()) {//left
                StdDraw.line(xMin, itr.point.y(), itrParent.point.x(), itr.point.y());
                xMax = itrParent.point.x();
            } else {// right
                StdDraw.line(itrParent.point.x(), itr.point.y(), xMax, itr.point.y());
                xMin = itrParent.point.x();
            }
        }
        drawLinesRecursive(itr.left, itr, xMin, yMin, xMax, yMax);
        drawLinesRecursive(itr.right, itr, xMin, yMin, xMax, yMax);
    }

    // draw all points to standard draw
    public void draw() {
        StdDraw.enableDoubleBuffering();
        StdDraw.setPenRadius(0.01);
        drawPointsRecursive(root);
        StdDraw.setPenRadius(0.002);
        drawLinesRecursive(root, null, 0, 0, 1, 1);
        StdDraw.show();
    }

    private void rangeSearch(Node itr, RectHV rect, Stack<Point2D> points) {
        if (itr == null) return;
        if (rect.contains(itr.point)) {
            points.push(itr.point);
        }
        if (itr.level % 2 == 0) {
            if (rect.xmax() < itr.point.x()) {
                rangeSearch(itr.left, rect, points);
            } else if (rect.xmin() > itr.point.x()) {
                rangeSearch(itr.right, rect, points);
            } else {
                rangeSearch(itr.left, rect, points);
                rangeSearch(itr.right, rect, points);
            }
        } else {
            if (rect.ymax() < itr.point.y()) {
                rangeSearch(itr.left, rect, points);
            } else if (rect.ymin() > itr.point.y()) {
                rangeSearch(itr.right, rect, points);
            } else {
                rangeSearch(itr.left, rect, points);
                rangeSearch(itr.right, rect, points);
            }
        }
    }

    // all points that are inside the rectangle (or on the boundary)
    public Iterable<Point2D> range(RectHV rect) {
        if (rect == null)
            throw new IllegalArgumentException();
        Stack<Point2D> points = new Stack<>();
        rangeSearch(root, rect, points);
        return points;
    }

    private void nearestSearch(Point2D query, double qx, double qy, Node itr, double champdist) {
        if (itr == null) return;
        if (query.distanceSquaredTo(itr.point) < champdist) {
            champ = itr.point;
            champdist = query.distanceSquaredTo(champ);
        }
        if (itr.level % 2 == 0) {
            if (qx < itr.point.x()) {//on the left side
                nearestSearch(query, qx, qy, itr.left, champdist);
                if (!champ.equals(itr.point))
                    nearestSearch(query, qx, qy, itr.right, champdist);
            } else {
                nearestSearch(query, qx, qy, itr.right, champdist);
                if (!champ.equals(itr.point))
                    nearestSearch(query, qx, qy, itr.left, champdist);
            }
        } else {
            if (qy < itr.point.y()) {
                nearestSearch(query, qx, qy, itr.left, champdist);
                nearestSearch(query, qx, qy, itr.right, champdist);
            } else {
                nearestSearch(query, qx, qy, itr.right, champdist);
                nearestSearch(query, qx, qy, itr.left, champdist);
            }
        }

    }


    // a nearest neighbor in the Tree to point p; null if the set is empty
    public Point2D nearest(Point2D p) {
        if (p == null)
            throw new IllegalArgumentException();
        champ = new Point2D(Integer.MAX_VALUE, Integer.MAX_VALUE);
        nearestSearch(p, p.x(), p.y(), root, p.distanceSquaredTo(champ));
        return champ;
    }


    // unit testing of the methods (optional)
    public static void main(String[] args) {
        KdTree test = new KdTree();
        test.insert(new Point2D(0.7, 0.2));
        test.insert(new Point2D(0.5, 0.4));
        test.insert(new Point2D(0.2, 0.3));
        test.insert(new Point2D(0.4, 0.7));
        test.insert(new Point2D(0.9, 0.6));
        RectHV rectHV = new RectHV(0.25, 0, 0.5, 0.75);
        Stack<Point2D> stack = (Stack<Point2D>) test.range(rectHV);
        System.out.println(test.isEmpty());
        for (Point2D point2D : stack) {
            System.out.println(point2D.toString());
        }

        Point2D query = new Point2D(0, 0);
        System.out.println(test.nearest(query).toString());
        System.out.println(test.contains(query));
        System.out.println(test.size());
        test.draw();

    }
}


