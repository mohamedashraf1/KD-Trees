import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.Stack;
import edu.princeton.cs.algs4.StdDraw;


public class KdTree {
    private Node root;
    private Point2D champ;
    private double champdist;

    private static class Node {
        Node left, right;
        Point2D point;
        int level;
        int size;
        RectHV rect;

        public Node(Node left, Node right, Point2D point, int level, int size, RectHV rect) {
            this.left = left;
            this.right = right;
            this.point = point;
            this.level = level;
            this.size = size;
            this.rect = rect;
        }
    }

    // construct the Tree
    public KdTree() {
        root = new Node(null, null, null, 0, 0, new RectHV(0, 0, 1, 1));

    }

    // is the set empty?
    public boolean isEmpty() {
        return size() == 0;
    }

    // number of points in the set
    public int size() {
        return root.size;
    }

    private Node put(Node itr, Point2D p, double px, double py, int level, double xMin, double yMin, double xMax, double yMax) {
        if (itr == null) return new Node(null, null, p, level + 1, 1, new RectHV(xMin, yMin, xMax, yMax));
        if (itr.level % 2 == 0) { // for the even level(vertical)
            if (px < itr.point.x()) {
                xMax = itr.point.x();
                itr.left = put(itr.left, p, px, py, itr.level, xMin, yMin, xMax, yMax);
            } else {
                xMin = itr.point.x();
                itr.right = put(itr.right, p, px, py, itr.level, xMin, yMin, xMax, yMax);
            }
        } else { // for the odd level(horizontal)
            if (py < itr.point.y()) {
                yMax = itr.point.y();
                itr.left = put(itr.left, p, px, py, itr.level, xMin, yMin, xMax, yMax);
            } else {
                yMin = itr.point.y();
                itr.right = put(itr.right, p, px, py, itr.level, xMin, yMin, xMax, yMax);
            }
        }
        itr.size = itr.size + 1;
        return itr;
    }

    // add the point to the Tree (if it is not already in the Tree)
    public void insert(Point2D p) {
        if (p == null)
            throw new IllegalArgumentException();
        if (root.size == 0) {
            root = new Node(null, null, p, 0, 1, new RectHV(0, 0, 1, 1));
            return;
        }
        if (contains(p))
            return;
        root = put(root, p, p.x(), p.y(), root.level, 0, 0, 1, 1);
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

    private void drawLinesRecursive(Node itr) {
        if (itr == null)
            return;
        if (itr.level % 2 == 0) {
            StdDraw.setPenColor(StdDraw.RED);
            StdDraw.line(itr.point.x(), itr.rect.ymin(), itr.point.x(), itr.rect.ymax());
        } else {
            StdDraw.setPenColor(StdDraw.BLUE);
            StdDraw.line(itr.rect.xmin(), itr.point.y(), itr.rect.xmax(), itr.point.y());
        }
        drawLinesRecursive(itr.left);
        drawLinesRecursive(itr.right);
    }

    // draw all points to standard draw
    public void draw() {
        StdDraw.enableDoubleBuffering();
        StdDraw.setPenRadius(0.01);
        drawPointsRecursive(root);
        StdDraw.setPenRadius(0.002);
        drawLinesRecursive(root);
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


    private void nearestSearch(Point2D query, double qx, double qy, Node itr) {
        if (itr == null) return;
        if (champdist < itr.rect.distanceSquaredTo(query)) {
            return;
        }

        if (query.distanceSquaredTo(itr.point) < champdist) {
            champ = itr.point;
            champdist = itr.point.distanceSquaredTo(query);
        }

        if (itr.level % 2 == 0) {
            if (qx < itr.point.x()) {//on the left side
                nearestSearch(query, qx, qy, itr.left);
                nearestSearch(query, qx, qy, itr.right);
            } else {
                nearestSearch(query, qx, qy, itr.right);
                nearestSearch(query, qx, qy, itr.left);
            }
        } else {
            if (qy < itr.point.y()) {
                nearestSearch(query, qx, qy, itr.left);
                nearestSearch(query, qx, qy, itr.right);
            } else {
                nearestSearch(query, qx, qy, itr.right);
                nearestSearch(query, qx, qy, itr.left);
            }
        }
    }

    // a nearest neighbor in the Tree to point p; null if the set is empty
    public Point2D nearest(Point2D p) {
        if (p == null)
            throw new IllegalArgumentException();
        //champ = new Point2D(Integer.MAX_VALUE, Integer.MAX_VALUE);
        champ = root.point;
        champdist = p.distanceSquaredTo(champ);
        nearestSearch(p, p.x(), p.y(), root);
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
//        String filename = args[0];
//        In in = new In(filename);
//
//        KdTree test = new KdTree();
//        while (!in.isEmpty()) {
//            double x = in.readDouble();
//            double y = in.readDouble();
//            Point2D p = new Point2D(x, y);
//            test.insert(p);
//
//        }
        //test.print(test.root);

//        RectHV rectHV = new RectHV(0.25, 0, 0.5, 0.75);
//        Stack<Point2D> stack = (Stack<Point2D>) test.range(rectHV);
        System.out.println(test.isEmpty());
//        for (Point2D point2D : stack) {
//            System.out.println(point2D.toString());
//        }

        Point2D query = new Point2D(0.81, 0.30);
        ///Point2D query2 = new Point2D(0.5, 0.5);
        System.out.println(test.nearest(query).toString());


        test.draw();

    }
}


