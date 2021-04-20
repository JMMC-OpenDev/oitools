/* 
 * Copyright (C) 2018 CNRS - JMMC project ( http://www.jmmc.fr )
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools;

import static fr.jmmc.oitools.JUnitBaseTest.logger;
import fr.jmmc.oitools.model.DataModel;
import java.util.logging.Level;
import org.junit.Test;

/**
 * Warning: to avoid any side effect, calling DataModel.main() must be alone in the test file
 * @author kempsc
 */
public class DumpDataModelTest {

    @Test
    public void dumpDataModels() {
        try {
            DataModel.main(null);
        } catch (Throwable th) {
            logger.log(Level.SEVERE, "failure:", th);
            throw new RuntimeException(th);
        }
    }

}
