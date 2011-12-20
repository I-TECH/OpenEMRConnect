/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is OpenEMRConnect.
 *
 * The Initial Developer of the Original Code is International Training &
 * Education Center for Health (I-TECH) <http://www.go2itech.org/>
 *
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * ***** END LICENSE BLOCK ***** */
package ke.go.moh.oec.reception.data;

import java.util.Date;
import ke.go.moh.oec.reception.controller.PersonWrapper;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class Notification {

    public enum Type {

        MIGRATION,
        PREGNANCY,
        PREGNANCY_OUTCOME,
        DEATH;

        public String occurenceDateId() {
            if (this == MIGRATION) {
                return "Date of migration";
            } else if (this == PREGNANCY) {
                return "Expected delivery date";
            } else if (this == PREGNANCY_OUTCOME) {
                return "Pregnancy end date";
            } else if (this == DEATH) {
                return "Date of death";
            } else {
                return "Unknown event";
            }
        }

        @Override
        public String toString() {
            if (this == MIGRATION) {
                return "Migration";
            } else if (this == PREGNANCY) {
                return "Pregnancy";
            } else if (this == PREGNANCY_OUTCOME) {
                return "Pregnancy outcome";
            } else if (this == DEATH) {
                return "Death";
            } else {
                return "Unknown event";
            }
        }
    }
    private final PersonWrapper personWrapper;
    private final Notification.Type type;
    private final Date occurenceDate;
    private final String additionalInformation;
    private String reassignAggress;
    private boolean flaggedOff = false;

    public Notification(PersonWrapper personWrapper, Notification.Type type, Date occurenceDate, String additionalInformation) {
        this.personWrapper = personWrapper;
        this.type = type;
        this.occurenceDate = occurenceDate;
        this.additionalInformation = additionalInformation;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public Date getOccurenceDate() {
        return occurenceDate;
    }

    public PersonWrapper getPersonWrapper() {
        return personWrapper;
    }

    public Type getType() {
        return type;
    }

    public boolean isFlaggedOff() {
        return flaggedOff;
    }

    public void setFlaggedOff(boolean flaggedOff) {
        this.flaggedOff = flaggedOff;
    }

    public String getReassignAggress() {
        return reassignAggress;
    }

    public void setReassignAggress(String reassignAggress) {
        this.reassignAggress = reassignAggress;
    }

    @Override
    public String toString() {
        return personWrapper.getLongName() + ": " + type.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Notification other = (Notification) obj;
        if (this.type != other.type) {
            return false;
        }
        if ((this.additionalInformation == null) ? (other.additionalInformation != null) : !this.additionalInformation.equals(other.additionalInformation)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 97 * hash + (this.additionalInformation != null ? this.additionalInformation.hashCode() : 0);
        return hash;
    }
}
