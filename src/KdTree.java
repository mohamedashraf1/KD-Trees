import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.StdDraw;

import java.util.Stack;

public class KdTree {
    private Node root;


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

    private Node put(Node itr, Point2D p, int level) {
        if (itr == null) return new Node(null, null, p, level + 1, 1);
        if (itr.level % 2 == 0) { // for the even level(vertical)
            if (p.x() < itr.point.x()) {
                itr.left = put(itr.left, p, itr.level);
            } else {
                itr.right = put(itr.right, p, itr.level);
            }
        } else { // for the odd level(horizontal)
            if (p.y() < itr.point.y())
                itr.left = put(itr.left, p, itr.level);
            else
                itr.right = put(itr.right, p, itr.level);
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
        root = put(root, p, root.level);
    }

    // does the Tree contain point p?
    public boolean contains(Point2D p) {
        if (p == null)
            throw new IllegalArgumentException();
        Node itr = root;
        while (itr != null) {
            if (itr.level % 2 == 0) {
                if (p.x() < itr.point.x()) itr = itr.left;
                else if (p.x() > itr.point.x()) itr = itr.right;
                else if (p.equals(itr.point)) return true;
            } else {
                if (p.y() < itr.point.y()) itr = itr.left;
                else if (p.y() > itr.point.y()) itr = itr.right;
                else if (p.equals(itr.point)) return true;
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
        drawPointsRecursive(root);
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

    private void nearestSearch(Point2D query, Node itr) {
        if (itr == null) return;
        if (query.distanceTo(itr.point) < query.distanceTo(champ)) {
            champ = itr.point;
        }
        if (itr.level % 2 == 0) {
            if (query.x() < itr.point.x()) {//on the left side
                nearestSearch(query, itr.left);
                if (!champ.equals(itr.point))
                    nearestSearch(query, itr.right);
            } else {
                nearestSearch(query, itr.right);
                if (!champ.equals(itr.point))
                    nearestSearch(query, itr.left);
            }
        } else {
            if (query.y() < itr.point.y()) {
                nearestSearch(query, itr.left);
                nearestSearch(query, itr.right);
            } else {
                nearestSearch(query, itr.right);
                nearestSearch(query, itr.left);
            }
        }

    }

    private Point2D champ;

    // a nearest neighbor in the Tree to point p; null if the set is empty
    public Point2D nearest(Point2D p) {
        champ = new Point2D(Integer.MAX_VALUE, Integer.MAX_VALUE);
        nearestSearch(p, root);
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
        test.draw();
    }
}


