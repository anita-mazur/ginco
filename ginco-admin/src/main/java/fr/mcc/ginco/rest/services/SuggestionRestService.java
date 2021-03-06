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
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import fr.mcc.ginco.beans.Suggestion;
import fr.mcc.ginco.exceptions.BusinessException;
import fr.mcc.ginco.extjs.view.ExtJsonFormLoadData;
import fr.mcc.ginco.extjs.view.pojo.MySuggestionView;
import fr.mcc.ginco.extjs.view.pojo.SuggestionView;
import fr.mcc.ginco.extjs.view.utils.SuggestionViewConverter;
import fr.mcc.ginco.services.ISuggestionService;
import fr.mcc.ginco.services.IThesaurusConceptService;

/**
 * Suggestion REST service for all operation on suggestions (both term or
 * concept suggestions)
 * 
 */
@Service
@Path("/suggestionservice")
@Produces({ MediaType.APPLICATION_JSON })
@PreAuthorize("isAuthenticated()")
public class SuggestionRestService {

	@Inject
	@Named("thesaurusConceptService")
	private IThesaurusConceptService thesaurusConceptService;

	@Inject
	@Named("suggestionService")
	private ISuggestionService suggestionService;

	@Inject
	@Named("suggestionViewConverter")
	private SuggestionViewConverter suggestionViewConverter;

	private Logger logger = LoggerFactory
			.getLogger(SuggestionRestService.class);

	/**
	 * Gets the list of all concept or term suggestions
	 * 
	 * @param conceptId
	 * @param termId
	 * @param startIndex
	 * @param limit
	 * @return
	 */
	@GET
	@Path("/getSuggestions")
	@Produces({ MediaType.APPLICATION_JSON })
	public ExtJsonFormLoadData<List<SuggestionView>> getSuggestions(
			@QueryParam("conceptId") String conceptId,
			@QueryParam("termId") String termId,
			@QueryParam("start") Integer startIndex,
			@QueryParam("limit") Integer limit) {

		List<Suggestion> suggestions = new ArrayList<Suggestion>();
		Long total;
		if (StringUtils.isNotEmpty(conceptId)) {
			suggestions = suggestionService.getConceptSuggestionPaginatedList(
					conceptId, startIndex, limit);
			total = suggestionService.getConceptSuggestionCount(conceptId);
		} else if (StringUtils.isNotEmpty(termId)) {
			suggestions = suggestionService.getTermSuggestionPaginatedList(
					termId, startIndex, limit);
			total = suggestionService.getTermSuggestionCount(termId);
		} else {
			throw new BusinessException(
					"You need to specify an id for the concept or the term",
					"conceptid-or-termid-needed");
		}

		List<SuggestionView> suggestionViews = new ArrayList<SuggestionView>();
		for (Suggestion suggestion : suggestions) {
			suggestionViews.add(suggestionViewConverter.convert(suggestion));
		}
		ExtJsonFormLoadData<List<SuggestionView>> result = new ExtJsonFormLoadData<List<SuggestionView>>(
				suggestionViews);
		result.setTotal(total);
		return result;
	}

	/**
	 * Public method used to create new suggestions
	 * 
	 */
	@POST
	@Path("/updateSuggestions")
	@Consumes({ MediaType.APPLICATION_JSON })
	@PreAuthorize("hasPermission(#suggestionViews, '0') or hasPermission(#suggestionViews, '1')")
	public ExtJsonFormLoadData<List<SuggestionView>> updateSuggestions(
			List<SuggestionView> suggestionViews) {

		List<Suggestion> suggestions = new ArrayList<Suggestion>();
		List<SuggestionView> resultSuggestions = new ArrayList<SuggestionView>();

		for (SuggestionView view : suggestionViews) {
			suggestions.add(suggestionViewConverter.convert(view));
		}
		for (Suggestion suggestion : suggestions) {
			resultSuggestions.add(suggestionViewConverter
					.convert(suggestionService
							.createOrUpdateSuggestion(suggestion)));
		}

		return new ExtJsonFormLoadData<List<SuggestionView>>(resultSuggestions,
				resultSuggestions.size());
	}

	/**
	 * Public method used to create new suggestions
	 * 
	 */
	@POST
	@Path("/createSuggestions")
	@Consumes({ MediaType.APPLICATION_JSON })
	@PreAuthorize("hasPermission(#suggestionViews, '0') or hasPermission(#suggestionViews, '1')")
	public ExtJsonFormLoadData<List<SuggestionView>> createSuggestions(
			List<SuggestionView> suggestionViews,
			@QueryParam("conceptId") String conceptId,
			@QueryParam("termId") String termId){
		for (SuggestionView view : suggestionViews) {
			if (StringUtils.isNotEmpty(conceptId)) {
				logger.info("Updating suggestions for conceptid : " + conceptId);
				view.setConceptId(conceptId);
			} else if (StringUtils.isNotEmpty(termId)) {
				logger.info("Updating suggestions for termid : " + termId);
				view.setTermId(termId);
			} else {
				throw new BusinessException(
						"You need to specify an id for the concept or the term",
						"conceptid-or-termid-needed");
			}
		}

		List<Suggestion> suggestions = new ArrayList<Suggestion>();
		List<SuggestionView> resultSuggestions = new ArrayList<SuggestionView>();

		for (SuggestionView view : suggestionViews) {
			suggestions.add(suggestionViewConverter.convert(view));
		}
		for (Suggestion suggestion : suggestions) {
			resultSuggestions.add(suggestionViewConverter
					.convert(suggestionService
							.createOrUpdateSuggestion(suggestion)));
		}

		return new ExtJsonFormLoadData<List<SuggestionView>>(resultSuggestions,
				resultSuggestions.size());
	}

	/**
	 * Public method used to create new suggestions
	 * 
	 */
	@GET
	@Path("/getUserSuggestions")
	@Produces({ MediaType.APPLICATION_JSON })
	public ExtJsonFormLoadData<List<MySuggestionView>> getUserSuggestions(
			@QueryParam("start") Integer startIndex,
			@QueryParam("limit") Integer limit) {

		Authentication auth = SecurityContextHolder.getContext()
				.getAuthentication();
		String currentUser = auth.getName();

		List<Suggestion> suggestions = suggestionService
				.getSuggestionPaginatedListByRecipient(currentUser, startIndex,
						limit);

		List<MySuggestionView> resultSuggestions = new ArrayList<MySuggestionView>();

		for (Suggestion suggestion : suggestions) {
			SuggestionView view = suggestionViewConverter.convert(suggestion);

			MySuggestionView myView = new MySuggestionView(view);

			if (suggestion.getConcept() != null) {
				myView.setObjectIdentifier(suggestion.getConcept()
						.getIdentifier());
				myView.setObjectValue(thesaurusConceptService
						.getConceptLabel(suggestion.getConcept()
								.getIdentifier()));
				myView.setThesaurusTitle(suggestion.getConcept().getThesaurus()
						.getTitle());

			} else {
				myView.setObjectIdentifier(suggestion.getTerm().getIdentifier());
				myView.setObjectValue(suggestion.getTerm().getLexicalValue());
				myView.setThesaurusTitle(suggestion.getTerm().getThesaurus()
						.getTitle());

			}

			resultSuggestions.add(myView);
		}

		ExtJsonFormLoadData<List<MySuggestionView>> result = new ExtJsonFormLoadData<List<MySuggestionView>>(
				resultSuggestions);
		result.setTotal(suggestionService
				.getSuggestionByRecipientCount(currentUser));
		return result;
	}
	
	/**
	 * Public method used to destroy suggestions
	 */
	@POST
	@Path("/destroySuggestions")
	@Consumes({ MediaType.APPLICATION_JSON })
	@PreAuthorize("hasPermission(#suggestionViews, '0') or hasPermission(#suggestionViews, '1')")
	public void destroySuggestions(List<SuggestionView> suggestionViews) {
		for (SuggestionView suggestionView:suggestionViews) {
			Suggestion suggestion  = suggestionViewConverter.convert(suggestionView); 
			suggestionService.deleteSuggestion(suggestion);
		}
	
	}

}