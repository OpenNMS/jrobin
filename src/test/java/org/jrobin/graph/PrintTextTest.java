/*******************************************************************************
 * Copyright (c) 2011 The OpenNMS Group, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *******************************************************************************/
package org.jrobin.graph;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.jrobin.data.DataProcessor;
import org.jrobin.data.Plottable;
import org.junit.After;
import org.junit.Test;

public class PrintTextTest {

    class ConstantStaticDef extends Plottable {
        private double m_startTime = Double.NEGATIVE_INFINITY;
        private double m_endTime = Double.POSITIVE_INFINITY;
        private double m_value = Double.NaN;

        ConstantStaticDef(long startTime, long endTime, double value) {
            m_startTime = startTime;
            m_endTime = endTime;
            m_value = value;
        }

        @Override
        public double getValue(long timestamp) {
            if (m_startTime <= timestamp && m_endTime >= timestamp) {
                return m_value;
            } else {
                return Double.NaN;
            }
        }
    }

    @Test
    public void testTrim() throws java.io.IOException, org.jrobin.core.RrdException {
        Date endDate = new Date();
        Date startDate = new Date(endDate.getTime() - TimeUnit.HOURS.toMillis(4));
        ConstantStaticDef sdef = new ConstantStaticDef(TimeUnit.MILLISECONDS.toSeconds(startDate.getTime()), TimeUnit.MILLISECONDS.toSeconds(endDate.getTime()), 123456.0);

        PrintText ct = new PrintText("test", "AVERAGE", "%10.0lf\\g", true);
        DataProcessor dproc = new DataProcessor(startDate, endDate);
        ValueScaler valueScaler = new ValueScaler(1000);
        dproc.addDatasource("test", sdef);
        dproc.processData();
        ct.resolveText(dproc, valueScaler);
        assertTrue("    123456".equals(ct.resolvedText));
  }
}
