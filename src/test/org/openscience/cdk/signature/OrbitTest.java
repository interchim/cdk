/* Copyright (C) 2009-2010 maclean {gilleain.torrance@gmail.com}
*
* Contact: cdk-devel@lists.sourceforge.net
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public License
* as published by the Free Software Foundation; either version 2.1
* of the License, or (at your option) any later version.
* All we ask is that proper credit is given for our work, which includes
* - but is not limited to - adding the above copyright notice to the beginning
* of your source code files, and to any copyright notice that you may distribute
* with programs based on this work.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.openscience.cdk.signature;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @cdk.module test-signature
 * @author maclean
 *
 */
public class OrbitTest {
    
    private static Orbit orbit;
    
    @BeforeClass
    public void setUp() {
        
        // make a test orbit instance, with a nonsense
        // string label, and some number of 'indices'
        String orbitLabel = "ORBIT";
        int height = 2;
        orbit = new Orbit(orbitLabel, height);
        int[] atomIndices = new int[] {0, 1, 2, 3};
        for (int atomIndex : atomIndices) {
            orbit.addAtom(atomIndex);
        }
    }
    
    @Test
    public void testClone() {
        Orbit clonedOrbit = (Orbit)orbit.clone();
        Assert.assertEquals(clonedOrbit.toString(), orbit.toString());
    }
    
    @Test
    public void isEmptyTest() {
        Assert.assertFalse("The setUp method should have made an orbit with " +
        		"some indices in it", orbit.isEmpty());
        List<Integer> indices = new ArrayList<Integer>();
        for (int index : orbit) {
            indices.add(index);
        }
        for (int index : indices) {
            orbit.remove(index);
        }
        Assert.assertTrue("Orbit should now be empty", orbit.isEmpty());
    }
    
    @Test
    public void containsTest() {
        for (int index : orbit) {
            Assert.assertTrue("Index " + index + " not in orbit",
                    orbit.contains(index));
        }
    }


}
