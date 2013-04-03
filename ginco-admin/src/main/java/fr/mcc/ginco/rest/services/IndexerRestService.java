/**
 * Copyright or © or Copr. Ministère Français chargé de la Culture
 * et de la Communication (2013)
 * <p/>
 * contact.gincoculture_at_gouv.fr
 * <p/>
 * This software is a computer program whose purpose is to provide a thesaurus
 * management solution.
 * <p/>
 * This software is governed by the CeCILL license under French law and
 * abiding by the rules of distribution of free software. You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * <p/>
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited liability.
 * <p/>
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systemsand/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 * <p/>
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */
package fr.mcc.ginco.rest.services;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.stereotype.Service;

import fr.mcc.ginco.beans.NoteType;
import fr.mcc.ginco.beans.Thesaurus;
import fr.mcc.ginco.exceptions.BusinessException;
import fr.mcc.ginco.exceptions.TechnicalException;
import fr.mcc.ginco.extjs.view.ExtJsonFormLoadData;
import fr.mcc.ginco.services.IIndexerService;
import fr.mcc.ginco.solr.SearchResult;
import fr.mcc.ginco.solr.SearchResultList;

/**
 * Base REST service intended to be used for getting tree of {@link Thesaurus},
 * and its children.
 */
@Service
@Path("/indexerservice")
public class IndexerRestService {
   
	@Inject
    @Named("indexerService")
    private IIndexerService indexerService;

    @GET
    @Path("/reindex")
    @Produces({MediaType.APPLICATION_JSON})
    public Response forceIndexation() throws BusinessException, TechnicalException {
        indexerService.forceIndexing();
        return Response.status(Response.Status.OK)
                .entity("{success:true, message: 'Indexing started!'}")
                .build();
    }

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    public  ExtJsonFormLoadData<List<SearchResult>> search(@QueryParam("query") String query,
    		@QueryParam("start") Integer startIndex,
    	    @QueryParam("limit") Integer limit) {
        try {
        	SearchResultList searchResults  = indexerService.search(query, startIndex,limit);
        	ExtJsonFormLoadData<List<SearchResult>> extSearchResults = new ExtJsonFormLoadData<List<SearchResult>>(searchResults);
        	extSearchResults.setTotal((long) searchResults.getNumFound());
			return extSearchResults;
		} catch (SolrServerException e) {
			throw new TechnicalException("Search exception" , e) ;
		}

    }
}