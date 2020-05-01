/*
 * Licensed to the Hipparchus project under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The Hipparchus project licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hipparchus.analysis.differentiation;

import org.hipparchus.Field;
import org.hipparchus.util.Decimal64;
import org.hipparchus.util.Decimal64Field;

/**
 * Test for class {@link FieldDerivativeStructure} on {@link Decimal64}.
 */
public class FieldDerivativeStructureDecimal64Test extends FieldDerivativeStructureAbstractTest<Decimal64> {

    @Override
    protected Field<Decimal64> getField() {
        return Decimal64Field.getInstance();
    }

    @Override
    protected Decimal64 buildScalar(double value) {
        return new Decimal64(value);
    }

}
