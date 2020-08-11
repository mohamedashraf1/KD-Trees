import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.StdDraw;

import java.util.Stack;
import java.util.TreeSet;

public class PointSET {
    TreeSet<Point2D> mySet;

    // construct an empty set of points
    public PointSET() {
        mySet = new TreeSet<>();
    }

    // is the set empty?
    public boolean isEmpty() {
        return size() == 0;
    }

    // number of points in the set
    public int size() {
        return mySet.size();
    }

    // add the point to the set (if it is not already in the set)
    public void insert(Point2D p) {
        if (p == null)
            throw new IllegalArgumentException();
        mySet.add(p);
    }

    // does the set contain point p?
    public boolean contains(Point2D p) {
        if (p == null)
            throw new IllegalArgumentException();
        return mySet.contains(p);
    }

    // draw all points to standard draw
    public void draw() {
        StdDraw.enableDoubleBuffering();
        for (Point2D p : mySet) {
            p.draw();
        }
        StdDraw.show();
    }

    // all points that are inside the rectangle (or on the boundary)
    public Iterable<Point2D> range(RectHV rect) {
        if (rect == null)
            throw new IllegalArgumentException();
        Stack<Point2D> points = new Stack<>();
        for (Point2D p : mySet) {
            if (rect.contains(p)) {
                points.push(p);
            }
        }
        return points;
    }

    // a nearest neighbor in the set to point p; null if the set is empty
    public Point2D nearest(Point2D p) {
        if (p == null)
            throw new IllegalArgumentException();
        Point2D nearest = new Point2D(Integer.MAX_VALUE, Integer.MAX_VALUE);
        for (Point2D itr : mySet) {
            if (p.distanceTo(nearest) > p.distanceTo(itr)) {
                nearest = itr;
            }
        }
        return nearest;
    }

    // unit testing of the methods (optional)
    public static void main(String[] args) {
        PointSET test = new PointSET();
        System.out.println(test.isEmpty());
        test.insert(new Point2D(0.25, 0.25));
        test.insert(new Point2D(0.25, 0.5));
        test.insert(new Point2D(0.5, 0.25));
        test.insert(new Point2D(0.5, 0.5));

        System.out.println(test.isEmpty());
        System.out.println(test.size());
        test.draw();
        System.out.println(test.contains(new Point2D(0.5, 0.5)));
        RectHV rectHV = new RectHV(0, 0, 0.3, 0.5);
        for (Point2D p : test.range(rectHV))
            System.out.println(p.toString());
        Point2D nearest = new Point2D(0, 0);
        System.out.println(test.nearest(nearest));
    }
}
