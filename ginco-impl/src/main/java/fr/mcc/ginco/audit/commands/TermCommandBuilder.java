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
package fr.mcc.ginco.audit.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.stereotype.Service;

import fr.mcc.ginco.beans.ThesaurusConcept;
import fr.mcc.ginco.beans.ThesaurusTerm;

/**
 * Component in charge of building CommandLine relatives to terms changes
 *
 */
@Service("termCommandBuilder")
public class TermCommandBuilder {

	@Inject
	@Named("mistralStructuresBuilder")
	private MistralStructuresBuilder mistralStructuresBuilder;

	/**
	 * Builds the list of command lines for terms deleted between two revisions
	 *
	 * @param previousTerms
	 * @param currentTerms
	 * @return
	 */
	public List<CommandLine> buildDeletedTermsLines(List<ThesaurusTerm> previousTerms,
			List<ThesaurusTerm> currentTerms) {
		List<CommandLine> termsOperations = new ArrayList<CommandLine>();

		Map<String, ThesaurusTerm> newLexicalvalues = mistralStructuresBuilder
				.getTermVersionsView(currentTerms);

		// Terms deletion
		for (ThesaurusTerm oldTerm : previousTerms) {
			if (!newLexicalvalues.containsKey(oldTerm.getLexicalValue())) {
				CommandLine deletionLine = new CommandLine();
				deletionLine.setValue(CommandLine.REMOVED
						+ StringEscapeUtils.unescapeXml(oldTerm.getLexicalValue()));
				termsOperations.add(deletionLine);
			}
		}
		return termsOperations;
	}

	/**
	 * Builds the list of command lines for terms changed between two revisions
	 *
	 * @param previousTerms
	 * @param currentTerms
	 * @return
	 */
	public List<CommandLine> buildChangedTermsLines(List<ThesaurusTerm> previousTerms,
			List<ThesaurusTerm> currentTerms){
		List<CommandLine> termsOperations = new ArrayList<CommandLine>();

		Map<String, ThesaurusTerm> newLexicalvalues = mistralStructuresBuilder
				.getTermVersionsView(currentTerms);
		Map<String, List<ThesaurusTerm>> newNotPreferredTermsByTerm = mistralStructuresBuilder
				.getNotPreferredTermsByTerm(currentTerms);


		for (ThesaurusTerm oldTerm : previousTerms) {
			if (newLexicalvalues.containsKey(oldTerm.getLexicalValue())) {
				if (oldTerm.getPrefered() != newLexicalvalues.get(
						oldTerm.getLexicalValue()).getPrefered()) {
					if (!oldTerm.getPrefered()) {
						CommandLine preferredLine = new CommandLine();
						if (newNotPreferredTermsByTerm.get(oldTerm.getLexicalValue()).isEmpty()){
							preferredLine.setValue(StringEscapeUtils.unescapeXml(oldTerm.getLexicalValue()));
						}
						else {
							preferredLine.setValue(CommandLine.STARS
									+ StringEscapeUtils.unescapeXml(oldTerm.getLexicalValue()));
						}
						termsOperations.add(preferredLine);

					} else{
						CommandLine unpreferredLine = new CommandLine();
						unpreferredLine.setValue(CommandLine.UNPREFERRERD
								+ StringEscapeUtils.unescapeXml(oldTerm.getLexicalValue()));
						termsOperations.add(unpreferredLine);
					}
				}
			}
		}
		return termsOperations;
	}

	/**
	 * Builds the list of command lines for preferred terms added between two revisions
	 *
	 * @param previousTerms
	 * @param currentTerms
	 * @return
	 */
	public List<CommandLine> buildAddedPrefTermsLines(List<ThesaurusTerm> previousTerms,
			List<ThesaurusTerm> currentTerms) {
		List<CommandLine> termsOperations = new ArrayList<CommandLine>();

		Map<String, ThesaurusTerm> oldLexicalValues = mistralStructuresBuilder
				.getTermVersionsView(previousTerms);

		Map<String, List<ThesaurusTerm>> newNotPreferredTermsByTerm = mistralStructuresBuilder
				.getNotPreferredTermsByTerm(currentTerms);

		for (ThesaurusTerm currentTerm : currentTerms) {
			if (!oldLexicalValues.containsKey(currentTerm.getLexicalValue())
					&& currentTerm.getPrefered()) {
				CommandLine additionLine = new CommandLine();
				if (!newNotPreferredTermsByTerm.get(currentTerm.getLexicalValue()).isEmpty()){
					additionLine.setValue(CommandLine.STARS	+ StringEscapeUtils.unescapeXml(currentTerm.getLexicalValue()));
					termsOperations.add(additionLine);
				} else{
					Set<ThesaurusConcept> allParents = new HashSet<ThesaurusConcept>();
					for (ThesaurusTerm cuttentChildTerm : currentTerms) {
						allParents.addAll(cuttentChildTerm.getConcept().getParentConcepts());
					}
					if (currentTerm.getConcept().getParentConcepts().isEmpty()
							&& !allParents.contains(currentTerm.getConcept())){
						additionLine.setValue(StringEscapeUtils.unescapeXml(currentTerm.getLexicalValue()));
						termsOperations.add(additionLine);
					}
				}
			}
		}
		return termsOperations;
	}

}
