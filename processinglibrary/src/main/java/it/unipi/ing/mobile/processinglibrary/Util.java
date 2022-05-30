package it.unipi.ing.mobile.processinglibrary;

import java.util.ArrayList;
import java.util.stream.IntStream;

public class Util {

    public static final boolean OFFLOADED = true;

    public static Float getAverage(Float[] data){
        Float sum = new Float(0);
        for (Float sample : data) {
            sum += sample;
        }
        return sum/data.length;
    }

    public static Float getVariance(Float[] data){
        Float avg = getAverage(data);
        Float variance = new Float(0);
        for (Float sample : data) {
            variance += (float)Math.pow(sample - avg, 2);
        }
        variance /= data.length;
        return variance;
    }

    public static Float getRSS(Float[] data){
        Float rss = 0f;
        for (Float datum : data) {
            rss += (float)Math.pow(datum, 2);
        }
        float sqrt = (float) Math.sqrt(rss);
        return sqrt;
    }

    /**
     * Compute the Euclidean distance between two n-dimensional vector u and v taking into account
     * wrap around
     * i.e. the square root of the sum of the absolute values of the component-wise differences
     *
     * d = sqrt(abs(u_1 - v_1)^2 + abs(u_2 - v_2)^2 + ... + abs(u_n - v_n)^2)
     * @param u First vector
     * @param v Second vector
     * @param wrapValues
     * @return Euclidean distance between u and v
     * @throws IllegalArgumentException if either u or v has length zero or u and v have different
     * lengths
     */
    public static Float getEuclideanDistanceWithWrap(Float[] u, Float[] v, Float[] wrapValues) throws IllegalArgumentException {
        if(u.length == 0 || v.length == 0){
            throw new IllegalArgumentException("Vectors must have non-zero length.");
        }

        if(u.length != v.length){
            throw new IllegalArgumentException("Vectors must have the same length.");
        }

        int len = u.length;
        Float distance1 = new Float(0);
        Float distance2 = new Float(0);;
        float distanceSquared = 0;
        for (int i = 0; i < len; i++){
            distance1 = Math.abs(Math.max(u[i], v[i]) - Math.min(u[i], v[i]));
            distance2 = Math.abs(Math.min(u[i], v[i]) + wrapValues[i] - Math.max(u[i], v[i]));

            distanceSquared += Math.pow((Math.min(distance1, distance2)), 2);
        }
        return (float)Math.sqrt(distanceSquared);
    }

    /**
     * Component wise sum of two n-dimensional vectors
     * @param a first vector
     * @param b second vector
     * @return Vector addition of a and b
     * @throws IllegalArgumentException if the two vectors do not have the same size
     */
    public static Float[] vectorSum(Float[] a, Float[] b) throws IllegalArgumentException{
        if(a.length != b.length){
            throw new IllegalArgumentException("Start vector and shift vector must have the same length");
        }

        Float[] shifted = new Float[a.length];
        IntStream.range(0, a.length).forEach(i -> shifted[i] = a[i] + b[i]);
        return shifted;
    }

    /**
     * Given an n-dimensional point p and a set o n-dimensional vertices, find the vertex which is
     * the closest to p
     * @param p n-dimensional point whose nearest vertex has to be found
     * @param vertices array of n-dimensional points to search among
     * @param wrapValues
     * @return the index of the vertices array which corresponds to the point which is closest to p
     * @throws IllegalArgumentException if either p has length zero or the set of vertices is empty
     */
    public static int getClosestPointWithWrap(Float[] p, ArrayList<Float[]> vertices, Float[] wrapValues) throws IllegalArgumentException{
        if(p.length == 0){
            throw new IllegalArgumentException("Vector p must have non-zero length.");
        }
        if(vertices.size() == 0){
            throw new IllegalArgumentException("The set of vertices must be non-empty.");
        }

        float minDistance = getEuclideanDistanceWithWrap(p, vertices.get(0), wrapValues);
        int minIndex = -1;

        for (int i = 0; i < vertices.size(); i++) {
            float distance = 0;

            try {
                distance = getEuclideanDistanceWithWrap(p, vertices.get(i), wrapValues);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            if(distance < minDistance){
                minDistance = distance;
                minIndex = i;
            }
        }
        return minIndex;
    }
}
