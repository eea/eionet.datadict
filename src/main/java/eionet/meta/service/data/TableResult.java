/*
 * TableResult.java
 * 
 * Created on Mar 3, 2016
 *            www.eworx.gr
 */
package eionet.meta.service.data;

import eionet.meta.dao.domain.DataSetTable;
import java.util.List;

/**
 *
 * @author Ioannis Stamos <js@eworx.gr>
 * 
 */
public class TableResult extends PagedResult<DataSetTable> {

    public TableResult(List<DataSetTable> items, int totalItems, PagedRequest pagedRequest) {
        super(items, totalItems, pagedRequest);
    }

}
