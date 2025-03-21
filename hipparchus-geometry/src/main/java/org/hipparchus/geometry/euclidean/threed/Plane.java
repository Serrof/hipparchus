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
package org.hipparchus.geometry.euclidean.threed;

import org.hipparchus.exception.LocalizedCoreFormats;
import org.hipparchus.exception.MathRuntimeException;
import org.hipparchus.geometry.euclidean.oned.Vector1D;
import org.hipparchus.geometry.euclidean.twod.Euclidean2D;
import org.hipparchus.geometry.euclidean.twod.PolygonsSet;
import org.hipparchus.geometry.euclidean.twod.SubLine;
import org.hipparchus.geometry.euclidean.twod.Vector2D;
import org.hipparchus.geometry.partitioning.Embedding;
import org.hipparchus.geometry.partitioning.Hyperplane;
import org.hipparchus.geometry.partitioning.RegionFactory;
import org.hipparchus.util.FastMath;

/** The class represent planes in a three dimensional space.
 */
public class Plane
    implements Hyperplane<Euclidean3D, Vector3D, Plane, SubPlane>,
               Embedding<Euclidean3D, Vector3D,  Euclidean2D, Vector2D> {

    /** Offset of the origin with respect to the plane. */
    private double originOffset;

    /** Origin of the plane frame. */
    private Vector3D origin;

    /** First vector of the plane frame (in plane). */
    private Vector3D u;

    /** Second vector of the plane frame (in plane). */
    private Vector3D v;

    /** Third vector of the plane frame (plane normal). */
    private Vector3D w;

    /** Tolerance below which points are considered identical. */
    private final double tolerance;

    /** Build a plane normal to a given direction and containing the origin.
     * @param normal normal direction to the plane
     * @param tolerance tolerance below which points are considered identical
     * @exception MathRuntimeException if the normal norm is too small
     */
    public Plane(final Vector3D normal, final double tolerance)
        throws MathRuntimeException {
        setNormal(normal);
        this.tolerance = tolerance;
        originOffset = 0;
        setFrame();
    }

    /** Build a plane from a point and a normal.
     * @param p point belonging to the plane
     * @param normal normal direction to the plane
     * @param tolerance tolerance below which points are considered identical
     * @exception MathRuntimeException if the normal norm is too small
     */
    public Plane(final Vector3D p, final Vector3D normal, final double tolerance)
        throws MathRuntimeException {
        setNormal(normal);
        this.tolerance = tolerance;
        originOffset = -p.dotProduct(w);
        setFrame();
    }

    /** Build a plane from three points.
     * <p>The plane is oriented in the direction of
     * {@code (p2-p1) ^ (p3-p1)}</p>
     * @param p1 first point belonging to the plane
     * @param p2 second point belonging to the plane
     * @param p3 third point belonging to the plane
     * @param tolerance tolerance below which points are considered identical
     * @exception MathRuntimeException if the points do not constitute a plane
     */
    public Plane(final Vector3D p1, final Vector3D p2, final Vector3D p3, final double tolerance)
        throws MathRuntimeException {
        this(p1, p2.subtract(p1).crossProduct(p3.subtract(p1)), tolerance);
    }

    /** Copy constructor.
     * <p>The instance created is completely independent of the original
     * one. A deep copy is used, none of the underlying object are
     * shared.</p>
     * @param plane plane to copy
     */
    public Plane(final Plane plane) {
        originOffset = plane.originOffset;
        origin       = plane.origin;
        u            = plane.u;
        v            = plane.v;
        w            = plane.w;
        tolerance    = plane.tolerance;
    }

    /** Copy the instance.
     * <p>The instance created is completely independant of the original
     * one. A deep copy is used, none of the underlying objects are
     * shared (except for immutable objects).</p>
     * @return a new hyperplane, copy of the instance
     */
    @Override
    public Plane copySelf() {
        return new Plane(this);
    }

    /** Reset the instance as if built from a point and a normal.
     * @param p point belonging to the plane
     * @param normal normal direction to the plane
     * @exception MathRuntimeException if the normal norm is too small
     */
    public void reset(final Vector3D p, final Vector3D normal) throws MathRuntimeException {
        setNormal(normal);
        originOffset = -p.dotProduct(w);
        setFrame();
    }

    /** Reset the instance from another one.
     * <p>The updated instance is completely independant of the original
     * one. A deep reset is used none of the underlying object is
     * shared.</p>
     * @param original plane to reset from
     */
    public void reset(final Plane original) {
        originOffset = original.originOffset;
        origin       = original.origin;
        u            = original.u;
        v            = original.v;
        w            = original.w;
    }

    /** Set the normal vactor.
     * @param normal normal direction to the plane (will be copied)
     * @exception MathRuntimeException if the normal norm is too small
     */
    private void setNormal(final Vector3D normal) throws MathRuntimeException {
        final double norm = normal.getNorm();
        if (norm < 1.0e-10) {
            throw new MathRuntimeException(LocalizedCoreFormats.ZERO_NORM);
        }
        w = new Vector3D(1.0 / norm, normal);
    }

    /** Reset the plane frame.
     */
    private void setFrame() {
        origin = new Vector3D(-originOffset, w);
        u = w.orthogonal();
        v = Vector3D.crossProduct(w, u);
    }

    /** Get the origin point of the plane frame.
     * <p>The point returned is the orthogonal projection of the
     * 3D-space origin in the plane.</p>
     * @return the origin point of the plane frame (point closest to the
     * 3D-space origin)
     */
    public Vector3D getOrigin() {
        return origin;
    }

    /** Get the normalized normal vector.
     * <p>The frame defined by ({@link #getU() getU()}, {@link #getV() getV()},
     * {@code getNormal()}) is a right-handed orthonormalized
     * frame).</p>
     * @return normalized normal vector
     * @see #getU
     * @see #getV
     */
    public Vector3D getNormal() {
        return w;
    }

    /** Get the plane first canonical vector.
     * <p>The frame defined by ({@code getU()}, {@link #getV() getV()},
     * {@link #getNormal() getNormal()}) is a right-handed orthonormalized
     * frame).</p>
     * @return normalized first canonical vector
     * @see #getV
     * @see #getNormal
     */
    public Vector3D getU() {
        return u;
    }

    /** Get the plane second canonical vector.
     * <p>The frame defined by ({@link #getU() getU()}, {@code getV()},
     * {@link #getNormal() getNormal()}) is a right-handed orthonormalized
     * frame).</p>
     * @return normalized second canonical vector
     * @see #getU
     * @see #getNormal
     */
    public Vector3D getV() {
        return v;
    }

    /** {@inheritDoc}
     */
    @Override
    public Vector3D project(Vector3D point) {
        return toSpace(toSubSpace(point));
    }

    /** {@inheritDoc}
     */
    @Override
    public double getTolerance() {
        return tolerance;
    }

    /** Revert the plane.
     * <p>Replace the instance by a similar plane with opposite orientation.</p>
     * <p>The new plane frame is chosen in such a way that a 3D point that had
     * {@code (x, y)} in-plane coordinates and {@code z} offset with
     * respect to the plane and is unaffected by the change will have
     * {@code (y, x)} in-plane coordinates and {@code -z} offset with
     * respect to the new plane. This means that the {@code u} and {@code v}
     * vectors returned by the {@link #getU} and {@link #getV} methods are exchanged,
     * and the {@code w} vector returned by the {@link #getNormal} method is
     * reversed.</p>
     */
    public void revertSelf() {
        final Vector3D tmp = u;
        u = v;
        v = tmp;
        w = w.negate();
        originOffset = -originOffset;
    }

    /** Transform a 3D space point into an in-plane point.
     * @param point point of the space (must be a {@link Vector3D
     * Vector3D} instance)
     * @return in-plane point (really a {@link
     * org.hipparchus.geometry.euclidean.twod.Vector2D Vector2D} instance)
     * @see #toSpace
     */
    @Override
    public Vector2D toSubSpace(final Vector3D point) {
        return new Vector2D(point.dotProduct(u), point.dotProduct(v));
    }

    /** Transform an in-plane point into a 3D space point.
     * @param point in-plane point (must be a {@link
     * org.hipparchus.geometry.euclidean.twod.Vector2D Vector2D} instance)
     * @return 3D space point (really a {@link Vector3D Vector3D} instance)
     * @see #toSubSpace
     */
    @Override
    public Vector3D toSpace(final Vector2D point) {
        return new Vector3D(point.getX(), u, point.getY(), v, -originOffset, w);
    }

    /** Get one point from the 3D-space.
     * @param inPlane desired in-plane coordinates for the point in the
     * plane
     * @param offset desired offset for the point
     * @return one point in the 3D-space, with given coordinates and offset
     * relative to the plane
     */
    public Vector3D getPointAt(final Vector2D inPlane, final double offset) {
        return new Vector3D(inPlane.getX(), u, inPlane.getY(), v, offset - originOffset, w);
    }

    /** Check if the instance is similar to another plane.
     * <p>Planes are considered similar if they contain the same
     * points. This does not mean they are equal since they can have
     * opposite normals.</p>
     * @param plane plane to which the instance is compared
     * @return true if the planes are similar
     */
    public boolean isSimilarTo(final Plane plane) {
        final double angle = Vector3D.angle(w, plane.w);
        return ((angle < 1.0e-10) && (FastMath.abs(originOffset - plane.originOffset) < tolerance)) ||
               ((angle > (FastMath.PI - 1.0e-10)) && (FastMath.abs(originOffset + plane.originOffset) < tolerance));
    }

    /** Rotate the plane around the specified point.
     * <p>The instance is not modified, a new instance is created.</p>
     * @param center rotation center
     * @param rotation vectorial rotation operator
     * @return a new plane
     */
    public Plane rotate(final Vector3D center, final Rotation rotation) {

        final Vector3D delta = origin.subtract(center);
        final Plane plane = new Plane(center.add(rotation.applyTo(delta)),
                                      rotation.applyTo(w), tolerance);

        // make sure the frame is transformed as desired
        plane.u = rotation.applyTo(u);
        plane.v = rotation.applyTo(v);

        return plane;

    }

    /** Translate the plane by the specified amount.
     * <p>The instance is not modified, a new instance is created.</p>
     * @param translation translation to apply
     * @return a new plane
     */
    public Plane translate(final Vector3D translation) {

        final Plane plane = new Plane(origin.add(translation), w, tolerance);

        // make sure the frame is transformed as desired
        plane.u = u;
        plane.v = v;

        return plane;

    }

    /** Get the intersection of a line with the instance.
     * @param line line intersecting the instance
     * @return intersection point between between the line and the
     * instance (null if the line is parallel to the instance)
     */
    public Vector3D intersection(final Line line) {
        final Vector3D direction = line.getDirection();
        final double   dot       = w.dotProduct(direction);
        if (FastMath.abs(dot) < 1.0e-10) {
            return null;
        }
        final Vector3D point = line.toSpace(Vector1D.ZERO);
        final double   k     = -(originOffset + w.dotProduct(point)) / dot;
        return new Vector3D(1.0, point, k, direction);
    }

    /** Build the line shared by the instance and another plane.
     * @param other other plane
     * @return line at the intersection of the instance and the
     * other plane (really a {@link Line Line} instance)
     */
    public Line intersection(final Plane other) {
        final Vector3D direction = Vector3D.crossProduct(w, other.w);
        if (direction.getNorm() < tolerance) {
            return null;
        }
        final Vector3D point = intersection(this, other, new Plane(direction, tolerance));
        return new Line(point, point.add(direction), tolerance);
    }

    /** Get the intersection point of three planes.
     * @param plane1 first plane1
     * @param plane2 second plane2
     * @param plane3 third plane2
     * @return intersection point of three planes, null if some planes are parallel
     */
    public static Vector3D intersection(final Plane plane1, final Plane plane2, final Plane plane3) {

        // coefficients of the three planes linear equations
        final double a1 = plane1.w.getX();
        final double b1 = plane1.w.getY();
        final double c1 = plane1.w.getZ();
        final double d1 = plane1.originOffset;

        final double a2 = plane2.w.getX();
        final double b2 = plane2.w.getY();
        final double c2 = plane2.w.getZ();
        final double d2 = plane2.originOffset;

        final double a3 = plane3.w.getX();
        final double b3 = plane3.w.getY();
        final double c3 = plane3.w.getZ();
        final double d3 = plane3.originOffset;

        // direct Cramer resolution of the linear system
        // (this is still feasible for a 3x3 system)
        final double a23         = b2 * c3 - b3 * c2;
        final double b23         = c2 * a3 - c3 * a2;
        final double c23         = a2 * b3 - a3 * b2;
        final double determinant = a1 * a23 + b1 * b23 + c1 * c23;
        if (FastMath.abs(determinant) < 1.0e-10) {
            return null;
        }

        final double r = 1.0 / determinant;
        return new Vector3D(
                            (-a23 * d1 - (c1 * b3 - c3 * b1) * d2 - (c2 * b1 - c1 * b2) * d3) * r,
                            (-b23 * d1 - (c3 * a1 - c1 * a3) * d2 - (c1 * a2 - c2 * a1) * d3) * r,
                            (-c23 * d1 - (b1 * a3 - b3 * a1) * d2 - (b2 * a1 - b1 * a2) * d3) * r);

    }

    /** Build a region covering the whole hyperplane.
     * @return a region covering the whole hyperplane
     */
    @Override
    public SubPlane wholeHyperplane() {
        return new SubPlane(this, new PolygonsSet(tolerance));
    }

    /** {@inheritDoc} */
    @Override
    public SubPlane emptyHyperplane() {
        final RegionFactory<Euclidean2D, Vector2D, org.hipparchus.geometry.euclidean.twod.Line, SubLine> factory = new RegionFactory<>();
        return new SubPlane(this, factory.getComplement(new PolygonsSet(tolerance)));
    }

    /** Build a region covering the whole space.
     * @return a region containing the instance (really a {@link
     * PolyhedronsSet PolyhedronsSet} instance)
     */
    @Override
    public PolyhedronsSet wholeSpace() {
        return new PolyhedronsSet(tolerance);
    }

    /** Check if the instance contains a point.
     * @param p point to check
     * @return true if p belongs to the plane
     */
    public boolean contains(final Vector3D p) {
        return FastMath.abs(getOffset(p)) < tolerance;
    }

    /** Get the offset (oriented distance) of a parallel plane.
     * <p>This method should be called only for parallel planes otherwise
     * the result is not meaningful.</p>
     * <p>The offset is 0 if both planes are the same, it is
     * positive if the plane is on the plus side of the instance and
     * negative if it is on the minus side, according to its natural
     * orientation.</p>
     * @param plane plane to check
     * @return offset of the plane
     */
    public double getOffset(final Plane plane) {
        return originOffset + (sameOrientationAs(plane) ? -plane.originOffset : plane.originOffset);
    }

    /** Get the offset (oriented distance) of a point.
     * <p>The offset is 0 if the point is on the underlying hyperplane,
     * it is positive if the point is on one particular side of the
     * hyperplane, and it is negative if the point is on the other side,
     * according to the hyperplane natural orientation.</p>
     * @param point point to check
     * @return offset of the point
     */
    @Override
    public double getOffset(final Vector3D point) {
        return point.dotProduct(w) + originOffset;
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D moveToOffset(final Vector3D point, final double offset) {
        final double delta = offset - getOffset(point);
        return new Vector3D(point.getX() + delta * w.getX(),
                            point.getY() + delta * w.getY(),
                            point.getZ() + delta * w.getZ());
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D arbitraryPoint() {
        return origin;
    }

    /** Check if the instance has the same orientation as another hyperplane.
     * @param other other hyperplane to check against the instance
     * @return true if the instance and the other hyperplane have
     * the same orientation
     */
    @Override
    public boolean sameOrientationAs(final Plane other) {
        return other.w.dotProduct(w) > 0.0;
    }

}
