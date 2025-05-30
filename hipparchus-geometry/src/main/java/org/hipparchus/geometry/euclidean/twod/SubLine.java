/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This is not the original file distributed by the Apache Software Foundation
 * It has been modified by the Hipparchus project
 */
package org.hipparchus.geometry.euclidean.twod;

import java.util.ArrayList;
import java.util.List;

import org.hipparchus.geometry.euclidean.oned.Euclidean1D;
import org.hipparchus.geometry.euclidean.oned.Interval;
import org.hipparchus.geometry.euclidean.oned.IntervalsSet;
import org.hipparchus.geometry.euclidean.oned.OrientedPoint;
import org.hipparchus.geometry.euclidean.oned.SubOrientedPoint;
import org.hipparchus.geometry.euclidean.oned.Vector1D;
import org.hipparchus.geometry.partitioning.AbstractSubHyperplane;
import org.hipparchus.geometry.partitioning.BSPTree;
import org.hipparchus.geometry.partitioning.Region;
import org.hipparchus.geometry.partitioning.Region.Location;
import org.hipparchus.util.FastMath;

/** This class represents a sub-hyperplane for {@link Line}.
 */
public class SubLine
    extends AbstractSubHyperplane<Euclidean2D, Vector2D, Line, SubLine, Euclidean1D, Vector1D, OrientedPoint, SubOrientedPoint> {

    /** Simple constructor.
     * @param hyperplane underlying hyperplane
     * @param remainingRegion remaining region of the hyperplane
     */
    public SubLine(final Line hyperplane,
                   final Region<Euclidean1D, Vector1D, OrientedPoint, SubOrientedPoint> remainingRegion) {
        super(hyperplane, remainingRegion);
    }

    /** Create a sub-line from two endpoints.
     * @param start start point
     * @param end end point
     * @param tolerance tolerance below which points are considered identical
     */
    public SubLine(final Vector2D start, final Vector2D end, final double tolerance) {
        super(new Line(start, end, tolerance), buildIntervalSet(start, end, tolerance));
    }

    /** Create a sub-line from a segment.
     * @param segment single segment forming the sub-line
     */
    public SubLine(final Segment segment) {
        super(segment.getLine(),
              buildIntervalSet(segment.getStart(), segment.getEnd(), segment.getLine().getTolerance()));
    }

    /** Get the endpoints of the sub-line.
     * <p>
     * A subline may be any arbitrary number of disjoints segments, so the endpoints
     * are provided as a list of endpoint pairs. Each element of the list represents
     * one segment, and each segment contains a start point at index 0 and an end point
     * at index 1. If the sub-line is unbounded in the negative infinity direction,
     * the start point of the first segment will have infinite coordinates. If the
     * sub-line is unbounded in the positive infinity direction, the end point of the
     * last segment will have infinite coordinates. So a sub-line covering the whole
     * line will contain just one row and both elements of this row will have infinite
     * coordinates. If the sub-line is empty, the returned list will contain 0 segments.
     * </p>
     * @return list of segments endpoints
     */
    public List<Segment> getSegments() {

        final List<Interval> list = ((IntervalsSet) getRemainingRegion()).asList();
        final List<Segment> segments = new ArrayList<>(list.size());

        for (final Interval interval : list) {
            final Vector2D start = getHyperplane().toSpace(new Vector1D(interval.getInf()));
            final Vector2D end   = getHyperplane().toSpace(new Vector1D(interval.getSup()));
            segments.add(new Segment(start, end, getHyperplane()));
        }

        return segments;

    }

    /** Get the intersection of the instance and another sub-line.
     * <p>
     * This method is related to the {@link Line#intersection(Line)
     * intersection} method in the {@link Line Line} class, but in addition
     * to compute the point along infinite lines, it also checks the point
     * lies on both sub-line ranges.
     * </p>
     * @param subLine other sub-line which may intersect instance
     * @param includeEndPoints if true, endpoints are considered to belong to
     * instance (i.e. they are closed sets) and may be returned, otherwise endpoints
     * are considered to not belong to instance (i.e. they are open sets) and intersection
     * occurring on endpoints lead to null being returned
     * @return the intersection point if there is one, null if the sub-lines don't intersect
     */
    public Vector2D intersection(final SubLine subLine, final boolean includeEndPoints) {

        // retrieve the underlying lines
        Line line1 = getHyperplane();
        Line line2 = subLine.getHyperplane();

        // compute the intersection on infinite line
        Vector2D v2D = line1.intersection(line2);
        if (v2D == null) {
            return null;
        }

        // check location of point with respect to first sub-line
        Location loc1 = getRemainingRegion().checkPoint(line1.toSubSpace(v2D));

        // check location of point with respect to second sub-line
        Location loc2 = subLine.getRemainingRegion().checkPoint(line2.toSubSpace(v2D));

        if (includeEndPoints) {
            return ((loc1 != Location.OUTSIDE) && (loc2 != Location.OUTSIDE)) ? v2D : null;
        } else {
            return ((loc1 == Location.INSIDE) && (loc2 == Location.INSIDE)) ? v2D : null;
        }

    }

    /** Build an interval set from two points.
     * @param start start point
     * @param end end point
     * @param tolerance tolerance below which points are considered identical
     * @return an interval set
     */
    private static IntervalsSet buildIntervalSet(final Vector2D start, final Vector2D end, final double tolerance) {
        final Line line = new Line(start, end, tolerance);
        return new IntervalsSet(line.toSubSpace(start).getX(),
                                line.toSubSpace(end).getX(),
                                tolerance);
    }

    /** {@inheritDoc} */
    @Override
    protected SubLine buildNew(final Line hyperplane,
                               final Region<Euclidean1D, Vector1D, OrientedPoint, SubOrientedPoint> remainingRegion) {
        return new SubLine(hyperplane, remainingRegion);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D getInteriorPoint() {
        final Vector1D v = getRemainingRegion().getInteriorPoint();
        return isEmpty() ? null : getHyperplane().toSpace(v);
    }

    /** {@inheritDoc} */
    @Override
    public SplitSubHyperplane<Euclidean2D, Vector2D, Line, SubLine> split(final Line hyperplane) {

        final Line    thisLine  = getHyperplane();
        final Vector2D crossing = thisLine.intersection(hyperplane);
        final double tolerance  = thisLine.getTolerance();

        if (crossing == null) {
            // the lines are parallel
            final double global = hyperplane.getOffset(thisLine);
            if (global < -tolerance) {
                return new SplitSubHyperplane<>(null, this);
            } else if (global > tolerance) {
                return new SplitSubHyperplane<>(this, null);
            } else {
                return new SplitSubHyperplane<>(null, null);
            }
        }

        // the lines do intersect
        final boolean direct = FastMath.sin(thisLine.getAngle() - hyperplane.getAngle()) < 0;
        final Vector1D x      = thisLine.toSubSpace(crossing);
        final SubOrientedPoint subPlus  = new OrientedPoint(x, !direct, tolerance).wholeHyperplane();
        final SubOrientedPoint subMinus = new OrientedPoint(x,  direct, tolerance).wholeHyperplane();

        final BSPTree<Euclidean1D, Vector1D, OrientedPoint, SubOrientedPoint> splitTree =
                getRemainingRegion().getTree(false).split(subMinus);
        final BSPTree<Euclidean1D, Vector1D, OrientedPoint, SubOrientedPoint> plusTree  =
                getRemainingRegion().isEmpty(splitTree.getPlus()) ?
                                             new BSPTree<>(Boolean.FALSE) :
                                             new BSPTree<>(subPlus, new BSPTree<>(Boolean.FALSE), splitTree.getPlus(), null);
        final BSPTree<Euclidean1D, Vector1D, OrientedPoint, SubOrientedPoint> minusTree =
                getRemainingRegion().isEmpty(splitTree.getMinus()) ?
                                             new BSPTree<>(Boolean.FALSE) :
                                             new BSPTree<>(subMinus, new BSPTree<>(Boolean.FALSE), splitTree.getMinus(), null);
        return new SplitSubHyperplane<>(new SubLine(thisLine.copySelf(), new IntervalsSet(plusTree, tolerance)),
                                        new SubLine(thisLine.copySelf(), new IntervalsSet(minusTree, tolerance)));

    }

}
