/**
 * Copyright or © or Copr. Ministère Français chargé de la Culture
 * et de la Communication (2013)
 *
 * contact.gincoculture_at_gouv.fr
 *
 * This software is a computer program whose purpose is to provide a thesaurus
 * management solution.
 *
 * This software is governed by the CeCILL license under French law and
 * abiding by the rules of distribution of free software. You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */
package fr.mcc.ginco.beans;

import java.io.Serializable;

/**
 * Bean represents hierarchical relation between two {@link ThesaurusConcept}
 */
@SuppressWarnings("serial")
public class ConceptHierarchicalRelationship implements Serializable {
	
	public static class Id implements Serializable {
		private String childconceptid;
		private String parentconceptid;
    
        public Id() {}
     
        public String getChildconceptid() {
			return childconceptid;
		}

		public void setChildconceptid(String childconceptid) {
			this.childconceptid = childconceptid;
		}

		public String getParentconceptid() {
			return parentconceptid;
		}

		public void setParentconceptid(String parentconceptid) {
			this.parentconceptid = parentconceptid;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((childconceptid == null) ? 0 : childconceptid.hashCode());
			result = prime * result
					+ ((parentconceptid == null) ? 0 : parentconceptid.hashCode());
			return result;
		}


		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
            }
			if (obj == null) {
				return false;
            }
			if (getClass() != obj.getClass()) {
				return false;
            }
			Id other = (Id) obj;
			if (childconceptid == null) {
				if (other.childconceptid != null) {
					return false;
                }
			} else if (!childconceptid.equals(other.childconceptid)) {
				return false;
            }
			if (parentconceptid == null) {
				if (other.parentconceptid != null) {
					return false;
                }
			} else if (!parentconceptid.equals(other.parentconceptid)) {
				return false;
            }
			return true;
		}
		
    }

	private Id identifier;
	private Integer role;
	
	public Id getIdentifier() {
		return identifier;
	}

	public void setIdentifier(Id identifier) {
		this.identifier = identifier;
	}

	public Integer getRole() {
		return role;
	}

	public void setRole(Integer role) {
		this.role = role;
	}
}