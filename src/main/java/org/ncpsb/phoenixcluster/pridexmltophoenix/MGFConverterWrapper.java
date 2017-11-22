package org.ncpsb.phoenixcluster.pridexmltophoenix;

import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.exporters.MGFConverter;

/**
 * Created by baimi on 2017/11/21.
 * To access the protected convert method.
 */
public class MGFConverterWrapper extends MGFConverter{
    public MGFConverterWrapper(DataAccessController controller, String outputFilePath) {
        super(controller, outputFilePath);
    }
    public void convert() throws Exception {
        super.convert();
    }
}
