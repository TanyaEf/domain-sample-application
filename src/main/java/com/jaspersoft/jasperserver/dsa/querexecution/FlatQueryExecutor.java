package com.jaspersoft.jasperserver.dsa.querexecution;

import com.jaspersoft.jasperserver.dsa.common.AppConfiguration;
import com.jaspersoft.jasperserver.dsa.domain.DomainMetadataUtil;
import com.jaspersoft.jasperserver.dto.adhoc.datasource.ClientDataSourceField;
import com.jaspersoft.jasperserver.dto.adhoc.query.ClientMultiLevelQuery;
import com.jaspersoft.jasperserver.dto.adhoc.query.field.ClientQueryField;
import com.jaspersoft.jasperserver.dto.adhoc.query.select.ClientSelect;
import com.jaspersoft.jasperserver.dto.executions.ClientFlatQueryResultData;
import com.jaspersoft.jasperserver.dto.executions.ClientMultiLevelQueryExecution;
import com.jaspersoft.jasperserver.dto.executions.ClientQueryParams;
import com.jaspersoft.jasperserver.dto.resources.domain.PresentationElement;
import com.jaspersoft.jasperserver.dto.resources.domain.PresentationGroupElement;
import com.jaspersoft.jasperserver.dto.resources.domain.PresentationSingleElement;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.adhoc.queryexecution.QueryExecutionAdapter;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * <p/>
 * <p/>
 *
 * @author tetiana.iefimenko
 * @version $Id$
 * @see
 */
public class FlatQueryExecutor extends QueryExecutor {

    private static final Logger appLogger = Logger.getLogger(FlatQueryExecutor.class);


    public FlatQueryExecutor(AppConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public QueryExecutor retrieveMetadata() {
        DomainMetadataUtil domainMetadataUtil = new DomainMetadataUtil(configuration);
        this.dataIslandsContainer = domainMetadataUtil.fetchMetadata(supermartDpmainUri);
        return this;
    }

    @Override
    public QueryExecutor buildQuery() {
        appLogger.info("Build flat query for domain " + supermartDpmainUri);
        PresentationGroupElement presentationGroupElement = dataIslandsContainer.
                getDataIslands().
                get(0);
        PresentationGroupElement presentationGroupElement1 = (PresentationGroupElement) presentationGroupElement.
                getElements().
                get(0);
        List<PresentationElement> presentationElements = presentationGroupElement1.getElements();
        PresentationGroupElement table = (PresentationGroupElement) presentationElements.get(0);

        List<PresentationElement> fields = table.getElements();
        List<ClientQueryField> queryFields = new LinkedList<ClientQueryField>();
        for (PresentationElement field : fields) {
            queryFields.add(new ClientQueryField().
                    setDataSourceField(new ClientDataSourceField().
                            setName((((PresentationSingleElement)field).getHierarchicalName()))));
        }
        this.query =
                new ClientMultiLevelQuery().
                setSelect(new ClientSelect().setFields(queryFields)).setLimit(1000);

        return this;
    }

    @Override
    public QueryExecutor executeQuery() {
        appLogger.info("Start to execute flat query");
        ClientMultiLevelQueryExecution queryExecution = new ClientMultiLevelQueryExecution().
                setDataSourceUri(supermartDpmainUri).
                setQuery((ClientMultiLevelQuery) query).
                setParams(new ClientQueryParams().setOffset(new int[]{0}).setPageSize(new int[]{100}));

        QueryExecutionAdapter<ClientFlatQueryResultData> queryExecutionAdapter = configuration.getSession().
                queryExecutionService().
                flatQuery();
        if (configuration.getResponseFormat() != null) {
            if (configuration.getResponseFormat().toLowerCase().equals("xml")) {
                queryExecutionAdapter = queryExecutionAdapter.asXml();
            }
            if (configuration.getResponseFormat().toLowerCase().equals("json")) {
                queryExecutionAdapter = queryExecutionAdapter.asJson();
            }
        }
        operationResult = queryExecutionAdapter.
                execute(queryExecution);
        if (operationResult.getResponseStatus() == 200) {
            appLogger.info("Flat query was executed successfully");
        } else {
            appLogger.warn("Executing of flat query failed with response status " + operationResult.getResponseStatus());
        }
        return this;
    }
}
