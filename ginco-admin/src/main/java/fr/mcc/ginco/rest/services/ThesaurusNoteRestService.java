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

import java.io.IOException;
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

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import fr.mcc.ginco.beans.Note;
import fr.mcc.ginco.beans.NoteType;
import fr.mcc.ginco.exceptions.BusinessException;
import fr.mcc.ginco.extjs.view.ExtJsonFormLoadData;
import fr.mcc.ginco.extjs.view.NoteParentEntityResponse;
import fr.mcc.ginco.extjs.view.pojo.ThesaurusNoteView;
import fr.mcc.ginco.extjs.view.utils.ThesaurusNoteViewConverter;
import fr.mcc.ginco.rest.services.utils.ThesaurusNoteRestServiceUtils;
import fr.mcc.ginco.services.INoteService;
import fr.mcc.ginco.services.INoteTypeService;
import fr.mcc.ginco.solr.IConceptIndexerService;
import fr.mcc.ginco.solr.INoteIndexerService;
import fr.mcc.ginco.solr.ITermIndexerService;

/**
 * Thesaurus Note REST service for all operation on notes (both term or concept notes)
 *
 */
@Service
@Path("/thesaurusnoteservice")
@Produces({ MediaType.APPLICATION_JSON })
@PreAuthorize("isAuthenticated()")
public class ThesaurusNoteRestService {
	@Inject
	@Named("noteTypeService")
	private INoteTypeService noteTypeService;

	@Inject
	@Named("noteService")
	private INoteService noteService;

    @Inject
    @Named("thesaurusNoteViewConverter")
    private ThesaurusNoteViewConverter thesaurusNoteViewConverter;

    @Inject
	@Named("noteIndexerService")
	private INoteIndexerService noteIndexerService;

    @Inject
	@Named("termIndexerService")
	private ITermIndexerService termIndexerService;

    @Inject
	@Named("conceptIndexerService")
	private IConceptIndexerService conceptIndexerService;

    @Inject
    private ThesaurusNoteRestServiceUtils thesaurusNoteRestServiceUtils;

	private Logger logger  = LoggerFactory.getLogger(ThesaurusNoteRestService.class);


	/**
	 * Public method used to get the list of all concept note types in the database.
	 *
	 * @return list of NoteType objects for a concept, if not found - {@code null}
	 */
	@GET
	@Path("/getConceptNoteTypes")
	@Produces({ MediaType.APPLICATION_JSON })
	public ExtJsonFormLoadData<List<NoteType>> getConceptNoteTypes() {
		List<NoteType> noteTypes = new ArrayList<NoteType>();
		noteTypes = noteTypeService.getConceptNoteTypeList();
		ExtJsonFormLoadData<List<NoteType>> types = new ExtJsonFormLoadData<List<NoteType>>(noteTypes);
		types.setTotal((long) noteTypes.size());
		return types;
	}

	/**
	 * Public method used to get the list of all term note types in the database.
	 *
	 * @return list of NoteType objects for a term, if not found - {@code null}
	 */
	@GET
	@Path("/getTermNoteTypes")
	@Produces({ MediaType.APPLICATION_JSON })
	public ExtJsonFormLoadData<List<NoteType>> getTermNoteTypes(){
		List<NoteType> noteTypes = new ArrayList<NoteType>();
		noteTypes = noteTypeService.getTermNoteTypeList();
		ExtJsonFormLoadData<List<NoteType>> types = new ExtJsonFormLoadData<List<NoteType>>(noteTypes);
		types.setTotal((long) noteTypes.size());
		return types;
	}

	/**
	 * Public method used to get the list of all notes for a concept or a term.
	 *
	 * @return list of Note objects for a term, if not found - {@code null}
	 */
	@GET
	@Path("/getNotes")
	@Produces({ MediaType.APPLICATION_JSON })
	public ExtJsonFormLoadData<List<ThesaurusNoteView>> getNotes(
			@QueryParam("conceptId") String conceptId,
			@QueryParam("termId") String termId,
			@QueryParam("start") Integer startIndex,
		     @QueryParam("limit") Integer limit) {

		logger.info("Getting Notes for a concept or a term with following parameters : " + "conceptId " +conceptId + " and termId " + termId + " and startIndex " + startIndex + " with a limit of " + limit);

		List<Note> notes = new ArrayList<Note>();
		Long total;
		if (conceptId != null) {
			notes = noteService.getConceptNotePaginatedList(conceptId, startIndex, limit);
			total = noteService.getConceptNoteCount(conceptId);
		} else if (termId != null) {
			notes = noteService.getTermNotePaginatedList(termId, startIndex, limit);
			total = noteService.getTermNoteCount(termId);
		} else {
			throw new BusinessException("You need to specify an id for the concept or the term", "conceptid-or-termid-needed");
		}
		ExtJsonFormLoadData<List<ThesaurusNoteView>> result = new ExtJsonFormLoadData<List<ThesaurusNoteView>>(thesaurusNoteViewConverter.convert(notes));
		result.setTotal(total);
		return result;
	}

	/**
	 * Public method used to create new notes
	 */
	@POST
	@Path("/createNotes")
	@Consumes({ MediaType.APPLICATION_JSON })
	@PreAuthorize("hasPermission(#noteViews, '0') or hasPermission(#noteViews, '1')")
	public ExtJsonFormLoadData<List<ThesaurusNoteView>> createNotes(List<ThesaurusNoteView> noteViews, @QueryParam("conceptId") String conceptId, @QueryParam("termId") String termId) {

		//We set for each note if it belongs to a concept or a term
		for (ThesaurusNoteView view : noteViews) {
			if(conceptId != null) {
				logger.info("Updating notes for conceptid : " + conceptId);
				view.setConceptId(conceptId);
			} else if (termId != null) {
				logger.info("Updating notes for termid : " + termId);
				view.setTermId(termId);
			} else {
				throw new BusinessException("You need to specify an id for the concept or the term", "conceptid-or-termid-needed");
			}
		}

		List<Note> notes = thesaurusNoteViewConverter.convertToNote(noteViews);

		List<Note> resultNotes = new ArrayList<Note>() ;
		for (Note note : notes) {
			if (!note.getLexicalValue().isEmpty() && note.getLanguage() != null && note.getNoteType() != null) {
				thesaurusNoteRestServiceUtils.checkExpertAccessToNote(note);
				resultNotes.add(noteService.createOrUpdateNote(note));
				noteIndexerService.addNote(note);
				if (note.getTerm() != null){
					termIndexerService.addTerm(note.getTerm());
				}
				if (note.getConcept() != null){
					conceptIndexerService.addConcept(note.getConcept());
				}
			} else {
				throw new BusinessException("Failed to update the note : fields required not filled in", "note-update-failed-empty-fields");
			}
		}

		//Returns all the created notes converted
		return new ExtJsonFormLoadData<List<ThesaurusNoteView>>(thesaurusNoteViewConverter.convert(resultNotes));

	}

	/**
	 * Public method used to create new notes
	 */
	@POST
	@Path("/updateNotes")
	@Consumes({ MediaType.APPLICATION_JSON })
	@PreAuthorize("hasPermission(#noteViews, '0') or hasPermission(#noteViews, '1')")
	public ExtJsonFormLoadData<List<ThesaurusNoteView>> updateNotes(List<ThesaurusNoteView> noteViews) {

		List<Note> notes = thesaurusNoteViewConverter.convertToNote(noteViews);
		List<Note> resultNotes = new ArrayList<Note>() ;

		for (Note note : notes) {
			if (note.getLexicalValue() != null && note.getLanguage() != null && note.getNoteType() != null) {
				thesaurusNoteRestServiceUtils.checkExpertAccessToNote(note);
				resultNotes.add(noteService.createOrUpdateNote(note));
				noteIndexerService.addNote(note);
				if (note.getConcept() != null){
					conceptIndexerService.addConcept(note.getConcept());
				}
				if (note.getTerm() != null){
					termIndexerService.addTerm(note.getTerm());
				}
			} else
			{
				throw new BusinessException("Failed to update the note : fields required not filled in", "note-update-failed-empty-fields");
			}
		}

		return new ExtJsonFormLoadData<List<ThesaurusNoteView>>(thesaurusNoteViewConverter.convert(resultNotes));
	}

	/**
	 * Public method used to create new notes
	 */
	@POST
	@Path("/destroyNotes")
	@Consumes({ MediaType.APPLICATION_JSON })
	@PreAuthorize("hasPermission(#noteViews, '0') or hasPermission(#noteViews, '1')")
	public ExtJsonFormLoadData<List<ThesaurusNoteView>> destroyNotes(List<ThesaurusNoteView> noteViews){

		List<Note> notes = thesaurusNoteViewConverter.convertToNote(noteViews);
		List<Note> resultNotes = new ArrayList<Note>() ;

		for (Note note : notes) {
			thesaurusNoteRestServiceUtils.checkExpertAccessToNote(note);
			resultNotes.add(noteService.deleteNote(note));
			noteIndexerService.removeNote(note);
			if (note.getTerm() != null){
				termIndexerService.addTerm(note.getTerm());
			}
			if (note.getConcept() != null){
				conceptIndexerService.addConcept(note.getConcept());
			}
		}

		return new ExtJsonFormLoadData<List<ThesaurusNoteView>>(thesaurusNoteViewConverter.convert(resultNotes));
	}

	@GET
	@Path("/getNoteParentEntity")
	@Produces({ MediaType.APPLICATION_JSON })
	public String getNoteParentEntity(
			@QueryParam("noteId") String noteId)
					throws IOException {

		NoteParentEntityResponse response = new NoteParentEntityResponse();
		Note note = noteService.getNoteById(noteId);
		if (note.getTerm() != null){
			response.setParentEntityId(note.getTerm().getIdentifier());
			response.setIsConcept(false);
		}
		if (note.getConcept() != null){
			response.setParentEntityId(note.getConcept().getIdentifier());
			response.setIsConcept(true);
		}

		ObjectMapper mapper = new ObjectMapper();
		String serialized = mapper.writeValueAsString(new ExtJsonFormLoadData(
				response));
		return serialized;
	}

}