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
package org.hipparchus.geometry.enclosing;

import java.util.List;

import org.hipparchus.geometry.Point;
import org.hipparchus.geometry.Space;

/** Interface for generating balls based on support points.
 * <p>
 * This generator is used in the {@link WelzlEncloser Emo Welzl} algorithm
 * and its derivatives.
 * </p>
 * @param <S> Space type.
 * @param <P> Point type.
 * @see EnclosingBall
 */
public interface SupportBallGenerator<S extends Space, P extends Point<S, P>> {

    /** Create a ball whose boundary lies on prescribed support points.
     * @param support support points (may be empty)
     * @return ball whose boundary lies on the prescribed support points
     */
    EnclosingBall<S, P> ballOnSupport(List<P> support);

}
